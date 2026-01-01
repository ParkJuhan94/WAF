import React, { useState } from 'react';
import { Card, List, Typography, Space, Empty, Button, Modal, Descriptions, Tag } from 'antd';
import { EyeOutlined, ReloadOutlined } from '@ant-design/icons';
import { useWAFDashboardData } from '../../hooks/useRealtimeData';
import { SeverityBadge, AttackTypeBadge, BlockedBadge } from '../common/StatusBadge';
import { formatRelativeTime, formatIpAddress, formatDate } from '../../utils/formatters';
import { Loading } from '../common/Loading';
import { AttackEvent } from '../../types/waf';

const { Title, Text } = Typography;

export const RecentAttacks: React.FC = () => {
  const { recentAttacks, isLoading, refetchAll } = useWAFDashboardData();
  const [selectedAttack, setSelectedAttack] = useState<AttackEvent | null>(null);
  const [isModalVisible, setIsModalVisible] = useState(false);

  const showAttackDetail = (attack: AttackEvent) => {
    setSelectedAttack(attack);
    setIsModalVisible(true);
  };

  const getSeverityColor = (severity: string): string => {
    switch (severity) {
      case 'critical': return 'red';
      case 'high': return 'orange';
      case 'medium': return 'gold';
      case 'low': return 'blue';
      default: return 'default';
    }
  };

  return (
    <>
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
                  onClick={() => showAttackDetail(attack)}
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

                  {attack.matchedRules && attack.matchedRules.length > 0 && (
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

    <Modal
      title="공격 상세 정보"
      open={isModalVisible}
      onCancel={() => setIsModalVisible(false)}
      footer={null}
      width={800}
      className="attack-detail-modal"
    >
      {selectedAttack && (
        <Descriptions bordered column={2} size="small">
          <Descriptions.Item label="공격 ID" span={2}>
            {selectedAttack.id}
          </Descriptions.Item>
          <Descriptions.Item label="발생 시간">
            {formatDate(selectedAttack.timestamp)}
          </Descriptions.Item>
          <Descriptions.Item label="출발지 IP">
            {formatIpAddress(selectedAttack.sourceIp)}
          </Descriptions.Item>
          <Descriptions.Item label="공격 유형">
            <Tag color={getSeverityColor(selectedAttack.severity)}>
              {selectedAttack.attackType}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="위험도">
            <Tag color={getSeverityColor(selectedAttack.severity)}>
              {selectedAttack.severity.toUpperCase()}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="대상 경로" span={2}>
            <code className="text-sm">{selectedAttack.targetPath}</code>
          </Descriptions.Item>
          <Descriptions.Item label="차단 여부" span={2}>
            <Tag color={selectedAttack.blocked ? 'red' : 'green'}>
              {selectedAttack.blocked ? '차단됨' : '허용됨'}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="User-Agent" span={2}>
            <div className="text-sm break-all">
              {selectedAttack.userAgent || 'N/A'}
            </div>
          </Descriptions.Item>
          <Descriptions.Item label="Payload" span={2}>
            <pre className="bg-gray-100 dark:bg-gray-800 p-2 rounded text-xs overflow-x-auto max-h-60">
              {selectedAttack.payload || 'N/A'}
            </pre>
          </Descriptions.Item>
          <Descriptions.Item label="매칭된 룰" span={2}>
            {selectedAttack.matchedRules && selectedAttack.matchedRules.length > 0 ? (
              <div className="flex flex-wrap gap-1">
                {selectedAttack.matchedRules.map((rule, idx) => (
                  <Tag key={idx} color="blue">{rule}</Tag>
                ))}
              </div>
            ) : (
              'N/A'
            )}
          </Descriptions.Item>
        </Descriptions>
      )}
    </Modal>
    </>
  );
};