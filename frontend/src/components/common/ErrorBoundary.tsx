import React, { Component, ErrorInfo, ReactNode } from 'react';
import { Result, Button } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    return {
      hasError: true,
      error
    };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo);
  }

  handleReload = () => {
    window.location.reload();
  };

  handleReset = () => {
    this.setState({ hasError: false, error: undefined });
  };

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback;
      }

      return (
        <div className="min-h-screen bg-bg-primary flex items-center justify-center">
          <Result
            status="error"
            title="앱에서 오류가 발생했습니다"
            subTitle={
              process.env.NODE_ENV === 'development'
                ? this.state.error?.message
                : '예상치 못한 오류가 발생했습니다. 페이지를 새로고침해 주세요.'
            }
            extra={[
              <Button key="retry" type="primary" onClick={this.handleReset}>
                다시 시도
              </Button>,
              <Button key="reload" icon={<ReloadOutlined />} onClick={this.handleReload}>
                페이지 새로고침
              </Button>
            ]}
            className="bg-bg-primary text-text-primary"
          />
        </div>
      );
    }

    return this.props.children;
  }
}