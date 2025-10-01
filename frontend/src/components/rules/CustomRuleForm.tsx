import React, { useState, useEffect } from 'react';
import { SaveOutlined, CloseOutlined, InfoCircleOutlined, ExclamationCircleOutlined, CodeOutlined } from '@ant-design/icons';
import { CustomRule, CustomRuleRequest, RuleType, RuleSeverity } from '../../types/waf';
import { apiClient } from '../../services/api';

interface CustomRuleFormProps {
  rule?: CustomRule; // undefined면 생성 모드, 있으면 수정 모드
  onSave: (rule: CustomRule) => void;
  onCancel: () => void;
}

const RULE_TYPE_OPTIONS: { value: RuleType; label: string; description: string }[] = [
  { value: 'BLOCK', label: '차단 (BLOCK)', description: '요청을 완전히 차단합니다' },
  { value: 'DENY', label: '거부 (DENY)', description: '요청을 거부하고 로깅합니다' },
  { value: 'DROP', label: '폐기 (DROP)', description: '요청을 조용히 폐기합니다' },
  { value: 'LOG', label: '로그 (LOG)', description: '요청을 로깅만 하고 허용합니다' },
  { value: 'RATE_LIMIT', label: '속도제한 (RATE_LIMIT)', description: '요청 속도를 제한합니다' },
  { value: 'REDIRECT', label: '리다이렉트 (REDIRECT)', description: '요청을 다른 URL로 리다이렉트합니다' },
  { value: 'CUSTOM', label: '커스텀 (CUSTOM)', description: '사용자 정의 액션을 수행합니다' }
];

const SEVERITY_OPTIONS: { value: RuleSeverity; label: string; color: string }[] = [
  { value: 'INFO', label: '정보', color: 'text-blue-600' },
  { value: 'LOW', label: '낮음', color: 'text-green-600' },
  { value: 'MEDIUM', label: '보통', color: 'text-yellow-600' },
  { value: 'HIGH', label: '높음', color: 'text-orange-600' },
  { value: 'CRITICAL', label: '위험', color: 'text-red-600' }
];

const RULE_EXAMPLES = {
  SQL_INJECTION: `# SQL Injection 탐지
SecRule ARGS "@detectSQLi" \\
    "id:1001,\\
    phase:2,\\
    block,\\
    msg:'SQL Injection Attack Detected',\\
    logdata:'Matched Data: %{MATCHED_VAR} found within %{MATCHED_VAR_NAME}: %{ARGS.%{MATCHED_VAR_NAME}}',\\
    tag:'attack-sqli'"`,

  XSS: `# Cross-Site Scripting 탐지
SecRule ARGS "@detectXSS" \\
    "id:1002,\\
    phase:2,\\
    block,\\
    msg:'XSS Attack Detected',\\
    logdata:'Matched Data: %{MATCHED_VAR} found within %{MATCHED_VAR_NAME}: %{ARGS.%{MATCHED_VAR_NAME}}',\\
    tag:'attack-xss'"`,

  RATE_LIMIT: `# Rate Limiting
SecRule IP:CONNECTION_COUNT "@gt 10" \\
    "id:1003,\\
    phase:1,\\
    deny,\\
    msg:'Rate limit exceeded - too many connections',\\
    expirevar:IP:CONNECTION_COUNT=60"`,

  FILE_UPLOAD: `# 파일 업로드 제한
SecRule FILES_TMPNAMES "@inspectFile /path/to/script.lua" \\
    "id:1004,\\
    phase:2,\\
    block,\\
    msg:'Malicious file upload detected'"`,

  CUSTOM: `# 커스텀 룰 예제
SecRule REQUEST_URI "@contains /admin" \\
    "id:1005,\\
    phase:1,\\
    block,\\
    msg:'Admin access blocked',\\
    tag:'access-control'"`
};

export const CustomRuleForm: React.FC<CustomRuleFormProps> = ({
  rule,
  onSave,
  onCancel
}) => {
  const [formData, setFormData] = useState<CustomRuleRequest>({
    name: '',
    description: '',
    ruleContent: '',
    type: 'BLOCK',
    severity: 'MEDIUM',
    priority: 1000,
    targetService: '',
    targetPath: ''
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedExample, setSelectedExample] = useState<string>('');

  const isEditMode = !!rule;

  useEffect(() => {
    if (rule) {
      setFormData({
        name: rule.name,
        description: rule.description,
        ruleContent: rule.ruleContent,
        type: rule.type,
        severity: rule.severity,
        priority: rule.priority,
        targetService: rule.targetService || '',
        targetPath: rule.targetPath || ''
      });
    }
  }, [rule]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      // 빈 문자열을 undefined로 변환
      const requestData: CustomRuleRequest = {
        ...formData,
        description: formData.description || undefined,
        targetService: formData.targetService || undefined,
        targetPath: formData.targetPath || undefined
      };

      const savedRule = isEditMode
        ? await apiClient.updateCustomRule(rule.id, requestData)
        : await apiClient.createCustomRule(requestData);

      onSave(savedRule);
    } catch (err: any) {
      setError(err.response?.data?.message || '룰 저장 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (
    field: keyof CustomRuleRequest,
    value: string | number
  ) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleExampleSelect = (exampleKey: string) => {
    if (exampleKey && RULE_EXAMPLES[exampleKey as keyof typeof RULE_EXAMPLES]) {
      setFormData(prev => ({
        ...prev,
        ruleContent: RULE_EXAMPLES[exampleKey as keyof typeof RULE_EXAMPLES]
      }));
    }
    setSelectedExample('');
  };

  const validateForm = () => {
    return formData.name.trim() && formData.ruleContent.trim();
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">
            {isEditMode ? '커스텀 룰 수정' : '새 커스텀 룰 생성'}
          </h2>
          <button
            onClick={onCancel}
            className="text-gray-400 hover:text-gray-600"
          >
            <CloseOutlined className="w-6 h-6" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="flex flex-col h-full">
          <div className="flex-1 overflow-y-auto p-6 space-y-6">
            {/* Error Message */}
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg flex items-center gap-2">
                <ExclamationCircleOutlined className="w-5 h-5" />
                {error}
              </div>
            )}

            {/* Basic Information */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  룰 이름 *
                </label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => handleInputChange('name', e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="예: SQL Injection Block Rule"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  우선순위
                </label>
                <input
                  type="number"
                  value={formData.priority}
                  onChange={(e) => handleInputChange('priority', parseInt(e.target.value) || 1000)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  min="1"
                  max="9999"
                />
                <p className="text-xs text-gray-500 mt-1">
                  높은 숫자일수록 우선순위가 높습니다 (1-9999)
                </p>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                설명
              </label>
              <textarea
                value={formData.description}
                onChange={(e) => handleInputChange('description', e.target.value)}
                rows={2}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="룰에 대한 상세 설명을 입력하세요"
              />
            </div>

            {/* Rule Type and Severity */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  룰 타입 *
                </label>
                <select
                  value={formData.type}
                  onChange={(e) => handleInputChange('type', e.target.value as RuleType)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                >
                  {RULE_TYPE_OPTIONS.map(option => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
                <p className="text-xs text-gray-500 mt-1">
                  {RULE_TYPE_OPTIONS.find(opt => opt.value === formData.type)?.description}
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  심각도 *
                </label>
                <select
                  value={formData.severity}
                  onChange={(e) => handleInputChange('severity', e.target.value as RuleSeverity)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                >
                  {SEVERITY_OPTIONS.map(option => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Target Configuration */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  대상 서비스
                </label>
                <input
                  type="text"
                  value={formData.targetService}
                  onChange={(e) => handleInputChange('targetService', e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="예: user-service, api-gateway"
                />
                <p className="text-xs text-gray-500 mt-1">
                  비워두면 모든 서비스에 적용됩니다
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  대상 경로
                </label>
                <input
                  type="text"
                  value={formData.targetPath}
                  onChange={(e) => handleInputChange('targetPath', e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="예: /api/user/*, /admin/*"
                />
                <p className="text-xs text-gray-500 mt-1">
                  비워두면 모든 경로에 적용됩니다
                </p>
              </div>
            </div>

            {/* Rule Content */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="block text-sm font-medium text-gray-700">
                  룰 내용 (ModSecurity 문법) *
                </label>
                <div className="flex items-center gap-2">
                  <CodeOutlined className="w-4 h-4 text-gray-400" />
                  <select
                    value={selectedExample}
                    onChange={(e) => handleExampleSelect(e.target.value)}
                    className="text-sm border border-gray-300 rounded px-2 py-1"
                  >
                    <option value="">예제 선택</option>
                    <option value="SQL_INJECTION">SQL Injection 탐지</option>
                    <option value="XSS">XSS 탐지</option>
                    <option value="RATE_LIMIT">Rate Limiting</option>
                    <option value="FILE_UPLOAD">파일 업로드 제한</option>
                    <option value="CUSTOM">커스텀 룰</option>
                  </select>
                </div>
              </div>
              <textarea
                value={formData.ruleContent}
                onChange={(e) => handleInputChange('ruleContent', e.target.value)}
                rows={8}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent font-mono text-sm"
                placeholder="ModSecurity 룰을 입력하세요..."
                required
              />
              <div className="mt-2 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                <div className="flex items-start gap-2">
                  <InfoCircleOutlined className="w-4 h-4 text-blue-600 mt-0.5" />
                  <div className="text-sm text-blue-800">
                    <p className="font-medium mb-1">ModSecurity 룰 작성 팁:</p>
                    <ul className="text-xs space-y-1">
                      <li>• 각 룰은 고유한 ID를 가져야 합니다 (id:1000-9999)</li>
                      <li>• phase는 요청 처리 단계를 의미합니다 (1-5)</li>
                      <li>• 액션: allow, block, deny, drop, pass, log</li>
                      <li>• 연산자: @detectSQLi, @detectXSS, @contains, @rx 등</li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Footer */}
          <div className="flex items-center justify-end gap-3 p-6 border-t border-gray-200 bg-gray-50">
            <button
              type="button"
              onClick={onCancel}
              className="px-4 py-2 text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50"
              disabled={loading}
            >
              취소
            </button>
            <button
              type="submit"
              className="btn-primary flex items-center gap-2"
              disabled={loading || !validateForm()}
            >
              <SaveOutlined className="w-4 h-4" />
              {loading ? '저장 중...' : isEditMode ? '수정' : '생성'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};