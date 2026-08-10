import React, { createContext, useState, useCallback, useEffect } from 'react';
import type { UserDto, AuthResponse } from '../types/api';
import { setTokens, clearTokens, getRefreshToken } from './tokenStore';
import * as authApi from '../api/authApi';

export interface AuthState {
  user: UserDto | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

export interface AuthContextType extends AuthState {
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  hasRole: (role: string) => boolean;
  hasAnyRole: (...roles: string[]) => boolean;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<UserDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const isAuthenticated = user !== null;

  // Try to restore session on mount using stored refresh token
  useEffect(() => {
    const restoreSession = async () => {
      const storedRefreshToken = getRefreshToken();
      if (!storedRefreshToken) {
        setIsLoading(false);
        return;
      }

      try {
        const response = await authApi.refreshToken({ refreshToken: storedRefreshToken });
        setTokens(response.accessToken, response.refreshToken);
        setUser(response.user);
      } catch {
        clearTokens();
      } finally {
        setIsLoading(false);
      }
    };

    restoreSession();
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    const response: AuthResponse = await authApi.login({ username, password });
    setTokens(response.accessToken, response.refreshToken);
    setUser(response.user);
  }, []);

  const logout = useCallback(async () => {
    const storedRefreshToken = getRefreshToken();
    try {
      await authApi.logout(storedRefreshToken);
    } catch {
      // Logout failure should not block client-side cleanup
    } finally {
      clearTokens();
      setUser(null);
    }
  }, []);

  const hasRole = useCallback(
    (role: string) => {
      return user?.roles?.includes(role) ?? false;
    },
    [user]
  );

  const hasAnyRole = useCallback(
    (...roles: string[]) => {
      return roles.some((role) => user?.roles?.includes(role) ?? false);
    },
    [user]
  );

  return (
    <AuthContext.Provider
      value={{ user, isAuthenticated, isLoading, login, logout, hasRole, hasAnyRole }}
    >
      {children}
    </AuthContext.Provider>
  );
}
