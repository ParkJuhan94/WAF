import React from 'react';
import { ConfigProvider, theme } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ErrorBoundary } from './components/common/ErrorBoundary';
import { AppLayout } from './components/layout/AppLayout';
import { GoogleLogin } from './components/auth/GoogleLogin';
import { useAuthStore } from './stores/useAuthStore';
import './index.css';

// Create a client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 3,
      staleTime: 5 * 60 * 1000, // 5 minutes
      refetchOnWindowFocus: false,
    },
  },
});

// Dark theme configuration for Ant Design
const darkTheme = {
  algorithm: theme.darkAlgorithm,
  token: {
    colorPrimary: '#1ec997',
    colorBgBase: '#1b2431',
    colorBgContainer: '#242c3a',
    colorBgElevated: '#2c3545',
    colorBorder: '#3a4553',
    colorText: '#ffffff',
    colorTextSecondary: '#a8b2c1',
    colorError: '#ff6b6b',
    colorWarning: '#feca57',
    colorSuccess: '#1ec997',
    colorInfo: '#48cae4',
  },
};

const AppContent: React.FC = () => {
  const { isAuthenticated } = useAuthStore();

  if (!isAuthenticated) {
    return <GoogleLogin />;
  }

  return <AppLayout />;
};

const App: React.FC = () => {
  return (
    <ErrorBoundary>
      <ConfigProvider theme={darkTheme}>
        <QueryClientProvider client={queryClient}>
          <AppContent />
        </QueryClientProvider>
      </ConfigProvider>
    </ErrorBoundary>
  );
};

export default App;