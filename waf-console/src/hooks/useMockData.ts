import { useState, useEffect } from 'react';

// Mock data for development
const mockWAFStats = {
  totalRequests: 12847,
  blockedRequests: 234,
  allowedRequests: 12613,
  blockRate: 1.8,
  avgResponseTime: 145,
  uptime: 2847392000 // milliseconds
};

const mockWAFStatus = {
  status: 'active' as const,
  version: 'ModSecurity v3.0.8',
  lastRestart: new Date(Date.now() - 86400000).toISOString(), // 1 day ago
  configLastUpdated: new Date(Date.now() - 3600000).toISOString(), // 1 hour ago
  rulesCount: 1247,
  activeRulesCount: 1198
};

const mockTrafficData = Array.from({ length: 24 }, (_, i) => ({
  timestamp: new Date(Date.now() - (23 - i) * 3600000).toISOString(),
  totalRequests: Math.floor(Math.random() * 500) + 100,
  blockedRequests: Math.floor(Math.random() * 50) + 5,
  allowedRequests: Math.floor(Math.random() * 450) + 95,
  responseTime: Math.floor(Math.random() * 200) + 50
}));

const mockRecentAttacks = Array.from({ length: 10 }, (_, i) => ({
  id: `attack-${i}`,
  timestamp: new Date(Date.now() - i * 300000).toISOString(), // Every 5 minutes
  sourceIp: `192.168.1.${100 + i}`,
  targetPath: `/admin/login.php`,
  attackType: ['sql_injection', 'xss', 'command_injection'][i % 3],
  severity: ['high', 'medium', 'critical', 'low'][i % 4] as any,
  blocked: Math.random() > 0.2, // 80% blocked
  userAgent: 'Mozilla/5.0 (compatible; AttackBot/1.0)',
  payload: `' OR '1'='1' --`,
  matchedRules: [`rule-${1000 + i}`, `rule-${2000 + i}`]
}));

export const useMockWAFDashboardData = () => {
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Simulate loading delay
    const timer = setTimeout(() => setIsLoading(false), 1000);
    return () => clearTimeout(timer);
  }, []);

  return {
    status: mockWAFStatus,
    stats: mockWAFStats,
    trafficData: mockTrafficData,
    recentAttacks: mockRecentAttacks,
    isLoading,
    isError: false,
    refetchAll: () => {
      console.log('Refetching mock data...');
    }
  };
};

export const useMockRealtimeData = () => {
  return {
    isConnected: true,
    reconnect: () => console.log('Mock reconnect')
  };
};