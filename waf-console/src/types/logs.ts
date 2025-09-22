export interface LogEntry {
  id: string;
  timestamp: string;
  sourceIp: string;
  method: string;
  path: string;
  statusCode: number;
  responseSize: number;
  userAgent: string;
  referer?: string;
  blocked: boolean;
  attackType?: string;
  severity?: 'low' | 'medium' | 'high' | 'critical';
  matchedRules: string[];
  requestHeaders: Record<string, string>;
  requestBody?: string;
  responseTime: number;
  geoLocation?: {
    country: string;
    city: string;
    latitude: number;
    longitude: number;
  };
}

export interface LogFilter {
  dateRange: [string, string];
  sourceIp?: string;
  attackType?: string;
  severity?: string;
  blocked?: boolean;
  search?: string;
  page: number;
  pageSize: number;
}

export interface LogStats {
  totalLogs: number;
  blockedLogs: number;
  allowedLogs: number;
  uniqueIps: number;
  topAttackTypes: Array<{
    type: string;
    count: number;
  }>;
  topSourceIps: Array<{
    ip: string;
    count: number;
    country?: string;
  }>;
}