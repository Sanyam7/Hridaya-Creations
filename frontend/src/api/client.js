// =====================================================================
// Lightweight API client for the Hridaya Creations backend.
//
// Base URL resolves from REACT_APP_API_URL (set in Netlify env) and falls
// back to the live Render backend so the app works out of the box.
// Wraps fetch, attaches the JWT, and unwraps the standard ApiResponse
// envelope: { success, message, data, timestamp, errors }.
// =====================================================================

const DEFAULT_BASE = "https://hridaya-creations-backend.onrender.com/api/v1";

export const API_BASE_URL = (
  process.env.REACT_APP_API_URL || DEFAULT_BASE
).replace(/\/+$/, "");

const ACCESS_KEY = "hc_access_token";
const REFRESH_KEY = "hc_refresh_token";

export const tokenStore = {
  get access() {
    return localStorage.getItem(ACCESS_KEY);
  },
  get refresh() {
    return localStorage.getItem(REFRESH_KEY);
  },
  set({ accessToken, refreshToken }) {
    if (accessToken) localStorage.setItem(ACCESS_KEY, accessToken);
    if (refreshToken) localStorage.setItem(REFRESH_KEY, refreshToken);
  },
  clear() {
    localStorage.removeItem(ACCESS_KEY);
    localStorage.removeItem(REFRESH_KEY);
  },
};

/** Error carrying the backend message + field errors and HTTP status. */
export class ApiError extends Error {
  constructor(message, status, errors) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.errors = errors || [];
  }
}

/**
 * Perform a request.
 * @param {string} method  HTTP verb
 * @param {string} path    path beginning with "/", relative to API base
 * @param {object} [opts]  { body, auth }
 */
export async function request(method, path, { body, auth = false } = {}) {
  const headers = { "Content-Type": "application/json" };
  if (auth && tokenStore.access) {
    headers.Authorization = `Bearer ${tokenStore.access}`;
  }

  let res;
  try {
    res = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch (e) {
    throw new ApiError(
      "Cannot reach the server. Please check your connection and try again.",
      0
    );
  }

  // 204 / empty body
  const text = await res.text();
  const payload = text ? safeParse(text) : {};

  if (res.status === 401 && auth) {
    // Token invalid/expired — drop it so the app falls back to logged-out state.
    tokenStore.clear();
  }

  if (!res.ok || payload.success === false) {
    const msg =
      payload.message ||
      (res.status === 0 ? "Network error" : `Request failed (${res.status})`);
    throw new ApiError(msg, res.status, payload.errors);
  }

  // Unwrap ApiResponse envelope when present, else return raw payload.
  return payload && "data" in payload ? payload.data : payload;
}

function safeParse(text) {
  try {
    return JSON.parse(text);
  } catch {
    return {};
  }
}
