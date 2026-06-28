import { useEffect, useState, useCallback } from "react";
import { adminApi } from "../../api";
import { Pager } from "./AdminProducts";

const PAGE_SIZE = 10;

export default function AdminUsers() {
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [data, setData] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try {
      const d = await adminApi.users.list({ keyword, page, size: PAGE_SIZE, sortBy: "createdAt", sortDir: "desc" });
      setData(d);
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  }, [keyword, page]);

  useEffect(() => { load(); }, [load]);

  const toggle = async (u) => {
    setBusyId(u.id); setError("");
    try { await adminApi.users.setStatus(u.id, !u.enabled); await load(); }
    catch (e) { setError(e.message); }
    finally { setBusyId(null); }
  };

  return (
    <>
      <div className="admin-head">
        <div>
          <h1 className="admin-title">Users</h1>
          <p className="admin-sub">{data.totalElements} registered user{data.totalElements !== 1 ? "s" : ""}</p>
        </div>
      </div>

      {error && <div className="admin-error">⚠ {error}</div>}

      <div className="admin-panel">
        <form className="admin-toolbar" onSubmit={(e) => { e.preventDefault(); setPage(0); load(); }}>
          <input className="ad-input grow" placeholder="Search by name / email…" value={keyword} onChange={e => setKeyword(e.target.value)} />
          <button className="ad-btn" type="submit">Search</button>
        </form>

        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead><tr><th>Name</th><th>Email</th><th>Mobile</th><th>Roles</th><th>Status</th><th></th></tr></thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={6}><div className="admin-state">Loading…</div></td></tr>
              ) : data.content.length === 0 ? (
                <tr><td colSpan={6}><div className="admin-state">No users found.</div></td></tr>
              ) : data.content.map(u => {
                const isAdmin = (u.roles || []).includes("ROLE_ADMIN");
                return (
                  <tr key={u.id}>
                    <td style={{ fontWeight: 600 }}>{u.fullName || `${u.firstName} ${u.lastName}`}</td>
                    <td>{u.email}</td>
                    <td>{u.mobileNumber || "—"}</td>
                    <td>
                      {(u.roles || []).map(r => (
                        <span key={r} className={`badge ${r === "ROLE_ADMIN" ? "badge--purple" : "badge--gray"}`} style={{ marginRight: 4 }}>
                          {r.replace("ROLE_", "")}
                        </span>
                      ))}
                    </td>
                    <td><span className={`badge ${u.enabled ? "badge--green" : "badge--red"}`}>{u.enabled ? "Active" : "Disabled"}</span></td>
                    <td>
                      <div className="cell-actions">
                        <button
                          className={`ad-btn ad-btn--sm ${u.enabled ? "ad-btn--danger" : ""}`}
                          disabled={busyId === u.id || isAdmin}
                          title={isAdmin ? "Admin accounts can't be disabled here" : ""}
                          onClick={() => toggle(u)}
                        >
                          {u.enabled ? "Disable" : "Enable"}
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
        <Pager page={page} totalPages={data.totalPages} onPage={setPage} />
      </div>
    </>
  );
}
