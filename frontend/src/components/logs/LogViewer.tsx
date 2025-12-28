import React from 'react';
import { Card } from 'antd';
import { FileTextOutlined } from '@ant-design/icons';

export const LogViewer: React.FC = () => {
  return (
    <div className="p-6">
      <Card className="bg-bg-card border-border">
        <div className="flex flex-col items-center justify-center py-12">
          <FileTextOutlined className="text-6xl text-text-secondary mb-4" />
          <h2 className="text-2xl font-bold text-text-primary mb-2">
            로그 뷰어
          </h2>
          <p className="text-text-secondary text-center max-w-md">
            WAF 로그 뷰어 기능은 현재 개발 중입니다.
            <br />
            곧 실시간 로그 조회 및 분석 기능이 추가될 예정입니다.
          </p>
        </div>
      </Card>
    </div>
  );
};
