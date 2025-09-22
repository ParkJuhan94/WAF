import React from 'react';
import { Card, Typography } from 'antd';

const { Title } = Typography;

export const LogViewer: React.FC = () => {
  return (
    <Card className="bg-bg-card border-border">
      <Title level={3} className="text-text-primary">
        로그 분석
      </Title>
      <div className="text-text-secondary">
        실시간 로그 뷰어, 검색 및 필터링 기능이 구현될 예정입니다.
      </div>
    </Card>
  );
};