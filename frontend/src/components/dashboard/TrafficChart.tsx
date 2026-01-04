import React, { useEffect, useRef, useState } from 'react';
import { Card, Select, Space, Typography, Spin, Statistic, Alert } from 'antd';
import { ReloadOutlined, SafetyCertificateOutlined, WarningOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import * as echarts from 'echarts';
import { useWAFStore } from '../../stores/useWAFStore';
import { getSecurityTrafficChartOptions } from '../../utils/chartConfig';
import { formatNumber, formatPercentage } from '../../utils/formatters';
import { Loading } from '../common/Loading';

const { Title, Text } = Typography;
const { Option } = Select;

export const TrafficChart: React.FC = () => {
  const chartRef = useRef<HTMLDivElement>(null);
  const chartInstance = useRef<echarts.ECharts | null>(null);
  const [timeRange, setTimeRange] = useState<number>(24);
  const [isRefreshing, setIsRefreshing] = useState(false);

  const wafStore = useWAFStore();

  // Traffic data query with dynamic time range (no auto refetch)
  const trafficQuery = useQuery({
    queryKey: ['traffic-data', timeRange],
    queryFn: () => wafStore.fetchTrafficData(timeRange),
    staleTime: Infinity, // Don't auto refetch
  });

  // Stats query (auto refetch for real-time stats)
  const statsQuery = useQuery({
    queryKey: ['waf-stats'],
    queryFn: wafStore.fetchStats,
    refetchInterval: 5000,
    staleTime: 1000,
  });

  const trafficData = wafStore.trafficData;
  const stats = wafStore.stats;
  const isLoading = trafficQuery.isLoading || statsQuery.isLoading;

  // Initialize chart
  useEffect(() => {
    if (chartRef.current && !chartInstance.current) {
      chartInstance.current = echarts.init(chartRef.current, 'dark');
    }

    return () => {
      if (chartInstance.current) {
        chartInstance.current.dispose();
        chartInstance.current = null;
      }
    };
  }, []);

  // Update chart data
  useEffect(() => {
    if (chartInstance.current && trafficData.length > 0) {
      // 차단율 최대값 계산
      const maxBlockRate = Math.max(
        ...trafficData.map(d => {
          const total = d.totalRequests || 1;
          return (d.blockedRequests / total) * 100;
        })
      );
      const options = getSecurityTrafficChartOptions(trafficData, maxBlockRate);
      chartInstance.current.setOption(options);
    }
  }, [trafficData]);

  // Handle window resize
  useEffect(() => {
    const handleResize = () => {
      if (chartInstance.current) {
        chartInstance.current.resize();
      }
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const handleTimeRangeChange = async (value: string) => {
    const hours = parseInt(value);
    setTimeRange(hours);
    setIsRefreshing(true);
    setTimeout(() => setIsRefreshing(false), 500);
  };

  const handleRefresh = async () => {
    setIsRefreshing(true);
    await trafficQuery.refetch();
    await statsQuery.refetch();
    setTimeout(() => setIsRefreshing(false), 500);
  };

  // 차단율 계산
  const blockRate = stats
    ? (stats.blockedRequests / stats.totalRequests) * 100
    : 0;

  // 경고 레벨 계산
  const getAlertLevel = () => {
    if (blockRate >= 10) return { type: 'error' as const, message: '경고: 차단율이 10%를 초과했습니다!' };
    if (blockRate >= 5) return { type: 'warning' as const, message: '주의: 차단율이 5%를 초과했습니다.' };
    return null;
  };

  const alertLevel = getAlertLevel();

  return (
    <Card className="bg-bg-card border-border" bordered={false}>
      {/* 헤더: 통계 + 필터 */}
      <div className="flex items-start justify-between mb-6">
        {/* 차단된 공격 통계 */}
        <div className="flex items-center gap-6">
          {/* 차단된 공격 */}
          <div
            className="px-6 py-4 rounded-lg"
            style={{
              background: 'linear-gradient(135deg, rgba(255, 107, 107, 0.2) 0%, rgba(255, 107, 107, 0.1) 100%)',
              border: '2px solid rgba(255, 107, 107, 0.3)',
            }}
          >
            <Text className="text-text-secondary text-xs block mb-2">차단된 공격</Text>
            <div className="flex items-center gap-3">
              <SafetyCertificateOutlined style={{ fontSize: '32px', color: '#ff6b6b' }} />
              <span className="font-bold" style={{ fontSize: '36px', color: '#ff6b6b' }}>
                {stats ? formatNumber(stats.blockedRequests) : '0'}
              </span>
            </div>
          </div>

          {/* 차단율 */}
          <div
            className="px-6 py-4 rounded-lg"
            style={{
              background: 'linear-gradient(135deg, rgba(255, 107, 107, 0.15) 0%, rgba(255, 107, 107, 0.05) 100%)',
              border: '2px solid rgba(255, 107, 107, 0.2)',
            }}
          >
            <Text className="text-text-secondary text-xs block mb-2">차단율</Text>
            <div className="flex items-center">
              <span className="font-bold" style={{ fontSize: '36px', color: '#ff6b6b' }}>
                {stats ? formatPercentage(stats.blockedRequests, stats.totalRequests) : '0%'}
              </span>
            </div>
          </div>
        </div>

        {/* 필터 컨트롤 */}
        <Space>
          <Select
            value={timeRange.toString()}
            size="small"
            onChange={handleTimeRangeChange}
            className="w-24"
            disabled={isRefreshing}
          >
            <Option value="1">1시간</Option>
            <Option value="6">6시간</Option>
            <Option value="24">24시간</Option>
            <Option value="168">7일</Option>
            <Option value="720">30일</Option>
          </Select>
          <Spin spinning={isRefreshing} size="small">
            <ReloadOutlined
              className={`text-text-secondary hover:text-accent-primary cursor-pointer transition-all ${
                isRefreshing ? 'animate-spin' : ''
              }`}
              onClick={handleRefresh}
              style={{ fontSize: '16px' }}
            />
          </Spin>
        </Space>
      </div>

      {/* 경고 메시지 */}
      {alertLevel && (
        <Alert
          message={alertLevel.message}
          type={alertLevel.type}
          showIcon
          icon={<WarningOutlined />}
          className="mb-4"
        />
      )}

      {/* 차트 */}
      {isLoading && trafficData.length === 0 ? (
        <Loading tip="로딩 중..." />
      ) : (
        <div
          ref={chartRef}
          className="w-full"
          style={{ height: '320px' }}
        />
      )}
    </Card>
  );
};