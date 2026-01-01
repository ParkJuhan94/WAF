import { useState, useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../services/api';
import { useLogStore } from '../stores/useLogStore';
import { LogEntry, LogFilter } from '../types/logs';

export const useLogExporter = () => {
  const [isExporting, setIsExporting] = useState(false);
  const logStore = useLogStore();

  const exportLogs = useCallback(async (
    format: 'csv' | 'json' | 'pdf',
    customFilter?: LogFilter
  ): Promise<void> => {
    setIsExporting(true);
    try {
      const filter = customFilter || logStore.filter;
      const blob = await apiClient.exportLogs(filter, format);

      // Create download link
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;

      // Generate filename with timestamp
      const timestamp = new Date().toISOString().split('T')[0];
      const extension = format === 'csv' ? 'csv' : format === 'json' ? 'json' : 'pdf';
      link.download = `waf-logs-${timestamp}.${extension}`;

      // Trigger download
      document.body.appendChild(link);
      link.click();

      // Cleanup
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Failed to export logs:', error);
      throw error;
    } finally {
      setIsExporting(false);
    }
  }, [logStore.filter]);

  const exportSelectedLogs = useCallback(async (
    logs: LogEntry[],
    format: 'csv' | 'json' | 'pdf'
  ): Promise<void> => {
    setIsExporting(true);
    try {
      // For selected logs, we need to handle the export differently
      // This would require a different API endpoint or client-side processing
      if (format === 'json') {
        const data = JSON.stringify(logs, null, 2);
        const blob = new Blob([data], { type: 'application/json' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `selected-logs-${new Date().toISOString().split('T')[0]}.json`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      } else if (format === 'csv') {
        const csvContent = convertLogsToCSV(logs);
        const blob = new Blob([csvContent], { type: 'text/csv' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `selected-logs-${new Date().toISOString().split('T')[0]}.csv`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      } else {
        throw new Error('PDF export for selected logs not implemented');
      }
    } catch (error) {
      console.error('Failed to export selected logs:', error);
      throw error;
    } finally {
      setIsExporting(false);
    }
  }, []);

  return {
    isExporting,
    exportLogs,
    exportSelectedLogs
  };
};

const convertLogsToCSV = (logs: LogEntry[]): string => {
  const headers = [
    'Timestamp',
    'Source IP',
    'Method',
    'Path',
    'Status Code',
    'Response Size',
    'User Agent',
    'Blocked',
    'Attack Type',
    'Severity',
    'Matched Rules',
    'Response Time'
  ];

  const csvRows = [
    headers.join(','),
    ...logs.map(log => [
      log.timestamp,
      log.sourceIp,
      log.method,
      `"${log.path}"`,
      log.statusCode,
      log.responseSize,
      `"${log.userAgent || ''}"`,
      log.blocked,
      log.attackType || '',
      log.severity || '',
      `"${log.matchedRules ? log.matchedRules.join('; ') : ''}"`,
      log.responseTime
    ].join(','))
  ];

  return csvRows.join('\n');
};

export const useLogSearch = () => {
  const logStore = useLogStore();

  const {
    data: logsData,
    isLoading,
    isError,
    refetch
  } = useQuery({
    queryKey: ['logs', logStore.filter],
    queryFn: async () => {
      logStore.setLoading(true);
      try {
        const result = await apiClient.getLogs(logStore.filter);
        logStore.setLogs(result.items, result.total, result.totalPages);
        return result;
      } finally {
        logStore.setLoading(false);
      }
    },
    staleTime: 30000, // 30 seconds
    enabled: true
  });

  const {
    data: statsData,
    isLoading: isLoadingStats
  } = useQuery({
    queryKey: ['log-stats', logStore.filter],
    queryFn: async () => {
      logStore.setLoadingStats(true);
      try {
        const stats = await apiClient.getLogStats(logStore.filter);
        logStore.setLogStats(stats);
        return stats;
      } finally {
        logStore.setLoadingStats(false);
      }
    },
    staleTime: 60000, // 1 minute
    enabled: true
  });

  return {
    logs: logStore.logs,
    stats: logStore.logStats,
    filter: logStore.filter,
    selectedLog: logStore.selectedLog,
    total: logStore.total,
    totalPages: logStore.totalPages,
    isLoading: isLoading || logStore.isLoading,
    isLoadingStats: isLoadingStats || logStore.isLoadingStats,
    isError,

    // Actions
    setFilter: logStore.setFilter,
    resetFilter: logStore.resetFilter,
    setSelectedLog: logStore.setSelectedLog,
    refetch
  };
};