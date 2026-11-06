/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Base URL of the REST API, e.g. "/api/v1" (dev, via Vite proxy) or "https://api.example.org/api/v1". */
  readonly VITE_API_BASE_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
