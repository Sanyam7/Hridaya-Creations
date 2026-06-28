import { createContext, useContext, useState, useCallback } from "react";

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const [items, setItems] = useState([]);

  /** Add or update an item in the cart */
  const addToCart = useCallback((product, customization) => {
    const cartItem = {
      cartId:        Date.now() + Math.random(),   // unique per addition
      productId:     product.id,
      // Key used at checkout to resolve the matching backend product
      // (see src/data/productMap.json). Mirrors "<variantId>|<variantName>".
      variantKey:    `${product.id}|${product.name}`,
      name:          product.name,
      emoji:         product.emoji,
      price:         product.price,
      qty:           parseInt(customization.qty, 10) || 1,
      customization,                                // { printName, message, color, font, special, fileName }
    };
    setItems(prev => [...prev, cartItem]);
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
