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

/** Drop empty/null/undefined params, then build a query string. */
function query(params = {}) {
  const clean = {};
  for (const [k, v] of Object.entries(params)) {
    if (v !== undefined && v !== null && v !== "") clean[k] = v;
  }
  const qs = new URLSearchParams(clean).toString();
  return qs ? `?${qs}` : "";
}

// ---- Admin (ROLE_ADMIN only) ----
export const adminApi = {
  products: {
    list: (params) => request("GET", `/admin/products${query(params)}`, { auth: true }),
    get: (id) => request("GET", `/admin/products/${id}`, { auth: true }),
    create: (body) => request("POST", "/admin/products", { auth: true, body }),
    update: (id, body) => request("PUT", `/admin/products/${id}`, { auth: true, body }),
    remove: (id) => request("DELETE", `/admin/products/${id}`, { auth: true }),
    enable: (id) => request("PATCH", `/admin/products/${id}/enable`, { auth: true }),
    disable: (id) => request("PATCH", `/admin/products/${id}/disable`, { auth: true }),
    pricing: (id, body) => request("PATCH", `/admin/products/${id}/pricing`, { auth: true, body }),
    stock: (id, stockQuantity) =>
      request("PATCH", `/admin/products/${id}/stock`, { auth: true, body: { stockQuantity } }),
  },
  categories: {
    list: (params) => request("GET", `/admin/categories${query(params)}`, { auth: true }),
    create: (body) => request("POST", "/admin/categories", { auth: true, body }),
    update: (id, body) => request("PUT", `/admin/categories/${id}`, { auth: true, body }),
    remove: (id) => request("DELETE", `/admin/categories/${id}`, { auth: true }),
  },
  orders: {
    list: (params) => request("GET", `/admin/orders${query(params)}`, { auth: true }),
    get: (id) => request("GET", `/admin/orders/${id}`, { auth: true }),
    setStatus: (id, status) =>
      request("PATCH", `/admin/orders/${id}/status`, { auth: true, body: { status } }),
  },
  users: {
    list: (params) => request("GET", `/admin/users${query(params)}`, { auth: true }),
    setStatus: (id, enabled) =>
      request("PATCH", `/admin/users/${id}/status`, { auth: true, body: { enabled } }),
  },
};

// Allowed forward order-status transitions (mirrors the backend state machine).
export const ORDER_STATUS_TRANSITIONS = {
  PENDING: ["CONFIRMED", "CANCELLED"],
  CONFIRMED: ["PROCESSING", "CANCELLED"],
  PROCESSING: ["SHIPPED", "CANCELLED"],
  SHIPPED: ["DELIVERED"],
  DELIVERED: [],
  CANCELLED: [],
};

export const PRODUCT_STATUSES = ["ACTIVE", "INACTIVE", "OUT_OF_STOCK", "DISCONTINUED"];
export const ORDER_STATUSES = ["PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"];

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
