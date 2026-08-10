import { useState } from 'react';
import { Box, Toolbar } from '@mui/material';
import { Outlet } from 'react-router-dom';
import Sidebar, { DRAWER_WIDTH } from './Sidebar';
import Topbar from './Topbar';

export default function AppShell() {
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      <Topbar onMenuToggle={() => setMobileOpen(!mobileOpen)} />
      <Sidebar mobileOpen={mobileOpen} onMobileClose={() => setMobileOpen(false)} />
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
          p: 3,
        }}
      >
        <Toolbar /> {/* Spacer for fixed AppBar */}
        <Outlet />
      </Box>
    </Box>
  );
}
