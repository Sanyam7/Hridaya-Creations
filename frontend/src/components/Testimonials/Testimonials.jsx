import { TESTIMONIALS } from "../../data/products";
import "./Testimonials.css";

export default function Testimonials() {
  return (
    <section className="testi-section" id="testi">
      <div className="section-label">✦ Customer Love ✦</div>
      <h2 className="section-title">What Our Customers Say</h2>
      <p className="section-desc">
        Real stories from real people who turned their moments into memories with us.
      </p>

      <div className="testi-grid">
        {TESTIMONIALS.map((t, i) => (
          <div className="testi-card" key={i}>
            <div className="testi-stars">{"★".repeat(t.stars)}</div>
            <p className="testi-text">"{t.text}"</p>
            <div className="testi-author">
              <div className="testi-avatar">{t.avatar}</div>
              <div>
                <div className="testi-name">{t.name}</div>
                <div className="testi-loc">📍 {t.loc}</div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
