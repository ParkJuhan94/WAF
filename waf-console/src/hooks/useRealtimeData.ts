import { useEffect, useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { wafWebSocket } from '../services/websocket';
import { useWAFStore } from '../stores/useWAFStore';
import { useLogStore } from '../stores/useLogStore';
import { REFRESH_INTERVALS } from '../utils/constants';

export const useRealtimeData = () => {
  const wafStore = useWAFStore();
  const logStore = useLogStore();

  // Setup WebSocket connection and event handlers
  useEffect(() => {
    const connectWebSocket = async () => {
      try {
        await wafWebSocket.connect();
      } catch (error) {
        console.error('Failed to connect WebSocket:', error);
      }
    };

    connectWebSocket();

    // WebSocket event handlers
    const handleTrafficUpdate = (data: any) => {
      wafStore.updateTrafficData(data);
    };

    const handleAttackBlocked = (data: any) => {
      wafStore.addAttackEvent(data);
    };

    const handleNewLog = (data: any) => {
      logStore.addNewLog(data);
    };

    const handleStatusChange = (data: any) => {
      wafStore.updateStatus(data);
    };

    // Register event handlers
    wafWebSocket.on('trafficUpdate', handleTrafficUpdate);
    wafWebSocket.on('attackBlocked', handleAttackBlocked);
    wafWebSocket.on('newLog', handleNewLog);
    wafWebSocket.on('statusChange', handleStatusChange);

    // Cleanup on unmount
    return () => {
      wafWebSocket.off('trafficUpdate', handleTrafficUpdate);
      wafWebSocket.off('attackBlocked', handleAttackBlocked);
      wafWebSocket.off('newLog', handleNewLog);
      wafWebSocket.off('statusChange', handleStatusChange);
      wafWebSocket.disconnect();
    };
  }, [wafStore, logStore]);

  return {
    isConnected: wafWebSocket.isConnected(),
    reconnect: () => wafWebSocket.connect()
  };
};

export const useWAFDashboardData = () => {
  const wafStore = useWAFStore();

  // WAF Status Query
  const statusQuery = useQuery({
    queryKey: ['waf-status'],
    queryFn: wafStore.fetchStatus,
    refetchInterval: REFRESH_INTERVALS.NORMAL,
    staleTime: REFRESH_INTERVALS.FAST,
  });

  // WAF Stats Query
  const statsQuery = useQuery({
    queryKey: ['waf-stats'],
    queryFn: wafStore.fetchStats,
    refetchInterval: REFRESH_INTERVALS.FAST,
    staleTime: REFRESH_INTERVALS.REAL_TIME,
  });

  // Traffic Data Query
  const trafficQuery = useQuery({
    queryKey: ['traffic-data'],
    queryFn: () => wafStore.fetchTrafficData(24),
    refetchInterval: REFRESH_INTERVALS.FAST,
    staleTime: REFRESH_INTERVALS.REAL_TIME,
  });

  // Recent Attacks Query
  const attacksQuery = useQuery({
    queryKey: ['recent-attacks'],
    queryFn: () => wafStore.fetchRecentAttacks(10),
    refetchInterval: REFRESH_INTERVALS.FAST,
    staleTime: REFRESH_INTERVALS.REAL_TIME,
  });

  const refetchAll = useCallback(() => {
    statusQuery.refetch();
    statsQuery.refetch();
    trafficQuery.refetch();
    attacksQuery.refetch();
  }, [statusQuery, statsQuery, trafficQuery, attacksQuery]);

  return {
    status: wafStore.status,
    stats: wafStore.stats,
    trafficData: wafStore.trafficData,
    recentAttacks: wafStore.recentAttacks,
    isLoading: statusQuery.isLoading || statsQuery.isLoading || trafficQuery.isLoading || attacksQuery.isLoading,
    isError: statusQuery.isError || statsQuery.isError || trafficQuery.isError || attacksQuery.isError,
    refetchAll
  };
};

export const useRulesData = () => {
  const wafStore = useWAFStore();

  const rulesQuery = useQuery({
    queryKey: ['waf-rules'],
    queryFn: wafStore.fetchRules,
    staleTime: REFRESH_INTERVALS.NORMAL,
  });

  return {
    rules: wafStore.rules,
    isLoading: wafStore.isLoadingRules,
    isError: rulesQuery.isError,
    refetch: rulesQuery.refetch,
    toggleRule: wafStore.toggleRule,
    addCustomRule: wafStore.addCustomRule,
    updateRule: wafStore.updateRule,
    deleteRule: wafStore.deleteRule
  };
};