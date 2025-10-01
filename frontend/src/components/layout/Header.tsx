import React from 'react';
import { Layout, Avatar, Dropdown, Space, Typography, Badge } from 'antd';
import {
  UserOutlined,
  LogoutOutlined,
  SettingOutlined,
  BellOutlined,
  WifiOutlined,
  DisconnectOutlined,
  DownOutlined
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
    <AntHeader
      className="bg-bg-card border-b border-border flex items-center justify-between"
      style={{
        height: '80px',
        paddingLeft: '24px',
        paddingRight: '24px',
        paddingTop: '12px',
        paddingBottom: '12px'
      }}
    >
      {/* Left side - Logo and Title */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          {/* Enhanced Logo */}
          <div style={{
            width: '40px',
            height: '40px',
            background: 'linear-gradient(135deg, #1ec997 0%, #48bb78 100%)',
            borderRadius: '10px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 4px 12px rgba(30, 201, 151, 0.3)'
          }}>
            <svg width="24" height="24" fill="none" stroke="white" viewBox="0 0 24 24" strokeWidth="2.5">
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
          </div>
          <div>
            <div className="text-text-primary font-bold" style={{ fontSize: '18px', lineHeight: '20px' }}>
              WAF Console
            </div>
            <div className="text-text-secondary" style={{ fontSize: '11px', opacity: 0.7, lineHeight: '14px' }}>
              Web Application Firewall
            </div>
          </div>
        </div>

        {/* Connection Status - Development mode shows as connected */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          padding: '8px 12px',
          backgroundColor: '#2c3545',
          borderRadius: '8px'
        }}>
          <div style={{
            width: '8px',
            height: '8px',
            backgroundColor: '#1ec997',
            borderRadius: '50%',
            animation: 'pulse 2s infinite'
          }}></div>
          <Text style={{ fontSize: '13px', fontWeight: 500, color: '#1ec997' }}>WAF 엔진 활성</Text>
        </div>
      </div>

      {/* Right side - User actions */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        {/* Notifications */}
        <div style={{
          padding: '8px',
          borderRadius: '8px',
          cursor: 'pointer',
          transition: 'background-color 0.2s'
        }}>
          <Badge count={0} showZero={false}>
            <BellOutlined
              className="text-text-secondary cursor-pointer hover:text-accent-primary"
              style={{ fontSize: '18px' }}
            />
          </Badge>
        </div>

        {/* User dropdown - Always visible for demo */}
        <Dropdown
          menu={{ items: userMenuItems }}
          placement="bottomRight"
          arrow
        >
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            cursor: 'pointer',
            padding: '8px 12px',
            borderRadius: '8px',
            transition: 'background-color 0.2s'
          }}
          className="hover:bg-bg-surface"
          >
            <Avatar
              size={44}
              src="https://cdn.pixabay.com/photo/2024/02/28/07/42/european-shorthair-8601492_640.jpg"
              icon={<UserOutlined />}
              className="bg-accent-primary"
              style={{ border: '2px solid #1ec997' }}
            />
            <div style={{ display: 'flex', alignItems: 'center', height: '44px', gap: '8px' }}>
              <div style={{ fontSize: '16px', fontWeight: 600, color: '#ffffff' }}>
                고양이 관리자 님
              </div>
              <DownOutlined style={{ fontSize: '12px', color: '#a8b2c1' }} />
            </div>
          </div>
        </Dropdown>
      </div>
    </AntHeader>
  );
};