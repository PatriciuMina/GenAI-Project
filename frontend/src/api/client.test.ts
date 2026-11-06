import { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { apiClient, setUnauthorizedHandler } from './client';
import { storeToken } from './tokenStorage';

/** Axios adapter that answers every request with the given status and records the request. */
function respondWith(status: number, seen: InternalAxiosRequestConfig[] = []) {
  return async (config: InternalAxiosRequestConfig) => {
    seen.push(config);
    const response = { status, statusText: '', headers: {}, config, data: {} };
    if (status >= 400) {
      throw new AxiosError('failed', 'ERR_BAD_REQUEST', config, null, response);
    }
    return response;
  };
}

describe('apiClient', () => {
  let unregister: (() => void) | undefined;
  afterEach(() => unregister?.());

  it('sends the stored token as a Bearer header', async () => {
    storeToken('abc.def.ghi');
    const seen: InternalAxiosRequestConfig[] = [];
    await apiClient.get('/organizations', { adapter: respondWith(200, seen) });
    expect(seen[0].headers.get('Authorization')).toBe('Bearer abc.def.ghi');
  });

  it('calls the unauthorized handler on a 401 for an authenticated request', async () => {
    storeToken('abc.def.ghi');
    const handler = vi.fn();
    unregister = setUnauthorizedHandler(handler);
    await expect(apiClient.get('/organizations', { adapter: respondWith(401) })).rejects.toThrow();
    expect(handler).toHaveBeenCalledTimes(1);
  });

  it('does not call the handler for a 401 without a token (wrong password on login)', async () => {
    const handler = vi.fn();
    unregister = setUnauthorizedHandler(handler);
    await expect(
      apiClient.post('/auth/login', {}, { adapter: respondWith(401) }),
    ).rejects.toThrow();
    expect(handler).not.toHaveBeenCalled();
  });
});
