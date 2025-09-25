import React from 'react';
import { Card, Typography, Tabs, Switch, Form, Input, Button, Space, Divider } from 'antd';
import { SettingOutlined, BellOutlined, KeyOutlined } from '@ant-design/icons';

const { Title } = Typography;
const { TabPane } = Tabs;

export const SettingsPage: React.FC = () => {
  return (
    <div className="p-6">
      <Title level={2} className="text-text-primary mb-6">
        시스템 설정
      </Title>

      <Card className="bg-bg-card border-border">
        <Tabs defaultActiveKey="system" className="text-text-primary">
          <TabPane tab={<span><SettingOutlined />시스템</span>} key="system">
            <Form layout="vertical">
              <Form.Item label="WAF 엔진 상태">
                <Space>
                  <Switch defaultChecked />
                  <span className="text-text-secondary">활성화</span>
                </Space>
              </Form.Item>

              <Form.Item label="차단 모드">
                <Space>
                  <Switch defaultChecked />
                  <span className="text-text-secondary">공격 차단 (비활성화 시 탐지만)</span>
                </Space>
              </Form.Item>

              <Divider />

              <Form.Item label="로그 보관 기간 (일)">
                <Input defaultValue="30" type="number" style={{ width: 200 }} />
              </Form.Item>

              <Form.Item>
                <Button type="primary">설정 저장</Button>
              </Form.Item>
            </Form>
          </TabPane>

          <TabPane tab={<span><BellOutlined />알림</span>} key="notifications">
            <Form layout="vertical">
              <Form.Item label="이메일 알림">
                <Space direction="vertical" style={{ width: '100%' }}>
                  <div className="flex items-center justify-between">
                    <span>공격 탐지 알림</span>
                    <Switch defaultChecked />
                  </div>
                  <div className="flex items-center justify-between">
                    <span>일일 보고서</span>
                    <Switch />
                  </div>
                  <div className="flex items-center justify-between">
                    <span>시스템 상태 변경</span>
                    <Switch defaultChecked />
                  </div>
                </Space>
              </Form.Item>

              <Form.Item label="알림 이메일">
                <Input placeholder="admin@example.com" />
              </Form.Item>

              <Form.Item>
                <Button type="primary">알림 설정 저장</Button>
              </Form.Item>
            </Form>
          </TabPane>

          <TabPane tab={<span><KeyOutlined />API</span>} key="api">
            <div className="space-y-4">
              <div>
                <Title level={4} className="text-text-primary">API 키 관리</Title>
                <p className="text-text-secondary mb-4">
                  외부 시스템과의 연동을 위한 API 키를 관리합니다.
                </p>

                <div className="bg-bg-surface p-4 rounded mb-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <div className="font-mono text-sm">waf_xxxxxxxxxxxxxxxx</div>
                      <div className="text-text-secondary text-xs">마지막 사용: 2시간 전</div>
                    </div>
                    <Space>
                      <Button size="small">복사</Button>
                      <Button size="small" danger>삭제</Button>
                    </Space>
                  </div>
                </div>

                <Button type="primary">새 API 키 생성</Button>
              </div>
            </div>
          </TabPane>
        </Tabs>
      </Card>
    </div>
  );
};