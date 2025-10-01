import React from 'react';
import { Spin, Space } from 'antd';
import { LoadingOutlined } from '@ant-design/icons';

interface LoadingProps {
  size?: 'small' | 'default' | 'large';
  tip?: string;
  spinning?: boolean;
  children?: React.ReactNode;
}

const antIcon = <LoadingOutlined style={{ fontSize: 24, color: '#1ec997' }} spin />;

export const Loading: React.FC<LoadingProps> = ({
  size = 'default',
  tip = '로딩 중...',
  spinning = true,
  children
}) => {
  if (children) {
    return (
      <Spin
        spinning={spinning}
        tip={tip}
        indicator={antIcon}
        size={size}
      >
        {children}
      </Spin>
    );
  }

  return (
    <div className="flex items-center justify-center h-full min-h-32">
      <Space direction="vertical" align="center">
        <Spin indicator={antIcon} size={size} />
        <span className="text-text-secondary">{tip}</span>
      </Space>
    </div>
  );
};

export const PageLoading: React.FC<{ tip?: string }> = ({ tip = '페이지 로딩 중...' }) => (
  <div className="flex items-center justify-center h-screen">
    <Space direction="vertical" align="center" size="large">
      <Spin indicator={antIcon} size="large" />
      <span className="text-text-secondary text-lg">{tip}</span>
    </Space>
  </div>
);

export const ButtonLoading: React.FC = () => (
  <LoadingOutlined style={{ fontSize: 16, color: '#ffffff' }} spin />
);