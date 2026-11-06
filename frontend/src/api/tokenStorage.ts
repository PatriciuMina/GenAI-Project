// Where the JWT lives in the browser (decision D1: localStorage, survives reloads and new tabs).
// Everything that reads or writes the token goes through this file.

const TOKEN_KEY = 'ubb-events.accessToken';

export function getStoredToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null; // storage disabled (e.g. some private modes)
  }
}

export function storeToken(token: string): void {
  try {
    localStorage.setItem(TOKEN_KEY, token);
  } catch {
    // ignore: the user just stays logged in for this page load only
  }
}

export function clearStoredToken(): void {
  try {
    localStorage.removeItem(TOKEN_KEY);
  } catch {
    // ignore
  }
}

/**
 * Reads the `exp` claim of a JWT and returns it in milliseconds since epoch, or null if the token
 * cannot be decoded. Only used to log out on time; the backend still verifies the signature.
 */
export function getTokenExpiry(token: string): number | null {
  const parts = token.split('.');
  if (parts.length !== 3) return null;
  try {
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=');
    const payload: unknown = JSON.parse(atob(padded));
    if (typeof payload === 'object' && payload !== null && 'exp' in payload) {
      const exp = (payload as { exp: unknown }).exp;
      return typeof exp === 'number' ? exp * 1000 : null;
    }
    return null;
  } catch {
    return null;
  }
}

/** True when the token has no readable expiry or the expiry is in the past. */
export function isTokenExpired(token: string, now: number = Date.now()): boolean {
  const expiry = getTokenExpiry(token);
  return expiry === null || expiry <= now;
}
