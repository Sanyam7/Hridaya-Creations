import "./Stats.css";

const STATS = [
  ["500+", "Happy Customers"],
  ["10+",  "Unique Products"],
  ["100%", "Customizable"],
  ["5★",   "Avg. Rating"],
];

export default function Stats() {
  return (
    <section className="stats-section">
      <div className="stats-grid">
        {STATS.map(([num, label]) => (
          <div className="stat-item" key={label}>
            <div className="stat-number">{num}</div>
            <div className="stat-label">{label}</div>
          </div>
        ))}
      </div>
    </section>
  );
}
