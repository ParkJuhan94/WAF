import React, { useState } from 'react';
import { SafetyCertificateOutlined, PlusOutlined, SettingOutlined } from '@ant-design/icons';
import { CustomRuleList } from '../components/rules/CustomRuleList';
import { CustomRuleForm } from '../components/rules/CustomRuleForm';
import { CustomRuleDetail } from '../components/rules/CustomRuleDetail';
import { CustomRule } from '../types/waf';

type ViewMode = 'list' | 'create' | 'edit' | 'detail';

export const RulesPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'owasp' | 'custom'>('custom');
  const [viewMode, setViewMode] = useState<ViewMode>('list');
  const [selectedRule, setSelectedRule] = useState<CustomRule | null>(null);
  const [selectedRuleId, setSelectedRuleId] = useState<number | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  const handleCreateRule = () => {
    setSelectedRule(null);
    setViewMode('create');
  };

  const handleEditRule = (rule: CustomRule) => {
    setSelectedRule(rule);
    setViewMode('edit');
  };

  const handleViewRule = (ruleId: number) => {
    setSelectedRuleId(ruleId);
    setViewMode('detail');
  };

  const handleDeleteRule = (ruleId: number) => {
    setRefreshKey(prev => prev + 1);
    setViewMode('list');
  };

  const handleSaveRule = (rule: CustomRule) => {
    setRefreshKey(prev => prev + 1);
    setViewMode('list');
  };

  const handleCancel = () => {
    setSelectedRule(null);
    setSelectedRuleId(null);
    setViewMode('list');
  };

  const handleRuleUpdated = () => {
    setRefreshKey(prev => prev + 1);
  };

  const renderOWASPRules = () => (
    <div className="bg-white rounded-lg shadow-sm p-8">
      <div className="text-center">
        <SafetyCertificateOutlined className="w-16 h-16 text-gray-400 mx-auto mb-4" />
        <h3 className="text-lg font-semibold text-gray-900 mb-2">
          OWASP CRS 룰 관리
        </h3>
        <p className="text-gray-600 mb-6">
          OWASP Core Rule Set 기반의 보안 룰을 관리합니다
        </p>
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <p className="text-blue-800 text-sm">
            📋 OWASP CRS 룰 관리 기능은 추후 구현 예정입니다.<br />
            현재는 커스텀 룰 관리 기능을 사용해주세요.
          </p>
        </div>
      </div>
    </div>
  );

  const renderCustomRules = () => {
    if (viewMode === 'create' || viewMode === 'edit') {
      return (
        <CustomRuleForm
          rule={selectedRule}
          onSave={handleSaveRule}
          onCancel={handleCancel}
        />
      );
    }

    if (viewMode === 'detail' && selectedRuleId) {
      return (
        <CustomRuleDetail
          ruleId={selectedRuleId}
          onClose={handleCancel}
          onEdit={handleEditRule}
          onDelete={handleDeleteRule}
          onRuleUpdated={handleRuleUpdated}
        />
      );
    }

    return (
      <CustomRuleList
        key={refreshKey}
        onCreateRule={handleCreateRule}
        onEditRule={handleEditRule}
        onViewRule={handleViewRule}
        onDeleteRule={handleDeleteRule}
      />
    );
  };

  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">룰 관리</h1>
        {activeTab === 'custom' && viewMode === 'list' && (
          <div className="flex items-center gap-3">
            <button
              onClick={handleCreateRule}
              className="btn-primary flex items-center gap-2"
            >
              <PlusOutlined className="w-4 h-4" />
              새 커스텀 룰 생성
            </button>
            <button className="btn-secondary flex items-center gap-2">
              <SettingOutlined className="w-4 h-4" />
              룰 설정
            </button>
          </div>
        )}
      </div>

      {/* Tab Navigation */}
      <div className="border-b border-gray-200 mb-6">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => {
              setActiveTab('custom');
              setViewMode('list');
            }}
            className={`whitespace-nowrap pb-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'custom'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            커스텀 룰
          </button>
          <button
            onClick={() => {
              setActiveTab('owasp');
              setViewMode('list');
            }}
            className={`whitespace-nowrap pb-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'owasp'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            OWASP CRS 룰
          </button>
        </nav>
      </div>

      {/* Content */}
      <div className="min-h-[calc(100vh-200px)]">
        {activeTab === 'custom' ? renderCustomRules() : renderOWASPRules()}
      </div>
    </div>
  );
};