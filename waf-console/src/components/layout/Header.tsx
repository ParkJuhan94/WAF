import React from 'react';
import { Layout, Avatar, Dropdown, Space, Typography, Badge } from 'antd';
import {
  UserOutlined,
  LogoutOutlined,
  SettingOutlined,
  BellOutlined,
  WifiOutlined,
  DisconnectOutlined
} from '@ant-design/icons';
import { useAuthStore } from '../../stores/useAuthStore';
import { useRealtimeData } from '../../hooks/useRealtimeData';
import { StatusBadge } from '../common/StatusBadge';

const { Header: AntHeader } = Layout;
const { Text } = Typography;

export const Header: React.FC = () => {
  const { user, logout } = useAuthStore();
  const { isConnected } = useRealtimeData();

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '프로필',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '설정',
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '로그아웃',
      onClick: logout,
    },
  ];

  return (
    <AntHeader className="bg-bg-card border-b border-border px-8 flex items-center justify-between h-18">
      {/* Left side - Logo and Title */}
      <div className="flex items-center space-x-6">
        <div className="flex items-center space-x-3">
          {/* Enhanced Logo */}
          <div className="relative">
            <div className="w-10 h-10 bg-gradient-to-br from-accent-primary to-green-400 rounded-xl flex items-center justify-center shadow-lg">
              <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
              </svg>
            </div>
          </div>
          <div>
            <div className="text-text-primary font-bold text-xl">WAF Console</div>
            <div className="text-text-secondary text-xs">Web Application Firewall</div>
          </div>
        </div>

        {/* Connection Status */}
        <div className="flex items-center space-x-2 bg-bg-surface px-3 py-2 rounded-lg">
          {isConnected ? (
            <>
              <div className="w-2 h-2 bg-success rounded-full animate-pulse"></div>
              <Text className="text-success text-sm font-medium">실시간 연결됨</Text>
            </>
          ) : (
            <>
              <div className="w-2 h-2 bg-danger rounded-full"></div>
              <Text className="text-danger text-sm font-medium">연결 끊김</Text>
            </>
          )}
        </div>
      </div>

      {/* Right side - User actions */}
      <div className="flex items-center space-x-6">
        {/* Notifications */}
        <div className="relative">
          <Badge count={0} showZero={false}>
            <div className="p-2 hover:bg-bg-surface rounded-lg cursor-pointer transition-colors">
              <BellOutlined className="text-text-secondary text-lg hover:text-accent-primary" />
            </div>
          </Badge>
        </div>

        {/* User dropdown */}
        <Dropdown
          menu={{ items: userMenuItems }}
          placement="bottomRight"
          arrow
        >
          <div className="flex items-center space-x-3 cursor-pointer hover:bg-bg-surface rounded-lg px-4 py-2 transition-colors">
            <Avatar
              size={36}
              src={user?.avatar}
              icon={<UserOutlined />}
              className="bg-accent-primary"
            />
            <div className="flex flex-col">
              <Text className="text-text-primary text-sm font-medium">
                {user?.name || '관리자'}
              </Text>
              <Text className="text-text-secondary text-xs">
                {user?.role || 'Administrator'}
              </Text>
            </div>
          </div>
        </Dropdown>
      </div>
    </AntHeader>
  );
};