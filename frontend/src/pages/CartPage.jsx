import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useCart } from "../context/CartContext";
import { useAuth } from "../context/AuthContext";
import "./CartPage.css";

export default function CartPage() {
  const { items, removeFromCart, updateQty, clearCart, totalItems, totalPrice } = useCart();
  const { currentUser } = useAuth();
  const navigate        = useNavigate();
  const [ordered, setOrdered] = useState(false);
  const [removing, setRemoving] = useState(null);

  const handleRemove = (cartId) => {
    setRemoving(cartId);
    setTimeout(() => { removeFromCart(cartId); setRemoving(null); }, 300);
  };

  const handleOrder = () => {
    if (!currentUser) { navigate("/login"); return; }
    setOrdered(true);
    clearCart();
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
            <div className="os-info-row"><span>📧 Confirmation sent to</span><span>{currentUser?.email}</span></div>
            <div className="os-info-row"><span>🕐 Expected dispatch</span><span>3–5 business days</span></div>
            <div className="os-info-row"><span>🚚 Free delivery on orders above ₹500</span><span>✅</span></div>
          </div>
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
                {/* Product icon + info */}
                <div className="item-icon">{item.emoji}</div>

                <div className="item-details">
                  <div className="item-name">{item.name}</div>

                  {/* Customization summary */}
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

                  {/* Color swatch */}
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

                {/* Qty + price + remove */}
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

            <button className="checkout-btn" onClick={handleOrder}>
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
    </div>
  );
}
