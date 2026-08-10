import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';

interface RoleRouteProps {
  roles: string[];
  children: React.ReactNode;
}

export default function RoleRoute({ roles, children }: RoleRouteProps) {
  const { hasAnyRole } = useAuth();

  if (!hasAnyRole(...roles)) {
    return <Navigate to="/forbidden" replace />;
  }

  return <>{children}</>;
}
