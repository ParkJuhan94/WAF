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