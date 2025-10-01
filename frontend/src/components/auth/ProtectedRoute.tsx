import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../stores/useAuthStore';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAuth?: boolean;
  requiredPermission?: string;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requireAuth = true,
  requiredPermission
}) => {
  const { isAuthenticated, user } = useAuthStore();
  const location = useLocation();

  // Development mode bypass
  const isDevelopment = import.meta.env.DEV;

  // Check authentication
  if (requireAuth && !isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Check permissions (if required)
  if (requiredPermission && user && !checkUserPermission(user, requiredPermission)) {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
};

// Helper function to check user permissions
const checkUserPermission = (user: any, permission: string): boolean => {
  // In development, allow all permissions
  if (import.meta.env.DEV) return true;

  // TODO: Implement actual permission checking logic
  // This would typically check user.permissions array or role
  switch (permission) {
    case 'admin':
      return user.role === 'admin';
    case 'premium':
      return ['premium', 'enterprise', 'admin'].includes(user.role);
    case 'rules.write':
      return ['premium', 'enterprise', 'admin'].includes(user.role);
    default:
      return true;
  }
};