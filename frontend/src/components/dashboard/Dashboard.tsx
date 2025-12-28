import React from 'react';
import { Row, Col, message } from 'antd';
import { TrafficChart } from './TrafficChart';
import { StatsCards } from './StatsCards';
import { RecentAttacks } from './RecentAttacks';
import { SystemStatus } from './SystemStatus';
import { QuickActions } from './QuickActions';
import { RealtimeIndicator } from './RealtimeIndicator';
import { useRealtimeData } from '../../hooks/useRealtimeData';

export const Dashboard: React.FC = () => {
  useRealtimeData(); // Initialize real-time data connection

  const handleQuickAction = (action: string) => {
    switch (action) {
      case 'run-test':
        message.info('공격 테스트를 시작합니다...');
        // Navigate to testing tab or open modal
        break;
      case 'view-logs':
        message.info('로그 페이지로 이동합니다...');
        // Navigate to logs tab
        break;
      case 'emergency-block':
        message.warning('긴급 차단 모드를 활성화하시겠습니까?');
        // Show confirmation modal
        break;
      case 'export-report':
        message.info('증거 리포트를 생성합니다...');
        // Generate and download report
        break;
      case 'rule-management':
        message.info('룰 관리 페이지로 이동합니다...');
        // Navigate to rules tab
        break;
      case 'waf-restart':
        message.warning('WAF를 재시작하시겠습니까?');
        // Show confirmation modal
        break;
      default:
        message.info(`액션: ${action}`);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header with real-time indicator */}
      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '16px' }}>
        <RealtimeIndicator />
      </div>

      {/* Top row - Main chart and stats */}
      <Row gutter={[24, 24]}>
        <Col xs={24} xl={16}>
          <TrafficChart />
        </Col>
        <Col xs={24} xl={8}>
          <StatsCards />
        </Col>
      </Row>

      {/* Bottom row - Recent attacks, system status, and quick actions */}
      <Row gutter={[24, 24]}>
        <Col xs={24} lg={12}>
          <RecentAttacks />
        </Col>
        <Col xs={24} lg={6}>
          <SystemStatus />
        </Col>
        <Col xs={24} lg={6}>
          <QuickActions onActionClick={handleQuickAction} />
        </Col>
      </Row>
    </div>
  );
};