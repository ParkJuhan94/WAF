import React from 'react';
import { Badge, Tag } from 'antd';
import {
  CheckCircleOutlined,
  StopOutlined,
  ExclamationCircleOutlined,
  EyeOutlined,
  CloseCircleOutlined,
  ToolOutlined
} from '@ant-design/icons';
import { STATUS_COLORS, SEVERITY_COLORS } from '../../utils/constants';

interface StatusBadgeProps {
  status: 'active' | 'inactive' | 'error' | 'maintenance';
  showText?: boolean;
  size?: 'small' | 'default';
}

interface SeverityBadgeProps {
  severity: 'low' | 'medium' | 'high' | 'critical';
  showText?: boolean;
  size?: 'small' | 'default';
}

interface AttackTypeBadgeProps {
  type: string;
  size?: 'small' | 'default';
}

const getStatusIcon = (status: string) => {
  switch (status) {
    case 'active':
      return <CheckCircleOutlined className="pulse" />;
    case 'inactive':
      return <StopOutlined />;
    case 'error':
      return <CloseCircleOutlined className="blink" />;
    case 'maintenance':
      return <ToolOutlined />;
    default:
      return <EyeOutlined />;
  }
};

const getStatusText = (status: string) => {
  switch (status) {
    case 'active':
      return '활성';
    case 'inactive':
      return '비활성';
    case 'error':
      return '오류';
    case 'maintenance':
      return '유지보수';
    default:
      return '알 수 없음';
  }
};

const getSeverityText = (severity: string) => {
  switch (severity) {
    case 'low':
      return '낮음';
    case 'medium':
      return '보통';
    case 'high':
      return '높음';
    case 'critical':
      return '위험';
    default:
      return severity;
  }
};

const getAttackTypeText = (type: string) => {
  switch (type) {
    case 'sql_injection':
      return 'SQL 인젝션';
    case 'xss':
      return 'XSS';
    case 'file_upload':
      return '파일 업로드';
    case 'command_injection':
      return '커맨드 인젝션';
    case 'path_traversal':
      return '경로 조작';
    default:
      return type;
  }
};

export const StatusBadge: React.FC<StatusBadgeProps> = ({
  status,
  showText = true,
  size = 'default'
}) => {
  const color = STATUS_COLORS[status];
  const icon = getStatusIcon(status);
  const text = getStatusText(status);

  if (showText) {
    return (
      <Tag
        icon={icon}
        color={color}
        className={`border-0 ${size === 'small' ? 'text-xs' : ''}`}
      >
        {text}
      </Tag>
    );
  }

  return (
    <Badge
      status={status === 'active' ? 'processing' : status === 'error' ? 'error' : 'default'}
      className={status === 'active' ? 'pulse' : status === 'error' ? 'blink' : ''}
    />
  );
};

export const SeverityBadge: React.FC<SeverityBadgeProps> = ({
  severity,
  showText = true,
  size = 'default'
}) => {
  const color = SEVERITY_COLORS[severity];
  const text = getSeverityText(severity);

  return (
    <Tag
      color={color}
      className={`border-0 ${size === 'small' ? 'text-xs' : ''} ${
        severity === 'critical' ? 'blink' : ''
      }`}
    >
      {showText ? text : '●'}
    </Tag>
  );
};

export const AttackTypeBadge: React.FC<AttackTypeBadgeProps> = ({
  type,
  size = 'default'
}) => {
  const text = getAttackTypeText(type);

  const getTypeColor = (attackType: string) => {
    switch (attackType) {
      case 'sql_injection':
        return '#ff6b6b';
      case 'xss':
        return '#feca57';
      case 'file_upload':
        return '#48cae4';
      case 'command_injection':
        return '#ff6b6b';
      case 'path_traversal':
        return '#feca57';
      default:
        return '#a8b2c1';
    }
  };

  return (
    <Tag
      color={getTypeColor(type)}
      className={`border-0 ${size === 'small' ? 'text-xs' : ''}`}
    >
      {text}
    </Tag>
  );
};

export const BlockedBadge: React.FC<{ blocked: boolean; size?: 'small' | 'default' }> = ({
  blocked,
  size = 'default'
}) => {
  return (
    <Tag
      color={blocked ? '#ff6b6b' : '#1ec997'}
      className={`border-0 ${size === 'small' ? 'text-xs' : ''}`}
    >
      {blocked ? '차단됨' : '허용됨'}
    </Tag>
  );
};