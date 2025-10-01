import React, { useState, useEffect } from 'react';
import { CloseOutlined, EditOutlined, DeleteOutlined, StopOutlined, CheckCircleOutlined, ClockCircleOutlined, UserOutlined, SafetyCertificateOutlined, DashboardOutlined, ExclamationCircleOutlined, CodeOutlined, EnvironmentOutlined } from '@ant-design/icons';
import { CustomRule, RuleType, RuleSeverity } from '../../types/waf';
import { apiClient } from '../../services/api';

interface CustomRuleDetailProps {
  ruleId: number;
  onClose: () => void;
  onEdit: (rule: CustomRule) => void;
  onDelete: (ruleId: number) => void;
  onRuleUpdated: () => void;
}

const RULE_TYPE_LABELS: Record<RuleType, string> = {
  'BLOCK': '차단 (BLOCK)',
  'DENY': '거부 (DENY)',
  'DROP': '폐기 (DROP)',
  'LOG': '로그 (LOG)',
  'RATE_LIMIT': '속도제한 (RATE_LIMIT)',
  'REDIRECT': '리다이렉트 (REDIRECT)',
  'CUSTOM': '커스텀 (CUSTOM)'
};

const SEVERITY_CONFIG: Record<RuleSeverity, { label: string; color: string; bgColor: string }> = {
  'INFO': { label: '정보', color: 'text-blue-600', bgColor: 'bg-blue-100' },
  'LOW': { label: '낮음', color: 'text-green-600', bgColor: 'bg-green-100' },
  'MEDIUM': { label: '보통', color: 'text-yellow-600', bgColor: 'bg-yellow-100' },
  'HIGH': { label: '높음', color: 'text-orange-600', bgColor: 'bg-orange-100' },
  'CRITICAL': { label: '위험', color: 'text-red-600', bgColor: 'bg-red-100' }
};

export const CustomRuleDetail: React.FC<CustomRuleDetailProps> = ({
  ruleId,
  onClose,
  onEdit,
  onDelete,
  onRuleUpdated
}) => {
  const [rule, setRule] = useState<CustomRule | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [toggleLoading, setToggleLoading] = useState(false);

  useEffect(() => {
    loadRule();
  }, [ruleId]);

  const loadRule = async () => {
    try {
      setLoading(true);
      setError(null);
      const ruleData = await apiClient.getCustomRule(ruleId);
      setRule(ruleData);
    } catch (err: any) {
      setError(err.response?.data?.message || '룰 정보를 불러오는 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async () => {
    if (!rule) return;

    try {
      setToggleLoading(true);
      const updatedRule = await apiClient.toggleCustomRuleStatus(rule.id);
      setRule(updatedRule);
      onRuleUpdated();
    } catch (err: any) {
      setError(err.response?.data?.message || '룰 상태 변경 중 오류가 발생했습니다.');
    } finally {
      setToggleLoading(false);
    }
  };

  const handleEdit = () => {
    if (rule) {
      onEdit(rule);
    }
  };

  const handleDelete = () => {
    if (rule && window.confirm('정말로 이 룰을 삭제하시겠습니까?')) {
      onDelete(rule.id);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('ko-KR');
  };

  if (loading) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
        <div className="bg-white rounded-lg p-8 max-w-md w-full">
          <div className="flex items-center justify-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            <span className="ml-3 text-gray-600">룰 정보를 불러오는 중...</span>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
        <div className="bg-white rounded-lg p-6 max-w-md w-full">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900">오류</h3>
            <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
              <CloseOutlined className="w-6 h-6" />
            </button>
          </div>
          <div className="flex items-center gap-2 text-red-600 mb-4">
            <ExclamationCircleOutlined className="w-5 h-5" />
            <span>{error}</span>
          </div>
          <div className="flex justify-end gap-2">
            <button
              onClick={loadRule}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              다시 시도
            </button>
            <button
              onClick={onClose}
              className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
            >
              닫기
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (!rule) {
    return null;
  }

  const severityConfig = SEVERITY_CONFIG[rule.severity];

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <div className="flex items-center gap-3">
            <h2 className="text-xl font-semibold text-gray-900">{rule.name}</h2>
            <span className={`px-2 py-1 text-xs font-medium rounded-full ${severityConfig.bgColor} ${severityConfig.color}`}>
              {severityConfig.label}
            </span>
            <div className="flex items-center gap-1">
              {rule.enabled ? (
                <CheckCircleOutlined className="w-5 h-5 text-green-600" />
              ) : (
                <StopOutlined className="w-5 h-5 text-gray-400" />
              )}
              <span className={`text-sm ${rule.enabled ? 'text-green-600' : 'text-gray-500'}`}>
                {rule.enabled ? '활성' : '비활성'}
              </span>
            </div>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600"
          >
            <CloseOutlined className="w-6 h-6" />
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6">
          <div className="space-y-6">
            {/* Description */}
            {rule.description && (
              <div>
                <h3 className="text-sm font-medium text-gray-700 mb-2">설명</h3>
                <p className="text-gray-600 bg-gray-50 p-3 rounded-lg">{rule.description}</p>
              </div>
            )}

            {/* Basic Information */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <div className="bg-gray-50 p-4 rounded-lg">
                <div className="flex items-center gap-2 mb-1">
                  <SafetyCertificateOutlined className="w-4 h-4 text-gray-500" />
                  <span className="text-sm font-medium text-gray-700">룰 타입</span>
                </div>
                <p className="text-gray-900">{RULE_TYPE_LABELS[rule.type]}</p>
              </div>

              <div className="bg-gray-50 p-4 rounded-lg">
                <div className="flex items-center gap-2 mb-1">
                  <DashboardOutlined className="w-4 h-4 text-gray-500" />
                  <span className="text-sm font-medium text-gray-700">우선순위</span>
                </div>
                <p className="text-gray-900">{rule.priority}</p>
              </div>

              <div className="bg-gray-50 p-4 rounded-lg">
                <div className="flex items-center gap-2 mb-1">
                  <DashboardOutlined className="w-4 h-4 text-gray-500" />
                  <span className="text-sm font-medium text-gray-700">매칭 횟수</span>
                </div>
                <p className="text-gray-900">{rule.matchCount.toLocaleString()}</p>
              </div>

              <div className="bg-gray-50 p-4 rounded-lg">
                <div className="flex items-center gap-2 mb-1">
                  <SafetyCertificateOutlined className="w-4 h-4 text-gray-500" />
                  <span className="text-sm font-medium text-gray-700">차단 횟수</span>
                </div>
                <p className="text-gray-900">{rule.blockCount.toLocaleString()}</p>
              </div>
            </div>

            {/* Target Configuration */}
            {(rule.targetService || rule.targetPath) && (
              <div>
                <h3 className="text-sm font-medium text-gray-700 mb-3 flex items-center gap-2">
                  <EnvironmentOutlined className="w-4 h-4" />
                  대상 설정
                </h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {rule.targetService && (
                    <div className="bg-blue-50 p-4 rounded-lg border border-blue-200">
                      <span className="text-sm font-medium text-blue-700">대상 서비스</span>
                      <p className="text-blue-900 mt-1">{rule.targetService}</p>
                    </div>
                  )}
                  {rule.targetPath && (
                    <div className="bg-blue-50 p-4 rounded-lg border border-blue-200">
                      <span className="text-sm font-medium text-blue-700">대상 경로</span>
                      <p className="text-blue-900 mt-1">{rule.targetPath}</p>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Rule Content */}
            <div>
              <h3 className="text-sm font-medium text-gray-700 mb-3 flex items-center gap-2">
                <CodeOutlined className="w-4 h-4" />
                룰 내용 (ModSecurity)
              </h3>
              <pre className="bg-gray-900 text-green-400 p-4 rounded-lg text-sm overflow-x-auto font-mono whitespace-pre-wrap">
                {rule.ruleContent}
              </pre>
            </div>

            {/* Metadata */}
            <div>
              <h3 className="text-sm font-medium text-gray-700 mb-3 flex items-center gap-2">
                <ClockCircleOutlined className="w-4 h-4" />
                메타데이터
              </h3>
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <div className="space-y-3">
                  <div className="flex items-center gap-3">
                    <UserOutlined className="w-4 h-4 text-gray-500" />
                    <div>
                      <span className="text-sm text-gray-500">생성자</span>
                      <p className="text-gray-900">{rule.createdBy.name} ({rule.createdBy.email})</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <ClockCircleOutlined className="w-4 h-4 text-gray-500" />
                    <div>
                      <span className="text-sm text-gray-500">생성일시</span>
                      <p className="text-gray-900">{formatDate(rule.createdAt)}</p>
                    </div>
                  </div>
                </div>
                <div className="space-y-3">
                  <div className="flex items-center gap-3">
                    <ClockCircleOutlined className="w-4 h-4 text-gray-500" />
                    <div>
                      <span className="text-sm text-gray-500">수정일시</span>
                      <p className="text-gray-900">{formatDate(rule.updatedAt)}</p>
                    </div>
                  </div>
                  {rule.lastMatchedAt && (
                    <div className="flex items-center gap-3">
                      <DashboardOutlined className="w-4 h-4 text-gray-500" />
                      <div>
                        <span className="text-sm text-gray-500">마지막 매칭</span>
                        <p className="text-gray-900">{formatDate(rule.lastMatchedAt)}</p>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-between p-6 border-t border-gray-200 bg-gray-50">
          <div className="flex items-center gap-2">
            <button
              onClick={handleToggleStatus}
              disabled={toggleLoading}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg transition-colors ${
                rule.enabled
                  ? 'bg-orange-100 text-orange-700 hover:bg-orange-200'
                  : 'bg-green-100 text-green-700 hover:bg-green-200'
              }`}
            >
              {rule.enabled ? <StopOutlined className="w-4 h-4" /> : <CheckCircleOutlined className="w-4 h-4" />}
              {toggleLoading ? '변경 중...' : rule.enabled ? '비활성화' : '활성화'}
            </button>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleEdit}
              className="flex items-center gap-2 px-4 py-2 text-blue-700 border border-blue-300 rounded-lg hover:bg-blue-50"
            >
              <EditOutlined className="w-4 h-4" />
              수정
            </button>
            <button
              onClick={handleDelete}
              className="flex items-center gap-2 px-4 py-2 text-red-700 border border-red-300 rounded-lg hover:bg-red-50"
            >
              <DeleteOutlined className="w-4 h-4" />
              삭제
            </button>
            <button
              onClick={onClose}
              className="px-4 py-2 text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50"
            >
              닫기
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};