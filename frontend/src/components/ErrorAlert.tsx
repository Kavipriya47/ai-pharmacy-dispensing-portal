import { Alert, AlertTitle } from '@mui/material';
import { AxiosError } from 'axios';
import type { ProblemDetail } from '../types/api';

interface ErrorAlertProps {
  error: unknown;
  title?: string;
}

export function extractErrorMessage(error: unknown): string {
  if (error instanceof AxiosError && error.response?.data) {
    const pd = error.response.data as ProblemDetail;
    if (pd.detail) return pd.detail;
    if (pd.title) return pd.title;
  }
  if (error instanceof Error) return error.message;
  return 'An unexpected error occurred';
}

export default function ErrorAlert({ error, title = 'Error' }: ErrorAlertProps) {
  return (
    <Alert severity="error" sx={{ my: 2 }}>
      <AlertTitle>{title}</AlertTitle>
      {extractErrorMessage(error)}
    </Alert>
  );
}
