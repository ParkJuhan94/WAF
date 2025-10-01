import { create } from 'zustand';
import { LogEntry, LogFilter, LogStats } from '../types/logs';

interface LogState {
  // Current filter state
  filter: LogFilter;

  // Data
  logs: LogEntry[];
  logStats: LogStats | null;
  selectedLog: LogEntry | null;

  // Pagination
  total: number;
  totalPages: number;

  // Loading states
  isLoading: boolean;
  isLoadingStats: boolean;

  // Actions
  setFilter: (filter: Partial<LogFilter>) => void;
  resetFilter: () => void;
  setSelectedLog: (log: LogEntry | null) => void;
  setLogs: (logs: LogEntry[], total: number, totalPages: number) => void;
  setLogStats: (stats: LogStats) => void;
  addNewLog: (log: LogEntry) => void;
  setLoading: (loading: boolean) => void;
  setLoadingStats: (loading: boolean) => void;
}

const defaultFilter: LogFilter = {
  dateRange: [
    new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(), // 24 hours ago
    new Date().toISOString() // now
  ],
  page: 1,
  pageSize: 20
};

export const useLogStore = create<LogState>((set, get) => ({
  // Initial state
  filter: defaultFilter,
  logs: [],
  logStats: null,
  selectedLog: null,
  total: 0,
  totalPages: 0,
  isLoading: false,
  isLoadingStats: false,

  // Actions
  setFilter: (newFilter: Partial<LogFilter>) => {
    const currentFilter = get().filter;
    const updatedFilter = {
      ...currentFilter,
      ...newFilter,
      // Reset to page 1 when filter changes (except when changing page)
      page: 'page' in newFilter ? newFilter.page! : 1
    };
    set({ filter: updatedFilter });
  },

  resetFilter: () => {
    set({ filter: defaultFilter });
  },

  setSelectedLog: (log: LogEntry | null) => {
    set({ selectedLog: log });
  },

  setLogs: (logs: LogEntry[], total: number, totalPages: number) => {
    set({ logs, total, totalPages });
  },

  setLogStats: (stats: LogStats) => {
    set({ logStats: stats });
  },

  addNewLog: (log: LogEntry) => {
    const currentLogs = get().logs;

    // Add to beginning and limit to current page size
    const updatedLogs = [log, ...currentLogs].slice(0, get().filter.pageSize);

    set({
      logs: updatedLogs,
      total: get().total + 1
    });
  },

  setLoading: (loading: boolean) => {
    set({ isLoading: loading });
  },

  setLoadingStats: (loading: boolean) => {
    set({ isLoadingStats: loading });
  }
}));