export interface WAFStats {
  totalRequests: number;
  blockedRequests: number;
  allowedRequests: number;
  blockRate: number;
  avgResponseTime: number;
  uptime: number;
}

export interface WAFStatus {
  status: 'active' | 'inactive' | 'error' | 'maintenance';
  version: string;
  lastRestart: string;
  configLastUpdated: string;
  rulesCount: number;
  activeRulesCount: number;
}

export interface Rule {
  id: string;
  name: string;
  description: string;
  severity: 'low' | 'medium' | 'high' | 'critical';
  category: string;
  enabled: boolean;
  custom: boolean;
  content: string;
  lastModified: string;
  matchCount: number;
}

export interface AttackPattern {
  type: 'sql_injection' | 'xss' | 'file_upload' | 'command_injection' | 'path_traversal' | 'other';
  count: number;
  lastSeen: string;
  severity: 'low' | 'medium' | 'high' | 'critical';
}

export interface TrafficData {
  timestamp: string;
  totalRequests: number;
  blockedRequests: number;
  allowedRequests: number;
  responseTime: number;
}

export interface AttackEvent {
  id: string;
  timestamp: string;
  sourceIp: string;
  targetPath: string;
  attackType: string;
  severity: 'low' | 'medium' | 'high' | 'critical';
  blocked: boolean;
  userAgent: string;
  payload: string;
  matchedRules: string[];
}

// Custom Rule types matching backend API
export type RuleType = 'BLOCK' | 'DENY' | 'DROP' | 'LOG' | 'RATE_LIMIT' | 'REDIRECT' | 'CUSTOM';
export type RuleSeverity = 'INFO' | 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface CustomRule {
  id: number;
  name: string;
  description: string;
  ruleContent: string;
  type: RuleType;
  severity: RuleSeverity;
  enabled: boolean;
  priority: number;
  targetService?: string;
  targetPath?: string;
  createdBy: {
    id: number;
    name: string;
    email: string;
  };
  createdAt: string;
  updatedAt: string;
  lastMatchedAt?: string;
  matchCount: number;
  blockCount: number;
}

export interface CustomRuleRequest {
  name: string;
  description?: string;
  ruleContent: string;
  type: RuleType;
  severity: RuleSeverity;
  priority?: number;
  targetService?: string;
  targetPath?: string;
}

export interface CustomRuleFilter {
  enabled?: boolean;
  type?: RuleType;
  severity?: RuleSeverity;
  targetService?: string;
  keyword?: string;
}

export interface RuleStatistics {
  totalRules: number;
  enabledRules: number;
  disabledRules: number;
  totalMatches: number;
  totalBlocks: number;
}