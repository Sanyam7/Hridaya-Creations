import { useEffect, useState, useCallback } from "react";
import { adminApi, PRODUCT_STATUSES } from "../../api";

const PAGE_SIZE = 10;
const emptyForm = {
  name: "", categoryId: "", sellingPrice: "", originalPrice: "",
  stockQuantity: "", shortDescription: "", description: "", sku: "",
  productStatus: "ACTIVE", customizable: true, featured: false, tags: "",
};

export default function AdminProducts() {
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [data, setData] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [cats, setCats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [editing, setEditing] = useState(null); // null=closed, {}=new, {..}=edit
  const [busyId, setBusyId] = useState(null);

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try {
      const d = await adminApi.products.list({
        keyword, status, categoryId, page, size: PAGE_SIZE, sortBy: "createdAt", sortDir: "desc",
      });
      setData(d);
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  }, [keyword, status, categoryId, page]);

  useEffect(() => { load(); }, [load]);
  useEffect(() => {
    adminApi.categories.list({ size: 200 }).then(d => setCats(d.content || [])).catch(() => {});
  }, []);

  const refresh = () => load();

  const act = async (id, fn) => {
    setBusyId(id); setError("");
    try { await fn(); await load(); }
    catch (e) { setError(e.message); }
    finally { setBusyId(null); }
  };

  const onSearch = (e) => { e.preventDefault(); setPage(0); load(); };

  const restock = (p) => {
    const v = window.prompt(`Set stock for "${p.name}"`, p.stockQuantity ?? 0);
    if (v === null) return;
    const n = parseInt(v, 10);
    if (Number.isNaN(n) || n < 0) { setError("Stock must be a non-negative number."); return; }
    act(p.id, () => adminApi.products.stock(p.id, n));
  };

  const del = (p) => {
    if (!window.confirm(`Delete "${p.name}"? This cannot be undone.`)) return;
    act(p.id, () => adminApi.products.remove(p.id));
  };

  return (
    <>
      <div className="admin-head">
        <div>
          <h1 className="admin-title">Products</h1>
          <p className="admin-sub">{data.totalElements} product{data.totalElements !== 1 ? "s" : ""} in the catalog</p>
        </div>
        <button className="ad-btn ad-btn--primary" onClick={() => setEditing({ ...emptyForm })}>+ New Product</button>
      </div>

      {error && <div className="admin-error">⚠ {error}</div>}

      <div className="admin-panel">
        <form className="admin-toolbar" onSubmit={onSearch}>
          <input className="ad-input grow" placeholder="Search by name / SKU…" value={keyword}
            onChange={e => setKeyword(e.target.value)} />
          <select className="ad-select" value={categoryId} onChange={e => { setCategoryId(e.target.value); setPage(0); }}>
            <option value="">All categories</option>
            {cats.map(c => <option key={c.id} value={c.id}>{c.categoryName}</option>)}
          </select>
          <select className="ad-select" value={status} onChange={e => { setStatus(e.target.value); setPage(0); }}>
            <option value="">All statuses</option>
            {PRODUCT_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
          </select>
          <button className="ad-btn" type="submit">Search</button>
        </form>

        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Product</th><th>Category</th><th>Price</th><th>Stock</th><th>Status</th><th></th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={6}><div className="admin-state">Loading…</div></td></tr>
              ) : data.content.length === 0 ? (
                <tr><td colSpan={6}><div className="admin-state">No products found.</div></td></tr>
              ) : data.content.map(p => (
                <tr key={p.id}>
                  <td>
                    <div style={{ fontWeight: 600 }}>{p.name}</div>
                    <div style={{ color: "var(--ad-muted)", fontSize: ".78rem" }}>{p.sku}</div>
                  </td>
                  <td>{p.categoryName || "—"}</td>
                  <td>
                    ₹{Number(p.sellingPrice).toLocaleString()}
                    {p.originalPrice ? <span style={{ color: "var(--ad-muted)", textDecoration: "line-through", marginLeft: 6, fontSize: ".78rem" }}>₹{Number(p.originalPrice).toLocaleString()}</span> : null}
                  </td>
                  <td>{p.stockQuantity}</td>
                  <td><StatusBadge status={p.productStatus} /></td>
                  <td>
                    <div className="cell-actions">
                      <button className="ad-btn ad-btn--sm" disabled={busyId === p.id} onClick={() => setEditing(toForm(p))}>Edit</button>
                      <button className="ad-btn ad-btn--sm" disabled={busyId === p.id} onClick={() => restock(p)}>Stock</button>
                      {p.productStatus === "ACTIVE" ? (
                        <button className="ad-btn ad-btn--sm" disabled={busyId === p.id} onClick={() => act(p.id, () => adminApi.products.disable(p.id))}>Disable</button>
                      ) : (
                        <button className="ad-btn ad-btn--sm" disabled={busyId === p.id} onClick={() => act(p.id, () => adminApi.products.enable(p.id))}>Enable</button>
                      )}
                      <button className="ad-btn ad-btn--sm ad-btn--danger" disabled={busyId === p.id} onClick={() => del(p)}>Delete</button>
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
        <ProductForm
          initial={editing}
          cats={cats}
          onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); refresh(); }}
        />
      )}
    </>
  );
}

function toForm(p) {
  return {
    id: p.id,
    name: p.name || "",
    categoryId: p.categoryId || "",
    sellingPrice: p.sellingPrice ?? "",
    originalPrice: p.originalPrice ?? "",
    stockQuantity: p.stockQuantity ?? "",
    shortDescription: p.shortDescription || "",
    description: p.description || "",
    sku: p.sku || "",
    productStatus: p.productStatus || "ACTIVE",
    customizable: !!p.customizable,
    featured: !!p.featured,
    tags: (p.tags || []).join(", "),
  };
}

function ProductForm({ initial, cats, onClose, onSaved }) {
  const isEdit = !!initial.id;
  const [f, setF] = useState(initial);
  const [saving, setSaving] = useState(false);
  const [err, setErr] = useState("");
  const set = (k, v) => { setF(p => ({ ...p, [k]: v })); setErr(""); };

  const submit = async (e) => {
    e.preventDefault();
    if (!f.name.trim()) return setErr("Name is required.");
    if (!f.categoryId) return setErr("Please choose a category.");
    if (!(Number(f.sellingPrice) > 0)) return setErr("Selling price must be greater than 0.");
    if (f.stockQuantity === "" || Number(f.stockQuantity) < 0) return setErr("Stock quantity is required.");

    const body = {
      name: f.name.trim(),
      description: f.description.trim() || undefined,
      shortDescription: f.shortDescription.trim() || undefined,
      categoryId: Number(f.categoryId),
      sellingPrice: Number(f.sellingPrice),
      originalPrice: f.originalPrice === "" ? undefined : Number(f.originalPrice),
      stockQuantity: Number(f.stockQuantity),
      productStatus: f.productStatus,
      featured: f.featured,
      customizable: f.customizable,
      tags: f.tags ? f.tags.split(",").map(t => t.trim()).filter(Boolean) : undefined,
    };
    if (!isEdit && f.sku.trim()) body.sku = f.sku.trim();

    setSaving(true); setErr("");
    try {
      if (isEdit) await adminApi.products.update(initial.id, body);
      else await adminApi.products.create(body);
      onSaved();
    } catch (e2) { setErr(e2.message); setSaving(false); }
  };

  return (
    <div className="ad-modal-overlay" onClick={() => !saving && onClose()}>
      <form className="ad-modal" onClick={e => e.stopPropagation()} onSubmit={submit}>
        <h3>{isEdit ? "Edit product" : "New product"}</h3>
        {err && <div className="admin-error">⚠ {err}</div>}
        <div className="ad-form-grid">
          <div className="ad-field col-span">
            <label>Name *</label>
            <input className="ad-input" value={f.name} onChange={e => set("name", e.target.value)} />
          </div>
          <div className="ad-field">
            <label>Category *</label>
            <select className="ad-select" value={f.categoryId} onChange={e => set("categoryId", e.target.value)}>
              <option value="">Select…</option>
              {cats.map(c => <option key={c.id} value={c.id}>{c.categoryName}</option>)}
            </select>
          </div>
          <div className="ad-field">
            <label>Status</label>
            <select className="ad-select" value={f.productStatus} onChange={e => set("productStatus", e.target.value)}>
              {PRODUCT_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
          <div className="ad-field">
            <label>Selling price (₹) *</label>
            <input className="ad-input" type="number" min="0" step="0.01" value={f.sellingPrice} onChange={e => set("sellingPrice", e.target.value)} />
          </div>
          <div className="ad-field">
            <label>Original price (₹)</label>
            <input className="ad-input" type="number" min="0" step="0.01" value={f.originalPrice} onChange={e => set("originalPrice", e.target.value)} />
          </div>
          <div className="ad-field">
            <label>Stock quantity *</label>
            <input className="ad-input" type="number" min="0" value={f.stockQuantity} onChange={e => set("stockQuantity", e.target.value)} />
          </div>
          <div className="ad-field">
            <label>SKU {isEdit ? "(read-only)" : "(optional)"}</label>
            <input className="ad-input" value={f.sku} disabled={isEdit} onChange={e => set("sku", e.target.value)} placeholder="auto-generated" />
          </div>
          <div className="ad-field col-span">
            <label>Short description</label>
            <input className="ad-input" value={f.shortDescription} onChange={e => set("shortDescription", e.target.value)} />
          </div>
          <div className="ad-field col-span">
            <label>Description</label>
            <textarea className="ad-input" value={f.description} onChange={e => set("description", e.target.value)} />
          </div>
          <div className="ad-field col-span">
            <label>Tags (comma-separated)</label>
            <input className="ad-input" value={f.tags} onChange={e => set("tags", e.target.value)} placeholder="mug, photo, birthday" />
          </div>
          <label className="ad-check"><input type="checkbox" checked={f.customizable} onChange={e => set("customizable", e.target.checked)} /> Customizable</label>
          <label className="ad-check"><input type="checkbox" checked={f.featured} onChange={e => set("featured", e.target.checked)} /> Featured</label>
        </div>
        <div className="ad-modal-actions">
          <button type="button" className="ad-btn" onClick={onClose} disabled={saving}>Cancel</button>
          <button type="submit" className="ad-btn ad-btn--primary" disabled={saving}>
            {saving ? "Saving…" : isEdit ? "Save changes" : "Create product"}
          </button>
        </div>
      </form>
    </div>
  );
}

export function StatusBadge({ status }) {
  const map = {
    ACTIVE: "badge--green", INACTIVE: "badge--gray",
    OUT_OF_STOCK: "badge--amber", DISCONTINUED: "badge--red",
  };
  return <span className={`badge ${map[status] || "badge--gray"}`}>{status}</span>;
}

export function Pager({ page, totalPages, onPage }) {
  if (totalPages <= 1) return null;
  return (
    <div className="admin-pager">
      <span>Page {page + 1} of {totalPages}</span>
      <div style={{ display: "flex", gap: ".5rem" }}>
        <button className="ad-btn ad-btn--sm" disabled={page <= 0} onClick={() => onPage(page - 1)}>← Prev</button>
        <button className="ad-btn ad-btn--sm" disabled={page >= totalPages - 1} onClick={() => onPage(page + 1)}>Next →</button>
      </div>
    </div>
  );
}
