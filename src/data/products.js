import coffee from "../assets/Coffee.jpeg"
import MousePad from "../assets/MousePad.jpeg"
import cap from "../assets/Cap.jpeg"
import keyChain from "../assets/KeyChain.jpeg"
import pillow from "../assets/Pillow.jpeg"
import bottle from "../assets/Bottle.jpeg"
import frame from "../assets/PhotoFrame.jpeg"
import MDFFrame from "../assets/MDF.jpeg"
import pen from "../assets/Pen.jpeg"
import hanging from "../assets/HangingScrolls.jpeg"
import tShirt from "../assets/Tshirt.jpeg"
import corporate from "../assets/Corporate.jpeg"

export const PRODUCTS = [
  {
    id: 1, emoji: "🍶", name: "Water Bottle", image:bottle,
    desc: "Custom printed steel/sipper bottles. Perfect for daily use and gifting.",
    tag: "Bestseller",
    colors: ["#e63946","#457b9d","#2d6a4f","#f77f00","#9d4edd"],
    features: ["Food-grade stainless steel","Leak-proof cap","500ml / 750ml options","Full wrap printing"],
    variants: [
      { id:"1-1", name:"Slim Steel Sipper",      emoji:"🍶", price:299, desc:"Sleek 500ml stainless steel bottle with single-side photo print.", badge:"Lightweight", colors:["#e63946","#457b9d","#2d6a4f","#f77f00","#9d4edd"] },
      { id:"1-2", name:"Temperature",      emoji:"🥤", price:349, desc:"750ml wide-mouth tumbler with full-wrap 360° custom print.", badge:"Bestseller", colors:["#1a1a1a","#ffffff","#3a86ff","#06d6a0","#9d4edd"] },
      { id:"1-3", name:"Sublimation",  emoji:"♨️", price:499, desc:"Double-wall vacuum flask keeps drinks hot/cold for 12hrs.", badge:"Premium", colors:["#2b2d42","#ef233c","#0077b6","#f77f00","#6b4226"] },
    ],
  },
  {
    id: 2, emoji: "☕", name: "Mug", image:coffee,
    desc: "Magic mugs, photo mugs & more. Start your day with your memories.",
    tag: "Popular",
    colors: ["#ffffff","#ffd700","#ff6b6b","#4ecdc4","#45b7d1"],
    features: ["11oz / 15oz options","Dishwasher safe","Color-changing magic mug","Premium ceramic"],
    variants: [
      { id:"2-1", name:"Classic Photo Mug",       emoji:"☕", price:199, desc:"Standard 11oz white ceramic mug with your photo printed.", badge:"Popular", colors:["#ffffff","#ffd700","#ff6b6b","#4ecdc4","#45b7d1"] },
      { id:"2-2", name:"Magic Color-Change Mug",  emoji:"🌡️", price:299, desc:"Black mug reveals your photo when hot liquid is poured!", badge:"Magic ✨", colors:["#1a1a1a","#2b2d42","#0077b6","#2d6a4f","#9d4edd"] },
      { id:"2-4", name:"Heart Handle Mug",        emoji:"💕", price:279, desc:"Cute heart-shaped handle mug ideal for couples & gifting.", badge:"Gift Fav", colors:["#ff6b6b","#ffc8dd","#fff8e7","#ffffff","#ffd700"] },
      { id:"2-5", name:"Black Patch Mug",     emoji:"🎨", price:259, desc:"Colored body with white patch for sharp photo printing.", badge:"Trendy", colors:["#e63946","#3a86ff","#06d6a0","#9d4edd","#ffd700"] },
    ],
  },
  {
    id: 3, emoji: "👕", name: "T-Shirts", image:tShirt,
    desc: "Premium cotton tees with your design, quote or photo printed.",
    tag: "Custom",
    colors: ["#ffffff","#1a1a1a","#e63946","#3a86ff","#06d6a0"],
    features: ["100% cotton","S / M / L / XL / XXL","Front & back print","Colourfast dye"],
    variants: [
      { id:"3-1", name:"Classic Round-Neck Tee",  emoji:"👕", price:449, desc:"100% cotton round-neck tee with front photo/text print.", badge:"Classic", colors:["#ffffff","#1a1a1a","#e63946","#3a86ff","#06d6a0"] },
      { id:"3-2", name:"Polyester T-Shirt",    emoji:"👔", price:599, desc:"Polo collar tee with left-chest logo + back full print.", badge:"Corporate", colors:["#ffffff","#1a1a1a","#0077b6","#2d6a4f","#2b2d42"] },
    ],
  },
  {
    id: 4, emoji: "📓", name: "Corporate Combo", image:corporate,
    desc: "Branded diaries with your logo & name. Ideal for corporate gifting.",
    tag: "Corporate",
    colors: ["#2b2d42","#8d99ae","#ef233c","#0077b6","#2d6a4f"],
    features: ["A5 / A4 size","Hard-bound cover","160 ruled pages","Name embossing"],
    variants: [
      { id:"4-1", name:"A5 Soft-Cover Diary",     emoji:"📔", price:349, desc:"A5 soft-bound diary with logo printing on cover.", badge:"Compact", colors:["#2b2d42","#0077b6","#2d6a4f","#8d99ae","#ef233c"] },
      { id:"4-2", name:"A5 Hard-Bound Diary",     emoji:"📓", price:499, desc:"A5 hard-bound 160-page diary with name embossing.", badge:"Bestseller", colors:["#1a1a1a","#2b2d42","#8b4513","#0077b6","#2d6a4f"] },
      { id:"4-3", name:"A4 Executive Diary",      emoji:"📒", price:699, desc:"A4 premium hard-bound executive diary with gold foiling.", badge:"Executive", colors:["#1a1a1a","#ffd700","#8b4513","#2b2d42","#0077b6"] },
      { id:"4-4", name:"Spiral Notebook",         emoji:"🗒️", price:279, desc:"A5 spiral-bound 120-page notebook with custom cover.", badge:"Budget", colors:["#ffffff","#3a86ff","#e63946","#06d6a0","#ffd700"] },
      { id:"4-5", name:"Leatherette Journal",     emoji:"📜", price:799, desc:"Faux-leather premium journal with laser-engraved name.", badge:"Luxury", colors:["#6b4226","#8b4513","#1a1a1a","#d4a373","#2b2d42"] },
      { id:"4-6", name:"Bulk Corporate Set (10)", emoji:"🎁", price:3999, desc:"Pack of 10 A5 hard-bound diaries with company branding.", badge:"Bulk Deal", colors:["#1a1a1a","#0077b6","#2d6a4f","#2b2d42","#8d99ae"] },
    ],
  },
  {
    id: 5, emoji: "✒️", name: "Pen", image:pen,
    desc: "Engraved custom pens for personal & corporate use.",
    tag: "Gift",
    colors: ["#ffd700","#1a1a1a","#c0c0c0","#b5838d","#457b9d"],
    features: ["Laser engraving","Metal body","Smooth ink flow","Gift box included"],
    variants: [
      { id:"5-1", name:"Metal Ball Pen",          emoji:"✒️", price:149, desc:"Sleek metal ball pen with laser-engraved name on barrel.", badge:"Classic", colors:["#c0c0c0","#ffd700","#1a1a1a","#457b9d","#b5838d"] },
      { id:"5-2", name:"Roller Ball Pen",         emoji:"🖊️", price:199, desc:"Smooth roller ball pen with custom name engraving.", badge:"Smooth Write", colors:["#1a1a1a","#c0c0c0","#0077b6","#2d6a4f","#ffd700"] },
      { id:"5-3", name:"Wooden Engraved Pen",     emoji:"🪵", price:229, desc:"Natural wood-finish pen with deep laser engraving.", badge:"Eco & Rustic", colors:["#8b4513","#d4a373","#6b4226","#a0522d","#3e1f00"] },
      { id:"5-4", name:"Pen + Diary Combo",       emoji:"🎁", price:599, desc:"Matching custom pen + A5 notebook in a gift box.", badge:"Gift Set", colors:["#1a1a1a","#ffd700","#0077b6","#8b4513","#2b2d42"] },
      { id:"5-5", name:"Gel Pen Set (5 pcs)",     emoji:"🖋️", price:349, desc:"Set of 5 custom gel pens with name print — bulk gifting.", badge:"Set of 5", colors:["#e63946","#3a86ff","#06d6a0","#ffd700","#9d4edd"] },
      { id:"5-6", name:"Executive Fountain Pen",  emoji:"🖋️", price:449, desc:"Premium fountain pen with gold-tip & personalized engraving.", badge:"Luxury", colors:["#ffd700","#1a1a1a","#c0c0c0","#8b4513","#0077b6"] },
    ],
  },
  {
    id: 6, emoji: "🔑", name: "Key Chains", image:keyChain,
    desc: "Photo keychains, name keychains, acrylic & more styles.",
    tag: "Cute",
    colors: ["#ffd700","#e63946","#3a86ff","#2d6a4f","#9d4edd"],
    features: ["Acrylic / metal","Photo printed","Name engraved","Durable clasp"],
    variants: [
      { id:"6-1", name:"Acrylic Photo Keychain",  emoji:"🔑", price:99,  desc:"Clear acrylic keychain with your photo printed inside.", badge:"Popular", colors:["#ffffff","#4ecdc4","#ffd700","#ff6b6b","#c084fc"] },
      { id:"6-2", name:"Metal Name Keychain",     emoji:"🗝️", price:129, desc:"Stainless steel keychain with laser-engraved name.", badge:"Durable", colors:["#c0c0c0","#ffd700","#1a1a1a","#457b9d","#b5838d"] },
      { id:"6-3", name:"Heart-Shape Keychain",    emoji:"💕", price:119, desc:"Heart-shaped acrylic keychain with couple photo print.", badge:"Couples", colors:["#ff6b6b","#ffc8dd","#ffd700","#ffffff","#e63946"] },
      { id:"6-4", name:"Round Epoxy Keychain",    emoji:"🔵", price:109, desc:"Round dome epoxy keychain — glossy finish, vivid colors.", badge:"Glossy", colors:["#3a86ff","#06d6a0","#ffd700","#ff6b6b","#9d4edd"] },
      { id:"6-5", name:"Alphabet Keychain",    emoji:"🟫", price:149, desc:"Faux-leather keychain with debossed name/initials.", badge:"Elegant", colors:["#8b4513","#d4a373","#6b4226","#1a1a1a","#a0522d"] },
      { id:"6-6", name:"MDF Keychain",            emoji:"🪵", price:89,  desc:"Lightweight wooden MDF keychain with laser-cut design.", badge:"Rustic", colors:["#8b4513","#d4a373","#a0522d","#6b4226","#ffd700"] },
    ],
  },
  {
    id: 7, emoji: "🛋️", name: "Pillow", image:pillow,
    desc: "Cosy custom photo pillows for your bedroom or sofa.",
    tag: "Cozy",
    colors: ["#fff8e7","#ffd6ff","#bde0fe","#caffbf","#ffc8dd"],
    features: ["12x12 / 16x16 inch","Soft microfibre","Vibrant print","Pillow cover + insert"],
    variants: [
      { id:"7-1", name:'Square Photo Pillow 12"', emoji:"🛋️", price:399, desc:'12x12 inch soft microfibre pillow with full-face photo print.', badge:"Compact", colors:["#fff8e7","#ffd6ff","#bde0fe","#caffbf","#ffc8dd"] },
      { id:"7-2", name:'Magic Pillow', emoji:"🛋️", price:499, desc:"16x16 inch large pillow — great for sofa & bedroom decor.", badge:"Large", colors:["#fff8e7","#ffd6ff","#bde0fe","#ffc8dd","#c084fc"] },
      { id:"7-3", name:"Heart-Shape Pillow",      emoji:"💕", price:549, desc:"Heart-shaped pillow with couple photo — romantic gift.", badge:"Romantic", colors:["#ff6b6b","#ffc8dd","#ffd700","#fff8e7","#e63946"] },
      { id:"7-4", name:"Emoji Pillow",          emoji:"🖼️", price:599, desc:"Multi-photo collage pillow — fit up to 6 photos in one.", badge:"Collage", colors:["#fff8e7","#ffd6ff","#bde0fe","#caffbf","#ffc8dd"] },
      { id:"7-5", name:"LED Furry Pillow",            emoji:"✍️", price:449, desc:"Custom quote + name on premium satin pillow cover.", badge:"Quote Lover", colors:["#ffffff","#ffd700","#ff6b6b","#9d4edd","#1a1a1a"] },
      { id:"7-6", name:"Printed Only",      emoji:"🎨", price:299, desc:"Pillow cover only (no insert) — custom print, any size.", badge:"Cover Only", colors:["#fff8e7","#ffd6ff","#bde0fe","#caffbf","#ffc8dd"] },
    ],
  },
  {
    id: 8, emoji: "🖼️", name: "Photo Frames", image:frame,
    desc: "Beautiful printed frames in various sizes and finishes.",
    tag: "Memories",
    colors: ["#ffffff","#ffd700","#8b4513","#1a1a1a","#d4a373"],
    features: ["4x6 to 12x18 inch","Wooden / acrylic","Table-top & wall","UV-resistant print"],
    variants: [
      { id:"8-1", name:"Classic Wood Frame 5x7",  emoji:"🖼️", price:299, desc:"5x7 inch wooden frame with photo printed on canvas.", badge:"Classic", colors:["#8b4513","#d4a373","#1a1a1a","#ffffff","#ffd700"] },
      { id:"8-2", name:"Acrylic LED Frame",       emoji:"💡", price:599, desc:"Glowing edge-lit acrylic frame with warm LED lighting.", badge:"Glowing ✨", colors:["#ffffff","#4ecdc4","#45b7d1","#c084fc","#ffd700"] },
      { id:"8-3", name:"Collage Multi-Frame",     emoji:"🗂️", price:499, desc:"Multi-opening frame for 4-6 photos — perfect wall display.", badge:"Multi-Photo", colors:["#1a1a1a","#ffffff","#8b4513","#d4a373","#ffd700"] },
      { id:"8-4", name:"3D Crystal Frame",        emoji:"💎", price:799, desc:"Laser-etched 3D photo inside crystal block — stunning gift.", badge:"Luxury", colors:["#ffffff","#4ecdc4","#45b7d1","#c084fc","#e63946"] },
      { id:"8-5", name:"Magnetic Photo Frame",    emoji:"🧲", price:349, desc:"Fridge/board magnetic frame with printed photo insert.", badge:"Magnetic", colors:["#ffffff","#e63946","#3a86ff","#ffd700","#06d6a0"] },
      { id:"8-6", name:"Large Wall Frame 12x18",  emoji:"🎨", price:799, desc:"Statement 12x18 inch wall frame with premium UV print.", badge:"Wall Art", colors:["#1a1a1a","#8b4513","#ffffff","#d4a373","#ffd700"] },
    ],
  },
  {
    id: 9, emoji: "🪵", name: "MDF Frames", image:MDFFrame,
    desc: "Wooden MDF engraved frames with personalized designs.",
    tag: "Rustic",
    colors: ["#8b4513","#d4a373","#a0522d","#6b4226","#3e1f00"],
    features: ["Laser engraved","Natural wood texture","8x10 / 10x12 inch","Free-standing"],
    variants: [
      { id:"9-1", name:"Simple Name MDF Frame",   emoji:"🪵", price:349, desc:"8x10 MDF frame with laser-engraved name & date.", badge:"Simple", colors:["#8b4513","#d4a373","#a0522d","#6b4226","#3e1f00"] },
      { id:"9-2", name:"Photo + Quote MDF",       emoji:"📜", price:449, desc:"MDF frame with photo print + engraved quote below.", badge:"Popular", colors:["#8b4513","#d4a373","#1a1a1a","#6b4226","#ffd700"] },
      { id:"9-3", name:"Shaped MDF (Heart/Star)", emoji:"⭐", price:399, desc:"Heart or star-shaped MDF with engraved photo + name.", badge:"Shaped", colors:["#8b4513","#d4a373","#e63946","#ffd700","#a0522d"] },
      { id:"9-4", name:"Family Tree MDF",         emoji:"🌳", price:699, desc:"Family tree wall plaque with engraved names per branch.", badge:"Family", colors:["#2d6a4f","#8b4513","#d4a373","#6b4226","#1a1a1a"] },
      { id:"9-5", name:"Large MDF Wall Art 12x16",emoji:"🖼️", price:799, desc:"Large wall-mounted MDF panel with intricate laser-cut art.", badge:"Wall Art", colors:["#8b4513","#d4a373","#1a1a1a","#a0522d","#ffd700"] },
      { id:"9-6", name:"Table-Top MDF Plaque",    emoji:"🏆", price:499, desc:"Freestanding table-top MDF plaque with engraved message.", badge:"Desk Decor", colors:["#8b4513","#ffd700","#1a1a1a","#d4a373","#6b4226"] },
    ],
  },
  {
    id: 10, emoji: "📜", name: "Hanging Scrolls", image:hanging,
    desc: "Custom photo and calligraphy scrolls for wall decor.",
    tag: "Decor",
    colors: ["#fff8e7","#c8b8a2","#a0522d","#e63946","#1a1a1a"],
    features: ["Satin / canvas","Multiple sizes","Ready-to-hang","Premium print quality"],
    variants: [
      { id:"10-1", name:"Single Photo Scroll",     emoji:"📜", price:499, desc:"12x36 inch satin scroll with one large portrait photo.", badge:"Classic", colors:["#fff8e7","#c8b8a2","#a0522d","#1a1a1a","#e63946"] },
      { id:"10-2", name:"Collage Photo Scroll",    emoji:"🖼️", price:649, desc:"Multi-photo collage scroll — up to 8 photos, 18x36 inches.", badge:"Collage", colors:["#fff8e7","#ffd6ff","#bde0fe","#caffbf","#ffc8dd"] },
      { id:"10-3", name:"Calligraphy Quote Scroll",emoji:"✍️", price:549, desc:"Elegant calligraphy-style printed quote with floral borders.", badge:"Elegant", colors:["#fff8e7","#c8b8a2","#a0522d","#ffd700","#e63946"] },
      { id:"10-4", name:"Canvas Roll Painting",    emoji:"🎨", price:799, desc:"Canvas-texture scroll with photo converted to painting style.", badge:"Artistic", colors:["#c8b8a2","#8b4513","#d4a373","#fff8e7","#1a1a1a"] },
      { id:"10-5", name:"Wedding Scroll",          emoji:"💒", price:699, desc:"Wedding photo scroll with date & names — beautiful keepsake.", badge:"Wedding", colors:["#fff8e7","#ffd700","#e63946","#c8b8a2","#ffffff"] },
      { id:"10-6", name:"Bamboo Rod Scroll",       emoji:"🎋", price:599, desc:"Traditional bamboo rod hanging scroll — premium rustic feel.", badge:"Traditional", colors:["#2d6a4f","#8b4513","#d4a373","#fff8e7","#c8b8a2"] },
    ],
  },
  {
    id: 11, emoji: "🖱️", name: "Mouse Pad", image:MousePad,
    desc: "Custom photo and calligraphy scrolls for wall decor.",
    tag: "Decor",
    colors: ["#fff8e7","#c8b8a2","#a0522d","#e63946","#1a1a1a"],
    features: ["Satin / canvas","Multiple sizes","Ready-to-hang","Premium print quality"],
    variants: [
      { id:"10-1", name:"Single Photo Scroll",     emoji:"📜", price:499, desc:"12x36 inch satin scroll with one large portrait photo.", badge:"Classic", colors:["#fff8e7","#c8b8a2","#a0522d","#1a1a1a","#e63946"] },
      { id:"10-2", name:"Collage Photo Scroll",    emoji:"🖼️", price:649, desc:"Multi-photo collage scroll — up to 8 photos, 18x36 inches.", badge:"Collage", colors:["#fff8e7","#ffd6ff","#bde0fe","#caffbf","#ffc8dd"] },
      { id:"10-3", name:"Calligraphy Quote Scroll",emoji:"✍️", price:549, desc:"Elegant calligraphy-style printed quote with floral borders.", badge:"Elegant", colors:["#fff8e7","#c8b8a2","#a0522d","#ffd700","#e63946"] },
      { id:"10-4", name:"Canvas Roll Painting",    emoji:"🎨", price:799, desc:"Canvas-texture scroll with photo converted to painting style.", badge:"Artistic", colors:["#c8b8a2","#8b4513","#d4a373","#fff8e7","#1a1a1a"] },
      { id:"10-5", name:"Wedding Scroll",          emoji:"💒", price:699, desc:"Wedding photo scroll with date & names — beautiful keepsake.", badge:"Wedding", colors:["#fff8e7","#ffd700","#e63946","#c8b8a2","#ffffff"] },
      { id:"10-6", name:"Bamboo Rod Scroll",       emoji:"🎋", price:599, desc:"Traditional bamboo rod hanging scroll — premium rustic feel.", badge:"Traditional", colors:["#2d6a4f","#8b4513","#d4a373","#fff8e7","#c8b8a2"] },
    ],
  },
  {
    id: 11, emoji: "🖱️", name: "Cap", image:cap,
    desc: "Custom photo and calligraphy scrolls for wall decor.",
    tag: "Decor",
    colors: ["#fff8e7","#c8b8a2","#a0522d","#e63946","#1a1a1a"],
    features: ["Satin / canvas","Multiple sizes","Ready-to-hang","Premium print quality"],
    variants: [
      { id:"10-1", name:"Single Photo Scroll",     emoji:"📜", price:499, desc:"12x36 inch satin scroll with one large portrait photo.", badge:"Classic", colors:["#fff8e7","#c8b8a2","#a0522d","#1a1a1a","#e63946"] },
      { id:"10-2", name:"Collage Photo Scroll",    emoji:"🖼️", price:649, desc:"Multi-photo collage scroll — up to 8 photos, 18x36 inches.", badge:"Collage", colors:["#fff8e7","#ffd6ff","#bde0fe","#caffbf","#ffc8dd"] },
      { id:"10-3", name:"Calligraphy Quote Scroll",emoji:"✍️", price:549, desc:"Elegant calligraphy-style printed quote with floral borders.", badge:"Elegant", colors:["#fff8e7","#c8b8a2","#a0522d","#ffd700","#e63946"] },
      { id:"10-4", name:"Canvas Roll Painting",    emoji:"🎨", price:799, desc:"Canvas-texture scroll with photo converted to painting style.", badge:"Artistic", colors:["#c8b8a2","#8b4513","#d4a373","#fff8e7","#1a1a1a"] },
      { id:"10-5", name:"Wedding Scroll",          emoji:"💒", price:699, desc:"Wedding photo scroll with date & names — beautiful keepsake.", badge:"Wedding", colors:["#fff8e7","#ffd700","#e63946","#c8b8a2","#ffffff"] },
      { id:"10-6", name:"Bamboo Rod Scroll",       emoji:"🎋", price:599, desc:"Traditional bamboo rod hanging scroll — premium rustic feel.", badge:"Traditional", colors:["#2d6a4f","#8b4513","#d4a373","#fff8e7","#c8b8a2"] },
    ],
  },
];

export const STEPS = [
  { num:"1", icon:"🛍️", title:"Choose Your Product", desc:"Browse our wide range of customizable products and pick the one that speaks to you." },
  { num:"2", icon:"🎨", title:"Personalize It",       desc:"Add your name, photo, message or design. Our tool makes it super easy." },
  { num:"3", icon:"🔨", title:"We Craft It",          desc:"Our skilled team brings your vision to life with premium materials and precision." },
  { num:"4", icon:"🚚", title:"Delivered to You",     desc:"Your custom creation is carefully packed and shipped right to your doorstep." },
];

export const WHY_CARDS = [
  { icon:"💎", title:"Premium Quality",      desc:"We use top-grade materials ensuring your product looks great and lasts long." },
  { icon:"🎨", title:"Unlimited Creativity", desc:"No design limit. Upload photos, add text, pick colors — make it truly yours." },
  { icon:"⚡", title:"Fast Turnaround",      desc:"Express production options available. Never miss a gifting deadline." },
  { icon:"🤝", title:"100% Satisfaction",    desc:"If you are not happy, we will redo it. Your smile is our mission." },
  { icon:"🎁", title:"Perfect for Gifting",  desc:"Birthdays, anniversaries, corporate events — personalized gifts always wow." },
  { icon:"🔒", title:"Secure Ordering",      desc:"Your data and payment are always safe with us. Order with confidence." },
];

export const TESTIMONIALS = [
  { stars:5, text:"Ordered a custom mug for my mom's birthday and she absolutely loved it! The quality was outstanding and delivery was super fast. Hridaya Creations never disappoints!", name:"Priya Sharma",    loc:"Mumbai", avatar:"👩" },
  { stars:5, text:"Got 50 corporate diaries made for our annual conference. The branding was crisp, the quality premium, and the team was very helpful throughout. Highly recommended!",  name:"Rahul Mehta",    loc:"Pune",   avatar:"👨" },
  { stars:5, text:"The photo pillow I ordered was so beautiful, exactly as I imagined. The colors were vibrant and the material was super soft. Will definitely order again!",            name:"Sneha Kulkarni", loc:"Nagpur", avatar:"👩‍🦱" },
];

export const NAV_LINKS = [
  { label:"Products",     id:"products" },
  { label:"How It Works", id:"how" },
  { label:"Why Us",       id:"why" },
  { label:"Testimonials", id:"testi" },
];
