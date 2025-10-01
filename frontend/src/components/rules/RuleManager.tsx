import React from 'react';
import { Card, Typography } from 'antd';

const { Title } = Typography;

export const RuleManager: React.FC = () => {
  return (
    <Card className="bg-bg-card border-border">
      <Title level={3} className="text-text-primary">
        룰 관리
      </Title>
      <div className="text-text-secondary">
        WAF 룰 관리, CRUD 작업 및 테스트 기능이 구현될 예정입니다.
      </div>
    </Card>
  );
};