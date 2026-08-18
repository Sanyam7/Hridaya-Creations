import { Link } from "react-router-dom";
import { formatPrice, productPath } from "../../constants/catalog";
import { galleryBadge } from "../../constants/productMedia";
import { colorSwatchStyle } from "../../constants/productColors";
import { useState } from "react";

/**
 * One product in a listing grid.
 *
 * The whole tile is a single link to the product page and contains no other interactive
 * element — nesting a button inside a link is invalid and makes the card ambiguous to
 * keyboard and screen-reader users. Buying decisions (quantity, customization, add to cart)
 * belong on the detail page, which is also the flow the customer expects: look first, act
 * second.
 */
export default function ProductTile({ product }) {
  const [broken, setBroken] = useState(false);
  const image = product.gallery?.[0] || null;
  const photoBadge = galleryBadge(product.gallery?.length || 0);
  const customizable = product.productType === "CUSTOMIZABLE";
  const discounted = product.originalPrice && product.originalPrice > product.price;

  return (
    <Link className="pt" to={productPath(product.id)} aria-label={product.name}>
      <div className="pt-media">
        {image && !broken ? (
          <img
            className="pt-img"
            src={image.url}
            alt={product.name}
            loading="lazy"
            decoding="async"
            onError={() => setBroken(true)}
          />
        ) : (
          <span className="pt-emoji">{product.emoji}</span>
        )}

        {product.badge && <span className="pt-badge">{product.badge}</span>}
        {photoBadge && <span className="pt-photos">🖼 {photoBadge}</span>}
        {!product.inStock && <span className="pt-oos">Out of stock</span>}

        <span className="pt-overlay"><span className="pt-overlay-text">View Product</span></span>
      </div>

      <div className="pt-body">
        <h3 className="pt-name">{product.name}</h3>
        {product.desc && <p className="pt-desc">{product.desc}</p>}

        {product.hasColors && product.colors.length > 0 && (
          <div className="pt-colors">
            {/* Colour names in text so the row never depends on colour alone. */}
            <span className="sr-only">
              Available in {product.colors.map((c) => c.name).join(", ")}
            </span>
            {product.colors.slice(0, 5).map((color) => (
              <span key={color.id} className="pt-swatch" style={colorSwatchStyle(color.hexCode)}
                title={color.name} aria-hidden="true" />
            ))}
            {product.colors.length > 5 && (
              <span className="pt-swatch-more" aria-hidden="true">+{product.colors.length - 5}</span>
            )}
          </div>
        )}

        <div className="pt-foot">
          <span className="pt-price">
            {formatPrice(product.price)}
            {discounted && <span className="pt-was">{formatPrice(product.originalPrice)}</span>}
          </span>
          {customizable && <span className="pt-chip">Customizable</span>}
        </div>
      </div>
    </Link>
  );
}
