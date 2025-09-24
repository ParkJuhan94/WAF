import React from 'react';
import { MockDashboard } from '../components/dashboard/MockDashboard';

export const DashboardPage: React.FC = () => {
  return (
    <div className="p-6">
      <MockDashboard />
    </div>
  );
};