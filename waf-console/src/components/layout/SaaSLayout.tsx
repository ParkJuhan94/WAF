import React, { useState } from 'react';
import { Layout, Menu } from 'antd';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  DashboardOutlined,
  SecurityScanOutlined,
  BarChartOutlined,
  SettingOutlined,
  UserOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined
} from '@ant-design/icons';
import { Header } from './Header';

const { Sider, Content } = Layout;

export const SaaSLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const menuItems = [
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: '대시보드',
    },
    {
      key: '/rules',
      icon: <SecurityScanOutlined />,
      label: '룰 관리',
    },
    {
      key: '/analytics',
      icon: <BarChartOutlined />,
      label: '분석',
    },
    {
      key: '/settings',
      icon: <SettingOutlined />,
      label: '설정',
    },
    {
      key: '/profile',
      icon: <UserOutlined />,
      label: '프로필',
    },
  ];

  const handleMenuClick = (e: any) => {
    navigate(e.key);
  };

  return (
    <Layout className="min-h-screen">
      <Header />
      <Layout>
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
            <div
              className="flex items-center justify-center cursor-pointer text-text-secondary hover:text-accent-primary"
              onClick={() => setCollapsed(!collapsed)}
            >
              {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            </div>
          </div>

          {/* Navigation menu */}
          <Menu
            theme="dark"
            mode="inline"
            selectedKeys={[location.pathname]}
            items={menuItems}
            onClick={handleMenuClick}
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
                  <div>WAF SaaS v1.0.0</div>
                  <div>ModSecurity + OWASP CRS</div>
                </div>
              </div>
            </div>
          )}
        </Sider>

        <Layout>
          <Content className="bg-bg-primary overflow-auto">
            <Outlet />
          </Content>
        </Layout>
      </Layout>
    </Layout>
  );
};