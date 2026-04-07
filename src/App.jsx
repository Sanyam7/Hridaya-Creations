import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { CartProvider } from "./context/CartContext";
import Navbar   from "./components/Navbar/Navbar";
import Footer   from "./components/Footer/Footer";
import HomePage  from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import CartPage  from "./pages/CartPage";
import "./styles/global.css";

function Layout({ children }) {
  return (
    <>
      <Navbar />
      <main>{children}</main>
      <Footer />
    </>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <Routes>
            {/* Auth pages — no footer/navbar chrome */}
            <Route path="/login"  element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />

            {/* Main app pages */}
            <Route path="/" element={
              <Layout><HomePage /></Layout>
            }/>
            <Route path="/cart" element={
              <Layout><CartPage /></Layout>
            }/>

            {/* Fallback */}
            <Route path="*" element={
              <Layout>
                <div style={{ minHeight:"60vh", display:"flex", flexDirection:"column", alignItems:"center", justifyContent:"center", gap:"1rem" }}>
                  <div style={{ fontSize:"4rem" }}>😕</div>
                  <h2 style={{ fontFamily:"'Playfair Display',serif", fontSize:"2rem", color:"var(--pink)" }}>Page Not Found</h2>
                  <a href="/" style={{ color:"var(--pink-light)", textDecoration:"none" }}>← Back to Home</a>
                </div>
              </Layout>
            }/>
          </Routes>
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
