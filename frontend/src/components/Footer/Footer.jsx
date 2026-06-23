import { Link } from "react-router-dom";
import { PRODUCTS } from "../../data/products";
import "./Footer.css";

export default function Footer() {
  const scrollTo = (id) =>
    document.getElementById(id)?.scrollIntoView({ behavior: "smooth" });

  return (
    <footer className="footer">
      <div className="footer-inner">

        {/* Brand */}
        <div className="footer-brand">
          <div className="footer-logo">Hridaya Creations</div>
          <p className="footer-tagline">
            Turning your memories into meaningful, personalized gifts crafted
            with love and precision. Based in Nagpur, delivering across India.
          </p>
          <div className="footer-socials">
            {["📸 Instagram","💬 WhatsApp","📘 Facebook"].map(s => (
              <a key={s} href="#" className="social-link">{s}</a>
            ))}
          </div>
        </div>

        {/* Products col 1 */}
        <div className="footer-col">
          <h4>Products</h4>
          <ul>
            {PRODUCTS.slice(0, 5).map(p => (
              <li key={p.id}>
                <button className="footer-link" onClick={() => scrollTo("products")}>
                  {p.emoji} {p.name}
                </button>
              </li>
            ))}
          </ul>
        </div>

        {/* Products col 2 */}
        <div className="footer-col">
          <h4>More Products</h4>
          <ul>
            {PRODUCTS.slice(5).map(p => (
              <li key={p.id}>
                <button className="footer-link" onClick={() => scrollTo("products")}>
                  {p.emoji} {p.name}
                </button>
              </li>
            ))}
          </ul>
        </div>

        {/* Contact */}
        <div className="footer-col">
          <h4>Get in Touch</h4>
          <ul>
            <li><a href="mailto:hello@hridayacreations.in" className="footer-link">📧 hello@hridayacreations.in</a></li>
            <li><a href="tel:+919876543210" className="footer-link">📱 +91 98765 43210</a></li>
            <li><span className="footer-link-plain">📍 Yavatmal, Maharashtra</span></li>
            <li><span className="footer-link-plain">🕐 Available 24/7</span></li>
          </ul>
          <div className="footer-badges">
            <span className="f-badge">🔒 Secure Checkout</span>
            <span className="f-badge">✅ 100% Genuine</span>
          </div>
        </div>

      </div>

      <div className="footer-bottom">
        <span>© {new Date().getFullYear()} Hridaya Creations. All rights reserved.</span>
        <span>Made with <span className="heart">♥</span> in India</span>
      </div>
    </footer>
  );
}
