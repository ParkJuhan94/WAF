import React from 'react';
import { Card, Typography, Button, Space } from 'antd';
import { PlusOutlined, SettingOutlined } from '@ant-design/icons';

const { Title } = Typography;

export const RulesPage: React.FC = () => {
  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <Title level={2} className="text-text-primary m-0">
          룰 관리
        </Title>
        <Space>
          <Button type="primary" icon={<PlusOutlined />}>
            새 룰 생성
          </Button>
          <Button icon={<SettingOutlined />}>
            룰 설정
          </Button>
        </Space>
      </div>

      <Card className="bg-bg-card border-border">
        <div className="space-y-4">
          <div className="text-center py-12">
            <SettingOutlined className="text-6xl text-text-secondary mb-4" />
            <Title level={3} className="text-text-secondary">
              WAF 룰 관리
            </Title>
            <p className="text-text-secondary mb-6">
              OWASP CRS 룰 관리 및 커스텀 룰 생성 기능
            </p>
            <Space direction="vertical" size="middle">
              <div className="flex justify-center gap-4">
                <Button type="primary" size="large">
                  OWASP CRS 룰 보기
                </Button>
                <Button size="large">
                  커스텀 룰 관리
                </Button>
              </div>
              <div className="text-sm text-text-secondary">
                사용자별 룰 네임스페이스로 격리된 룰 관리
              </div>
            </Space>
          </div>
        </div>
      </Card>
    </div>
  );
};