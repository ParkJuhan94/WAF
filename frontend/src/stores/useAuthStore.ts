import { create } from 'zustand';
import { User } from '../types/api';
import { apiClient } from '../services/api';

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (googleToken: string) => Promise<void>;
  logout: () => void;
  refreshToken: () => Promise<void>;
  setUser: (user: User) => void;
}

export const useAuthStore = create<AuthState>()((set, get) => ({
  user: null,
  token: localStorage.getItem('auth_token'),
  isAuthenticated: !!localStorage.getItem('auth_token'),
  isLoading: false,

  login: async (googleToken: string) => {
    set({ isLoading: true });
    try {
      const response = await apiClient.login(googleToken);
      const { token, user } = response;

      localStorage.setItem('auth_token', token);
      set({
        user,
        token,
        isAuthenticated: true,
        isLoading: false
      });
    } catch (error) {
      console.error('Login failed:', error);
      set({ isLoading: false });
      throw error;
    }
  },

  logout: () => {
    localStorage.removeItem('auth_token');
    set({
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false
    });

    // Call API logout if needed
    apiClient.logout().catch(console.error);
  },

  refreshToken: async () => {
    try {
      const response = await apiClient.refreshToken();
      const { token } = response;

      localStorage.setItem('auth_token', token);
      set({ token });
    } catch (error) {
      console.error('Token refresh failed:', error);
      get().logout();
      throw error;
    }
  },

  setUser: (user: User) => {
    set({ user });
  }
}));