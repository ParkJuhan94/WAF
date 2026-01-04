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
  const { recentAttacks, isLoading, refetchAll} = useWAFDashboardData();
  const [selectedAttack, setSelectedAttack] = useState<AttackEvent | null>(null);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 5;

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

  // Pagination data slicing
  const startIndex = (currentPage - 1) * pageSize;
  const endIndex = startIndex + pageSize;
  const paginatedAttacks = recentAttacks.slice(startIndex, endIndex);

  return (
    <>
    <Card
      className="bg-bg-card border-border"
      title={
        <Title level={5} className="text-text-primary m-0">
          최근 공격 이벤트
        </Title>
      }
      bordered={false}
    >
      {isLoading ? (
        <Loading tip="로딩 중..." />
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
          dataSource={paginatedAttacks}
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

                <div className="space-y-2">
                  <div className="flex items-center gap-3">
                    <div className="flex items-center gap-2 flex-shrink-0">
                      <code className="text-accent-primary font-mono text-sm px-2 py-1 bg-gray-900 rounded border border-gray-700">
                        {formatIpAddress(attack.sourceIp)}
                      </code>
                      <span className="text-text-secondary">→</span>
                    </div>
                    <Text className="text-text-secondary text-sm truncate flex-1" style={{ maxWidth: '400px' }}>
                      {attack.targetPath}
                    </Text>
                  </div>

                  {attack.payload && (
                    <div className="bg-gray-900 border border-gray-700 rounded px-3 py-2">
                      <code className="text-accent-primary text-xs font-mono block overflow-x-auto whitespace-nowrap">
                        {attack.payload}
                      </code>
                    </div>
                  )}

                  {attack.matchedRules && attack.matchedRules.length > 0 && (
                    <div className="flex flex-wrap gap-1">
                      {attack.matchedRules.slice(0, 3).map((rule, idx) => (
                        <Tag key={idx} color="orange" className="text-xs">
                          {rule}
                        </Tag>
                      ))}
                      {attack.matchedRules.length > 3 && (
                        <Tag color="default" className="text-xs">
                          +{attack.matchedRules.length - 3}개
                        </Tag>
                      )}
                    </div>
                  )}
                </div>
              </div>
            </List.Item>
          )}
          pagination={{
            current: currentPage,
            pageSize: pageSize,
            total: recentAttacks.length,
            onChange: (page) => setCurrentPage(page),
            showSizeChanger: false,
            size: 'small',
            className: 'mt-4'
          }}
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