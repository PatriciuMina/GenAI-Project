import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AxiosError, AxiosHeaders, type AxiosResponse } from 'axios';
import type { ReactNode } from 'react';
import type { ApiErrorBody } from '../api/types';

/** A fresh QueryClient without retries, so failing queries fail immediately in tests. */
export function createTestQueryClient(): QueryClient {
  return new QueryClient({ defaultOptions: { queries: { retry: false } } });
}

export function withQueryClient(children: ReactNode, client = createTestQueryClient()) {
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

/** Builds the AxiosError axios throws when the backend answers with an ApiError body. */
export function apiError(
  status: number,
  code: string,
  message: string,
  fieldErrors: ApiErrorBody['fieldErrors'] = [],
): AxiosError {
  const config = { headers: new AxiosHeaders() };
  const response: AxiosResponse<ApiErrorBody> = {
    status,
    statusText: '',
    headers: {},
    config,
    data: { timestamp: new Date().toISOString(), status, code, message, fieldErrors },
  };
  return new AxiosError(message, 'ERR_BAD_REQUEST', config, null, response);
}

/** An unsigned JWT-shaped token whose payload has the given `exp` (seconds since epoch). */
export function fakeJwt(expSeconds: number): string {
  const encode = (obj: object) =>
    btoa(JSON.stringify(obj)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'HS256' })}.${encode({ sub: '1', exp: expSeconds })}.signature`;
}
