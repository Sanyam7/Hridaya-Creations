import { NavLink, Outlet, Navigate, Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import "./admin.css";

const LINKS = [
  { to: "/admin", label: "Dashboard", icon: "📊", end: true },
  { to: "/admin/products", label: "Products", icon: "📦" },
  { to: "/admin/categories", label: "Categories", icon: "🗂️" },
  { to: "/admin/orders", label: "Orders", icon: "🧾" },
  { to: "/admin/users", label: "Users", icon: "👥" },
];

export default function AdminLayout() {
  const { currentUser, loading, logout } = useAuth();
  const navigate = useNavigate();

  if (loading) {
    return <div className="admin-state" style={{ padding: "20vh 1rem" }}>Loading…</div>;
  }
  // Gate the whole area to administrators.
  if (!currentUser) return <Navigate to="/login" replace />;
  if (!currentUser.isAdmin) return <Navigate to="/" replace />;

  const handleLogout = () => { logout(); navigate("/"); };

  return (
    <div className="admin-shell">
      <aside className="admin-side">
        <Link to="/admin" className="admin-brand">Hridaya Admin</Link>
        <nav className="admin-nav">
          {LINKS.map((l) => (
            <NavLink key={l.to} to={l.to} end={l.end}
              className={({ isActive }) => (isActive ? "active" : "")}>
              <span>{l.icon}</span> {l.label}
            </NavLink>
          ))}
        </nav>
        <div className="admin-side-foot">
          <Link to="/">← Back to store</Link>
          <button onClick={handleLogout}>Logout ({currentUser.firstName})</button>
        </div>
      </aside>
      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  );
}
