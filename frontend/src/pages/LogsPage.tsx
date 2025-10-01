import React from 'react';
import { FileTextOutlined } from '@ant-design/icons';

export const LogsPage: React.FC = () => {
  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <FileTextOutlined className="w-8 h-8 text-blue-600" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">로그 관리</h1>
            <p className="text-gray-600">WAF 로그 조회, 분석 및 내보내기 (구현 중)</p>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="bg-white rounded-lg shadow-sm p-8">
        <div className="text-center">
          <FileTextOutlined className="w-16 h-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-gray-900 mb-2">
            로그 관리 시스템
          </h3>
          <p className="text-gray-600 mb-6">
            WAF 로그 조회 및 분석 기능을 구현 중입니다.
          </p>
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <p className="text-blue-800 text-sm">
              📋 로그 필터링 및 조회 기능은 추후 구현 예정입니다.<br />
              현재는 커스텀 룰 관리 기능을 사용해주세요.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};