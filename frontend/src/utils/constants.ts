export const API_ENDPOINTS = {
  AUTH: '/auth',
  DASHBOARD: '/dashboard',
  LOGS: '/logs',
  RULES: '/rules',
  WHITELIST: '/whitelist',
  TESTING: '/testing',
  WEBSOCKET: '/ws'
};

export const ATTACK_TYPES = {
  SQL_INJECTION: 'sql_injection',
  XSS: 'xss',
  FILE_UPLOAD: 'file_upload',
  COMMAND_INJECTION: 'command_injection',
  PATH_TRAVERSAL: 'path_traversal',
  OTHER: 'other'
} as const;

export const ATTACK_SCENARIOS = {
  sqlInjection: {
    name: 'SQL Injection',
    payload: "' OR '1'='1",
    description: 'Basic SQL injection attempt'
  },
  xss: {
    name: 'Cross-Site Scripting',
    payload: '<script>alert(1)</script>',
    description: 'Basic XSS payload'
  },
  fileUpload: {
    name: 'Malicious File Upload',
    payload: 'test.php',
    description: 'PHP file upload attempt'
  },
  commandInjection: {
    name: 'Command Injection',
    payload: '; ls && cat /etc/passwd',
    description: 'Unix command injection'
  },
  pathTraversal: {
    name: 'Path Traversal',
    payload: '../../../etc/passwd',
    description: 'Directory traversal attack'
  }
};

export const SEVERITY_COLORS = {
  low: '#1ec997',
  medium: '#48cae4',
  high: '#feca57',
  critical: '#ff6b6b'
};

export const STATUS_COLORS = {
  active: '#1ec997',
  inactive: '#a8b2c1',
  error: '#ff6b6b',
  maintenance: '#feca57'
};

export const CHART_COLORS = {
  primary: '#1ec997',
  secondary: '#48cae4',
  danger: '#ff6b6b',
  warning: '#feca57',
  info: '#48cae4',
  success: '#1ec997'
};

export const REFRESH_INTERVALS = {
  REAL_TIME: 1000,
  FAST: 5000,
  NORMAL: 30000,
  SLOW: 60000
};

export const PAGE_SIZES = [10, 20, 50, 100] as const;