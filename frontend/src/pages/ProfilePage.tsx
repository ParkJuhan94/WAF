import React from 'react';
import { Card, Typography, Avatar, Descriptions, Button, Space, Tag } from 'antd';
import { UserOutlined, GoogleOutlined, KeyOutlined } from '@ant-design/icons';
import { useAuthStore } from '../stores/useAuthStore';

const { Title } = Typography;

export const ProfilePage: React.FC = () => {
  const { user } = useAuthStore();

  return (
    <div>
      <Title level={2} className="text-text-primary mb-6">
        사용자 프로필
      </Title>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Profile Info */}
        <div className="lg:col-span-2">
          <Card className="bg-bg-card border-border">
            <div className="flex items-start space-x-4 mb-6">
              <Avatar size={80} icon={<UserOutlined />} className="bg-accent-primary" />
              <div className="flex-1">
                <Title level={3} className="text-text-primary m-0">
                  {user?.name || 'Developer User'}
                </Title>
                <p className="text-text-secondary mb-2">
                  {user?.email || 'dev@example.com'}
                </p>
                <Tag color="#1ec997">Premium</Tag>
              </div>
            </div>

            <Descriptions column={1} className="text-text-primary">
              <Descriptions.Item label="가입일">2024년 1월 15일</Descriptions.Item>
              <Descriptions.Item label="마지막 로그인">방금 전</Descriptions.Item>
              <Descriptions.Item label="구독 플랜">Premium (월간)</Descriptions.Item>
              <Descriptions.Item label="API 사용량">2,847 / 10,000 요청</Descriptions.Item>
            </Descriptions>

            <div className="mt-6">
              <Space>
                <Button type="primary" icon={<GoogleOutlined />}>
                  Google 계정 연동
                </Button>
                <Button icon={<KeyOutlined />}>
                  API 키 관리
                </Button>
              </Space>
            </div>
          </Card>
        </div>

        {/* Usage Stats */}
        <div>
          <Card className="bg-bg-card border-border" title="사용 통계">
            <div className="space-y-4">
              <div className="text-center">
                <div className="text-2xl font-bold text-accent-primary">12,847</div>
                <div className="text-text-secondary text-sm">총 처리된 요청</div>
              </div>

              <div className="text-center">
                <div className="text-xl font-bold text-danger">234</div>
                <div className="text-text-secondary text-sm">차단된 공격</div>
              </div>

              <div className="text-center">
                <div className="text-lg font-bold text-success">99.2%</div>
                <div className="text-text-secondary text-sm">가동률</div>
              </div>

              <div className="text-center">
                <div className="text-lg font-bold text-warning">45</div>
                <div className="text-text-secondary text-sm">활성 룰</div>
              </div>
            </div>
          </Card>

          <Card className="bg-bg-card border-border mt-4" title="구독 관리">
            <div className="text-center">
              <Tag color="#1ec997" className="mb-2">Premium</Tag>
              <div className="text-text-secondary text-sm mb-4">
                다음 갱신일: 2024년 10월 15일
              </div>
              <Space direction="vertical" style={{ width: '100%' }}>
                <Button type="primary" block>
                  구독 관리
                </Button>
                <Button block>
                  플랜 변경
                </Button>
              </Space>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};