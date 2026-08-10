import { Badge, IconButton, Tooltip } from '@mui/material';
import NotificationsIcon from '@mui/icons-material/Notifications';
import { useQuery } from '@tanstack/react-query';
import { getUnreadCount } from '../api/notificationApi';
import { useAuth } from '../auth/useAuth';
import { ROLES } from '../types/api';

export default function NotificationBadge() {
  const { hasAnyRole } = useAuth();
  const canViewNotifications = hasAnyRole(ROLES.ADMIN, ROLES.PHARMACIST);

  const { data: unreadCount = 0 } = useQuery<number>({
    queryKey: ['notificationsUnreadCount'],
    queryFn: getUnreadCount,
    refetchInterval: 60_000,  // Poll every 60 seconds
    enabled: canViewNotifications,
  });

  if (!canViewNotifications) return null;

  return (
    <Tooltip title={`${unreadCount} unread notification${unreadCount !== 1 ? 's' : ''}`}>
      <IconButton color="inherit" aria-label="notifications">
        <Badge badgeContent={unreadCount} color="error" max={99}>
          <NotificationsIcon />
        </Badge>
      </IconButton>
    </Tooltip>
  );
}
