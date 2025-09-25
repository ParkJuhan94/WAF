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
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Page Title with Quick Actions */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <h1 style={{
            color: '#ffffff',
            fontSize: '28px',
            fontWeight: 'bold',
            margin: '0 0 8px 0'
          }}>
            대시보드
          </h1>
          <p style={{
            color: '#a8b2c1',
            fontSize: '16px',
            margin: 0
          }}>
            WAF 실시간 모니터링 및 보안 상태
          </p>
        </div>

        {/* Quick Actions */}
        <div style={{ display: 'flex', gap: '12px' }}>
          <button
            onClick={() => handleQuickAction('run-test')}
            style={{
              background: '#1ec997',
              border: 'none',
              borderRadius: '6px',
              padding: '8px 12px',
              color: 'white',
              cursor: 'pointer',
              fontSize: '13px',
              fontWeight: 600,
              transition: 'background-color 0.2s',
              boxShadow: '0 2px 4px rgba(30, 201, 151, 0.2)'
            }}
            onMouseOver={(e) => e.currentTarget.style.background = '#17a085'}
            onMouseOut={(e) => e.currentTarget.style.background = '#1ec997'}
          >
            공격 테스트
          </button>
          <button
            onClick={() => handleQuickAction('view-logs')}
            style={{
              background: '#48cae4',
              border: 'none',
              borderRadius: '6px',
              padding: '8px 12px',
              color: 'white',
              cursor: 'pointer',
              fontSize: '13px',
              fontWeight: 600,
              transition: 'background-color 0.2s',
              boxShadow: '0 2px 4px rgba(72, 202, 228, 0.2)'
            }}
            onMouseOver={(e) => e.currentTarget.style.background = '#3ba3c7'}
            onMouseOut={(e) => e.currentTarget.style.background = '#48cae4'}
          >
            실시간 로그
          </button>
          <button
            onClick={() => handleQuickAction('emergency-block')}
            style={{
              background: '#ff6b6b',
              border: 'none',
              borderRadius: '6px',
              padding: '8px 12px',
              color: 'white',
              cursor: 'pointer',
              fontSize: '13px',
              fontWeight: 600,
              transition: 'background-color 0.2s',
              boxShadow: '0 2px 4px rgba(255, 107, 107, 0.2)'
            }}
            onMouseOver={(e) => e.currentTarget.style.background = '#e55555'}
            onMouseOut={(e) => e.currentTarget.style.background = '#ff6b6b'}
          >
            긴급 차단
          </button>
        </div>
      </div>

      {/* First row - Traffic monitoring and System statistics */}
      <Row gutter={[24, 24]}>
        <Col xs={24} lg={16}>
          <div style={{
            background: '#242c3a',
            border: '1px solid #3a4553',
            borderRadius: '12px',
            padding: '24px',
            boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
          }}>
            <h2 style={{
              color: '#ffffff',
              fontSize: '20px',
              fontWeight: 'bold',
              marginBottom: '20px',
              margin: 0
            }}>
              실시간 트래픽 모니터링
            </h2>
            <div style={{
              display: 'flex',
              justifyContent: 'space-around',
              alignItems: 'center',
              minHeight: '200px',
              background: '#1b2431',
              borderRadius: '8px',
              color: '#a8b2c1',
              padding: '20px'
            }}>
              <div style={{ textAlign: 'center' }}>
                <div style={{
                  fontSize: '48px',
                  color: '#1ec997',
                  marginBottom: '8px',
                  fontWeight: 'bold'
                }}>
                  12,847
                </div>
                <div style={{
                  fontSize: '14px',
                  fontWeight: 500
                }}>
                  총 요청
                </div>
              </div>
              <div style={{ textAlign: 'center' }}>
                <div style={{
                  fontSize: '48px',
                  color: '#ff6b6b',
                  marginBottom: '8px',
                  fontWeight: 'bold'
                }}>
                  234
                </div>
                <div style={{
                  fontSize: '14px',
                  fontWeight: 500
                }}>
                  차단됨
                </div>
              </div>
              <div style={{ textAlign: 'center' }}>
                <div style={{
                  fontSize: '48px',
                  color: '#48cae4',
                  marginBottom: '8px',
                  fontWeight: 'bold'
                }}>
                  12,613
                </div>
                <div style={{
                  fontSize: '14px',
                  fontWeight: 500
                }}>
                  허용됨
                </div>
              </div>
            </div>
          </div>
        </Col>
        <Col xs={24} lg={8}>
          <div style={{
            background: '#242c3a',
            border: '1px solid #3a4553',
            borderRadius: '12px',
            padding: '24px',
            boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
          }}>
            <h3 style={{
              color: '#ffffff',
              fontSize: '20px',
              fontWeight: 'bold',
              marginBottom: '20px',
              margin: 0
            }}>
              시스템 통계
            </h3>
            <div style={{ color: '#a8b2c1', lineHeight: '2.2', fontSize: '16px' }}>
              <div style={{ marginBottom: '10px' }}>
                차단율: <span style={{ color: '#ff6b6b', fontWeight: 'bold', fontSize: '18px' }}>1.8%</span>
              </div>
              <div style={{ marginBottom: '10px' }}>
                평균 응답시간: <span style={{ color: '#feca57', fontWeight: 'bold', fontSize: '18px' }}>145ms</span>
              </div>
              <div style={{ marginBottom: '10px' }}>
                가동시간: <span style={{ color: '#1ec997', fontWeight: 'bold', fontSize: '18px' }}>33일</span>
              </div>
              <div>
                활성 룰: <span style={{ color: '#48cae4', fontWeight: 'bold', fontSize: '18px' }}>1,198 / 1,247</span>
              </div>
            </div>
          </div>
        </Col>
      </Row>

      {/* Second row - Recent attacks and System status */}
      <Row gutter={[24, 24]}>
        <Col xs={24} lg={16}>
          <div style={{
            background: '#242c3a',
            border: '1px solid #3a4553',
            borderRadius: '12px',
            padding: '24px',
            boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
          }}>
            <h3 style={{
              color: '#ffffff',
              fontSize: '20px',
              fontWeight: 'bold',
              marginBottom: '20px',
              margin: 0
            }}>
              최근 공격 이벤트
            </h3>
            <div style={{ color: '#a8b2c1' }}>
              {mockDashboardData.recentAttacks.slice(0, 5).map((attack, index) => (
                <div key={attack.id} style={{
                  padding: '14px 0',
                  borderBottom: index < 4 ? '1px solid #3a4553' : 'none'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                    <span style={{ color: '#ff6b6b', fontWeight: 'bold', fontSize: '16px' }}>{attack.sourceIp}</span>
                    <span style={{ fontSize: '14px', color: '#a8b2c1' }}>
                      {new Date(attack.timestamp).toLocaleTimeString()}
                    </span>
                  </div>
                  <div style={{ fontSize: '14px', marginTop: '4px', fontWeight: 500 }}>
                    <span style={{ color: '#feca57' }}>{attack.attackType}</span> → <span style={{ color: '#48cae4' }}>{attack.targetPath}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </Col>
        <Col xs={24} lg={8}>
          <div style={{
            background: '#242c3a',
            border: '1px solid #3a4553',
            borderRadius: '12px',
            padding: '24px',
            boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
          }}>
            <h3 style={{
              color: '#ffffff',
              fontSize: '20px',
              fontWeight: 'bold',
              marginBottom: '20px',
              margin: 0
            }}>
              시스템 상태
            </h3>
            <div style={{ color: '#a8b2c1', lineHeight: '2.2', fontSize: '16px' }}>
              <div style={{ marginBottom: '10px' }}>
                <span style={{ color: '#1ec997', fontSize: '18px', marginRight: '8px' }}>● </span>
                <span style={{ fontWeight: 'bold' }}>WAF 엔진: </span>
                <span style={{ color: '#1ec997', fontWeight: 'bold' }}>활성</span>
              </div>
              <div style={{ marginBottom: '10px' }}>
                <span style={{ fontWeight: 'bold' }}>버전: </span>
                <span style={{ color: '#48cae4' }}>ModSecurity v3.0.8</span>
              </div>
              <div style={{ marginBottom: '10px' }}>
                <span style={{ fontWeight: 'bold' }}>마지막 재시작: </span>
                <span>1일 전</span>
              </div>
              <div>
                <span style={{ fontWeight: 'bold' }}>설정 업데이트: </span>
                <span style={{ color: '#feca57' }}>1시간 전</span>
              </div>
            </div>
          </div>
        </Col>
      </Row>
    </div>
  );
};