import { WHY_CARDS } from "../../data/products";
import "./WhyUs.css";

export default function WhyUs() {
  return (
    <section className="why-section" id="why">
      <div className="section-label">✦ Our Promise ✦</div>
      <h2 className="section-title">Why Choose Hridaya?</h2>
      <p className="section-desc">We pour our heart into every creation. Here's what makes us different.</p>

      <div className="why-grid">
        {WHY_CARDS.map(card => (
          <div className="why-card" key={card.title}>
            <div className="why-icon">{card.icon}</div>
            <h3 className="why-title">{card.title}</h3>
            <p className="why-desc">{card.desc}</p>
          </div>
        ))}
      </div>
    </section>
  );
}
