// Turns any thrown value (usually an AxiosError carrying the backend ApiError JSON) into something
// a page can show: one readable message plus per-field messages for forms.
import axios from 'axios';
import type { ApiErrorBody } from './types';

export interface ParsedApiError {
  /** HTTP status, or undefined for network errors. */
  status?: number;
  /** Backend error code, e.g. "VALIDATION_FAILED", "NOT_FOUND", "CONFLICT". */
  code?: string;
  /** Human-readable message, safe to show to the user. */
  message: string;
  /** Field name -> message(s), e.g. { password: "size must be between 8 and 72" }. */
  fieldErrors: Record<string, string>;
}

function isApiErrorBody(data: unknown): data is ApiErrorBody {
  return (
    typeof data === 'object' &&
    data !== null &&
    typeof (data as { message?: unknown }).message === 'string' &&
    typeof (data as { status?: unknown }).status === 'number'
  );
}

export function parseApiError(error: unknown): ParsedApiError {
  if (axios.isAxiosError(error)) {
    if (!error.response) {
      return {
        message: 'Cannot reach the server. Check your connection or try again later.',
        fieldErrors: {},
      };
    }
    const { status, data } = error.response;
    if (isApiErrorBody(data)) {
      const fieldErrors: Record<string, string> = {};
      for (const fe of data.fieldErrors ?? []) {
        fieldErrors[fe.field] = fieldErrors[fe.field]
          ? `${fieldErrors[fe.field]}; ${fe.message}`
          : fe.message;
      }
      const message =
        data.code === 'VALIDATION_FAILED' && Object.keys(fieldErrors).length > 0
          ? 'Please correct the highlighted fields.'
          : data.message;
      return { status, code: data.code, message, fieldErrors };
    }
    return { status, message: `Request failed (HTTP ${status}).`, fieldErrors: {} };
  }
  if (error instanceof Error) {
    return { message: error.message, fieldErrors: {} };
  }
  return { message: 'Something went wrong.', fieldErrors: {} };
}

/** HTTP status of an error, if it came from the backend. */
export function errorStatus(error: unknown): number | undefined {
  return axios.isAxiosError(error) ? error.response?.status : undefined;
}
