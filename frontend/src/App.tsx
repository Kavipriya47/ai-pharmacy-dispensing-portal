import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import theme from './theme/theme';
import { AuthProvider } from './auth/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import RoleRoute from './components/RoleRoute';
import AppShell from './components/AppShell';
import LoginPage from './pages/LoginPage';
import ForbiddenPage from './pages/ForbiddenPage';
import DashboardPage from './pages/DashboardPage';
import MedicinesPage from './pages/MedicinesPage';
import SuppliersPage from './pages/SuppliersPage';
import InventoryPage from './pages/InventoryPage';
import DispensingPage from './pages/DispensingPage';
import ReportsPage from './pages/ReportsPage';
import AuditPage from './pages/AuditPage';
import { ROLES } from './types/api';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <AuthProvider>
          <BrowserRouter>
            <Routes>
              {/* Public route */}
              <Route path="/login" element={<LoginPage />} />

              {/* Forbidden route */}
              <Route path="/forbidden" element={<ForbiddenPage />} />

              {/* Protected routes inside AppShell */}
              <Route
                path="/"
                element={
                  <ProtectedRoute>
                    <AppShell />
                  </ProtectedRoute>
                }
              >
                <Route index element={<DashboardPage />} />
                <Route path="medicines" element={<MedicinesPage />} />
                <Route
                  path="suppliers"
                  element={
                    <RoleRoute roles={[ROLES.ADMIN, ROLES.PHARMACIST]}>
                      <SuppliersPage />
                    </RoleRoute>
                  }
                />
                <Route path="inventory" element={<InventoryPage />} />
                <Route
                  path="dispensing"
                  element={
                    <RoleRoute roles={[ROLES.ADMIN, ROLES.PHARMACIST]}>
                      <DispensingPage />
                    </RoleRoute>
                  }
                />
                <Route path="reports" element={<ReportsPage />} />
                <Route
                  path="audit"
                  element={
                    <RoleRoute roles={[ROLES.ADMIN, ROLES.AUDITOR]}>
                      <AuditPage />
                    </RoleRoute>
                  }
                />
              </Route>

              {/* Catch-all redirect */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </BrowserRouter>
        </AuthProvider>
      </ThemeProvider>
    </QueryClientProvider>
  );
}
