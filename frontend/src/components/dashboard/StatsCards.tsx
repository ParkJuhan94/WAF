import React from 'react';
import { Card, Statistic, Row, Col, Progress } from 'antd';
import {
  GlobalOutlined,
  SafetyCertificateOutlined,
  ExclamationCircleOutlined,
  ClockCircleOutlined,
  RiseOutlined,
  SafetyCertificateOutlined
} from '@ant-design/icons';
import { useWAFDashboardData } from '../../hooks/useRealtimeData';
import { formatNumber, formatDuration, formatPercentage } from '../../utils/formatters';
import { Loading } from '../common/Loading';

export const StatsCards: React.FC = () => {
  const { stats, isLoading } = useWAFDashboardData();

  if (isLoading || !stats) {
    return <Loading tip="통계 데이터 로딩 중..." />;
  }

  const blockRate = (stats.blockedRequests / stats.totalRequests) * 100;
  const allowRate = (stats.allowedRequests / stats.totalRequests) * 100;

  const statsData = [
    {
      title: '총 요청 수',
      value: stats.totalRequests,
      formatter: formatNumber,
      prefix: <GlobalOutlined className="text-info" />,
      valueStyle: { color: '#48cae4' },
    },
    {
      title: '차단된 요청',
      value: stats.blockedRequests,
      formatter: formatNumber,
      prefix: <SafetyCertificateOutlined className="text-danger" />,
      valueStyle: { color: '#ff6b6b' },
    },
    {
      title: '허용된 요청',
      value: stats.allowedRequests,
      formatter: formatNumber,
      prefix: <ExclamationCircleOutlined className="text-success" />,
      valueStyle: { color: '#1ec997' },
    },
    {
      title: '평균 응답 시간',
      value: stats.avgResponseTime,
      formatter: (val: number) => `${val}ms`,
      prefix: <ClockCircleOutlined className="text-warning" />,
      valueStyle: { color: '#feca57' },
    },
  ];

  return (
    <div className="space-y-4">
      {/* Main stats grid */}
      <Row gutter={[16, 16]}>
        {statsData.map((stat, index) => (
          <Col span={12} key={index}>
            <Card className="bg-bg-card border-border" bordered={false}>
              <Statistic
                title={<span className="text-text-secondary">{stat.title}</span>}
                value={stat.value}
                formatter={stat.formatter}
                prefix={stat.prefix}
                valueStyle={stat.valueStyle}
              />
            </Card>
          </Col>
        ))}
      </Row>

      {/* Block rate visualization */}
      <Card className="bg-bg-card border-border" bordered={false}>
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-text-primary font-medium">차단율</span>
            <span className="text-text-primary font-bold text-lg">
              {formatPercentage(stats.blockedRequests, stats.totalRequests)}
            </span>
          </div>

          <Progress
            percent={blockRate}
            strokeColor={{
              '0%': '#ff6b6b',
              '100%': '#ff4757',
            }}
            trailColor="#2c3545"
            showInfo={false}
            strokeWidth={8}
          />

          <div className="flex justify-between text-sm">
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 rounded-full bg-danger"></div>
              <span className="text-text-secondary">차단됨</span>
              <span className="text-danger font-medium">
                {formatPercentage(stats.blockedRequests, stats.totalRequests)}
              </span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 rounded-full bg-success"></div>
              <span className="text-text-secondary">허용됨</span>
              <span className="text-success font-medium">
                {formatPercentage(stats.allowedRequests, stats.totalRequests)}
              </span>
            </div>
          </div>
        </div>
      </Card>

      {/* Uptime card */}
      <Card className="bg-bg-card border-border" bordered={false}>
        <Statistic
          title={<span className="text-text-secondary">시스템 가동 시간</span>}
          value={stats.uptime}
          formatter={(val) => formatDuration(val as number)}
          prefix={<RiseOutlined className="text-success" />}
          valueStyle={{ color: '#1ec997' }}
        />
      </Card>
    </div>
  );
};