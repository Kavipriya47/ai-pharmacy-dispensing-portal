import axios, { AxiosError } from 'axios';
import type { InternalAxiosRequestConfig } from 'axios';
import { getAccessToken, getRefreshToken, setTokens, clearTokens } from '../auth/tokenStore';
import type { AuthResponse } from '../types/api';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor - attach access token
apiClient.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Single-flight refresh mechanism
let isRefreshing = false;
let refreshPromise: Promise<string> | null = null;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token!);
    }
  });
  failedQueue = [];
};

const AUTH_PATHS = ['/auth/login', '/auth/refresh', '/auth/logout'];

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // Don't intercept auth endpoints
    if (originalRequest?.url && AUTH_PATHS.some((path) => originalRequest.url!.includes(path))) {
      return Promise.reject(error);
    }

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      if (isRefreshing) {
        // Queue this request
        return new Promise<string>((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return apiClient(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      isRefreshing = true;
      const currentRefreshToken = getRefreshToken();

      if (!currentRefreshToken) {
        clearTokens();
        window.location.href = '/login';
        return Promise.reject(error);
      }

      refreshPromise = axios
        .post<AuthResponse>(`${API_BASE_URL}/api/v1/auth/refresh`, {
          refreshToken: currentRefreshToken,
        })
        .then((res) => {
          const { accessToken, refreshToken } = res.data;
          setTokens(accessToken, refreshToken);
          processQueue(null, accessToken);
          return accessToken;
        })
        .catch((refreshError) => {
          processQueue(refreshError, null);
          clearTokens();
          window.location.href = '/login';
          throw refreshError;
        })
        .finally(() => {
          isRefreshing = false;
          refreshPromise = null;
        });

      try {
        const newToken = await refreshPromise;
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
