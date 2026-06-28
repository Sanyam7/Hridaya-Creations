import { useEffect, useState, useCallback } from "react";
import { adminApi, ORDER_STATUSES, ORDER_STATUS_TRANSITIONS } from "../../api";
import { Pager } from "./AdminProducts";

const PAGE_SIZE = 10;
const STATUS_BADGE = {
  PENDING: "badge--amber", CONFIRMED: "badge--blue", PROCESSING: "badge--purple",
  SHIPPED: "badge--blue", DELIVERED: "badge--green", CANCELLED: "badge--red",
};

export default function AdminOrders() {
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState("");
  const [data, setData] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);
  const [detail, setDetail] = useState(null);

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try {
      const d = await adminApi.orders.list({ status, page, size: PAGE_SIZE, sortBy: "createdAt", sortDir: "desc" });
      setData(d);
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  }, [status, page]);

  useEffect(() => { load(); }, [load]);

  const changeStatus = async (o, next) => {
    if (!next || next === o.status) return;
    setBusyId(o.id); setError("");
    try { await adminApi.orders.setStatus(o.id, next); await load(); }
    catch (e) { setError(e.message); }
    finally { setBusyId(null); }
  };

  return (
    <>
      <div className="admin-head">
        <div>
          <h1 className="admin-title">Orders</h1>
          <p className="admin-sub">{data.totalElements} order{data.totalElements !== 1 ? "s" : ""}</p>
        </div>
        <select className="ad-select" value={status} onChange={e => { setStatus(e.target.value); setPage(0); }}>
          <option value="">All statuses</option>
          {ORDER_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>

      {error && <div className="admin-error">⚠ {error}</div>}

      <div className="admin-panel">
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr><th>Order</th><th>Items</th><th>Total</th><th>Payment</th><th>Status</th><th>Update</th><th></th></tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={7}><div className="admin-state">Loading…</div></td></tr>
              ) : data.content.length === 0 ? (
                <tr><td colSpan={7}><div className="admin-state">No orders found.</div></td></tr>
              ) : data.content.map(o => {
                const next = ORDER_STATUS_TRANSITIONS[o.status] || [];
                return (
                  <tr key={o.id}>
                    <td>
                      <div style={{ fontWeight: 600 }}>{o.orderNumber}</div>
                      <div style={{ color: "var(--ad-muted)", fontSize: ".78rem" }}>{fmtDate(o.placedAt || o.createdAt)}</div>
                    </td>
                    <td>{o.totalItems}</td>
                    <td>₹{Number(o.totalAmount).toLocaleString()}</td>
                    <td>
                      <div style={{ fontSize: ".8rem" }}>{(o.paymentMethod || "").replace(/_/g, " ")}</div>
                      <div style={{ color: "var(--ad-muted)", fontSize: ".74rem" }}>{o.paymentStatus}</div>
                    </td>
                    <td><span className={`badge ${STATUS_BADGE[o.status] || "badge--gray"}`}>{o.status}</span></td>
                    <td>
                      {next.length === 0 ? (
                        <span style={{ color: "var(--ad-muted)", fontSize: ".8rem" }}>—</span>
                      ) : (
                        <select className="ad-select" disabled={busyId === o.id} value="" onChange={e => changeStatus(o, e.target.value)}>
                          <option value="">Move to…</option>
                          {next.map(s => <option key={s} value={s}>{s}</option>)}
                        </select>
                      )}
                    </td>
                    <td><button className="ad-btn ad-btn--sm" onClick={() => setDetail(o)}>View</button></td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
        <Pager page={page} totalPages={data.totalPages} onPage={setPage} />
      </div>

      {detail && <OrderDetail order={detail} onClose={() => setDetail(null)} />}
    </>
  );
}

function OrderDetail({ order, onClose }) {
  const a = order.shippingAddress || {};
  return (
    <div className="ad-modal-overlay" onClick={onClose}>
      <div className="ad-modal" onClick={e => e.stopPropagation()}>
        <h3>{order.orderNumber}</h3>
        <div style={{ display: "flex", gap: ".5rem", flexWrap: "wrap", marginBottom: "1rem" }}>
          <span className="badge badge--blue">{order.status}</span>
          <span className="badge badge--gray">{(order.paymentMethod || "").replace(/_/g, " ")}</span>
          <span className="badge badge--gray">{order.paymentStatus}</span>
        </div>

        <table className="admin-table" style={{ marginBottom: "1rem" }}>
          <thead><tr><th>Item</th><th>Qty</th><th>Total</th></tr></thead>
          <tbody>
            {(order.items || []).map((it, i) => (
              <tr key={i}>
                <td>{it.productName || it.name}</td>
                <td>{it.quantity}</td>
                <td>₹{Number(it.lineTotal ?? 0).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>

        <div style={{ fontSize: ".88rem", lineHeight: 1.7 }}>
          <div><b>Subtotal:</b> ₹{Number(order.subtotal ?? 0).toLocaleString()}</div>
          <div><b>GST:</b> ₹{Number(order.gstAmount ?? 0).toLocaleString()}</div>
          <div><b>Delivery:</b> ₹{Number(order.deliveryCharge ?? 0).toLocaleString()}</div>
          <div><b>Total:</b> ₹{Number(order.totalAmount ?? 0).toLocaleString()}</div>
        </div>

        {(a.fullName || a.street) && (
          <div style={{ marginTop: "1rem", fontSize: ".86rem", color: "var(--ad-muted)" }}>
            <b style={{ color: "var(--ad-ink)" }}>Ship to:</b> {a.fullName}, {a.mobileNumber}<br />
            {[a.houseNumber, a.street, a.city, a.state, a.pincode, a.country].filter(Boolean).join(", ")}
          </div>
        )}

        {order.notes && (
          <div style={{ marginTop: "1rem", fontSize: ".84rem", whiteSpace: "pre-wrap", background: "var(--ad-bg)", padding: ".7rem", borderRadius: 9 }}>
            <b>Notes / customization:</b>{"\n"}{order.notes}
          </div>
        )}

        <div className="ad-modal-actions">
          <button className="ad-btn ad-btn--primary" onClick={onClose}>Close</button>
        </div>
      </div>
    </div>
  );
}

function fmtDate(s) {
  if (!s) return "";
  try { return new Date(s).toLocaleString(); } catch { return s; }
}
