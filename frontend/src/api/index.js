// =====================================================================
// Typed-ish service functions grouped by domain. Each maps directly to a
// backend REST endpoint (see Swagger at <backend>/swagger-ui.html).
// =====================================================================
import { request } from "./client";

export { ApiError, API_BASE_URL, tokenStore } from "./client";

export const authApi = {
  register: (payload) =>
    request("POST", "/auth/register", { body: payload }),
  login: (payload) => request("POST", "/auth/login", { body: payload }),
  me: () => request("GET", "/users/me", { auth: true }),
  logout: () => request("POST", "/auth/logout", { auth: true }),
};

export const productApi = {
  list: (params = {}) => {
    const qs = new URLSearchParams(params).toString();
    return request("GET", `/products${qs ? `?${qs}` : ""}`);
  },
  get: (id) => request("GET", `/products/${id}`),
  categories: () => request("GET", "/categories"),
};

export const cartApi = {
  get: () => request("GET", "/cart", { auth: true }),
  addItem: (productId, quantity) =>
    request("POST", "/cart/items", { auth: true, body: { productId, quantity } }),
  updateItem: (itemId, quantity) =>
    request("PUT", `/cart/items/${itemId}`, { auth: true, body: { quantity } }),
  removeItem: (itemId) =>
    request("DELETE", `/cart/items/${itemId}`, { auth: true }),
  clear: () => request("DELETE", "/cart", { auth: true }),
};

export const addressApi = {
  list: () => request("GET", "/addresses", { auth: true }),
  create: (payload) =>
    request("POST", "/addresses", { auth: true, body: payload }),
};

export const orderApi = {
  list: (params = {}) => {
    const qs = new URLSearchParams(params).toString();
    return request("GET", `/orders${qs ? `?${qs}` : ""}`, { auth: true });
  },
  create: (payload) => request("POST", "/orders", { auth: true, body: payload }),
};

/** Map a backend UserResponse to the shape the UI expects. */
export function mapUser(u) {
  if (!u) return null;
  const name = u.fullName || [u.firstName, u.lastName].filter(Boolean).join(" ");
  const roles = u.roles || [];
  return {
    id: u.id,
    name: name || u.email,
    firstName: u.firstName,
    email: u.email,
    mobileNumber: u.mobileNumber,
    avatar: (u.firstName || u.email || "U").charAt(0).toUpperCase(),
    roles,
    isAdmin: roles.includes("ROLE_ADMIN"),
  };
}
