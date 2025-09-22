import React from 'react';
import { Card, Typography } from 'antd';

const { Title } = Typography;

export const SettingsPanel: React.FC = () => {
  return (
    <Card className="bg-bg-card border-border">
      <Title level={3} className="text-text-primary">
        설정
      </Title>
      <div className="text-text-secondary">
        시스템 설정 및 사용자 관리 기능이 구현될 예정입니다.
      </div>
    </Card>
  );
};