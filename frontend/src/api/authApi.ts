import apiClient from './axiosClient';
import type { LoginRequest, AuthResponse, UserDto, RefreshTokenRequest } from '../types/api';
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export async function login(data: LoginRequest): Promise<AuthResponse> {
  const response = await apiClient.post<AuthResponse>('/api/v1/auth/login', data);
  return response.data;
}

export async function refreshToken(data: RefreshTokenRequest): Promise<AuthResponse> {
  // Use raw axios to avoid interceptor loop
  const response = await axios.post<AuthResponse>(`${API_BASE_URL}/api/v1/auth/refresh`, data);
  return response.data;
}

export async function logout(refreshTokenStr: string | null): Promise<void> {
  await apiClient.post('/api/v1/auth/logout', refreshTokenStr ? { refreshToken: refreshTokenStr } : {});
}

export async function getMe(): Promise<UserDto> {
  const response = await apiClient.get<UserDto>('/api/v1/auth/me');
  return response.data;
}
