import React from 'react';
import { Card, Button, Typography, Tooltip } from 'antd';
import {
  PlayCircleOutlined,
  SafetyCertificateOutlined,
  InfoCircleOutlined
} from '@ant-design/icons';

const { Title } = Typography;

interface QuickActionsProps {
  onActionClick: (action: string) => void;
}

export const QuickActions: React.FC<QuickActionsProps> = ({ onActionClick }) => {
  const actions = [
    {
      key: 'emergency-block',
      title: '긴급 차단 모드',
      description: '모든 요청을 일시적으로 차단',
      icon: <SafetyCertificateOutlined style={{ fontSize: '22px' }} />,
      iconColor: '#ff6b6b',
      textColor: '#ff6b6b',
      bgColor: 'rgba(255, 107, 107, 0.12)',
      hoverBg: 'rgba(255, 107, 107, 0.18)',
      hoverBorder: '#ff6b6b',
    },
    {
      key: 'waf-restart',
      title: 'WAF 재시작',
      description: '설정 변경 사항 적용',
      icon: <PlayCircleOutlined style={{ fontSize: '22px' }} />,
      iconColor: '#48cae4',
      textColor: '#48cae4',
      bgColor: 'rgba(72, 202, 228, 0.12)',
      hoverBg: 'rgba(72, 202, 228, 0.18)',
      hoverBorder: '#48cae4',
    },
  ];

  return (
    <Card
      className="bg-bg-card border-border h-full"
      title={
        <Title level={5} className="text-text-primary m-0">
          빠른 액션
        </Title>
      }
      bordered={false}
    >
      <div className="space-y-4">
        {actions.map((action) => (
          <Button
            key={action.key}
            block
            size="large"
            onClick={() => onActionClick(action.key)}
            className="h-auto py-5 px-5 text-left border-border hover:shadow-lg transition-all"
            style={{
              borderWidth: '2px',
              backgroundColor: action.bgColor,
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.borderColor = action.hoverBorder;
              e.currentTarget.style.backgroundColor = action.hoverBg;
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.borderColor = '#3a4553';
              e.currentTarget.style.backgroundColor = action.bgColor;
            }}
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-4">
                <div
                  className="flex-shrink-0 flex items-center justify-center w-14 h-14 rounded-lg"
                  style={{
                    backgroundColor: `${action.iconColor}30`,
                    color: action.iconColor,
                  }}
                >
                  {action.icon}
                </div>
                <div className="flex-1">
                  <div
                    className="font-bold text-lg"
                    style={{ color: action.textColor }}
                  >
                    {action.title}
                  </div>
                </div>
              </div>
              <Tooltip title={action.description} placement="left">
                <InfoCircleOutlined
                  className="text-text-secondary"
                  style={{ fontSize: '18px', cursor: 'help' }}
                />
              </Tooltip>
            </div>
          </Button>
        ))}
      </div>
    </Card>
  );
};