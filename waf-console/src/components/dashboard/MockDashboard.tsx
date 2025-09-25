import React from 'react';
import { Row, Col } from 'antd';
import { TrafficChart } from './TrafficChart';
import { StatsCards } from './StatsCards';
import { RecentAttacks } from './RecentAttacks';
import { SystemStatus } from './SystemStatus';
import { QuickActions } from './QuickActions';
import { useMockRealtimeData, useMockWAFDashboardData } from '../../hooks/useMockData';

// Mock version of Dashboard that uses mock data instead of real API calls
export const MockDashboard: React.FC = () => {
  // Use mock data hooks instead of real ones
  const mockRealtimeData = useMockRealtimeData();
  const mockDashboardData = useMockWAFDashboardData();

  const handleQuickAction = (action: string) => {
    console.log(`Quick action clicked: ${action}`);
    // In development, just log the action
  };

  return (
    <div className="space-y-6">
      {/* Top row - Main chart and stats */}
      <Row gutter={[24, 24]}>
        <Col xs={24} xl={16}>
          {/* We'll create a simple TrafficChart that works without API */}
          <div style={{
            background: '#242c3a',
            border: '1px solid #3a4553',
            borderRadius: '6px',
            padding: '24px',
            minHeight: '400px'
          }}>
            <h3 style={{ color: 'white', marginBottom: '16px' }}>실시간 트래픽 모니터링</h3>
            <div style={{
              display: 'flex',
              justifyContent: 'space-around',
              alignItems: 'center',
              height: '300px',
              background: '#1b2431',
              borderRadius: '4px',
              color: '#a8b2c1'
            }}>
              <div style={{ textAlign: 'center' }}>
                <div style={{ fontSize: '48px', color: '#1ec997', marginBottom: '8px' }}>12,847</div>
                <div>총 요청</div>
              </div>
              <div style={{ textAlign: 'center' }}>
                <div style={{ fontSize: '48px', color: '#ff6b6b', marginBottom: '8px' }}>234</div>
                <div>차단됨</div>
              </div>
              <div style={{ textAlign: 'center' }}>
                <div style={{ fontSize: '48px', color: '#48cae4', marginBottom: '8px' }}>12,613</div>
                <div>허용됨</div>
              </div>
            </div>
          </div>
        </Col>
        <Col xs={24} xl={8}>
          {/* Simple stats display */}
          <div style={{
            background: '#242c3a',
            border: '1px solid #3a4553',
            borderRadius: '6px',
            padding: '24px'
          }}>
            <h3 style={{ color: 'white', marginBottom: '16px' }}>시스템 통계</h3>
            <div style={{ color: '#a8b2c1', lineHeight: '2' }}>
              <div>차단율: <span style={{ color: '#ff6b6b' }}>1.8%</span></div>
              <div>평균 응답시간: <span style={{ color: '#feca57' }}>145ms</span></div>
              <div>가동시간: <span style={{ color: '#1ec997' }}>33일</span></div>
              <div>활성 룰: <span style={{ color: '#48cae4' }}>1,198 / 1,247</span></div>
            </div>
          </div>
        </Col>
      </Row>

      {/* Bottom row - Recent attacks, system status, and quick actions */}
      <Row gutter={[24, 24]}>
        <Col xs={24} lg={12}>
          <div style={{
            background: '#242c3a',
            border: '1px solid #3a4553',
            borderRadius: '6px',
            padding: '24px'
          }}>
            <h3 style={{ color: 'white', marginBottom: '16px' }}>최근 공격 이벤트</h3>
            <div style={{ color: '#a8b2c1' }}>
              {mockDashboardData.recentAttacks.slice(0, 5).map((attack, index) => (
                <div key={attack.id} style={{
                  padding: '12px 0',
                  borderBottom: index < 4 ? '1px solid #3a4553' : 'none'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ color: '#ff6b6b' }}>{attack.sourceIp}</span>
                    <span style={{ fontSize: '12px', color: '#a8b2c1' }}>
                      {new Date(attack.timestamp).toLocaleTimeString()}
                    </span>
                  </div>
                  <div style={{ fontSize: '12px', marginTop: '4px' }}>
                    {attack.attackType} → {attack.targetPath}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </Col>
        <Col xs={24} lg={6}>
          <div style={{
            background: '#242c3a',
            border: '1px solid #3a4553',
            borderRadius: '6px',
            padding: '24px'
          }}>
            <h3 style={{ color: 'white', marginBottom: '16px' }}>시스템 상태</h3>
            <div style={{ color: '#a8b2c1', lineHeight: '2' }}>
              <div>
                <span style={{ color: '#1ec997' }}>● </span>
                WAF 엔진: 활성
              </div>
              <div>버전: ModSecurity v3.0.8</div>
              <div>마지막 재시작: 1일 전</div>
              <div>설정 업데이트: 1시간 전</div>
            </div>
          </div>
        </Col>
        <Col xs={24} lg={6}>
          <div style={{
            background: '#242c3a',
            border: '1px solid #3a4553',
            borderRadius: '6px',
            padding: '24px'
          }}>
            <h3 style={{ color: 'white', marginBottom: '16px' }}>빠른 액션</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <button
                onClick={() => handleQuickAction('run-test')}
                style={{
                  background: '#1ec997',
                  border: 'none',
                  borderRadius: '4px',
                  padding: '8px 16px',
                  color: 'white',
                  cursor: 'pointer'
                }}
              >
                공격 테스트 실행
              </button>
              <button
                onClick={() => handleQuickAction('view-logs')}
                style={{
                  background: '#48cae4',
                  border: 'none',
                  borderRadius: '4px',
                  padding: '8px 16px',
                  color: 'white',
                  cursor: 'pointer'
                }}
              >
                실시간 로그
              </button>
              <button
                onClick={() => handleQuickAction('emergency-block')}
                style={{
                  background: '#ff6b6b',
                  border: 'none',
                  borderRadius: '4px',
                  padding: '8px 16px',
                  color: 'white',
                  cursor: 'pointer'
                }}
              >
                긴급 차단 모드
              </button>
            </div>
          </div>
        </Col>
      </Row>
    </div>
  );
};