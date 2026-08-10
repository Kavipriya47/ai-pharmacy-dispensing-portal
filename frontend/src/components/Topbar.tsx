import {
  AppBar,
  Toolbar,
  IconButton,
  Typography,
  Box,
  Menu,
  MenuItem,
  Avatar,
  Divider,
  ListItemIcon,
} from '@mui/material';
import { Menu as MenuIcon, Logout, Person, AdminPanelSettings } from '@mui/icons-material';
import { useState } from 'react';
import { useAuth } from '../auth/useAuth';
import NotificationBadge from './NotificationBadge';
import { DRAWER_WIDTH } from './Sidebar';
import { ROLES } from '../types/api';

interface TopbarProps {
  onMenuToggle: () => void;
}

function getRoleLabel(role: string): string {
  switch (role) {
    case ROLES.ADMIN: return 'Admin';
    case ROLES.PHARMACIST: return 'Pharmacist';
    case ROLES.AUDITOR: return 'Auditor';
    default: return role.replace('ROLE_', '');
  }
}

export default function Topbar({ onMenuToggle }: TopbarProps) {
  const { user, logout } = useAuth();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);

  const handleLogout = async () => {
    setAnchorEl(null);
    await logout();
  };

  return (
    <AppBar
      position="fixed"
      elevation={0}
      sx={{
        width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
        ml: { md: `${DRAWER_WIDTH}px` },
        bgcolor: 'background.paper',
        color: 'text.primary',
        borderBottom: '1px solid',
        borderColor: 'divider',
      }}
    >
      <Toolbar>
        <IconButton
          edge="start"
          onClick={onMenuToggle}
          sx={{ mr: 2, display: { md: 'none' } }}
        >
          <MenuIcon />
        </IconButton>

        <Box sx={{ flexGrow: 1 }} />

        <NotificationBadge />

        <Box
          sx={{ display: 'flex', alignItems: 'center', ml: 2, cursor: 'pointer' }}
          onClick={(e) => setAnchorEl(e.currentTarget)}
        >
          <Avatar
            sx={{ width: 36, height: 36, bgcolor: 'primary.main', fontSize: 16, mr: 1.5 }}
          >
            {user?.fullName?.charAt(0) || user?.username?.charAt(0)?.toUpperCase() || '?'}
          </Avatar>
          <Box sx={{ display: { xs: 'none', sm: 'block' } }}>
            <Typography variant="body2" sx={{ fontWeight: 600, lineHeight: 1.2 }}>
              {user?.fullName || user?.username}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {user?.roles?.map(getRoleLabel).join(', ')}
            </Typography>
          </Box>
        </Box>

        <Menu
          anchorEl={anchorEl}
          open={open}
          onClose={() => setAnchorEl(null)}
          transformOrigin={{ horizontal: 'right', vertical: 'top' }}
          anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
        >
          <MenuItem disabled>
            <ListItemIcon><Person fontSize="small" /></ListItemIcon>
            {user?.username}
          </MenuItem>
          <MenuItem disabled>
            <ListItemIcon><AdminPanelSettings fontSize="small" /></ListItemIcon>
            {user?.roles?.map(getRoleLabel).join(', ')}
          </MenuItem>
          <Divider />
          <MenuItem onClick={handleLogout}>
            <ListItemIcon><Logout fontSize="small" color="error" /></ListItemIcon>
            <Typography color="error">Sign Out</Typography>
          </MenuItem>
        </Menu>
      </Toolbar>
    </AppBar>
  );
}
