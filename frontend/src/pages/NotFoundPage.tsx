import React from 'react';
import { Result, Button } from 'antd';
import { useNavigate } from 'react-router-dom';

export const NotFoundPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-bg-primary flex items-center justify-center">
      <Result
        status="404"
        title="404"
        subTitle="죄송합니다. 요청하신 페이지를 찾을 수 없습니다."
        extra={
          <Button type="primary" onClick={() => navigate('/dashboard')}>
            대시보드로 돌아가기
          </Button>
        }
        className="text-text-primary"
      />
    </div>
  );
};