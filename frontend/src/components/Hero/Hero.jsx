import { useNavigate } from "react-router-dom";
import "./Hero.css";

const BADGES = ["10+ Products", "100% Customizable", "Fast Delivery", "Made with Love 💕"];

export default function Hero() {
  const navigate = useNavigate();

  const scrollTo = (id) =>
    document.getElementById(id)?.scrollIntoView({ behavior: "smooth" });

  return (
    <section className="hero">
      <div className="hero-bg" />
      <div className="hero-orb orb1" />
      <div className="hero-orb orb2" />
      <div className="hero-orb orb3" />

      <div className="hero-content">
        <span className="hero-eyebrow">✦ Personalized Gifting Studio ✦</span>
        <h1 className="hero-title">Hridaya Creations</h1>
        <p className="hero-sub">Where Every Gift Tells Your Story</p>
        <p className="hero-desc">
          From custom mugs to engraved frames — we turn your memories, messages and
          moments into beautifully crafted keepsakes that last a lifetime.
        </p>

        <div className="hero-btns">
          <button className="btn-primary" onClick={() => scrollTo("products")}>
            Explore Products 🎁
          </button>
          <button className="btn-outline" onClick={() => scrollTo("how")}>
            How It Works
          </button>
        </div>

        <div className="hero-badges">
          {BADGES.map(b => (
            <span className="badge" key={b}>
              <span className="badge-dot" /> {b}
            </span>
          ))}
        </div>
      </div>

      {/* Floating emoji decorations */}
      <span className="hero-deco deco1">🎁</span>
      <span className="hero-deco deco2">⭐</span>
      <span className="hero-deco deco3">💕</span>
    </section>
  );
}
