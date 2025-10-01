import { create } from 'zustand';
import { WAFStats, WAFStatus, Rule, AttackEvent, TrafficData } from '../types/waf';
import { apiClient } from '../services/api';

interface WAFState {
  // Status
  status: WAFStatus | null;
  stats: WAFStats | null;

  // Rules
  rules: Rule[];

  // Real-time data
  trafficData: TrafficData[];
  recentAttacks: AttackEvent[];

  // Loading states
  isLoadingStatus: boolean;
  isLoadingStats: boolean;
  isLoadingRules: boolean;

  // Actions
  fetchStatus: () => Promise<void>;
  fetchStats: () => Promise<void>;
  fetchRules: () => Promise<void>;
  fetchTrafficData: (hours?: number) => Promise<void>;
  fetchRecentAttacks: (limit?: number) => Promise<void>;

  // Rule management
  toggleRule: (ruleId: string, enabled: boolean) => Promise<void>;
  addCustomRule: (rule: Omit<Rule, 'id' | 'lastModified' | 'matchCount'>) => Promise<void>;
  updateRule: (ruleId: string, updates: Partial<Rule>) => Promise<void>;
  deleteRule: (ruleId: string) => Promise<void>;

  // Real-time updates
  updateTrafficData: (newData: TrafficData) => void;
  addAttackEvent: (attack: AttackEvent) => void;
  updateStatus: (status: Partial<WAFStatus>) => void;
}

export const useWAFStore = create<WAFState>((set, get) => ({
  // Initial state
  status: null,
  stats: null,
  rules: [],
  trafficData: [],
  recentAttacks: [],
  isLoadingStatus: false,
  isLoadingStats: false,
  isLoadingRules: false,

  // Fetch actions
  fetchStatus: async () => {
    set({ isLoadingStatus: true });
    try {
      const status = await apiClient.getWAFStatus();
      set({ status, isLoadingStatus: false });
    } catch (error) {
      console.error('Failed to fetch WAF status:', error);
      set({ isLoadingStatus: false });
    }
  },

  fetchStats: async () => {
    set({ isLoadingStats: true });
    try {
      const stats = await apiClient.getDashboardStats();
      set({ stats, isLoadingStats: false });
    } catch (error) {
      console.error('Failed to fetch WAF stats:', error);
      set({ isLoadingStats: false });
    }
  },

  fetchRules: async () => {
    set({ isLoadingRules: true });
    try {
      const rules = await apiClient.getRules();
      set({ rules, isLoadingRules: false });
    } catch (error) {
      console.error('Failed to fetch rules:', error);
      set({ isLoadingRules: false });
    }
  },

  fetchTrafficData: async (hours = 24) => {
    try {
      const trafficData = await apiClient.getTrafficData(hours);
      set({ trafficData });
    } catch (error) {
      console.error('Failed to fetch traffic data:', error);
    }
  },

  fetchRecentAttacks: async (limit = 10) => {
    try {
      const recentAttacks = await apiClient.getRecentAttacks(limit);
      set({ recentAttacks });
    } catch (error) {
      console.error('Failed to fetch recent attacks:', error);
    }
  },

  // Rule management actions
  toggleRule: async (ruleId: string, enabled: boolean) => {
    try {
      await apiClient.toggleRule(ruleId, enabled);

      // Update local state
      const rules = get().rules.map(rule =>
        rule.id === ruleId ? { ...rule, enabled } : rule
      );
      set({ rules });
    } catch (error) {
      console.error('Failed to toggle rule:', error);
      throw error;
    }
  },

  addCustomRule: async (ruleData) => {
    try {
      const newRule = await apiClient.createRule(ruleData);
      const rules = [...get().rules, newRule];
      set({ rules });
    } catch (error) {
      console.error('Failed to add custom rule:', error);
      throw error;
    }
  },

  updateRule: async (ruleId: string, updates: Partial<Rule>) => {
    try {
      const updatedRule = await apiClient.updateRule(ruleId, updates);
      const rules = get().rules.map(rule =>
        rule.id === ruleId ? updatedRule : rule
      );
      set({ rules });
    } catch (error) {
      console.error('Failed to update rule:', error);
      throw error;
    }
  },

  deleteRule: async (ruleId: string) => {
    try {
      await apiClient.deleteRule(ruleId);
      const rules = get().rules.filter(rule => rule.id !== ruleId);
      set({ rules });
    } catch (error) {
      console.error('Failed to delete rule:', error);
      throw error;
    }
  },

  // Real-time update actions
  updateTrafficData: (newData: TrafficData) => {
    const currentData = get().trafficData;
    const updatedData = [...currentData, newData].slice(-100); // Keep last 100 entries
    set({ trafficData: updatedData });
  },

  addAttackEvent: (attack: AttackEvent) => {
    const currentAttacks = get().recentAttacks;
    const updatedAttacks = [attack, ...currentAttacks].slice(0, 50); // Keep last 50 attacks
    set({ recentAttacks: updatedAttacks });
  },

  updateStatus: (statusUpdate: Partial<WAFStatus>) => {
    const currentStatus = get().status;
    if (currentStatus) {
      set({ status: { ...currentStatus, ...statusUpdate } });
    }
  }
}));