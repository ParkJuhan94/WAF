import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

type WebSocketEventHandler = (data: any) => void;

/**
 * WAF WebSocket 클라이언트 (STOMP + SockJS)
 *
 * 백엔드 WebSocket 서버와 STOMP 프로토콜로 통신합니다.
 * - 실시간 공격 이벤트 수신
 * - 트래픽 데이터 업데이트
 * - 통계 및 상태 변경 알림
 * - JWT 기반 인증
 * - 자동 재연결
 */
class WAFWebSocket {
  private client: Client | null = null;
  private eventHandlers: Map<string, WebSocketEventHandler[]> = new Map();
  private connected: boolean = false;
  private reconnectAttempts: number = 0;
  private maxReconnectAttempts: number = 5;

  /**
   * WebSocket 연결
   * @param token JWT 토큰
   */
  connect(token: string): Promise<void> {
    if (this.connected) {
      console.log('WebSocket already connected');
      return Promise.resolve();
    }

    return new Promise((resolve, reject) => {
      try {
        this.client = new Client({
          // SockJS factory (fallback 지원)
          webSocketFactory: () =>
            new SockJS(
              `${import.meta.env.VITE_API_URL || 'http://localhost:8081'}/ws?token=${token}`
            ),

          // 연결 성공
          onConnect: () => {
            console.log('✅ STOMP Connected');
            this.connected = true;
            this.reconnectAttempts = 0;
            this.subscribeToTopics();
            this.emit('connected', null);
            resolve();
          },

          // STOMP 에러
          onStompError: (frame) => {
            console.error('❌ STOMP Error:', frame.headers['message']);
            this.connected = false;
            this.emit('error', frame);
            reject(new Error(frame.headers['message'] || 'STOMP error'));
          },

          // 연결 종료
          onDisconnect: () => {
            console.log('🔌 STOMP Disconnected');
            this.connected = false;
            this.emit('disconnected', null);

            // 자동 재연결
            if (this.reconnectAttempts < this.maxReconnectAttempts) {
              this.scheduleReconnect(token);
            }
          },

          // 디버그 로깅
          debug: (str) => {
            if (import.meta.env.DEV) {
              console.log('📡 STOMP:', str);
            }
          },

          // 재연결 설정
          reconnectDelay: 5000,
          heartbeatIncoming: 10000,
          heartbeatOutgoing: 10000,
        });

        this.client.activate();
      } catch (error) {
        console.error('Failed to initialize STOMP client:', error);
        reject(error);
      }
    });
  }

  /**
   * 토픽 구독
   */
  private subscribeToTopics(): void {
    if (!this.client) return;

    // 공격 이벤트 구독
    this.client.subscribe('/topic/dashboard/attacks', (message: IMessage) => {
      try {
        const attack = JSON.parse(message.body);
        this.emit('attackBlocked', attack);
      } catch (error) {
        console.error('Failed to parse attack event:', error);
      }
    });

    // 트래픽 업데이트 구독
    this.client.subscribe('/topic/dashboard/traffic', (message: IMessage) => {
      try {
        const traffic = JSON.parse(message.body);
        this.emit('trafficUpdate', traffic);
      } catch (error) {
        console.error('Failed to parse traffic update:', error);
      }
    });

    // 통계 업데이트 구독
    this.client.subscribe('/topic/dashboard/stats', (message: IMessage) => {
      try {
        const stats = JSON.parse(message.body);
        this.emit('statsUpdate', stats);
      } catch (error) {
        console.error('Failed to parse stats update:', error);
      }
    });

    // 상태 변경 구독
    this.client.subscribe('/topic/dashboard/status', (message: IMessage) => {
      try {
        const status = JSON.parse(message.body);
        this.emit('statusChange', status);
      } catch (error) {
        console.error('Failed to parse status change:', error);
      }
    });

    console.log('✅ Subscribed to all dashboard topics');
  }

  /**
   * 연결 해제
   */
  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.connected = false;
      console.log('🔌 WebSocket manually disconnected');
    }
    this.reconnectAttempts = this.maxReconnectAttempts; // 재연결 방지
  }

  /**
   * 재연결 스케줄링
   * @param token JWT 토큰
   */
  private scheduleReconnect(token: string): void {
    this.reconnectAttempts++;
    console.log(
      `🔄 Reconnecting (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`
    );

    setTimeout(() => {
      this.connect(token).catch((error) => {
        console.error('Reconnection failed:', error);
      });
    }, 5000);
  }

  /**
   * 이벤트 핸들러 등록
   * @param event 이벤트 이름
   * @param handler 핸들러 함수
   */
  on(event: string, handler: WebSocketEventHandler): void {
    if (!this.eventHandlers.has(event)) {
      this.eventHandlers.set(event, []);
    }
    this.eventHandlers.get(event)!.push(handler);
  }

  /**
   * 이벤트 핸들러 제거
   * @param event 이벤트 이름
   * @param handler 핸들러 함수
   */
  off(event: string, handler: WebSocketEventHandler): void {
    const handlers = this.eventHandlers.get(event);
    if (handlers) {
      const index = handlers.indexOf(handler);
      if (index !== -1) {
        handlers.splice(index, 1);
      }
    }
  }

  /**
   * 이벤트 발행
   * @param event 이벤트 이름
   * @param data 데이터
   */
  private emit(event: string, data: any): void {
    const handlers = this.eventHandlers.get(event);
    if (handlers) {
      handlers.forEach((handler) => {
        try {
          handler(data);
        } catch (error) {
          console.error(`Error in handler for ${event}:`, error);
        }
      });
    }
  }

  /**
   * 메시지 전송 (서버로)
   * @param destination 목적지
   * @param body 메시지 본문
   */
  send(destination: string, body: any): void {
    if (this.client && this.connected) {
      this.client.publish({
        destination,
        body: JSON.stringify(body),
      });
    } else {
      console.warn('WebSocket is not connected. Cannot send message.');
    }
  }

  /**
   * 연결 상태 확인
   */
  isConnected(): boolean {
    return this.connected;
  }

  /**
   * 연결 상태 반환
   */
  getReadyState(): number {
    if (this.connected) {
      return WebSocket.OPEN;
    }
    return WebSocket.CLOSED;
  }
}

// 싱글톤 인스턴스
export const wafWebSocket = new WAFWebSocket();
