import React from 'react';
import { ConfigProvider, theme, Layout } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ErrorBoundary } from './components/common/ErrorBoundary';
import { MockDashboard } from './components/dashboard/MockDashboard';
import './index.css';

const { Header, Content } = Layout;

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
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{
        display: 'flex',
        alignItems: 'center',
        padding: '0 24px',
        borderBottom: '1px solid #3a4553'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{
            width: '32px',
            height: '32px',
            background: '#1ec997',
            borderRadius: '8px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontWeight: 'bold',
            color: 'white'
          }}>
            W
          </div>
          <span style={{ color: 'white', fontSize: '18px', fontWeight: 'bold' }}>
            WAF Console
          </span>
        </div>
      </Header>
      <Content style={{ padding: '24px', background: '#1b2431' }}>
        <MockDashboard />
      </Content>
    </Layout>
  );
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