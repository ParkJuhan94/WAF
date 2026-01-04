import React from 'react';
import { Card, Typography } from 'antd';
import { useWAFDashboardData } from '../../hooks/useRealtimeData';
import { StatusBadge } from '../common/StatusBadge';
import { formatRelativeTime } from '../../utils/formatters';
import { Loading } from '../common/Loading';

const { Title, Text } = Typography;

export const SystemStatus: React.FC = () => {
  const { status, isLoading } = useWAFDashboardData();

  return (
    <Card
      className="bg-bg-card border-border h-full"
      title={
        <Title level={5} className="text-text-primary m-0">
          시스템 상태
        </Title>
      }
      bordered={false}
    >
      {isLoading || !status ? (
        <Loading tip="로딩 중..." />
      ) : (
        <div className="space-y-4">
          {/* WAF 엔진 상태 */}
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <StatusBadge status={status.status} showText={false} />
              <Text className="text-text-primary font-medium">WAF 엔진</Text>
            </div>
            <Text className={`font-bold ${status.status === 'active' ? 'text-success' : 'text-danger'}`}>
              {status.status === 'active' ? '온라인' : '오프라인'}
            </Text>
          </div>

          {/* 구분선 */}
          <div className="border-t border-border"></div>

          {/* 간결한 정보 */}
          <div className="space-y-3">
            <div className="flex justify-between">
              <Text className="text-text-secondary text-sm">버전</Text>
              <Text className="text-text-primary text-sm font-mono">{status.version}</Text>
            </div>

            <div className="flex justify-between">
              <Text className="text-text-secondary text-sm">활성 룰</Text>
              <Text className="text-text-primary text-sm">
                {status.activeRulesCount} / {status.rulesCount}
              </Text>
            </div>

            <div className="flex justify-between">
              <Text className="text-text-secondary text-sm">마지막 재시작</Text>
              <Text className="text-text-primary text-sm">
                {formatRelativeTime(status.lastRestart)}
              </Text>
            </div>
          </div>
        </div>
      )}
    </Card>
  );
};