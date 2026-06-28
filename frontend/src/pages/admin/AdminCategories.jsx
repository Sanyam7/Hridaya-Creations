import { useEffect, useState, useCallback } from "react";
import { adminApi } from "../../api";
import { Pager } from "./AdminProducts";

const PAGE_SIZE = 20;
const empty = { categoryName: "", description: "", imageUrl: "", status: "ACTIVE" };

export default function AdminCategories() {
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [data, setData] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [editing, setEditing] = useState(null);
  const [busyId, setBusyId] = useState(null);

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try {
      const d = await adminApi.categories.list({ keyword, page, size: PAGE_SIZE });
      setData(d);
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  }, [keyword, page]);

  useEffect(() => { load(); }, [load]);

  const del = async (c) => {
    if (!window.confirm(`Delete category "${c.categoryName}"? Products must be moved/removed first.`)) return;
    setBusyId(c.id); setError("");
    try { await adminApi.categories.remove(c.id); await load(); }
    catch (e) { setError(e.message); }
    finally { setBusyId(null); }
  };

  return (
    <>
      <div className="admin-head">
        <div>
          <h1 className="admin-title">Categories</h1>
          <p className="admin-sub">{data.totalElements} categor{data.totalElements !== 1 ? "ies" : "y"}</p>
        </div>
        <button className="ad-btn ad-btn--primary" onClick={() => setEditing({ ...empty })}>+ New Category</button>
      </div>

      {error && <div className="admin-error">⚠ {error}</div>}

      <div className="admin-panel">
        <form className="admin-toolbar" onSubmit={(e) => { e.preventDefault(); setPage(0); load(); }}>
          <input className="ad-input grow" placeholder="Search categories…" value={keyword} onChange={e => setKeyword(e.target.value)} />
          <button className="ad-btn" type="submit">Search</button>
        </form>

        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead><tr><th>Name</th><th>Description</th><th>Products</th><th>Status</th><th></th></tr></thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={5}><div className="admin-state">Loading…</div></td></tr>
              ) : data.content.length === 0 ? (
                <tr><td colSpan={5}><div className="admin-state">No categories yet.</div></td></tr>
              ) : data.content.map(c => (
                <tr key={c.id}>
                  <td style={{ fontWeight: 600 }}>{c.categoryName}</td>
                  <td style={{ color: "var(--ad-muted)", maxWidth: 320, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{c.description || "—"}</td>
                  <td>{c.productCount ?? 0}</td>
                  <td><span className={`badge ${c.status === "ACTIVE" ? "badge--green" : "badge--gray"}`}>{c.status}</span></td>
                  <td>
                    <div className="cell-actions">
                      <button className="ad-btn ad-btn--sm" disabled={busyId === c.id} onClick={() => setEditing({ id: c.id, categoryName: c.categoryName, description: c.description || "", imageUrl: c.imageUrl || "", status: c.status || "ACTIVE" })}>Edit</button>
                      <button className="ad-btn ad-btn--sm ad-btn--danger" disabled={busyId === c.id} onClick={() => del(c)}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <Pager page={page} totalPages={data.totalPages} onPage={setPage} />
      </div>

      {editing && (
        <CategoryForm initial={editing} onClose={() => setEditing(null)} onSaved={() => { setEditing(null); load(); }} />
      )}
    </>
  );
}

function CategoryForm({ initial, onClose, onSaved }) {
  const isEdit = !!initial.id;
  const [f, setF] = useState(initial);
  const [saving, setSaving] = useState(false);
  const [err, setErr] = useState("");
  const set = (k, v) => { setF(p => ({ ...p, [k]: v })); setErr(""); };

  const submit = async (e) => {
    e.preventDefault();
    if (!f.categoryName.trim()) return setErr("Category name is required.");
    const body = {
      categoryName: f.categoryName.trim(),
      description: f.description.trim() || undefined,
      imageUrl: f.imageUrl.trim() || undefined,
      status: f.status,
    };
    setSaving(true); setErr("");
    try {
      if (isEdit) await adminApi.categories.update(initial.id, body);
      else await adminApi.categories.create(body);
      onSaved();
    } catch (e2) { setErr(e2.message); setSaving(false); }
  };

  return (
    <div className="ad-modal-overlay" onClick={() => !saving && onClose()}>
      <form className="ad-modal" onClick={e => e.stopPropagation()} onSubmit={submit}>
        <h3>{isEdit ? "Edit category" : "New category"}</h3>
        {err && <div className="admin-error">⚠ {err}</div>}
        <div className="ad-form-grid">
          <div className="ad-field col-span">
            <label>Name *</label>
            <input className="ad-input" value={f.categoryName} onChange={e => set("categoryName", e.target.value)} />
          </div>
          <div className="ad-field col-span">
            <label>Description</label>
            <textarea className="ad-input" value={f.description} onChange={e => set("description", e.target.value)} />
          </div>
          <div className="ad-field col-span">
            <label>Image URL</label>
            <input className="ad-input" value={f.imageUrl} onChange={e => set("imageUrl", e.target.value)} placeholder="https://…" />
          </div>
          <div className="ad-field">
            <label>Status</label>
            <select className="ad-select" value={f.status} onChange={e => set("status", e.target.value)}>
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
            </select>
          </div>
        </div>
        <div className="ad-modal-actions">
          <button type="button" className="ad-btn" onClick={onClose} disabled={saving}>Cancel</button>
          <button type="submit" className="ad-btn ad-btn--primary" disabled={saving}>{saving ? "Saving…" : isEdit ? "Save changes" : "Create"}</button>
        </div>
      </form>
    </div>
  );
}
