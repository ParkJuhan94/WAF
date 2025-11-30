import React, { useState, useEffect } from 'react';
import { Card, Typography, Space, message } from 'antd';
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
  const googleButtonRef = React.useRef<HTMLDivElement>(null);

  useEffect(() => {
    const initializeGoogleSignIn = () => {
      if (window.google && googleButtonRef.current) {
        try {
          window.google.accounts.id.initialize({
            client_id: import.meta.env.VITE_GOOGLE_CLIENT_ID,
            callback: handleCredentialResponse,
            auto_select: false,
            cancel_on_tap_outside: false,
            ux_mode: 'popup',
            context: 'signin'
          });

          // Render the Google Sign-In button
          window.google.accounts.id.renderButton(
            googleButtonRef.current,
            {
              theme: 'filled_blue',
              size: 'large',
              width: 350,
              text: 'signin_with',
              shape: 'rectangular',
              logo_alignment: 'left'
            }
          );

          console.log('Google Sign-In initialized and button rendered successfully');
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

      console.log('✅ Google 로그인 성공:', payload);

      // 사용자 정보 저장
      await login(credential);

      message.success(`환영합니다, ${payload.name}님!`);
      navigate('/dashboard');
    } catch (error) {
      console.error('❌ Login failed:', error);
      message.error('로그인에 실패했습니다.');
    } finally {
      setLoading(false);
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

          {/* Google Sign-In Button Container */}
          <div className="flex justify-center">
            <div ref={googleButtonRef}></div>
          </div>

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