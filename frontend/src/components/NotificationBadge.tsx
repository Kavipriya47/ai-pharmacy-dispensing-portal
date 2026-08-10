import { Badge, IconButton, Tooltip, Menu, MenuItem, Typography, Box, CircularProgress } from '@mui/material';
import NotificationsIcon from '@mui/icons-material/Notifications';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getUnreadCount, getNotifications, markAsRead } from '../api/notificationApi';
import { useAuth } from '../auth/useAuth';
import { ROLES } from '../types/api';
import type { NotificationDto } from '../types/api';
import React, { useState } from 'react';

export default function NotificationBadge() {
  const { hasAnyRole } = useAuth();
  const canViewNotifications = hasAnyRole(ROLES.ADMIN, ROLES.PHARMACIST);
  const queryClient = useQueryClient();
  
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);

  const { data: unreadCount = 0 } = useQuery<number>({
    queryKey: ['notificationsUnreadCount'],
    queryFn: getUnreadCount,
    refetchInterval: 60_000,
    enabled: canViewNotifications,
  });

  const { data: notifications = [], isLoading } = useQuery<NotificationDto[]>({
    queryKey: ['notifications'],
    queryFn: getNotifications,
    enabled: canViewNotifications && open,
  });

  const readMutation = useMutation({
    mutationFn: markAsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['notificationsUnreadCount'] });
    },
  });

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleNotificationClick = (notification: NotificationDto) => {
    if (!notification.read) {
      readMutation.mutate(notification.id);
    }
  };

  if (!canViewNotifications) return null;

  return (
    <>
      <Tooltip title={`${unreadCount} unread notification${unreadCount !== 1 ? 's' : ''}`}>
        <IconButton color="inherit" aria-label="notifications" onClick={handleClick}>
          <Badge badgeContent={unreadCount} color="error" max={99}>
            <NotificationsIcon />
          </Badge>
        </IconButton>
      </Tooltip>
      
      <Menu
        anchorEl={anchorEl}
        open={open}
        onClose={handleClose}
        sx={{
          '& .MuiPaper-root': {
            maxHeight: 400,
            width: '350px',
          }
        }}
      >
        <Box sx={{ px: 2, py: 1, borderBottom: '1px solid #e0e0e0' }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>Notifications</Typography>
        </Box>
        
        {isLoading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
            <CircularProgress size={24} />
          </Box>
        ) : notifications.length === 0 ? (
          <MenuItem disabled>No notifications</MenuItem>
        ) : (
          notifications.map((notif) => (
            <MenuItem 
              key={notif.id} 
              onClick={() => handleNotificationClick(notif)}
              sx={{ 
                whiteSpace: 'normal', 
                backgroundColor: notif.read ? 'inherit' : '#f0f7ff',
                borderBottom: '1px solid #f0f0f0',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'flex-start',
                py: 1.5
              }}
            >
              <Typography variant="body2" sx={{ fontWeight: notif.read ? 'normal' : 'bold' }}>
                {notif.title}
              </Typography>
              <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5 }}>
                {notif.message}
              </Typography>
            </MenuItem>
          ))
        )}
      </Menu>
    </>
  );
}
