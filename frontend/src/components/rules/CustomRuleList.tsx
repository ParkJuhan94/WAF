import React, { useState, useEffect } from 'react';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  FilterOutlined,
  ReloadOutlined,
  EyeOutlined,
  ExclamationCircleOutlined,
  SafetyCertificateOutlined,
  CheckCircleOutlined,
  StopOutlined
} from '@ant-design/icons';
import { CustomRule, CustomRuleFilter, RuleType, RuleSeverity } from '../../types/waf';
import { apiClient } from '../../services/api';
import { StatusBadge } from '../common/StatusBadge';
import { Loading } from '../common/Loading';

interface CustomRuleListProps {
  onCreateRule: () => void;
  onEditRule: (rule: CustomRule) => void;
  onViewRule: (rule: CustomRule) => void;
}

const RULE_TYPE_LABELS: Record<RuleType, string> = {
  BLOCK: '차단',
  DENY: '거부',
  DROP: '폐기',
  LOG: '로그',
  RATE_LIMIT: '속도제한',
  REDIRECT: '리다이렉트',
  CUSTOM: '커스텀'
};

const SEVERITY_COLORS: Record<RuleSeverity, string> = {
  INFO: 'bg-blue-100 text-blue-800',
  LOW: 'bg-green-100 text-green-800',
  MEDIUM: 'bg-yellow-100 text-yellow-800',
  HIGH: 'bg-orange-100 text-orange-800',
  CRITICAL: 'bg-red-100 text-red-800'
};

export const CustomRuleList: React.FC<CustomRuleListProps> = ({
  onCreateRule,
  onEditRule,
  onViewRule
}) => {
  const [rules, setRules] = useState<CustomRule[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [filter, setFilter] = useState<CustomRuleFilter>({});
  const [showFilters, setShowFilters] = useState(false);
  const [keyword, setKeyword] = useState('');

  const pageSize = 10;

  const loadRules = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await apiClient.getCustomRules(page, pageSize, 'createdAt', 'desc', {
        ...filter,
        keyword: keyword || undefined
      });

      setRules(response.content || []);
      setTotalPages(response.totalPages || 0);
      setTotalElements(response.totalElements || 0);
    } catch (err: any) {
      setError(err.response?.data?.message || '룰 목록을 불러오는 중 오류가 발생했습니다.');
      console.error('Failed to load rules:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRules();
  }, [page, filter, keyword]);

  const handleToggleRule = async (rule: CustomRule) => {
    try {
      await apiClient.toggleCustomRuleStatus(rule.id);
      await loadRules(); // 목록 새로고침
    } catch (err: any) {
      setError(err.response?.data?.message || '룰 상태 변경 중 오류가 발생했습니다.');
    }
  };

  const handleDeleteRule = async (rule: CustomRule) => {
    if (!confirm(`"${rule.name}" 룰을 삭제하시겠습니까?`)) {
      return;
    }

    try {
      await apiClient.deleteCustomRule(rule.id);
      await loadRules(); // 목록 새로고침
    } catch (err: any) {
      setError(err.response?.data?.message || '룰 삭제 중 오류가 발생했습니다.');
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    loadRules();
  };

  const handleFilterChange = (newFilter: Partial<CustomRuleFilter>) => {
    setFilter(prev => ({ ...prev, ...newFilter }));
    setPage(0);
  };

  const clearFilters = () => {
    setFilter({});
    setKeyword('');
    setPage(0);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getSeverityIcon = (severity: RuleSeverity) => {
    switch (severity) {
      case 'CRITICAL': return <ExclamationCircleOutlined className="w-4 h-4" />;
      case 'HIGH': return <ExclamationCircleOutlined className="w-4 h-4" />;
      default: return <SafetyCertificateOutlined className="w-4 h-4" />;
    }
  };

  if (loading && rules.length === 0) {
    return <Loading />;
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">커스텀 룰 관리</h2>
          <p className="text-gray-600">
            총 {totalElements}개의 룰 중 {rules.length}개 표시
          </p>
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => setShowFilters(!showFilters)}
            className="btn-secondary flex items-center gap-2"
          >
            <FilterOutlined className="w-4 h-4" />
            필터
          </button>
          <button
            onClick={loadRules}
            className="btn-secondary flex items-center gap-2"
            disabled={loading}
          >
            <ReloadOutlined className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            새로고침
          </button>
          <button
            onClick={onCreateRule}
            className="btn-primary flex items-center gap-2"
          >
            <PlusOutlined className="w-4 h-4" />
            새 룰 생성
          </button>
        </div>
      </div>

      {/* Search and Filters */}
      <div className="bg-white rounded-lg border border-gray-200 p-4">
        <form onSubmit={handleSearch} className="flex gap-2 mb-4">
          <div className="flex-1 relative">
            <SearchOutlined className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
            <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="룰 이름이나 설명으로 검색..."
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <button
            type="submit"
            className="btn-primary"
            disabled={loading}
          >
            검색
          </button>
        </form>

        {showFilters && (
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 pt-4 border-t border-gray-200">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                상태
              </label>
              <select
                value={filter.enabled?.toString() || ''}
                onChange={(e) => handleFilterChange({
                  enabled: e.target.value ? e.target.value === 'true' : undefined
                })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">전체</option>
                <option value="true">활성</option>
                <option value="false">비활성</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                타입
              </label>
              <select
                value={filter.type || ''}
                onChange={(e) => handleFilterChange({
                  type: e.target.value as RuleType || undefined
                })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">전체</option>
                {Object.entries(RULE_TYPE_LABELS).map(([value, label]) => (
                  <option key={value} value={value}>{label}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                심각도
              </label>
              <select
                value={filter.severity || ''}
                onChange={(e) => handleFilterChange({
                  severity: e.target.value as RuleSeverity || undefined
                })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">전체</option>
                <option value="INFO">정보</option>
                <option value="LOW">낮음</option>
                <option value="MEDIUM">보통</option>
                <option value="HIGH">높음</option>
                <option value="CRITICAL">위험</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                대상 서비스
              </label>
              <input
                type="text"
                value={filter.targetService || ''}
                onChange={(e) => handleFilterChange({
                  targetService: e.target.value || undefined
                })}
                placeholder="서비스명"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div className="md:col-span-4 flex justify-end">
              <button
                type="button"
                onClick={clearFilters}
                className="text-sm text-gray-600 hover:text-gray-800"
              >
                필터 초기화
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Error Message */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          {error}
        </div>
      )}

      {/* Rules List */}
      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        {rules.length === 0 ? (
          <div className="text-center py-12">
            <SafetyCertificateOutlined className="w-12 h-12 text-gray-400 mx-auto mb-4" />
            <p className="text-gray-500">등록된 커스텀 룰이 없습니다.</p>
            <button
              onClick={onCreateRule}
              className="mt-4 btn-primary"
            >
              첫 번째 룰 만들기
            </button>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    룰 정보
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    타입/심각도
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    대상
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    통계
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    상태
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    작업
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {rules.map((rule) => (
                  <tr key={rule.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div>
                        <div className="font-medium text-gray-900">{rule.name}</div>
                        <div className="text-sm text-gray-500">{rule.description}</div>
                        <div className="text-xs text-gray-400 mt-1">
                          생성: {formatDate(rule.createdAt)} |
                          수정: {formatDate(rule.updatedAt)}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="space-y-1">
                        <span className="inline-block px-2 py-1 text-xs font-medium bg-blue-100 text-blue-800 rounded">
                          {RULE_TYPE_LABELS[rule.type]}
                        </span>
                        <div className={`inline-flex items-center gap-1 px-2 py-1 text-xs font-medium rounded ${SEVERITY_COLORS[rule.severity]}`}>
                          {getSeverityIcon(rule.severity)}
                          {rule.severity}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm">
                        {rule.targetService && (
                          <div>서비스: {rule.targetService}</div>
                        )}
                        {rule.targetPath && (
                          <div>경로: {rule.targetPath}</div>
                        )}
                        {!rule.targetService && !rule.targetPath && (
                          <span className="text-gray-500">전체</span>
                        )}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm space-y-1">
                        <div>매치: {rule.matchCount.toLocaleString()}</div>
                        <div>차단: {rule.blockCount.toLocaleString()}</div>
                        <div className="text-xs text-gray-500">
                          우선순위: {rule.priority}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <StatusBadge
                        status={rule.enabled ? 'active' : 'inactive'}
                        showText={true}
                      />
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => onViewRule(rule)}
                          className="text-blue-600 hover:text-blue-800"
                          title="상세 보기"
                        >
                          <EyeOutlined className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => onEditRule(rule)}
                          className="text-gray-600 hover:text-gray-800"
                          title="수정"
                        >
                          <EditOutlined className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleToggleRule(rule)}
                          className={`${rule.enabled ? 'text-green-600 hover:text-green-800' : 'text-gray-400 hover:text-gray-600'}`}
                          title={rule.enabled ? '비활성화' : '활성화'}
                        >
                          {rule.enabled ? <CheckCircleOutlined className="w-4 h-4" /> : <StopOutlined className="w-4 h-4" />}
                        </button>
                        <button
                          onClick={() => handleDeleteRule(rule)}
                          className="text-red-600 hover:text-red-800"
                          title="삭제"
                        >
                          <DeleteOutlined className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center items-center space-x-2">
          <button
            onClick={() => setPage(page - 1)}
            disabled={page === 0}
            className="px-3 py-2 border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
          >
            이전
          </button>

          <div className="flex space-x-1">
            {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
              const pageNum = Math.max(0, Math.min(totalPages - 5, page - 2)) + i;
              return (
                <button
                  key={pageNum}
                  onClick={() => setPage(pageNum)}
                  className={`px-3 py-2 border border-gray-300 rounded-lg ${
                    page === pageNum
                      ? 'bg-blue-500 text-white border-blue-500'
                      : 'hover:bg-gray-50'
                  }`}
                >
                  {pageNum + 1}
                </button>
              );
            })}
          </div>

          <button
            onClick={() => setPage(page + 1)}
            disabled={page >= totalPages - 1}
            className="px-3 py-2 border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
          >
            다음
          </button>
        </div>
      )}
    </div>
  );
};