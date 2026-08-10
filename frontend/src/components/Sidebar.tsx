import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Box,
  Typography,
  Divider,
} from '@mui/material';
import {
  Dashboard,
  MedicalServices,
  Inventory2,
  LocalPharmacy,
  Assessment,
  Security,
  LocalShipping,
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';
import { ROLES } from '../types/api';

export const DRAWER_WIDTH = 260;

interface NavItem {
  label: string;
  path: string;
  icon: React.ReactNode;
  roles: string[];  // Empty = all authenticated
}

const NAV_ITEMS: NavItem[] = [
  { label: 'Dashboard', path: '/', icon: <Dashboard />, roles: [] },
  { label: 'Medicines', path: '/medicines', icon: <MedicalServices />, roles: [] },
  { label: 'Suppliers', path: '/suppliers', icon: <LocalShipping />, roles: [ROLES.ADMIN, ROLES.PHARMACIST] },
  { label: 'Inventory', path: '/inventory', icon: <Inventory2 />, roles: [] },
  { label: 'Dispensing', path: '/dispensing', icon: <LocalPharmacy />, roles: [ROLES.ADMIN, ROLES.PHARMACIST] },
  { label: 'Reports', path: '/reports', icon: <Assessment />, roles: [] },
  { label: 'Audit Trail', path: '/audit', icon: <Security />, roles: [ROLES.ADMIN, ROLES.AUDITOR] },
];

interface SidebarProps {
  mobileOpen: boolean;
  onMobileClose: () => void;
}

export default function Sidebar({ mobileOpen, onMobileClose }: SidebarProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const { hasAnyRole } = useAuth();

  const filteredItems = NAV_ITEMS.filter((item) => {
    if (item.roles.length === 0) return true;  // All authenticated users
    return hasAnyRole(...item.roles);
  });

  const drawerContent = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Toolbar sx={{ px: 2 }}>
        <LocalPharmacy sx={{ color: 'primary.main', mr: 1.5, fontSize: 28 }} />
        <Box>
          <Typography variant="subtitle1" color="primary.main" noWrap sx={{ fontWeight: 700 }}>
            PharmaCare
          </Typography>
          <Typography variant="caption" color="text.secondary" noWrap>
            Dispensing System
          </Typography>
        </Box>
      </Toolbar>
      <Divider />
      <List sx={{ flex: 1, px: 1.5, py: 1 }}>
        {filteredItems.map((item) => {
          const isActive = location.pathname === item.path;
          return (
            <ListItem key={item.path} disablePadding sx={{ mb: 0.5 }}>
              <ListItemButton
                onClick={() => {
                  navigate(item.path);
                  onMobileClose();
                }}
                selected={isActive}
                sx={{
                  borderRadius: 2,
                  '&.Mui-selected': {
                    bgcolor: 'primary.main',
                    color: 'white',
                    '& .MuiListItemIcon-root': { color: 'white' },
                    '&:hover': { bgcolor: 'primary.dark' },
                  },
                }}
              >
                <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
                <ListItemText primary={item.label} />
              </ListItemButton>
            </ListItem>
          );
        })}
      </List>
    </Box>
  );

  return (
    <Box component="nav" sx={{ width: { md: DRAWER_WIDTH }, flexShrink: { md: 0 } }}>
      {/* Mobile drawer */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={onMobileClose}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': { width: DRAWER_WIDTH },
        }}
      >
        {drawerContent}
      </Drawer>
      {/* Desktop drawer */}
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          '& .MuiDrawer-paper': { width: DRAWER_WIDTH },
        }}
        open
      >
        {drawerContent}
      </Drawer>
    </Box>
  );
}
