import React from 'react';
import { Card, Button, Typography, Space } from 'antd';
import { GoogleOutlined } from '@ant-design/icons';

const { Title, Text } = Typography;

export const GoogleLogin: React.FC = () => {
  const handleGoogleLogin = () => {
    // Google OAuth implementation will go here
    console.log('Google login clicked');
  };

  return (
    <div className="min-h-screen bg-bg-primary flex items-center justify-center">
      <Card className="bg-bg-card border-border w-96">
        <Space direction="vertical" size="large" className="w-full">
          <div className="text-center">
            <div className="flex justify-center mb-4">
              <div className="w-16 h-16 bg-accent-primary rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-2xl">W</span>
              </div>
            </div>
            <Title level={2} className="text-text-primary m-0">
              WAF Console
            </Title>
            <Text className="text-text-secondary">
              보안 웹 방화벽 관리 콘솔
            </Text>
          </div>

          <Button
            type="primary"
            size="large"
            icon={<GoogleOutlined />}
            onClick={handleGoogleLogin}
            className="w-full bg-accent-primary border-accent-primary hover:bg-opacity-80"
          >
            Google로 로그인
          </Button>

          <div className="text-center">
            <Text className="text-text-secondary text-sm">
              보안 관리자만 접근 가능합니다
            </Text>
          </div>
        </Space>
      </Card>
    </div>
  );
};