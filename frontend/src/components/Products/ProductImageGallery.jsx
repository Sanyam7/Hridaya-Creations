import { useCallback, useEffect, useState } from "react";
import "./ProductImageGallery.css";

/**
 * Every image the admin uploaded for a product, browsable.
 *
 * Reusable and count-agnostic: it is handed a gallery and a name and works out the rest, so
 * the same component covers a product with no image, one image, and twenty. Thumbnails,
 * arrows and the counter only appear when there is more than one image to move between —
 * a single-image product gets a plain picture rather than controls that do nothing.
 *
 * @param {{ images: {id:(string|number), url:string}[], productName: string, emoji?: string }} props
 */
export default function ProductImageGallery({ images = [], productName, emoji = "🎁" }) {
  const [index, setIndex] = useState(0);
  const [lightbox, setLightbox] = useState(false);
  const [failed, setFailed] = useState({});

  const count = images.length;
  const many = count > 1;
  // A product edited while this page is open can come back with fewer images.
  const safeIndex = Math.min(index, Math.max(count - 1, 0));
  const current = images[safeIndex] || null;

  const go = useCallback(
    (delta) => setIndex((i) => (count ? (i + delta + count) % count : 0)),
    [count]
  );

  // Arrow keys drive the gallery whenever it (or the lightbox) has focus, and Escape
  // always closes the lightbox — the two things a keyboard user will reach for first.
  useEffect(() => {
    if (!lightbox) return undefined;
    const onKey = (e) => {
      if (e.key === "Escape") setLightbox(false);
      else if (e.key === "ArrowRight") go(1);
      else if (e.key === "ArrowLeft") go(-1);
    };
    window.addEventListener("keydown", onKey);
    // Stop the page behind the lightbox scrolling with it.
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      window.removeEventListener("keydown", onKey);
      document.body.style.overflow = previousOverflow;
    };
  }, [lightbox, go]);

  const markFailed = (id) => setFailed((f) => ({ ...f, [id]: true }));
  const isBroken = (image) => !image || failed[image.id];

  if (count === 0) {
    return (
      <div className="pg" data-empty="true">
        <div className="pg-main pg-main--placeholder" aria-label={`${productName} — no photo available`}>
          <span className="pg-emoji">{emoji}</span>
          <span className="pg-placeholder-text">No photo yet</span>
        </div>
      </div>
    );
  }

  return (
    <div className="pg">
      {many && (
        <ul className="pg-thumbs" aria-label={`${productName} images`}>
          {images.map((image, i) => (
            <li key={image.id}>
              <button
                type="button"
                className={`pg-thumb${i === safeIndex ? " is-active" : ""}`}
                aria-label={`Show image ${i + 1} of ${count}`}
                aria-current={i === safeIndex}
                onClick={() => setIndex(i)}
                onMouseEnter={() => setIndex(i)}
              >
                {isBroken(image) ? (
                  <span className="pg-thumb-fallback">{emoji}</span>
                ) : (
                  <img
                    src={image.url}
                    alt=""
                    loading="lazy"
                    decoding="async"
                    onError={() => markFailed(image.id)}
                  />
                )}
              </button>
            </li>
          ))}
        </ul>
      )}

      <div className="pg-stage">
        <button
          type="button"
          className="pg-main"
          onClick={() => setLightbox(true)}
          aria-label={`Enlarge image ${safeIndex + 1} of ${count}`}
        >
          {isBroken(current) ? (
            <span className="pg-emoji">{emoji}</span>
          ) : (
            <img
              src={current.url}
              alt={`${productName} — image ${safeIndex + 1} of ${count}`}
              /* The first image is what the customer is waiting on; the rest can wait. */
              loading={safeIndex === 0 ? "eager" : "lazy"}
              decoding="async"
              onError={() => markFailed(current.id)}
            />
          )}
          <span className="pg-zoom-hint" aria-hidden="true">⤢</span>
        </button>

        {many && (
          <>
            <button type="button" className="pg-nav pg-nav--prev" onClick={() => go(-1)} aria-label="Previous image">‹</button>
            <button type="button" className="pg-nav pg-nav--next" onClick={() => go(1)} aria-label="Next image">›</button>
            <span className="pg-counter">{safeIndex + 1} / {count}</span>
          </>
        )}
      </div>

      {many && (
        <div className="pg-dots" role="tablist" aria-label="Choose image">
          {images.map((image, i) => (
            <button
              key={image.id}
              type="button"
              role="tab"
              aria-selected={i === safeIndex}
              aria-label={`Image ${i + 1}`}
              className={`pg-dot${i === safeIndex ? " is-active" : ""}`}
              onClick={() => setIndex(i)}
            />
          ))}
        </div>
      )}

      {lightbox && (
        <div
          className="pg-lightbox"
          role="dialog"
          aria-modal="true"
          aria-label={`${productName} image viewer`}
          onClick={() => setLightbox(false)}
        >
          <button type="button" className="pg-lb-close" aria-label="Close image viewer" autoFocus
            onClick={() => setLightbox(false)}>✕</button>

          {isBroken(current) ? (
            <span className="pg-emoji pg-emoji--lg">{emoji}</span>
          ) : (
            <img
              className="pg-lb-img"
              src={current.url}
              alt={`${productName} — image ${safeIndex + 1} of ${count}`}
              onClick={(e) => e.stopPropagation()}
              onError={() => markFailed(current.id)}
            />
          )}

          {many && (
            <>
              <button type="button" className="pg-nav pg-nav--prev pg-nav--lb"
                onClick={(e) => { e.stopPropagation(); go(-1); }} aria-label="Previous image">‹</button>
              <button type="button" className="pg-nav pg-nav--next pg-nav--lb"
                onClick={(e) => { e.stopPropagation(); go(1); }} aria-label="Next image">›</button>
              <span className="pg-lb-counter">{safeIndex + 1} / {count}</span>
            </>
          )}
        </div>
      )}
    </div>
  );
}
