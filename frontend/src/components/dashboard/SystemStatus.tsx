import React from 'react';
import { Card, Descriptions, Typography, Space, Button } from 'antd';
import { ReloadOutlined, SettingOutlined } from '@ant-design/icons';
import { useWAFDashboardData } from '../../hooks/useRealtimeData';
import { StatusBadge } from '../common/StatusBadge';
import { formatDate, formatRelativeTime } from '../../utils/formatters';
import { Loading } from '../common/Loading';

const { Title, Text } = Typography;

export const SystemStatus: React.FC = () => {
  const { status, isLoading, refetchAll } = useWAFDashboardData();

  return (
    <Card
      className="bg-bg-card border-border"
      title={
        <div className="flex items-center justify-between">
          <Title level={4} className="text-text-primary m-0">
            시스템 상태
          </Title>
          <Space>
            <Button
              type="text"
              size="small"
              icon={<SettingOutlined />}
              className="text-text-secondary hover:text-accent-primary"
            >
              설정
            </Button>
            <ReloadOutlined
              className="text-text-secondary hover:text-accent-primary cursor-pointer"
              onClick={refetchAll}
            />
          </Space>
        </div>
      }
      bordered={false}
    >
      {isLoading || !status ? (
        <Loading tip="시스템 상태 로딩 중..." />
      ) : (
        <div className="space-y-4">
          {/* Main status indicator */}
          <div className="flex items-center justify-between p-4 bg-bg-surface rounded-lg">
            <div className="flex items-center space-x-3">
              <StatusBadge status={status.status} showText={false} />
              <div>
                <Text className="text-text-primary font-medium block">
                  WAF 엔진
                </Text>
                <Text className="text-text-secondary text-sm">
                  {status.status === 'active' && '정상 작동 중'}
                  {status.status === 'inactive' && '비활성화됨'}
                  {status.status === 'error' && '오류 발생'}
                  {status.status === 'maintenance' && '유지보수 모드'}
                </Text>
              </div>
            </div>
            <div className="text-right">
              <Text className="text-text-primary font-bold text-lg block">
                {status.status === 'active' ? '온라인' : '오프라인'}
              </Text>
              <Text className="text-text-secondary text-sm">
                상태
              </Text>
            </div>
          </div>

          {/* System details */}
          <Descriptions
            column={1}
            size="small"
            className="bg-bg-surface rounded-lg p-4"
          >
            <Descriptions.Item
              label={<span className="text-text-secondary">버전</span>}
            >
              <Text className="text-text-primary">{status.version}</Text>
            </Descriptions.Item>

            <Descriptions.Item
              label={<span className="text-text-secondary">마지막 재시작</span>}
            >
              <Text className="text-text-primary">
                {formatRelativeTime(status.lastRestart)}
              </Text>
              <br />
              <Text className="text-text-secondary text-xs">
                {formatDate(status.lastRestart)}
              </Text>
            </Descriptions.Item>

            <Descriptions.Item
              label={<span className="text-text-secondary">설정 업데이트</span>}
            >
              <Text className="text-text-primary">
                {formatRelativeTime(status.configLastUpdated)}
              </Text>
              <br />
              <Text className="text-text-secondary text-xs">
                {formatDate(status.configLastUpdated)}
              </Text>
            </Descriptions.Item>

            <Descriptions.Item
              label={<span className="text-text-secondary">활성 룰</span>}
            >
              <Text className="text-text-primary">
                {status.activeRulesCount} / {status.rulesCount}
              </Text>
              <Text className="text-text-secondary text-sm ml-2">
                ({Math.round((status.activeRulesCount / status.rulesCount) * 100)}% 활성화)
              </Text>
            </Descriptions.Item>
          </Descriptions>

          {/* Quick actions */}
          <div className="flex space-x-2">
            <Button
              size="small"
              type="primary"
              className="bg-accent-primary border-accent-primary hover:bg-opacity-80"
            >
              재시작
            </Button>
            <Button
              size="small"
              className="border-border text-text-secondary hover:text-accent-primary hover:border-accent-primary"
            >
              로그 보기
            </Button>
          </div>
        </div>
      )}
    </Card>
  );
};