import React from 'react';
import { Card, Button, Space, Typography, Divider } from 'antd';
import {
  PlayCircleOutlined,
  BugOutlined,
  DownloadOutlined,
  SettingOutlined,
  ShieldOutlined,
  FileTextOutlined
} from '@ant-design/icons';

const { Title } = Typography;

interface QuickActionsProps {
  onActionClick: (action: string) => void;
}

export const QuickActions: React.FC<QuickActionsProps> = ({ onActionClick }) => {
  const actions = [
    {
      key: 'run-test',
      title: '공격 테스트 실행',
      description: 'DVWA 대상 5가지 공격 시나리오 테스트',
      icon: <BugOutlined />,
      type: 'primary' as const,
      danger: false,
    },
    {
      key: 'view-logs',
      title: '실시간 로그',
      description: '최근 요청 및 차단 이벤트 확인',
      icon: <FileTextOutlined />,
      type: 'default' as const,
      danger: false,
    },
    {
      key: 'emergency-block',
      title: '긴급 차단 모드',
      description: '모든 요청을 일시적으로 차단',
      icon: <ShieldOutlined />,
      type: 'default' as const,
      danger: true,
    },
    {
      key: 'export-report',
      title: '증거 리포트 생성',
      description: '환불 조건 검증용 PDF 리포트',
      icon: <DownloadOutlined />,
      type: 'default' as const,
      danger: false,
    },
    {
      key: 'rule-management',
      title: '룰 관리',
      description: 'WAF 룰 활성화/비활성화',
      icon: <SettingOutlined />,
      type: 'default' as const,
      danger: false,
    },
    {
      key: 'waf-restart',
      title: 'WAF 재시작',
      description: '설정 변경 사항 적용',
      icon: <PlayCircleOutlined />,
      type: 'default' as const,
      danger: false,
    },
  ];

  return (
    <Card
      className="bg-bg-card border-border"
      title={
        <Title level={4} className="text-text-primary m-0">
          빠른 액션
        </Title>
      }
      bordered={false}
    >
      <div className="space-y-3">
        {actions.map((action, index) => (
          <div key={action.key}>
            <Button
              block
              type={action.type}
              danger={action.danger}
              icon={action.icon}
              size="large"
              onClick={() => onActionClick(action.key)}
              className={`
                h-auto py-3 px-4 text-left
                ${action.type === 'primary'
                  ? 'bg-accent-primary border-accent-primary hover:bg-opacity-80'
                  : 'border-border text-text-primary hover:text-accent-primary hover:border-accent-primary'
                }
                ${action.danger ? 'hover:border-danger hover:text-danger' : ''}
              `}
            >
              <div className="flex items-start space-x-3">
                <div className="mt-1">{action.icon}</div>
                <div className="flex-1 text-left">
                  <div className="font-medium mb-1">{action.title}</div>
                  <div className={`text-sm ${
                    action.type === 'primary'
                      ? 'text-white opacity-80'
                      : 'text-text-secondary'
                  }`}>
                    {action.description}
                  </div>
                </div>
              </div>
            </Button>
            {index < actions.length - 1 && (
              <Divider className="border-border my-3" />
            )}
          </div>
        ))}
      </div>

      {/* Emergency contact */}
      <div className="mt-6 p-3 bg-bg-surface rounded-lg">
        <div className="text-text-secondary text-xs space-y-1">
          <div className="font-medium">긴급 상황</div>
          <div>📞 24/7 지원: 1588-1234</div>
          <div>📧 security@company.com</div>
        </div>
      </div>
    </Card>
  );
};