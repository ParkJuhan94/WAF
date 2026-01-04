import React from 'react';
import { Row, Col, message, Typography } from 'antd';
import { TrafficChart } from './TrafficChart';
import { RecentAttacks } from './RecentAttacks';
import { SystemStatus } from './SystemStatus';
import { QuickActions } from './QuickActions';
import { RealtimeIndicator } from './RealtimeIndicator';
import { useRealtimeData } from '../../hooks/useRealtimeData';

const { Title } = Typography;

export const Dashboard: React.FC = () => {
  useRealtimeData(); // Initialize real-time data connection

  const handleQuickAction = (action: string) => {
    switch (action) {
      case 'emergency-block':
        message.warning('긴급 차단 모드를 활성화하시겠습니까?');
        break;
      case 'waf-restart':
        message.warning('WAF를 재시작하시겠습니까?');
        break;
      default:
        message.info(`액션: ${action}`);
    }
  };

  return (
    <div className="p-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <Title level={3} className="text-text-primary m-0">
          대시보드
        </Title>
        <RealtimeIndicator />
      </div>

      {/* Main traffic chart with integrated stats */}
      <div className="mb-8">
        <TrafficChart />
      </div>

      {/* Recent attacks */}
      <div className="mb-8">
        <RecentAttacks />
      </div>

      {/* System status and quick actions */}
      <Row gutter={[32, 32]}>
        <Col xs={24} lg={10} xl={8}>
          <SystemStatus />
        </Col>
        <Col xs={24} lg={10} xl={8}>
          <QuickActions onActionClick={handleQuickAction} />
        </Col>
      </Row>
    </div>
  );
};