import React, { useEffect, useRef } from 'react';
import { Card, Select, Space, Typography } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';
import * as echarts from 'echarts';
import { useWAFDashboardData } from '../../hooks/useRealtimeData';
import { getTrafficChartOptions } from '../../utils/chartConfig';
import { Loading } from '../common/Loading';

const { Title } = Typography;
const { Option } = Select;

export const TrafficChart: React.FC = () => {
  const chartRef = useRef<HTMLDivElement>(null);
  const chartInstance = useRef<echarts.ECharts | null>(null);
  const { trafficData, isLoading, refetchAll } = useWAFDashboardData();

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
      const options = getTrafficChartOptions(trafficData);
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

  const handleTimeRangeChange = (value: string) => {
    const hours = parseInt(value);
    // Implement time range change logic
    console.log('Time range changed to:', hours, 'hours');
  };

  return (
    <Card
      className="bg-bg-card border-border"
      title={
        <div className="flex items-center justify-between">
          <Title level={4} className="text-text-primary m-0">
            실시간 트래픽 모니터링
          </Title>
          <Space>
            <Select
              defaultValue="24"
              size="small"
              onChange={handleTimeRangeChange}
              className="w-24"
            >
              <Option value="1">1시간</Option>
              <Option value="6">6시간</Option>
              <Option value="24">24시간</Option>
              <Option value="168">7일</Option>
            </Select>
            <ReloadOutlined
              className="text-text-secondary hover:text-accent-primary cursor-pointer"
              onClick={refetchAll}
            />
          </Space>
        </div>
      }
      bordered={false}
    >
      {isLoading && trafficData.length === 0 ? (
        <Loading tip="트래픽 데이터 로딩 중..." />
      ) : (
        <div
          ref={chartRef}
          className="w-full h-80"
          style={{ minHeight: '320px' }}
        />
      )}

      {/* Chart legend */}
      <div className="flex justify-center space-x-6 mt-4 text-sm">
        <div className="flex items-center space-x-2">
          <div className="w-3 h-3 rounded-full bg-accent-primary"></div>
          <span className="text-text-secondary">전체 요청</span>
        </div>
        <div className="flex items-center space-x-2">
          <div className="w-3 h-3 rounded-full bg-danger"></div>
          <span className="text-text-secondary">차단된 요청</span>
        </div>
        <div className="flex items-center space-x-2">
          <div className="w-3 h-3 rounded-full bg-success"></div>
          <span className="text-text-secondary">허용된 요청</span>
        </div>
      </div>
    </Card>
  );
};