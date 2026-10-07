"use client";
import React from "react";
import { motion } from "framer-motion";

const ProjectOverview: React.FC = () => {
  const features = [
    { label: "Architecture", value: "Distributed IoT" },
    { label: "Transport", value: "UDP unicast" },
    { label: "Safety", value: "Fail-Safe Logic" },
    { label: "Protocol", value: "Custom JSON" },
  ];

  return (
    <section id="project-overview" className="max-w-7xl mx-auto px-4 pt-12">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8 items-start">

        {/* Main Description */}
        <div className="md:col-span-2 space-y-6">
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5 }}
          >
            <h2 className="text-cyan-500 font-mono text-sm tracking-widest uppercase mb-2">
              // System Abstract
            </h2>
            <h3 className="text-3xl md:text-4xl font-bold text-white mb-6 leading-tight">
              Autonomous Carriage Control <br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-600">
                Network & Telemetry
              </span>
            </h3>

            <div className="text-gray-400 text-lg leading-relaxed border-l-2 border-cyan-900/50 pl-6 space-y-4">
              <p>
                The BladeRunner Project is a high-fidelity simulation of an industrial transportation system.
                It orchestrates real-time communication between a central
                <span className="text-cyan-300 font-mono text-sm mx-1">MCP</span> (Master Control Processor)
                and distributed
                <span className="text-cyan-300 font-mono text-sm mx-1">CCP</span> (Carriage Control Processor) nodes.
              </p>
              <p>
                By leveraging <strong className="text-white font-normal">UDP messaging</strong> and <strong className="text-white font-normal">ESP32 hardware integration</strong>,
                the system coordinates carriage movement, door actuation, and hazard detection.
              </p>
            </div>
          </motion.div>
        </div>

        {/* Stats / Tech Stack Grid */}
        <div className="md:col-span-1">
          <div className="grid grid-cols-1 gap-3">
            {features.map((feature, idx) => (
              <motion.div
                key={feature.label}
                initial={{ opacity: 0, y: 10 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ delay: idx * 0.1, duration: 0.4 }}
                className="bg-white/5 border border-white/10 p-4 rounded-lg flex items-center justify-between group hover:border-cyan-500/30 transition-colors backdrop-blur-sm"
              >
                <span className="text-gray-500 text-xs font-mono uppercase tracking-wider">{feature.label}</span>
                <span className="text-cyan-400 font-bold font-mono">{feature.value}</span>
              </motion.div>
            ))}
          </div>

          {/* Tech Stack Tags */}
          <motion.div
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: true }}
            transition={{ delay: 0.5 }}
            className="mt-6 pt-6 border-t border-white/10"
          >
            <h4 className="text-xs text-gray-500 uppercase font-mono mb-3">Core Technologies</h4>
            <div className="flex flex-wrap gap-2">
              {["Java", "C++", "Next.js", "UDP", "ESP32", "Tailwind"].map((tech) => (
                <span key={tech} className="px-3 py-1 bg-black border border-gray-800 rounded text-xs text-gray-300 font-mono hover:text-cyan-400 hover:border-cyan-500/50 transition-colors cursor-default">
                  {tech}
                </span>
              ))}
            </div>
          </motion.div>
        </div>

      </div>
    </section>
  );
};

export default ProjectOverview;
