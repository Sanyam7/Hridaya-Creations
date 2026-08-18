import { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useParams, useSearchParams } from "react-router-dom";
import { productApi } from "../api";
import { categorySlug, toCatalogProduct } from "../constants/catalog";
import ProductGrid from "../components/Products/ProductGrid";
import "./Catalog.css";

const SORTS = [
  { id: "newest", label: "Newest", sortBy: "createdAt", sortDir: "desc" },
  { id: "price-asc", label: "Price: low to high", sortBy: "sellingPrice", sortDir: "asc" },
  { id: "price-desc", label: "Price: high to low", sortBy: "sellingPrice", sortDir: "desc" },
  { id: "name", label: "Name (A–Z)", sortBy: "name", sortDir: "asc" },
];

const PAGE_SIZE = 24;

/**
 * Every product in one category, as a full-width listing.
 *
 * The category is addressed by a slug of its name rather than held in navigation state, so
 * the page survives a refresh and can be linked to or shared. Sorting and paging are pushed
 * to the existing search endpoint instead of being done in the browser, so a large category
 * does not have to be downloaded whole before the first row appears.
 */
export default function CategoryPage() {
  const { slug } = useParams();
  const [searchParams, setSearchParams] = useSearchParams();

  const sortId = searchParams.get("sort") || "newest";
  const sort = SORTS.find((s) => s.id === sortId) || SORTS[0];
  const page = Math.max(0, parseInt(searchParams.get("page") || "0", 10) || 0);

  const [category, setCategory] = useState(null);
  const [products, setProducts] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  // Resolve the slug to a real category once; the id is what the product query filters on.
  useEffect(() => {
    let active = true;
    setCategory(null);
    setError("");
    // Also clear the previous category's results: without this the old products stay
    // on screen while the new category is being resolved, which reads as the wrong
    // collection having loaded.
    setProducts([]);
    setLoading(true);
    productApi
      .categories()
      .then((data) => {
        if (!active) return;
        const items = Array.isArray(data) ? data : data.content || [];
        const match = items.find((c) => categorySlug(c.categoryName) === slug);
        if (!match) {
          setError("notfound");
          setLoading(false);
          return;
        }
        setCategory(match);
      })
      .catch(() => {
        if (active) { setError("network"); setLoading(false); }
      });
    return () => { active = false; };
  }, [slug, reloadKey]);

  useEffect(() => {
    if (!category) return undefined;
    let active = true;
    setLoading(true);
    setError("");
    productApi
      .list({ categoryId: category.id, page, size: PAGE_SIZE, sortBy: sort.sortBy, sortDir: sort.sortDir })
      .then((data) => {
        if (!active) return;
        setProducts((data.content || []).map(toCatalogProduct).filter(Boolean));
        setTotalPages(data.totalPages ?? 0);
        setTotalElements(data.totalElements ?? (data.content || []).length);
        setLoading(false);
      })
      .catch(() => {
        if (!active) return;
        setError("network");
        setLoading(false);
      });
    return () => { active = false; };
  }, [category, page, sort.sortBy, sort.sortDir, reloadKey]);

  // Paging should land the customer at the top of the new page, not halfway down it.
  useEffect(() => { window.scrollTo({ top: 0, behavior: "smooth" }); }, [page]);

  const update = useCallback((next) => {
    setSearchParams((prev) => {
      const params = new URLSearchParams(prev);
      for (const [k, v] of Object.entries(next)) {
        if (v === null || v === undefined || v === "") params.delete(k);
        else params.set(k, String(v));
      }
      return params;
    });
  }, [setSearchParams]);

  const title = category?.categoryName || prettifySlug(slug);
  const countLabel = useMemo(() => {
    if (loading || error) return null;
    return `${totalElements} ${totalElements === 1 ? "product" : "products"}`;
  }, [loading, error, totalElements]);

  if (error === "notfound") {
    return (
      <div className="catalog-shell">
        <StateBlock
          icon="🔍"
          title="Category not found"
          text="We couldn't find a category by that name. It may have been renamed or removed."
          action={<Link className="btn-primary" to="/#products">Browse all collections</Link>}
        />
      </div>
    );
  }

  return (
    <div className="catalog-shell">
      <nav className="crumbs" aria-label="Breadcrumb">
        <Link to="/">Home</Link>
        <span aria-hidden="true">›</span>
        <Link to="/#products">Collections</Link>
        <span aria-hidden="true">›</span>
        <span aria-current="page">{title}</span>
      </nav>

      <header className="catalog-head">
        <div>
          <h1 className="catalog-title">{title}</h1>
          {category?.description && <p className="catalog-sub">{category.description}</p>}
        </div>
        <div className="catalog-tools">
          {countLabel && <span className="catalog-count">{countLabel}</span>}
          <label className="catalog-sort">
            <span className="sr-only">Sort products by</span>
            <select
              className="form-select"
              value={sort.id}
              onChange={(e) => update({ sort: e.target.value, page: null })}
            >
              {SORTS.map((s) => <option key={s.id} value={s.id}>{s.label}</option>)}
            </select>
          </label>
        </div>
      </header>

      {error === "network" ? (
        <StateBlock
          icon="📡"
          title="Couldn't load this collection"
          text="The server didn't respond. It may be waking up — please try again."
          action={<button className="btn-primary" onClick={() => setReloadKey((k) => k + 1)}>Try again</button>}
        />
      ) : (
        <>
          <ProductGrid
            products={products}
            loading={loading}
            skeletonCount={8}
            emptyMessage={`No products available in ${title} yet. Please check back soon.`}
          />

          {totalPages > 1 && (
            <nav className="pager" aria-label="Pagination">
              <button className="pager-btn" disabled={page <= 0}
                onClick={() => update({ page: page - 1 })}>‹ Previous</button>
              <span className="pager-status">Page {page + 1} of {totalPages}</span>
              <button className="pager-btn" disabled={page >= totalPages - 1}
                onClick={() => update({ page: page + 1 })}>Next ›</button>
            </nav>
          )}
        </>
      )}
    </div>
  );
}

function StateBlock({ icon, title, text, action }) {
  return (
    <div className="state-block" role="status">
      <div className="state-icon">{icon}</div>
      <h2 className="state-title">{title}</h2>
      <p className="state-text">{text}</p>
      {action}
    </div>
  );
}

/** A readable heading while the real category name is still loading. */
function prettifySlug(slug) {
  return String(slug || "")
    .split("-")
    .filter(Boolean)
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(" ");
}
