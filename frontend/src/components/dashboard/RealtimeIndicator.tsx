import React, { useEffect, useState } from 'react';
import { Badge } from 'antd';
import { wafWebSocket } from '../../services/websocket';

/**
 * 실시간 연결 상태 인디케이터
 *
 * WebSocket 연결 상태를 실시간으로 표시합니다.
 * - LIVE: 연결됨 (녹색)
 * - OFFLINE: 연결 끊김 (빨강)
 */
export const RealtimeIndicator: React.FC = () => {
  const [connected, setConnected] = useState(false);
  const [reconnecting, setReconnecting] = useState(false);

  useEffect(() => {
    // 연결 성공 핸들러
    const handleConnected = () => {
      setConnected(true);
      setReconnecting(false);
    };

    // 연결 끊김 핸들러
    const handleDisconnected = () => {
      setConnected(false);
      setReconnecting(true);
      // 10초 후에도 재연결 안되면 reconnecting 상태 해제
      setTimeout(() => {
        if (!wafWebSocket.isConnected()) {
          setReconnecting(false);
        }
      }, 10000);
    };

    // 에러 핸들러
    const handleError = () => {
      setConnected(false);
    };

    // 이벤트 핸들러 등록
    wafWebSocket.on('connected', handleConnected);
    wafWebSocket.on('disconnected', handleDisconnected);
    wafWebSocket.on('error', handleError);

    // 초기 상태 설정
    setConnected(wafWebSocket.isConnected());

    // 정리
    return () => {
      wafWebSocket.off('connected', handleConnected);
      wafWebSocket.off('disconnected', handleDisconnected);
      wafWebSocket.off('error', handleError);
    };
  }, []);

  // 상태 텍스트
  const getStatusText = () => {
    if (connected) return 'LIVE';
    if (reconnecting) return 'Reconnecting...';
    return 'OFFLINE';
  };

  // Badge status
  const getBadgeStatus = (): 'processing' | 'error' | 'default' => {
    if (connected) return 'processing';
    if (reconnecting) return 'default';
    return 'error';
  };

  return (
    <div style={{ display: 'flex', alignItems: 'center' }}>
      <Badge status={getBadgeStatus()} text={getStatusText()} />
    </div>
  );
};

export default RealtimeIndicator;
