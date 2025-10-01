import React, { useState } from 'react';
import { Layout } from 'antd';
import { Header } from './Header';
import { Sidebar } from './Sidebar';
import { Dashboard } from '../dashboard/Dashboard';
import { LogViewer } from '../logs/LogViewer';
import { RuleManager } from '../rules/RuleManager';
import { WhitelistPanel } from '../whitelist/WhitelistPanel';
import { AttackSimulator } from '../testing/AttackSimulator';
import { SettingsPanel } from '../settings/SettingsPanel';

const { Content } = Layout;

export const AppLayout: React.FC = () => {
  const [activeTab, setActiveTab] = useState('dashboard');

  const renderContent = () => {
    switch (activeTab) {
      case 'dashboard':
        return <Dashboard />;
      case 'logs':
        return <LogViewer />;
      case 'rules':
        return <RuleManager />;
      case 'whitelist':
        return <WhitelistPanel />;
      case 'testing':
        return <AttackSimulator />;
      case 'settings':
        return <SettingsPanel />;
      default:
        return <Dashboard />;
    }
  };

  return (
    <Layout className="min-h-screen bg-bg-primary">
      <Header />
      <Layout>
        <Sidebar activeTab={activeTab} onTabChange={setActiveTab} />
        <Layout>
          <Content className="bg-bg-primary p-6 overflow-auto">
            {renderContent()}
          </Content>
        </Layout>
      </Layout>
    </Layout>
  );
};