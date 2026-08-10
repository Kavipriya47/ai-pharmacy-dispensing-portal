import { useState } from 'react';
import type { FormEvent } from 'react';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Alert,
  InputAdornment,
  IconButton,
  CircularProgress,
} from '@mui/material';
import { Visibility, VisibilityOff, LocalPharmacy } from '@mui/icons-material';
import { useAuth } from '../auth/useAuth';
import { useNavigate, useLocation } from 'react-router-dom';
import { extractErrorMessage } from '../components/ErrorAlert';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = (location.state as { from?: { pathname: string } })?.from?.pathname || '/';

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      await login(username, password);
      navigate(from, { replace: true });
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #f5f7fa 0%, #e4ecf1 100%)',
        p: 2,
      }}
    >
      <Card sx={{ maxWidth: 440, width: '100%', p: 1 }}>
        <CardContent sx={{ p: 4 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 4 }}>
            <Box
              sx={{
                width: 56,
                height: 56,
                borderRadius: '50%',
                bgcolor: 'primary.main',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                mb: 2,
              }}
            >
              <LocalPharmacy sx={{ color: 'white', fontSize: 30 }} />
            </Box>
            <Typography variant="h5" color="text.primary" sx={{ fontWeight: 700 }}>
              PharmaCare
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              Medication Dispensing System
            </Typography>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit}>
            <TextField
              id="login-username"
              label="Username"
              fullWidth
              required
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              margin="normal"
              autoFocus
              autoComplete="username"
            />
            <TextField
              id="login-password"
              label="Password"
              fullWidth
              required
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              margin="normal"
              autoComplete="current-password"
              slotProps={{
                input: {
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        onClick={() => setShowPassword(!showPassword)}
                        edge="end"
                        aria-label="toggle password visibility"
                      >
                        {showPassword ? <VisibilityOff /> : <Visibility />}
                      </IconButton>
                    </InputAdornment>
                  ),
                },
              }}
            />
            <Button
              id="login-submit"
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={isSubmitting || !username || !password}
              sx={{ mt: 3, py: 1.5 }}
            >
              {isSubmitting ? <CircularProgress size={24} color="inherit" /> : 'Sign In'}
            </Button>
          </Box>

          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', textAlign: 'center', mt: 3 }}>
            Access token stored in memory · Refresh token in local storage
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
}
