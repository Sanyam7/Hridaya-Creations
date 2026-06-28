import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useCart } from "../context/CartContext";
import { useAuth } from "../context/AuthContext";
import { cartApi, addressApi, orderApi } from "../api";
import productMap from "../data/productMap.json";
import "./CartPage.css";

export default function CartPage() {
  const { items, removeFromCart, updateQty, clearCart, totalItems, totalPrice } = useCart();
  const { currentUser } = useAuth();
  const navigate        = useNavigate();

  const [ordered, setOrdered]     = useState(false);
  const [orderResult, setOrderResult] = useState(null); // { orderNumber, totalAmount }
  const [removing, setRemoving]   = useState(null);
  const [checkout, setCheckout]   = useState(false);    // address form visible
  const [placing, setPlacing]     = useState(false);
  const [placeError, setPlaceError] = useState("");

  const [addr, setAddr] = useState({
    fullName: currentUser?.name || "",
    mobileNumber: currentUser?.mobileNumber || "",
    houseNumber: "",
    street: "",
    city: "",
    state: "",
    country: "India",
    pincode: "",
  });
  const setA = (k, v) => { setAddr(p => ({ ...p, [k]: v })); setPlaceError(""); };

  const handleRemove = (cartId) => {
    setRemoving(cartId);
    setTimeout(() => { removeFromCart(cartId); setRemoving(null); }, 300);
  };

  const startCheckout = () => {
    if (!currentUser) { navigate("/login"); return; }
    setPlaceError("");
    setCheckout(true);
  };

  // Compose a human-readable customization summary for the order notes.
  const buildNotes = () =>
    items
      .map((i) => {
        const c = i.customization || {};
        const bits = [];
        if (c.printName) bits.push(`Name: ${c.printName}`);
        if (c.color)     bits.push(`Color: ${c.color}`);
        if (c.font)      bits.push(`Font: ${c.font}`);
        if (c.message)   bits.push(`Msg: ${c.message}`);
        if (c.fileName)  bits.push(`File: ${c.fileName}`);
        if (c.special)   bits.push(`Note: ${c.special}`);
        return `- ${i.name} x${i.qty}${bits.length ? " (" + bits.join("; ") + ")" : ""}`;
      })
      .join("\n")
      .slice(0, 1000);

  const validAddress = () => {
    if (!addr.fullName.trim()) return "Please enter the recipient's full name.";
    if (!/^[6-9][0-9]{9}$/.test(addr.mobileNumber)) return "Enter a valid 10-digit mobile number.";
    if (!addr.street.trim())  return "Please enter the street / area.";
    if (!addr.city.trim())    return "Please enter the city.";
    if (!addr.state.trim())   return "Please enter the state.";
    if (!/^[1-9][0-9]{5}$/.test(addr.pincode)) return "Enter a valid 6-digit pincode.";
    return null;
  };

  const placeOrder = async (e) => {
    e.preventDefault();
    const err = validAddress();
    if (err) { setPlaceError(err); return; }

    // Resolve cart items to backend products and merge quantities.
    const merged = new Map();
    let unmapped = 0;
    for (const it of items) {
      const pid = productMap[it.variantKey];
      if (!pid) { unmapped++; continue; }
      merged.set(pid, (merged.get(pid) || 0) + it.qty);
    }
    if (merged.size === 0) {
      setPlaceError("These items aren't available for online checkout yet. Please contact us to order.");
      return;
    }

    setPlacing(true);
    setPlaceError("");
    try {
      // Start from a clean server-side cart, then add the current items.
      await cartApi.clear().catch(() => {});
      for (const [productId, quantity] of merged.entries()) {
        await cartApi.addItem(productId, quantity);
      }
      const created = await addressApi.create({
        fullName: addr.fullName.trim(),
        mobileNumber: addr.mobileNumber,
        houseNumber: addr.houseNumber.trim() || undefined,
        street: addr.street.trim(),
        city: addr.city.trim(),
        state: addr.state.trim(),
        country: addr.country.trim() || "India",
        pincode: addr.pincode,
        defaultAddress: true,
      });
      const order = await orderApi.create({
        addressId: created.id,
        paymentMethod: "CASH_ON_DELIVERY",
        notes: buildNotes(),
      });
      setOrderResult({
        orderNumber: order.orderNumber,
        totalAmount: order.totalAmount,
        unmapped,
      });
      clearCart();
      setCheckout(false);
      setOrdered(true);
    } catch (e2) {
      setPlaceError(e2.message || "Could not place your order. Please try again.");
    } finally {
      setPlacing(false);
    }
  };

  if (ordered) {
    return (
      <div className="cart-page">
        <div className="cart-bg" />
        <div className="order-success">
          <div className="os-icon">🎉</div>
          <h2 className="os-title">Order Placed Successfully!</h2>
          <p className="os-text">
            Thank you, <strong>{currentUser?.name}</strong>! Your personalized items are in
            our hands now. We'll contact you soon to confirm details and shipping.
          </p>
          <div className="os-info">
            {orderResult?.orderNumber && (
              <div className="os-info-row"><span>🧾 Order number</span><span>{orderResult.orderNumber}</span></div>
            )}
            {orderResult?.totalAmount != null && (
              <div className="os-info-row"><span>💰 Total payable</span><span>₹{Number(orderResult.totalAmount).toLocaleString()}</span></div>
            )}
            <div className="os-info-row"><span>📧 Confirmation sent to</span><span>{currentUser?.email}</span></div>
            <div className="os-info-row"><span>💳 Payment</span><span>Cash on Delivery</span></div>
            <div className="os-info-row"><span>🕐 Expected dispatch</span><span>3–5 business days</span></div>
          </div>
          {orderResult?.unmapped > 0 && (
            <p className="os-text" style={{ fontSize: ".85rem", opacity: .8 }}>
              Note: {orderResult.unmapped} item(s) couldn't be added online and aren't included in this order.
            </p>
          )}
          <Link to="/" className="btn-primary os-btn">Continue Shopping 🛍️</Link>
        </div>
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <div className="cart-page">
        <div className="cart-bg" />
        <div className="cart-empty">
          <div className="empty-icon">🛒</div>
          <h2 className="empty-title">Your cart is empty</h2>
          <p className="empty-text">
            Looks like you haven't added anything yet. Explore our collection and
            personalize something special!
          </p>
          <Link to="/" className="btn-primary">Browse Products 🎁</Link>
        </div>
      </div>
    );
  }

  const shipping = totalPrice >= 500 ? 0 : 60;
  const grandTotal = totalPrice + shipping;

  return (
    <div className="cart-page">
      <div className="cart-bg" />

      <div className="cart-container">
        {/* Header */}
        <div className="cart-header">
          <div>
            <h1 className="cart-title">Your Cart 🛒</h1>
            <p className="cart-subtitle">{totalItems} item{totalItems !== 1 ? "s" : ""} ready to be crafted with love</p>
          </div>
          <button className="clear-cart-btn" onClick={clearCart}>🗑 Clear All</button>
        </div>

        <div className="cart-layout">
          {/* Items list */}
          <div className="cart-items">
            {items.map(item => (
              <div
                key={item.cartId}
                className={`cart-item ${removing === item.cartId ? "cart-item--removing" : ""}`}
              >
                <div className="item-icon">{item.emoji}</div>

                <div className="item-details">
                  <div className="item-name">{item.name}</div>

                  <div className="item-custom">
                    {item.customization.printName && (
                      <span className="custom-chip">✏️ {item.customization.printName}</span>
                    )}
                    {item.customization.font && (
                      <span className="custom-chip">🔤 {item.customization.font}</span>
                    )}
                    {item.customization.fileName && (
                      <span className="custom-chip">🖼️ {item.customization.fileName}</span>
                    )}
                    {item.customization.message && (
                      <span className="custom-chip">💬 {item.customization.message}</span>
                    )}
                  </div>

                  {item.customization.special && (
                    <div className="item-note">📝 {item.customization.special}</div>
                  )}

                  {item.customization.color && (
                    <div className="item-color-row">
                      <span className="item-color-label">Color:</span>
                      <span
                        className="item-color-dot"
                        style={{
                          background: item.customization.color === "gold"
                            ? "linear-gradient(135deg,#ffd700,#ff8c00)"
                            : item.customization.color
                        }}
                      />
                    </div>
                  )}
                </div>

                <div className="item-right">
                  <div className="qty-control">
                    <button
                      className="qty-btn"
                      onClick={() => updateQty(item.cartId, item.qty - 1)}
                      disabled={item.qty <= 1}
                    >−</button>
                    <span className="qty-val">{item.qty}</span>
                    <button
                      className="qty-btn"
                      onClick={() => updateQty(item.cartId, item.qty + 1)}
                    >+</button>
                  </div>
                  <div className="item-price">₹{(item.price * item.qty).toLocaleString()}</div>
                  <div className="item-unit-price">₹{item.price} each</div>
                  <button className="item-remove" onClick={() => handleRemove(item.cartId)}>
                    🗑 Remove
                  </button>
                </div>
              </div>
            ))}
          </div>

          {/* Summary sidebar */}
          <div className="cart-summary">
            <h3 className="summary-title">Order Summary</h3>

            <div className="summary-rows">
              <div className="summary-row">
                <span>Subtotal ({totalItems} items)</span>
                <span>₹{totalPrice.toLocaleString()}</span>
              </div>
              <div className="summary-row">
                <span>Shipping</span>
                <span className={shipping === 0 ? "free-ship" : ""}>
                  {shipping === 0 ? "FREE 🎉" : `₹${shipping}`}
                </span>
              </div>
              {shipping > 0 && (
                <div className="free-ship-note">
                  Add ₹{(500 - totalPrice).toLocaleString()} more for free shipping!
                </div>
              )}
              <div className="summary-divider" />
              <div className="summary-row summary-total">
                <span>Total</span>
                <span>₹{grandTotal.toLocaleString()}</span>
              </div>
            </div>
            <p className="free-ship-note" style={{ marginTop: ".4rem" }}>
              Taxes (GST) &amp; final delivery are calculated at checkout.
            </p>

            <button className="checkout-btn" onClick={startCheckout}>
              {currentUser ? "Place Order 🎁" : "Login to Checkout 🔑"}
            </button>

            <div className="summary-perks">
              <div className="perk">🔒 Secure &amp; encrypted checkout</div>
              <div className="perk">🎨 Custom crafted just for you</div>
              <div className="perk">📦 Carefully packed &amp; delivered</div>
              <div className="perk">✅ 100% satisfaction guaranteed</div>
            </div>

            <Link to="/" className="keep-shopping">← Continue Shopping</Link>
          </div>
        </div>
      </div>

      {/* Address / checkout modal */}
      {checkout && (
        <div className="modal-overlay" onClick={() => !placing && setCheckout(false)}>
          <form className="modal" onClick={e => e.stopPropagation()} onSubmit={placeOrder}>
            <button type="button" className="modal-close" onClick={() => !placing && setCheckout(false)}>✕</button>
            <div className="modal-header">
              <div className="modal-emoji">🚚</div>
              <h2 className="modal-title">Delivery Details</h2>
              <p className="modal-subtitle">Cash on Delivery • {totalItems} item{totalItems !== 1 ? "s" : ""}</p>
            </div>

            <div className="form-group">
              <label className="form-label">Full Name *</label>
              <input className="form-input" value={addr.fullName} onChange={e => setA("fullName", e.target.value)} placeholder="Recipient name" />
            </div>
            <div className="form-group">
              <label className="form-label">Mobile Number *</label>
              <input className="form-input" value={addr.mobileNumber} inputMode="numeric" maxLength={10}
                onChange={e => setA("mobileNumber", e.target.value.replace(/\D/g, ""))} placeholder="10-digit mobile" />
            </div>
            <div className="form-group">
              <label className="form-label">House / Flat (optional)</label>
              <input className="form-input" value={addr.houseNumber} onChange={e => setA("houseNumber", e.target.value)} placeholder="Flat 4B, Lotus Residency" />
            </div>
            <div className="form-group">
              <label className="form-label">Street / Area *</label>
              <input className="form-input" value={addr.street} onChange={e => setA("street", e.target.value)} placeholder="MG Road" />
            </div>
            <div className="form-row">
              <div className="form-group" style={{ flex: 1 }}>
                <label className="form-label">City *</label>
                <input className="form-input" value={addr.city} onChange={e => setA("city", e.target.value)} placeholder="Bengaluru" />
              </div>
              <div className="form-group" style={{ flex: 1 }}>
                <label className="form-label">State *</label>
                <input className="form-input" value={addr.state} onChange={e => setA("state", e.target.value)} placeholder="Karnataka" />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group" style={{ flex: 1 }}>
                <label className="form-label">Pincode *</label>
                <input className="form-input" value={addr.pincode} inputMode="numeric" maxLength={6}
                  onChange={e => setA("pincode", e.target.value.replace(/\D/g, ""))} placeholder="560001" />
              </div>
              <div className="form-group" style={{ flex: 1 }}>
                <label className="form-label">Country</label>
                <input className="form-input" value={addr.country} onChange={e => setA("country", e.target.value)} />
              </div>
            </div>

            {placeError && <div className="form-error">⚠ {placeError}</div>}

            <button className="modal-submit" type="submit" disabled={placing}>
              {placing ? "Placing your order…" : "Confirm Order 🎁"}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
