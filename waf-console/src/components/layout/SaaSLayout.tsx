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
          style={{ display: 'flex', flexDirection: 'column' }}
        >
          {/* Collapse toggle button */}
          <div className="p-4 border-b border-border">
            <div
              className="flex items-center justify-center cursor-pointer text-text-secondary hover:text-accent-primary transition-colors p-3 rounded-lg hover:bg-bg-surface"
              onClick={() => setCollapsed(!collapsed)}
              style={{
                minHeight: '48px',
                fontSize: '18px'
              }}
            >
              {collapsed ? <MenuUnfoldOutlined style={{ fontSize: '18px' }} /> : <MenuFoldOutlined style={{ fontSize: '18px' }} />}
            </div>
          </div>

          {/* Navigation menu */}
          <div style={{ flex: 1 }}>
            <Menu
              theme="dark"
              mode="inline"
              selectedKeys={[location.pathname]}
              items={menuItems}
              onClick={handleMenuClick}
              className="bg-transparent border-0"
              style={{
                backgroundColor: 'transparent',
                height: '100%',
              }}
            />
          </div>

        </Sider>

        <Layout>
          <Content className="bg-bg-primary" style={{ padding: '32px' }}>
            <Outlet />
          </Content>
        </Layout>
      </Layout>
    </Layout>
  );
};