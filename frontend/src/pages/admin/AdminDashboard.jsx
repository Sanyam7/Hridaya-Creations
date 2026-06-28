import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { adminApi } from "../../api";

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [recent, setRecent] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    (async () => {
      try {
        const [prod, cats, orders, pending, users, recentOrders] = await Promise.all([
          adminApi.products.list({ size: 1 }),
          adminApi.categories.list({ size: 1 }),
          adminApi.orders.list({ size: 1 }),
          adminApi.orders.list({ size: 1, status: "PENDING" }),
          adminApi.users.list({ size: 1 }),
          adminApi.orders.list({ size: 5, sortBy: "createdAt", sortDir: "desc" }),
        ]);
        setStats({
          products: prod.totalElements,
          categories: cats.totalElements,
          orders: orders.totalElements,
          pending: pending.totalElements,
          users: users.totalElements,
        });
        setRecent(recentOrders.content || []);
      } catch (e) { setError(e.message); }
    })();
  }, []);

  return (
    <>
      <div className="admin-head">
        <div>
          <h1 className="admin-title">Dashboard</h1>
          <p className="admin-sub">Overview of your store</p>
        </div>
        <Link to="/admin/products" className="ad-btn ad-btn--primary">Manage Products →</Link>
      </div>

      {error && <div className="admin-error">⚠ {error}</div>}

      <div className="admin-cards">
        <Stat label="Products" value={stats?.products} to="/admin/products" />
        <Stat label="Categories" value={stats?.categories} to="/admin/categories" />
        <Stat label="Orders" value={stats?.orders} to="/admin/orders" />
        <Stat label="Pending orders" value={stats?.pending} to="/admin/orders" />
        <Stat label="Users" value={stats?.users} to="/admin/users" />
      </div>

      <div className="admin-panel">
        <div className="admin-toolbar"><b style={{ fontSize: ".95rem" }}>Recent orders</b></div>
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead><tr><th>Order</th><th>Items</th><th>Total</th><th>Status</th></tr></thead>
            <tbody>
              {recent.length === 0 ? (
                <tr><td colSpan={4}><div className="admin-state">No orders yet.</div></td></tr>
              ) : recent.map(o => (
                <tr key={o.id}>
                  <td style={{ fontWeight: 600 }}>{o.orderNumber}</td>
                  <td>{o.totalItems}</td>
                  <td>₹{Number(o.totalAmount).toLocaleString()}</td>
                  <td><span className="badge badge--gray">{o.status}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
}

function Stat({ label, value, to }) {
  return (
    <Link to={to} className="stat-card" style={{ textDecoration: "none", color: "inherit", display: "block" }}>
      <div className="label">{label}</div>
      <div className="value">{value == null ? "…" : value.toLocaleString()}</div>
    </Link>
  );
}
