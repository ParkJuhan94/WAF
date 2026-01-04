import React from 'react';
import { Card, Statistic, Progress, Typography } from 'antd';
import { SafetyCertificateOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { useWAFDashboardData } from '../../hooks/useRealtimeData';
import { formatNumber, formatPercentage } from '../../utils/formatters';
import { Loading } from '../common/Loading';

const { Text } = Typography;

export const StatsCards: React.FC = () => {
  const { stats, isLoading } = useWAFDashboardData();

  if (isLoading || !stats) {
    return <Loading tip="통계 데이터 로딩 중..." />;
  }

  const blockRate = (stats.blockedRequests / stats.totalRequests) * 100;

  return (
    <Card className="bg-bg-card border-border h-full" bordered={false}>
      <div className="space-y-6">
        {/* 차단된 공격 */}
        <div>
          <Statistic
            title={<span className="text-text-secondary text-sm">차단된 공격</span>}
            value={stats.blockedRequests}
            formatter={(val) => formatNumber(val as number)}
            prefix={<SafetyCertificateOutlined className="text-danger" />}
            valueStyle={{ color: '#ff6b6b', fontSize: '32px', fontWeight: 'bold' }}
          />
        </div>

        {/* 차단율 Progress */}
        <div>
          <div className="flex items-center justify-between mb-2">
            <Text className="text-text-secondary text-sm">차단율</Text>
            <Text className="text-text-primary font-bold text-xl">
              {formatPercentage(stats.blockedRequests, stats.totalRequests)}
            </Text>
          </div>
          <Progress
            percent={blockRate}
            strokeColor={{
              '0%': '#ff6b6b',
              '100%': '#ff4757',
            }}
            trailColor="#2c3545"
            showInfo={false}
            strokeWidth={10}
          />
        </div>

        {/* 요약 정보 */}
        <div className="pt-4 border-t border-border">
          <div className="grid grid-cols-2 gap-4">
            <div className="text-center">
              <Text className="text-text-secondary text-xs block mb-1">차단됨</Text>
              <Text className="text-danger font-bold text-lg">
                {formatNumber(stats.blockedRequests)}
              </Text>
            </div>
            <div className="text-center">
              <Text className="text-text-secondary text-xs block mb-1">허용됨</Text>
              <Text className="text-success font-bold text-lg">
                {formatNumber(stats.allowedRequests)}
              </Text>
            </div>
          </div>
        </div>
      </div>
    </Card>
  );
};