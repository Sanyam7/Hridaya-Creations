import Hero from "../components/Hero/Hero";
import Stats from "../components/Stats/Stats";
import Products from "../components/Products/Products";
import HowItWorks from "../components/HowItWorks/HowItWorks";
import WhyUs from "../components/WhyUs/WhyUs";
import Testimonials from "../components/Testimonials/Testimonials";
import CTA from "../components/CTA/CTA";

export default function HomePage() {
  return (
    <>
      <Hero />
      <Stats />
      <Products />
      <HowItWorks />
      <WhyUs />
      <Testimonials />
      <CTA />
    </>
  );
}
