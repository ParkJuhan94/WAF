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
    <AntHeader className="bg-bg-card border-b border-border px-6 flex items-center justify-between h-16">
      {/* Left side - Logo and Title */}
      <div className="flex items-center space-x-4">
        <div className="flex items-center space-x-2">
          <div className="w-8 h-8 bg-accent-primary rounded-lg flex items-center justify-center">
            <span className="text-white font-bold text-lg">W</span>
          </div>
          <span className="text-text-primary font-bold text-xl">WAF Console</span>
        </div>

        {/* Connection Status */}
        <div className="flex items-center space-x-2">
          {isConnected ? (
            <>
              <WifiOutlined className="text-success" />
              <Text className="text-success text-sm">실시간 연결됨</Text>
            </>
          ) : (
            <>
              <DisconnectOutlined className="text-danger" />
              <Text className="text-danger text-sm">연결 끊김</Text>
            </>
          )}
        </div>
      </div>

      {/* Right side - User actions */}
      <div className="flex items-center space-x-4">
        {/* Notifications */}
        <Badge count={0} showZero={false}>
          <BellOutlined className="text-text-secondary text-lg cursor-pointer hover:text-accent-primary" />
        </Badge>

        {/* User dropdown */}
        <Dropdown
          menu={{ items: userMenuItems }}
          placement="bottomRight"
          arrow
        >
          <div className="flex items-center space-x-2 cursor-pointer hover:bg-bg-surface rounded-lg px-3 py-2 transition-colors">
            <Avatar
              size="small"
              src={user?.avatar}
              icon={<UserOutlined />}
              className="bg-accent-primary"
            />
            <div className="flex flex-col">
              <Text className="text-text-primary text-sm font-medium">
                {user?.name || 'Unknown User'}
              </Text>
              <Text className="text-text-secondary text-xs">
                {user?.role || 'User'}
              </Text>
            </div>
          </div>
        </Dropdown>
      </div>
    </AntHeader>
  );
};