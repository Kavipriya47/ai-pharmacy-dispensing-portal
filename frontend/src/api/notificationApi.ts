import apiClient from './axiosClient';
import type { NotificationDto, Page } from '../types/api';

export async function getUnreadCount(): Promise<number> {
  const response = await apiClient.get<number>('/api/v1/notifications/unread-count');
  return response.data;
}

export async function getNotifications(): Promise<NotificationDto[]> {
  const response = await apiClient.get<Page<NotificationDto>>('/api/v1/notifications?size=50&sort=createdAt,desc');
  return response.data.content;
}

export async function markAsRead(id: number): Promise<void> {
  await apiClient.patch(`/api/v1/notifications/${id}/read`);
}
