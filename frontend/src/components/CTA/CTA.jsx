import "./CTA.css";

export default function CTA() {
  const scrollTo = (id) =>
    document.getElementById(id)?.scrollIntoView({ behavior: "smooth" });

  return (
    <section className="cta-section">
      <div className="cta-orb cta-orb1" />
      <div className="cta-orb cta-orb2" />
      <div className="cta-content">
        <h2 className="cta-title">Ready to Create Something Special?</h2>
        <p className="cta-desc">
          Every gift you give is a piece of your heart. Let us help you make it
          unforgettable. Browse our collection and start customizing today!
        </p>
        <div className="cta-btns">
          <button className="btn-primary" onClick={() => scrollTo("products")}>
            Start Customizing 🎨
          </button>
          <button className="btn-outline" onClick={() => scrollTo("how")}>
            Learn How It Works
          </button>
        </div>
        <div className="cta-note">🚚 Fast delivery &nbsp;·&nbsp; 💎 Premium quality &nbsp;·&nbsp; 🎁 Perfect gifting</div>
      </div>
    </section>
  );
}
