import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { ApiResponse, PaginatedResponse } from '../types/api';
import { WAFStats, WAFStatus, Rule, AttackEvent, TrafficData, CustomRule, CustomRuleRequest, CustomRuleFilter, RuleStatistics, RuleType, RuleSeverity } from '../types/waf';
import { LogEntry, LogFilter, LogStats } from '../types/logs';

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor
    this.client.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('auth_token');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor
    this.client.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          localStorage.removeItem('auth_token');
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  // Dashboard APIs
  async getDashboardStats(): Promise<WAFStats> {
    const response: AxiosResponse<ApiResponse<WAFStats>> = await this.client.get('/dashboard/stats');
    return response.data.data;
  }

  async getWAFStatus(): Promise<WAFStatus> {
    const response: AxiosResponse<ApiResponse<WAFStatus>> = await this.client.get('/dashboard/status');
    return response.data.data;
  }

  async getTrafficData(hours: number = 24): Promise<TrafficData[]> {
    const response: AxiosResponse<ApiResponse<TrafficData[]>> = await this.client.get(`/dashboard/traffic?hours=${hours}`);
    return response.data.data;
  }

  async getRecentAttacks(limit: number = 10): Promise<AttackEvent[]> {
    const response: AxiosResponse<ApiResponse<AttackEvent[]>> = await this.client.get(`/dashboard/attacks?limit=${limit}`);
    return response.data.data;
  }

  // Logs APIs
  async getLogs(filter: LogFilter): Promise<PaginatedResponse<LogEntry>> {
    const response: AxiosResponse<ApiResponse<PaginatedResponse<LogEntry>>> = await this.client.post('/logs/search', filter);
    return response.data.data;
  }

  async getLogStats(filter: Partial<LogFilter>): Promise<LogStats> {
    const response: AxiosResponse<ApiResponse<LogStats>> = await this.client.post('/logs/stats', filter);
    return response.data.data;
  }

  async getLogDetail(id: string): Promise<LogEntry> {
    const response: AxiosResponse<ApiResponse<LogEntry>> = await this.client.get(`/logs/${id}`);
    return response.data.data;
  }

  async exportLogs(filter: LogFilter, format: 'csv' | 'json' | 'pdf'): Promise<Blob> {
    const response = await this.client.post(`/logs/export?format=${format}`, filter, {
      responseType: 'blob'
    });
    return response.data;
  }

  // Rules APIs
  async getRules(): Promise<Rule[]> {
    const response: AxiosResponse<ApiResponse<Rule[]>> = await this.client.get('/rules');
    return response.data.data;
  }

  async toggleRule(ruleId: string, enabled: boolean): Promise<void> {
    await this.client.patch(`/rules/${ruleId}`, { enabled });
  }

  async createRule(rule: Omit<Rule, 'id' | 'lastModified' | 'matchCount'>): Promise<Rule> {
    const response: AxiosResponse<ApiResponse<Rule>> = await this.client.post('/rules', rule);
    return response.data.data;
  }

  async updateRule(ruleId: string, rule: Partial<Rule>): Promise<Rule> {
    const response: AxiosResponse<ApiResponse<Rule>> = await this.client.put(`/rules/${ruleId}`, rule);
    return response.data.data;
  }

  async deleteRule(ruleId: string): Promise<void> {
    await this.client.delete(`/rules/${ruleId}`);
  }

  async testRule(ruleContent: string, testPayload: string): Promise<{ matches: boolean; details: string }> {
    const response: AxiosResponse<ApiResponse<{ matches: boolean; details: string }>> =
      await this.client.post('/rules/test', { ruleContent, testPayload });
    return response.data.data;
  }

  // Whitelist APIs
  async getWhitelist(): Promise<string[]> {
    const response: AxiosResponse<ApiResponse<string[]>> = await this.client.get('/whitelist');
    return response.data.data;
  }

  async addToWhitelist(ip: string): Promise<void> {
    await this.client.post('/whitelist', { ip });
  }

  async removeFromWhitelist(ip: string): Promise<void> {
    await this.client.delete(`/whitelist/${encodeURIComponent(ip)}`);
  }

  // Testing APIs
  async simulateAttack(attackType: string, target: string, payload: string): Promise<{
    blocked: boolean;
    statusCode: number;
    response: string;
    matchedRules: string[];
  }> {
    const response = await this.client.post('/testing/simulate', {
      attackType,
      target,
      payload
    });
    return response.data.data;
  }

  async runDVWATest(): Promise<{
    normalRequests: { passed: number; total: number };
    attackTests: Array<{
      type: string;
      blocked: boolean;
      statusCode: number;
      screenshot?: string;
    }>;
  }> {
    const response = await this.client.post('/testing/dvwa');
    return response.data.data;
  }

  async generateTestReport(): Promise<Blob> {
    const response = await this.client.get('/testing/report', {
      responseType: 'blob'
    });
    return response.data;
  }

  // Custom Rules APIs
  async getCustomRules(
    page: number = 0,
    size: number = 20,
    sortBy: string = 'createdAt',
    sortDir: string = 'desc',
    filter?: CustomRuleFilter
  ): Promise<PaginatedResponse<CustomRule>> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir,
      ...(filter?.enabled !== undefined && { enabled: filter.enabled.toString() }),
      ...(filter?.type && { type: filter.type }),
      ...(filter?.severity && { severity: filter.severity }),
      ...(filter?.targetService && { targetService: filter.targetService }),
      ...(filter?.keyword && { keyword: filter.keyword })
    });

    const response: AxiosResponse<PaginatedResponse<CustomRule>> =
      await this.client.get(`/v1/rules?${params}`);
    return response.data;
  }

  async getCustomRule(id: number): Promise<CustomRule> {
    const response: AxiosResponse<CustomRule> = await this.client.get(`/v1/rules/${id}`);
    return response.data;
  }

  async getMyCustomRules(
    page: number = 0,
    size: number = 20,
    sortBy: string = 'createdAt',
    sortDir: string = 'desc'
  ): Promise<PaginatedResponse<CustomRule>> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir
    });

    const response: AxiosResponse<PaginatedResponse<CustomRule>> =
      await this.client.get(`/v1/rules/my?${params}`);
    return response.data;
  }

  async getActiveCustomRules(): Promise<CustomRule[]> {
    const response: AxiosResponse<CustomRule[]> = await this.client.get('/v1/rules/active');
    return response.data;
  }

  async getApplicableCustomRules(targetService: string): Promise<CustomRule[]> {
    const response: AxiosResponse<CustomRule[]> =
      await this.client.get(`/v1/rules/applicable?targetService=${encodeURIComponent(targetService)}`);
    return response.data;
  }

  async getRecentlyActiveCustomRules(page: number = 0, size: number = 10): Promise<PaginatedResponse<CustomRule>> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString()
    });

    const response: AxiosResponse<PaginatedResponse<CustomRule>> =
      await this.client.get(`/v1/rules/recent-active?${params}`);
    return response.data;
  }

  async createCustomRule(rule: CustomRuleRequest): Promise<CustomRule> {
    const response: AxiosResponse<CustomRule> = await this.client.post('/v1/rules', rule);
    return response.data;
  }

  async updateCustomRule(id: number, rule: CustomRuleRequest): Promise<CustomRule> {
    const response: AxiosResponse<CustomRule> = await this.client.put(`/v1/rules/${id}`, rule);
    return response.data;
  }

  async deleteCustomRule(id: number): Promise<void> {
    await this.client.delete(`/v1/rules/${id}`);
  }

  async toggleCustomRuleStatus(id: number): Promise<CustomRule> {
    const response: AxiosResponse<CustomRule> = await this.client.patch(`/v1/rules/${id}/toggle`);
    return response.data;
  }

  async recordCustomRuleMatch(id: number): Promise<void> {
    await this.client.post(`/v1/rules/${id}/match`);
  }

  async recordCustomRuleBlock(id: number): Promise<void> {
    await this.client.post(`/v1/rules/${id}/block`);
  }

  async getCustomRuleStatistics(): Promise<RuleStatistics> {
    const response: AxiosResponse<RuleStatistics> = await this.client.get('/v1/rules/statistics');
    return response.data;
  }

  async getRuleTypes(): Promise<RuleType[]> {
    const response: AxiosResponse<RuleType[]> = await this.client.get('/v1/rules/types');
    return response.data;
  }

  async getRuleSeverities(): Promise<RuleSeverity[]> {
    const response: AxiosResponse<RuleSeverity[]> = await this.client.get('/v1/rules/severities');
    return response.data;
  }

  // Auth APIs
  async login(googleToken: string): Promise<{ token: string; user: any }> {
    const response = await this.client.post('/v1/auth/google', { idToken: googleToken });
    return {
      token: response.data.accessToken,
      user: response.data.userProfile
    };
  }

  async logout(): Promise<void> {
    await this.client.post('/auth/logout');
  }

  async refreshToken(): Promise<{ token: string }> {
    const response = await this.client.post('/auth/refresh');
    return response.data.data;
  }
}

export const apiClient = new ApiClient();