import React from 'react';
import { Card, List, Typography, Space, Empty, Button } from 'antd';
import { EyeOutlined, ReloadOutlined } from '@ant-design/icons';
import { useWAFDashboardData } from '../../hooks/useRealtimeData';
import { SeverityBadge, AttackTypeBadge, BlockedBadge } from '../common/StatusBadge';
import { formatRelativeTime, formatIpAddress } from '../../utils/formatters';
import { Loading } from '../common/Loading';

const { Title, Text } = Typography;

export const RecentAttacks: React.FC = () => {
  const { recentAttacks, isLoading, refetchAll } = useWAFDashboardData();

  return (
    <Card
      className="bg-bg-card border-border"
      title={
        <div className="flex items-center justify-between">
          <Title level={4} className="text-text-primary m-0">
            최근 공격 이벤트
          </Title>
          <ReloadOutlined
            className="text-text-secondary hover:text-accent-primary cursor-pointer"
            onClick={refetchAll}
          />
        </div>
      }
      bordered={false}
    >
      {isLoading ? (
        <Loading tip="공격 이벤트 로딩 중..." />
      ) : recentAttacks.length === 0 ? (
        <Empty
          description={
            <span className="text-text-secondary">
              최근 공격 이벤트가 없습니다
            </span>
          }
          className="py-8"
        />
      ) : (
        <List
          dataSource={recentAttacks}
          renderItem={(attack) => (
            <List.Item
              className="border-b border-border last:border-b-0 py-3"
              actions={[
                <Button
                  key="view"
                  type="text"
                  size="small"
                  icon={<EyeOutlined />}
                  className="text-text-secondary hover:text-accent-primary"
                >
                  상세보기
                </Button>,
              ]}
            >
              <div className="w-full">
                <div className="flex items-start justify-between mb-2">
                  <div className="flex items-center space-x-2">
                    <AttackTypeBadge type={attack.attackType} size="small" />
                    <SeverityBadge severity={attack.severity} size="small" />
                    <BlockedBadge blocked={attack.blocked} size="small" />
                  </div>
                  <Text className="text-text-secondary text-xs">
                    {formatRelativeTime(attack.timestamp)}
                  </Text>
                </div>

                <div className="space-y-1">
                  <div className="flex items-center space-x-4">
                    <Text className="text-text-primary font-medium">
                      {formatIpAddress(attack.sourceIp)}
                    </Text>
                    <Text className="text-text-secondary">
                      → {attack.targetPath}
                    </Text>
                  </div>

                  {attack.payload && (
                    <Text
                      className="text-text-secondary text-sm font-mono block truncate"
                      style={{ maxWidth: '400px' }}
                    >
                      {attack.payload}
                    </Text>
                  )}

                  {attack.matchedRules.length > 0 && (
                    <div className="flex items-center space-x-2 text-xs">
                      <Text className="text-text-secondary">매칭된 룰:</Text>
                      <Text className="text-accent-primary">
                        {attack.matchedRules.slice(0, 2).join(', ')}
                        {attack.matchedRules.length > 2 && (
                          <span className="text-text-secondary">
                            {' '}외 {attack.matchedRules.length - 2}개
                          </span>
                        )}
                      </Text>
                    </div>
                  )}
                </div>
              </div>
            </List.Item>
          )}
          pagination={false}
          style={{ maxHeight: '400px', overflowY: 'auto' }}
        />
      )}
    </Card>
  );
};