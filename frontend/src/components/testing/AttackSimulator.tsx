import React from 'react';
import { Card, Typography } from 'antd';

const { Title } = Typography;

export const AttackSimulator: React.FC = () => {
  return (
    <Card className="bg-bg-card border-border">
      <Title level={3} className="text-text-primary">
        공격 시뮬레이터
      </Title>
      <div className="text-text-secondary">
        5가지 공격 시나리오 테스트 및 DVWA 연동 기능이 구현될 예정입니다.
      </div>
    </Card>
  );
};