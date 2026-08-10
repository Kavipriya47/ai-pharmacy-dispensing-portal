import apiClient from './axiosClient';

export async function getUnreadCount(): Promise<number> {
  const response = await apiClient.get<number>('/api/v1/notifications/unread-count');
  return response.data;
}
