import React, { useState } from 'react';
import { Layout, Menu, Button } from 'antd';
import {
  DashboardOutlined,
  FileTextOutlined,
  SecurityScanOutlined,
  SafetyCertificateOutlined,
  BugOutlined,
  SettingOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined
} from '@ant-design/icons';

const { Sider } = Layout;

interface SidebarProps {
  activeTab: string;
  onTabChange: (tab: string) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ activeTab, onTabChange }) => {
  const [collapsed, setCollapsed] = useState(false);

  const menuItems = [
    {
      key: 'dashboard',
      icon: <DashboardOutlined />,
      label: '대시보드',
    },
    {
      key: 'logs',
      icon: <FileTextOutlined />,
      label: '로그 분석',
    },
    {
      key: 'rules',
      icon: <SecurityScanOutlined />,
      label: '룰 관리',
    },
    {
      key: 'whitelist',
      icon: <SafetyCertificateOutlined />,
      label: '화이트리스트',
    },
    {
      key: 'testing',
      icon: <BugOutlined />,
      label: '공격 테스트',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '설정',
    },
  ];

  return (
    <Sider
      trigger={null}
      collapsible
      collapsed={collapsed}
      className="bg-bg-card border-r border-border"
      width={240}
      collapsedWidth={80}
    >
      {/* Collapse toggle button */}
      <div className="p-4 border-b border-border">
        <Button
          type="text"
          icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
          onClick={() => setCollapsed(!collapsed)}
          className="text-text-secondary hover:text-accent-primary w-full"
        />
      </div>

      {/* Navigation menu */}
      <Menu
        theme="dark"
        mode="inline"
        selectedKeys={[activeTab]}
        items={menuItems}
        onClick={(e) => onTabChange(e.key)}
        className="bg-transparent border-0 pt-4"
        style={{
          backgroundColor: 'transparent',
        }}
      />

      {/* Footer info */}
      {!collapsed && (
        <div className="absolute bottom-4 left-4 right-4">
          <div className="bg-bg-surface rounded-lg p-3">
            <div className="text-text-secondary text-xs space-y-1">
              <div>WAF Console v1.0.0</div>
              <div>ModSecurity + OWASP CRS</div>
            </div>
          </div>
        </div>
      )}
    </Sider>
  );
};