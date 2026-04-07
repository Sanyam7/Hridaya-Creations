import { STEPS } from "../../data/products";
import "./HowItWorks.css";

export default function HowItWorks() {
  return (
    <section className="how-section" id="how">
      <div className="section-label">✦ Simple Process ✦</div>
      <h2 className="section-title">How It Works</h2>
      <p className="section-desc">
        Getting your personalized creation is as easy as 1-2-3-4.
      </p>

      <div className="steps-grid">
        {STEPS.map((step, i) => (
          <div className="step-card" key={step.num}>
            <div className="step-num">{step.num}</div>
            <div className="step-icon">{step.icon}</div>
            <h3 className="step-title">{step.title}</h3>
            <p className="step-desc">{step.desc}</p>
            {i < STEPS.length - 1 && <div className="step-arrow">→</div>}
          </div>
        ))}
      </div>
    </section>
  );
}
