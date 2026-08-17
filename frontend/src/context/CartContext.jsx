import { createContext, useContext, useState, useCallback } from "react";

const CartContext = createContext(null);

/**
 * Stable identity for a set of customization values — sorted so it does not depend on the
 * order the fields were filled in. Only ever compared locally, so a readable string is
 * enough; the server derives its own digest for the same purpose.
 */
function customizationSignature(customization) {
  const entries = Object.entries(customization || {})
    .filter(([, v]) => v !== undefined && v !== null && v !== "")
    .sort(([a], [b]) => a.localeCompare(b));
  return entries.length ? JSON.stringify(entries) : "none";
}

export function CartProvider({ children }) {
  const [items, setItems] = useState([]);

  /**
   * Add an item to the cart.
   *
   * Two lines are the same line only when the product AND the personalisation match, so
   * "Water Bottle / John" and "Water Bottle / Sarah" stay separate while re-adding an
   * identical one bumps the quantity. Mirrors how the backend keys cart lines.
   *
   * @param product the catalog item being added
   * @param {{customization?: Object, qty?: number}} [selection] omit customization for readymade
   */
  const addToCart = useCallback((product, selection = {}) => {
    const customization = selection.customization || {};
    const qty = parseInt(selection.qty, 10) || 1;
    const signature = customizationSignature(customization);

    setItems(prev => {
      const match = prev.find(
        i => i.productId === product.id && i.signature === signature
      );
      if (match) {
        return prev.map(i => (i === match ? { ...i, qty: i.qty + qty } : i));
      }
      return [...prev, {
        cartId:        Date.now() + Math.random(),   // unique per line
        productId:     product.id,
        // Real backend product id when the item came from the live catalog;
        // checkout uses this directly (falling back to productMap otherwise).
        backendProductId: product.backendProductId ?? null,
        // Fallback key to resolve a backend product for legacy/local-catalog
        // items (see src/data/productMap.json). Mirrors "<variantId>|<variantName>".
        variantKey:    `${product.id}|${product.name}`,
        name:          product.name,
        emoji:         product.emoji,
        price:         product.price,
        qty,
        customization,                // keyed by customization option; {} for readymade
        signature,
      }];
    });
  }, []);

  /** Remove one cart entry by its cartId */
  const removeFromCart = useCallback((cartId) => {
    setItems(prev => prev.filter(i => i.cartId !== cartId));
  }, []);

  /** Update quantity inline */
  const updateQty = useCallback((cartId, qty) => {
    setItems(prev =>
      prev.map(i => i.cartId === cartId ? { ...i, qty: Math.max(1, qty) } : i)
    );
  }, []);

  const clearCart = useCallback(() => setItems([]), []);

  const totalItems    = items.reduce((s, i) => s + i.qty, 0);
  const totalPrice    = items.reduce((s, i) => s + i.price * i.qty, 0);

  return (
    <CartContext.Provider value={{ items, addToCart, removeFromCart, updateQty, clearCart, totalItems, totalPrice }}>
      {children}
    </CartContext.Provider>
  );
}

export const useCart = () => useContext(CartContext);
