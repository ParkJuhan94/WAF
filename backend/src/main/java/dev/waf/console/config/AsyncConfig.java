package dev.waf.console.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 비동기 처리를 위한 설정
 *
 * 주요 기능:
 * - 다중 스레드 풀 관리 (도메인별 분리)
 * - 보안 컨텍스트 전파
 * - 요청 컨텍스트 전파
 * - 예외 처리 및 모니터링
 * - 거부 정책 및 백프레셔 관리
 *
 * 스레드 풀 전략:
 * 1. General Pool: 일반적인 비동기 작업
 * 2. IO Pool: I/O 집약적 작업 (파일, 네트워크)
 * 3. CPU Pool: CPU 집약적 작업 (계산, 분석)
 * 4. Report Pool: 리포트 생성 (리소스 제한)
 *
 * @author WAF Console Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Value("${app.async.core-pool-size:5}")
    private int corePoolSize;

    @Value("${app.async.max-pool-size:20}")
    private int maxPoolSize;

    @Value("${app.async.queue-capacity:100}")
    private int queueCapacity;

    @Value("${app.async.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Value("${app.async.thread-name-prefix:WAF-Async-}")
    private String threadNamePrefix;

    /**
     * 기본 비동기 실행자 (일반적인 비동기 작업용)
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = createBaseExecutor(
            "General",
            corePoolSize,
            maxPoolSize,
            queueCapacity
        );

        // 기본 거부 정책: CallerRuns (호출 스레드에서 실행)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        log.info("General async executor configured: core={}, max={}, queue={}",
            corePoolSize, maxPoolSize, queueCapacity);

        return executor;
    }

    /**
     * I/O 집약적 작업용 실행자
     * - 파일 업로드/다운로드
     * - 외부 API 호출
     * - 데이터베이스 배치 작업
     */
    @Bean(name = "ioTaskExecutor")
    public Executor ioTaskExecutor() {
        ThreadPoolTaskExecutor executor = createBaseExecutor(
            "IO",
            corePoolSize * 2,    // I/O 작업은 더 많은 스레드 허용
            maxPoolSize * 2,
            queueCapacity * 2    // 더 큰 큐 허용
        );

        // I/O 작업은 대기보다는 새 스레드 생성 선호
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        log.info("I/O async executor configured: core={}, max={}, queue={}",
            corePoolSize * 2, maxPoolSize * 2, queueCapacity * 2);

        return executor;
    }

    /**
     * CPU 집약적 작업용 실행자
     * - 데이터 분석
     * - 암호화/복호화
     * - 이미지/파일 처리
     */
    @Bean(name = "cpuTaskExecutor")
    public Executor cpuTaskExecutor() {
        int cpuCoreCount = Runtime.getRuntime().availableProcessors();

        ThreadPoolTaskExecutor executor = createBaseExecutor(
            "CPU",
            cpuCoreCount,        // CPU 코어 수와 동일
            cpuCoreCount,        // CPU 집약적 작업은 코어 수 제한
            queueCapacity / 2    // 작은 큐로 빠른 거부
        );

        // CPU 작업은 큐 포화시 즉시 거부
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        log.info("CPU async executor configured: core={}, max={}, queue={}, cpuCores={}",
            cpuCoreCount, cpuCoreCount, queueCapacity / 2, cpuCoreCount);

        return executor;
    }

    /**
     * 리포트 생성 전용 실행자
     * - PDF 생성
     * - Excel 내보내기
     * - 스크린샷 생성
     * - 리소스 집약적이므로 제한적 운영
     */
    @Bean(name = "reportTaskExecutor")
    public Executor reportTaskExecutor() {
        ThreadPoolTaskExecutor executor = createBaseExecutor(
            "Report",
            2,                   // 적은 코어 스레드
            5,                   // 제한된 최대 스레드
            10                   // 작은 큐
        );

        // 리포트 작업은 리소스 보호를 위해 거부 정책 사용
        executor.setRejectedExecutionHandler(new ReportRejectedExecutionHandler());

        log.info("Report async executor configured: core=2, max=5, queue=10");

        return executor;
    }

    /**
     * 기본 스레드 풀 실행자 생성
     */
    private ThreadPoolTaskExecutor createBaseExecutor(String poolName, int core, int max, int queue) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(max);
        executor.setQueueCapacity(queue);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix(threadNamePrefix + poolName + "-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        // 컨텍스트 전파 데코레이터 설정
        executor.setTaskDecorator(new ContextPropagatingTaskDecorator());

        executor.initialize();
        return executor;
    }

    /**
     * 전역 비동기 예외 핸들러
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

    /**
     * 컨텍스트 전파 태스크 데코레이터
     * 비동기 실행 시 보안 컨텍스트와 요청 컨텍스트를 전파
     */
    public static class ContextPropagatingTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            // 현재 스레드의 컨텍스트 캡처
            SecurityContext securityContext = SecurityContextHolder.getContext();
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

            return () -> {
                try {
                    // 비동기 스레드에 컨텍스트 설정
                    SecurityContextHolder.setContext(securityContext);
                    if (requestAttributes != null) {
                        RequestContextHolder.setRequestAttributes(requestAttributes);
                    }

                    // 실제 작업 실행
                    runnable.run();
                } finally {
                    // 컨텍스트 정리
                    SecurityContextHolder.clearContext();
                    RequestContextHolder.resetRequestAttributes();
                }
            };
        }
    }

    /**
     * 커스텀 비동기 예외 핸들러
     */
    public static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        @Override
        public void handleUncaughtException(Throwable ex, java.lang.reflect.Method method, Object... params) {
            String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();

            log.error("Async execution failed in method: {} with params: {}",
                methodName, params, ex);

            // 메트릭스 업데이트 (향후 Micrometer 연동시)
            // meterRegistry.counter("async.execution.failed", "method", methodName).increment();

            // 중요한 에러의 경우 알림 발송 (향후 확장)
            if (isCriticalError(ex)) {
                log.error("CRITICAL: Async execution failed - immediate attention required: {}",
                    methodName, ex);
            }
        }

        private boolean isCriticalError(Throwable ex) {
            return ex instanceof OutOfMemoryError ||
                   ex instanceof StackOverflowError ||
                   ex.getCause() instanceof java.sql.SQLException;
        }
    }

    /**
     * 리포트 작업 전용 거부 핸들러
     * 리소스 보호를 위해 로깅과 함께 거부
     */
    public static class ReportRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.warn("Report task rejected - thread pool saturated. " +
                "Active: {}, Pool Size: {}, Queue: {}",
                executor.getActiveCount(),
                executor.getPoolSize(),
                executor.getQueue().size());

            // 거부된 리포트 요청 메트릭 (향후 모니터링용)
            // meterRegistry.counter("report.task.rejected").increment();

            throw new RuntimeException(
                "리포트 생성 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    /**
     * 스레드 풀 상태 모니터링 (관리자용)
     */
    @Bean
    public AsyncMonitoringService asyncMonitoringService(
            @org.springframework.beans.factory.annotation.Qualifier("taskExecutor")
            Executor generalExecutor,
            @org.springframework.beans.factory.annotation.Qualifier("ioTaskExecutor")
            Executor ioExecutor,
            @org.springframework.beans.factory.annotation.Qualifier("cpuTaskExecutor")
            Executor cpuExecutor,
            @org.springframework.beans.factory.annotation.Qualifier("reportTaskExecutor")
            Executor reportExecutor) {

        return new AsyncMonitoringService(generalExecutor, ioExecutor, cpuExecutor, reportExecutor);
    }

    /**
     * 비동기 스레드 풀 모니터링 서비스
     */
    public static class AsyncMonitoringService {
        private final Executor generalExecutor;
        private final Executor ioExecutor;
        private final Executor cpuExecutor;
        private final Executor reportExecutor;

        public AsyncMonitoringService(Executor generalExecutor, Executor ioExecutor,
                                    Executor cpuExecutor, Executor reportExecutor) {
            this.generalExecutor = generalExecutor;
            this.ioExecutor = ioExecutor;
            this.cpuExecutor = cpuExecutor;
            this.reportExecutor = reportExecutor;
        }

        /**
         * 모든 스레드 풀 상태 로깅
         */
        public void logAllPoolStats() {
            logPoolStats("General", generalExecutor);
            logPoolStats("I/O", ioExecutor);
            logPoolStats("CPU", cpuExecutor);
            logPoolStats("Report", reportExecutor);
        }

        private void logPoolStats(String poolName, Executor executor) {
            if (executor instanceof ThreadPoolTaskExecutor taskExecutor) {
                ThreadPoolExecutor threadPool = taskExecutor.getThreadPoolExecutor();

                log.info("{} Pool Stats - Active: {}, Core: {}, Max: {}, Pool: {}, Queue: {}/{}, Completed: {}",
                    poolName,
                    threadPool.getActiveCount(),
                    threadPool.getCorePoolSize(),
                    threadPool.getMaximumPoolSize(),
                    threadPool.getPoolSize(),
                    threadPool.getQueue().size(),
                    ((ThreadPoolTaskExecutor) executor).getQueueCapacity(),
                    threadPool.getCompletedTaskCount());
            }
        }

        /**
         * 특정 풀의 부하 상태 확인
         */
        public boolean isPoolOverloaded(String poolName) {
            Executor executor = switch (poolName.toLowerCase()) {
                case "general" -> generalExecutor;
                case "io" -> ioExecutor;
                case "cpu" -> cpuExecutor;
                case "report" -> reportExecutor;
                default -> null;
            };

            if (executor instanceof ThreadPoolTaskExecutor taskExecutor) {
                ThreadPoolExecutor threadPool = taskExecutor.getThreadPoolExecutor();
                double loadRatio = (double) threadPool.getActiveCount() / threadPool.getMaximumPoolSize();
                return loadRatio > 0.8; // 80% 이상 사용시 과부하로 판단
            }

            return false;
        }
    }
}