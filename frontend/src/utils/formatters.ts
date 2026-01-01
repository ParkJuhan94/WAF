export const formatNumber = (num: number): string => {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M';
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K';
  }
  return num.toString();
};

export const formatBytes = (bytes: number): string => {
  if (bytes === 0) return '0 B';

  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

export const formatDuration = (milliseconds: number): string => {
  const seconds = Math.floor(milliseconds / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (days > 0) return `${days}d ${hours % 24}h`;
  if (hours > 0) return `${hours}h ${minutes % 60}m`;
  if (minutes > 0) return `${minutes}m ${seconds % 60}s`;
  return `${seconds}s`;
};

export const formatDate = (date: string | Date | null | undefined): string => {
  if (!date) {
    return 'Invalid date';
  }
  try {
    return new Intl.DateTimeFormat('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }).format(new Date(date));
  } catch (error) {
    console.error('Date formatting error:', error);
    return 'Invalid date';
  }
};

export const formatRelativeTime = (date: string | Date | null | undefined): string => {
  if (!date) {
    return '알 수 없음';
  }

  try {
    const now = new Date();
    const target = new Date(date);

    if (isNaN(target.getTime())) {
      return '알 수 없음';
    }

    const diff = now.getTime() - target.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (days > 0) return `${days}일 전`;
    if (hours > 0) return `${hours}시간 전`;
    if (minutes > 0) return `${minutes}분 전`;
    return '방금 전';
  } catch (error) {
    console.error('Relative time formatting error:', error);
    return '알 수 없음';
  }
};

export const formatPercentage = (value: number, total: number): string => {
  if (total === 0) return '0%';
  return ((value / total) * 100).toFixed(1) + '%';
};

export const formatIpAddress = (ip: string | null | undefined): string => {
  // Handle null, undefined, or empty values
  if (!ip || typeof ip !== 'string' || ip.trim() === '') {
    return 'Unknown';
  }

  // IPv6 주소 단축
  if (ip.includes(':')) {
    return ip.replace(/::ffff:/, '');
  }

  return ip;
};

export const truncateText = (text: string | null | undefined, maxLength: number): string => {
  if (!text || typeof text !== 'string') {
    return '';
  }
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};