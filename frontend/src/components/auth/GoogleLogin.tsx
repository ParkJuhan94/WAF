import React, { useState, useEffect } from 'react';
import { Card, Button, Typography, Space, message } from 'antd';
import { GoogleOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../stores/useAuthStore';

const { Title, Text } = Typography;

declare global {
  interface Window {
    google: any;
  }
}

export const GoogleLogin: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuthStore();

  useEffect(() => {
    const initializeGoogleSignIn = () => {
      if (window.google) {
        try {
          window.google.accounts.id.initialize({
            client_id: import.meta.env.VITE_GOOGLE_CLIENT_ID,
            callback: handleCredentialResponse,
            auto_select: false,
            cancel_on_tap_outside: false,
            ux_mode: 'popup',
            context: 'signin'
          });
          console.log('Google Sign-In initialized successfully');
        } catch (error) {
          console.error('Failed to initialize Google Sign-In:', error);
        }
      }
    };

    const checkAndInitialize = () => {
      if (window.google) {
        initializeGoogleSignIn();
      } else {
        setTimeout(checkAndInitialize, 100);
      }
    };

    checkAndInitialize();
  }, []);

  const handleCredentialResponse = async (response: any) => {
    setLoading(true);
    try {
      const credential = response.credential;

      // JWT 토큰 디코딩 (base64url 디코딩)
      const payload = JSON.parse(atob(credential.split('.')[1]));

      console.log('Google 로그인 성공:', payload);

      // 사용자 정보 저장
      await login(credential);

      message.success(`환영합니다, ${payload.name}님!`);
      navigate('/dashboard');
    } catch (error) {
      console.error('Login failed:', error);
      message.error('로그인에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = () => {
    setLoading(true);

    if (window.google) {
      try {
        window.google.accounts.id.prompt((notification: any) => {
          console.log('Google prompt notification:', notification);
          if (notification.isNotDisplayed() || notification.isSkippedMoment()) {
            console.log('Prompt not displayed, trying renderButton approach');
            setLoading(false);
            message.warning('팝업이 차단되었습니다. 다시 시도해주세요.');
          }
        });
      } catch (error) {
        console.error('Google login error:', error);
        setLoading(false);
        message.error('Google 로그인 중 오류가 발생했습니다.');
      }
    } else {
      setLoading(false);
      message.error('Google 로그인 서비스를 초기화하는 중입니다. 잠시 후 다시 시도해주세요.');
    }
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
            loading={loading}
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