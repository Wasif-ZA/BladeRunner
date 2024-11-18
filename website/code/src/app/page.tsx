import React from "react";
import Hero from "./components/Hero";
// import Navbar from "./components/navbar";
import BladeRunnerPage from "./components/BladeRunnerPage";

const HomePage: React.FC = () => {
  return (
    <div>
      {/* <Navbar /> */}
      <Hero /> {/* Only the Navbar is rendered */}
      <BladeRunnerPage />
    </div>
  );
};

export default HomePage;
