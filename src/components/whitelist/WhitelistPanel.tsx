import React from 'react';
import { Card, Typography } from 'antd';

const { Title } = Typography;

export const WhitelistPanel: React.FC = () => {
  return (
    <Card className="bg-bg-card border-border">
      <Title level={3} className="text-text-primary">
        화이트리스트 관리
      </Title>
      <div className="text-text-secondary">
        IP 화이트리스트 관리 기능이 구현될 예정입니다.
      </div>
    </Card>
  );
};