import React from 'react';
import { Navigate } from 'react-router-dom';
import { GoogleLogin } from '../components/auth/GoogleLogin';
import { useAuthStore } from '../stores/useAuthStore';

export const LoginPage: React.FC = () => {
  const { isAuthenticated } = useAuthStore();

  // If already authenticated, redirect to dashboard
  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return <GoogleLogin />;
};