import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Spin, message } from 'antd';
import { useAuthStore } from '../../stores/useAuthStore';

export const GoogleCallback: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { login } = useAuthStore();

  useEffect(() => {
    const handleCallback = async () => {
      try {
        const code = searchParams.get('code');
        const state = searchParams.get('state');
        const error = searchParams.get('error');

        if (error) {
          message.error('Google 로그인이 취소되었습니다.');
          navigate('/login');
          return;
        }

        if (!code) {
          message.error('인증 코드가 없습니다.');
          navigate('/login');
          return;
        }

        // Parse state to get redirect URL
        let redirectTo = '/dashboard';
        if (state) {
          try {
            const stateData = JSON.parse(decodeURIComponent(state));
            redirectTo = stateData.from || '/dashboard';
          } catch (e) {
            console.warn('Invalid state parameter:', state);
          }
        }

        // Exchange code for tokens
        await login(code);
        message.success('로그인 성공!');
        navigate(redirectTo);
      } catch (error) {
        console.error('OAuth callback error:', error);
        message.error('로그인 처리 중 오류가 발생했습니다.');
        navigate('/login');
      }
    };

    handleCallback();
  }, [searchParams, login, navigate]);

  return (
    <div className="min-h-screen bg-bg-primary flex items-center justify-center">
      <div className="text-center">
        <Spin size="large" />
        <div className="mt-4 text-text-primary">
          로그인 처리 중...
        </div>
      </div>
    </div>
  );
};