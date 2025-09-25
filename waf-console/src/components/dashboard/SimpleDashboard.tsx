import React from 'react';
import { Card, Row, Col } from 'antd';

export const SimpleDashboard: React.FC = () => {
  return (
    <div>
      <h2 style={{ color: 'white', marginBottom: '24px' }}>WAF Dashboard</h2>
      <Row gutter={[16, 16]}>
        <Col span={12}>
          <Card title="Traffic Stats">
            <p>Total Requests: 1,234</p>
            <p>Blocked: 56</p>
            <p>Allowed: 1,178</p>
          </Card>
        </Col>
        <Col span={12}>
          <Card title="System Status">
            <p>WAF Status: Active</p>
            <p>Last Update: Just now</p>
            <p>Rules Active: 245</p>
          </Card>
        </Col>
      </Row>
    </div>
  );
};