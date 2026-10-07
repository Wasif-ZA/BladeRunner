"use client";
import React from "react";
import { motion } from "framer-motion";
import ProjectOverview from "./ProjectOverview";
import TableOfContents from "./TableOfContents";
import ProjectStructure from "./ProjectStructure";
import Requirements from "./Requirements";
import InstallationGuide from "./InstallationGuide";
import CodeExplanation from "./CodeExplanation";
import CodeDemo from "./CodeDemo";

const BladeRunnerPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-neutral-950 text-gray-300 selection:bg-cyan-500/30 overflow-hidden relative">

      {/* Background Tech Mesh (Subtle) */}
      <div className="fixed inset-0 z-0 pointer-events-none opacity-20"
        style={{ backgroundImage: 'radial-gradient(circle at 1px 1px, rgba(255,255,255,0.1) 1px, transparent 0)', backgroundSize: '40px 40px' }}>
      </div>

      <div className="relative z-10 space-y-24 pb-24 pt-12">

        {/* Project Overview Section */}
        <Section>
          <ProjectOverview />
        </Section>

        {/* Live System Demo - The "Feature" Section */}
        <section id="code-demo" className="scroll-mt-24">
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            whileInView={{ opacity: 1, scale: 1 }}
            viewport={{ once: true, margin: "-100px" }}
            transition={{ duration: 0.6 }}
            className="max-w-7xl mx-auto px-4"
          >
            <div className="border border-cyan-900/30 bg-neutral-900/40 backdrop-blur-sm rounded-xl p-8 shadow-[0_0_50px_-12px_rgba(6,182,212,0.15)]">
              <div className="text-center mb-10">
                <h2 className="text-3xl md:text-4xl font-black text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-500 uppercase tracking-widest mb-4 font-mono">
                  Live Simulation
                </h2>
                <div className="h-1 w-24 bg-cyan-500/50 mx-auto mb-6 rounded-full" />
                <p className="text-neutral-400 max-w-2xl mx-auto font-light text-lg">
                  An in-browser model of the <span className="text-cyan-400 font-mono">CCP.java</span> state machine.
                  It opens no network connection; run the Java CCP and MCP locally to see real UDP traffic.
                </p>
              </div>

              <CodeDemo />
            </div>
          </motion.div>
        </section>

        {/* Technical Deep Dive Sections */}
        <div className="max-w-7xl mx-auto px-4 space-y-24">
          <Section delay={0.1}>
            <TableOfContents />
          </Section>

          <Section delay={0.2}>
            <CodeExplanation />
          </Section>

          <Section delay={0.3}>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">
              <ProjectStructure />
              <Requirements />
            </div>
          </Section>

          <Section delay={0.4}>
            <InstallationGuide />
          </Section>
        </div>
      </div>
    </div>
  );
};

// Helper Component for consistent section reveals
const Section: React.FC<{ children: React.ReactNode; delay?: number }> = ({ children, delay = 0 }) => (
  <motion.div
    initial={{ opacity: 0, y: 50 }}
    whileInView={{ opacity: 1, y: 0 }}
    viewport={{ once: true, margin: "-50px" }}
    transition={{ duration: 0.7, delay, ease: "easeOut" }}
  >
    {children}
  </motion.div>
);

export default BladeRunnerPage;
