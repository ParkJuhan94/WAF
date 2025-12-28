export interface User {
  id: string;
  email: string;
  name: string;
  avatar?: string;
  role: 'admin' | 'operator' | 'viewer';
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  error?: string;
}

export interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
  // Spring Boot Page compatibility
  content?: T[];
  totalElements?: number;
}

export interface WebSocketMessage {
  type: 'traffic_update' | 'attack_blocked' | 'log_entry' | 'status_change';
  payload: any;
  timestamp: string;
}