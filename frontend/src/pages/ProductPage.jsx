import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { productApi } from "../api";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { categoryPath, formatPrice, toCatalogProduct } from "../constants/catalog";
import { colorSwatchStyle } from "../constants/productColors";
import { customerFacingOptions } from "../constants/productCustomization";
import ProductImageGallery from "../components/Products/ProductImageGallery";
import CustomizeModal from "../components/Modal/CustomizeModal";
import "./Catalog.css";
import "./ProductPage.css";

/**
 * The full product page: everything the customer needs before deciding, then one clear action.
 *
 * The product is fetched by id from the route rather than passed through navigation state, so a
 * refresh, a bookmark or a shared link all work. Which action appears is read from the product's
 * own type — customizable products open the existing dynamic form, readymade ones add straight to
 * the cart — so this page never needs to know what any given product offers.
 */
export default function ProductPage() {
  const { productId } = useParams();
  const navigate = useNavigate();
  const { currentUser } = useAuth();
  const { addToCart } = useCart();

  const [product, setProduct] = useState(null);
  const [status, setStatus] = useState("loading"); // loading | ready | notfound | error
  const [qty, setQty] = useState(1);
  const [customizing, setCustomizing] = useState(false);
  const [added, setAdded] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let active = true;
    setStatus("loading");
    setAdded(false);
    setQty(1);
    window.scrollTo({ top: 0 });

    productApi
      .get(productId)
      .then((data) => {
        if (!active) return;
        const mapped = toCatalogProduct(data);
        if (!mapped) { setStatus("notfound"); return; }
        setProduct(mapped);
        setStatus("ready");
      })
      .catch((e) => {
        if (!active) return;
        // A 404 means the product is gone; anything else is a transport problem worth retrying.
        setStatus(e && e.status === 404 ? "notfound" : "error");
      });
    return () => { active = false; };
  }, [productId, reloadKey]);

  const options = useMemo(() => (product ? customerFacingOptions(product) : []), [product]);

  const customizable = product?.productType === "CUSTOMIZABLE";
  const maxQty = Math.max(1, Math.min(product?.stockQuantity || 10, 10));

  const handleAddReadymade = () => {
    if (!currentUser) { navigate("/login"); return; }
    addToCart(product, { qty });
    setAdded(true);
  };

  if (status === "loading") return <ProductSkeleton />;

  if (status === "notfound") {
    return (
      <div className="catalog-shell">
        <div className="state-block" role="status">
          <div className="state-icon">😕</div>
          <h2 className="state-title">Product not found</h2>
          <p className="state-text">This product may have been removed or is no longer available.</p>
          <Link className="btn-primary" to="/#products">Browse all collections</Link>
        </div>
      </div>
    );
  }

  if (status === "error") {
    return (
      <div className="catalog-shell">
        <div className="state-block" role="status">
          <div className="state-icon">📡</div>
          <h2 className="state-title">Couldn&rsquo;t load this product</h2>
          <p className="state-text">
            The server didn&rsquo;t respond. It may be waking up — please try again.
          </p>
          <button className="btn-primary" onClick={() => setReloadKey((k) => k + 1)}>Try again</button>
        </div>
      </div>
    );
  }

  return (
    <div className="catalog-shell">
      <nav className="crumbs" aria-label="Breadcrumb">
        <Link to="/">Home</Link>
        <span aria-hidden="true">›</span>
        {product.categoryName && (
          <>
            <Link to={categoryPath(product.categoryName)}>{product.categoryName}</Link>
            <span aria-hidden="true">›</span>
          </>
        )}
        <span aria-current="page">{product.name}</span>
      </nav>

      <div className="pdp">
        <div className="pdp-media">
          <ProductImageGallery
            images={product.gallery}
            productName={product.name}
            emoji={product.emoji}
          />
        </div>

        <div className="pdp-info">
          {product.categoryName && (
            <Link className="pdp-category" to={categoryPath(product.categoryName)}>
              {product.categoryName}
            </Link>
          )}
          <h1 className="pdp-title">{product.name}</h1>

          <div className="pdp-price-row">
            <span className="pdp-price">{formatPrice(product.price)}</span>
            {product.originalPrice > product.price && (
              <>
                <span className="pdp-was">{formatPrice(product.originalPrice)}</span>
                {product.discountPercentage > 0 && (
                  <span className="pdp-off">{product.discountPercentage}% off</span>
                )}
              </>
            )}
          </div>

          <div className="pdp-meta">
            <span className={`pdp-stock${product.inStock ? "" : " is-out"}`}>
              {product.inStock ? "✓ In stock" : "Out of stock"}
            </span>
            <span className="pdp-type">
              {customizable ? "✨ Personalised to order" : "📦 Ready to ship"}
            </span>
          </div>

          {product.description && (
            <section className="pdp-section">
              <h2 className="pdp-h2">Description</h2>
              {/* Shown in full — the listing truncates, the detail page must not. */}
              <p className="pdp-desc">{product.description}</p>
            </section>
          )}

          {product.hasColors && product.colors.length > 0 && (
            <section className="pdp-section">
              <h2 className="pdp-h2">Available colours</h2>
              <ul className="pdp-colors">
                {product.colors.map((color) => (
                  <li key={color.id} className="pdp-color">
                    <span className="pdp-swatch" style={colorSwatchStyle(color.hexCode)} aria-hidden="true" />
                    <span>{color.name}</span>
                  </li>
                ))}
              </ul>
            </section>
          )}

          {customizable && options.length > 0 && (
            <section className="pdp-section">
              <h2 className="pdp-h2">What you can personalise</h2>
              <ul className="pdp-options">
                {options.map((option) => (
                  <li key={option.key}>
                    {option.label}
                    {option.required && <span className="pdp-req"> (required)</span>}
                  </li>
                ))}
              </ul>
            </section>
          )}

          {(product.sku || product.tags.length > 0 || product.stockQuantity != null) && (
            <section className="pdp-section">
              <h2 className="pdp-h2">Details</h2>
              <dl className="pdp-specs">
                {product.sku && (<><dt>SKU</dt><dd>{product.sku}</dd></>)}
                {product.categoryName && (<><dt>Category</dt><dd>{product.categoryName}</dd></>)}
                {product.stockQuantity != null && (
                  <><dt>Availability</dt><dd>{product.stockQuantity} in stock</dd></>
                )}
                {product.tags.length > 0 && (<><dt>Tags</dt><dd>{product.tags.join(", ")}</dd></>)}
              </dl>
            </section>
          )}

          {/* Action panel: exactly one primary action, decided by the product's own type. */}
          <div className="pdp-actions">
            {!product.inStock ? (
              <button className="pdp-cta" disabled>Out of stock</button>
            ) : customizable ? (
              <>
                <button className="pdp-cta" onClick={() => setCustomizing(true)}>
                  ✨ Customize Product
                </button>
                <p className="pdp-action-note">
                  You&rsquo;ll choose your personalisation next, then add it to your cart.
                </p>
              </>
            ) : (
              <>
                <div className="pdp-qty">
                  <span className="pdp-qty-label">Quantity</span>
                  <div className="pdp-qty-control">
                    <button type="button" onClick={() => setQty((q) => Math.max(1, q - 1))}
                      disabled={qty <= 1} aria-label="Decrease quantity">−</button>
                    <span aria-live="polite">{qty}</span>
                    <button type="button" onClick={() => setQty((q) => Math.min(maxQty, q + 1))}
                      disabled={qty >= maxQty} aria-label="Increase quantity">+</button>
                  </div>
                </div>
                <button className="pdp-cta" onClick={handleAddReadymade}>
                  {currentUser ? "🛒 Add to Cart" : "🔑 Login & Add to Cart"}
                </button>
              </>
            )}

            {added && (
              <div className="pdp-added" role="status">
                <span>🎉 Added to your cart.</span>
                <Link className="btn-outline pdp-added-btn" to="/cart">View Cart</Link>
              </div>
            )}
          </div>
        </div>
      </div>

      {customizing && (
        <CustomizeModal
          product={product}
          onClose={() => setCustomizing(false)}
          onCloseAll={() => setCustomizing(false)}
        />
      )}
    </div>
  );
}

/** Holds the page's shape while the product loads, so nothing jumps when it arrives. */
function ProductSkeleton() {
  return (
    <div className="catalog-shell" aria-busy="true">
      <div className="pdp">
        <div className="pdp-media"><div className="sk sk--stage" /></div>
        <div className="pdp-info">
          <span className="sk" style={{ height: 28, width: "70%" }} />
          <span className="sk" style={{ height: 22, width: "30%" }} />
          <span className="sk sk--line" />
          <span className="sk sk--line" />
          <span className="sk sk--line" style={{ width: "60%" }} />
          <span className="sk" style={{ height: 48, borderRadius: 50, marginTop: "1.5rem" }} />
        </div>
      </div>
      <span className="sr-only">Loading product…</span>
    </div>
  );
}
