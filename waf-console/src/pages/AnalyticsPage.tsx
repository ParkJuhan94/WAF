import React from 'react';
import { Card, Typography, Row, Col, Button, Space } from 'antd';
import { BarChartOutlined, FileTextOutlined, ExportOutlined } from '@ant-design/icons';

const { Title } = Typography;

export const AnalyticsPage: React.FC = () => {
  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <Title level={2} className="text-text-primary m-0">
          분석 및 리포트
        </Title>
        <Space>
          <Button type="primary" icon={<FileTextOutlined />}>
            보고서 생성
          </Button>
          <Button icon={<ExportOutlined />}>
            데이터 내보내기
          </Button>
        </Space>
      </div>

      <Row gutter={[24, 24]}>
        <Col xs={24} lg={12}>
          <Card className="bg-bg-card border-border" title="공격 패턴 트렌드">
            <div className="text-center py-8">
              <BarChartOutlined className="text-4xl text-accent-primary mb-4" />
              <p className="text-text-secondary">
                시간대별 공격 패턴 분석 차트
              </p>
            </div>
          </Card>
        </Col>

        <Col xs={24} lg={12}>
          <Card className="bg-bg-card border-border" title="차단률/오탐률 분석">
            <div className="text-center py-8">
              <div className="space-y-2">
                <div className="text-2xl font-bold text-success">98.2%</div>
                <div className="text-text-secondary">차단율</div>
                <div className="text-lg font-bold text-warning">1.8%</div>
                <div className="text-text-secondary">오탐율</div>
              </div>
            </div>
          </Card>
        </Col>

        <Col xs={24}>
          <Card className="bg-bg-card border-border" title="이상 패턴 AI 감지">
            <div className="text-center py-12">
              <Title level={3} className="text-text-secondary">
                고급 분석 기능
              </Title>
              <p className="text-text-secondary mb-6">
                AI 기반 이상 패턴 감지 및 위협 인텔리전스
              </p>
              <Space size="large">
                <div className="text-center">
                  <div className="text-xl font-bold text-info">247</div>
                  <div className="text-text-secondary">탐지된 패턴</div>
                </div>
                <div className="text-center">
                  <div className="text-xl font-bold text-warning">12</div>
                  <div className="text-text-secondary">위험 알림</div>
                </div>
                <div className="text-center">
                  <div className="text-xl font-bold text-success">99.1%</div>
                  <div className="text-text-secondary">정확도</div>
                </div>
              </Space>
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  );
};