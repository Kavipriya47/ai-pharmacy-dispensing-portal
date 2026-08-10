import apiClient from './axiosClient';
import { NotificationDto } from '../types/api';

export async function getUnreadCount(): Promise<number> {
  const response = await apiClient.get<number>('/api/v1/notifications/unread-count');
  return response.data;
}

export async function getNotifications(): Promise<NotificationDto[]> {
  const response = await apiClient.get<NotificationDto[]>('/api/v1/notifications');
  return response.data;
}

export async function markAsRead(id: number): Promise<void> {
  await apiClient.put(`/api/v1/notifications/${id}/read`);
}
