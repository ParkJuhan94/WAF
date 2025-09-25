import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { ApiResponse, PaginatedResponse } from '../types/api';
import { WAFStats, WAFStatus, Rule, AttackEvent, TrafficData } from '../types/waf';
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

  // Auth APIs
  async login(googleToken: string): Promise<{ token: string; user: any }> {
    const response = await this.client.post('/auth/login', { googleToken });
    return response.data.data;
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