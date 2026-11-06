import { QueryClient } from '@tanstack/react-query';
import { errorStatus } from './errors';

export function createQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 30_000,
        // Retry network/5xx errors once; never retry 4xx (404, 403, 401 ... will not change).
        retry: (failureCount, error) => {
          const status = errorStatus(error);
          if (status !== undefined && status < 500) return false;
          return failureCount < 1;
        },
        refetchOnWindowFocus: false,
      },
    },
  });
}
