"use client";
import React, { useState } from "react";
import { motion } from "framer-motion";

const ProjectStructure: React.FC = () => {
  return (
    <section id="architecture" className="h-full">
      <div className="h-full border border-cyan-900/30 bg-black/40 backdrop-blur-md rounded-xl p-8 shadow-[0_0_30px_-10px_rgba(6,182,212,0.1)] flex flex-col">

        {/* Header */}
        <div className="flex items-center justify-between mb-6 border-b border-white/10 pb-4">
          <div className="flex items-center gap-3">
            <div className="w-3 h-3 rounded-sm bg-cyan-900 border border-cyan-500 shadow-[0_0_8px_rgba(6,182,212,0.5)]" />
            <h2 className="text-xl font-mono uppercase tracking-widest text-cyan-400">
              System Directory
            </h2>
          </div>
          <span className="text-xs font-mono text-gray-500">ROOT: ~/BladeRunner</span>
        </div>

        {/* Tree Visualizer */}
        <div className="flex-grow font-mono text-sm overflow-hidden relative">
          {/* Connecting Line for the main tree */}
          <div className="absolute left-[21px] top-8 bottom-4 w-px bg-gradient-to-b from-cyan-900/50 to-transparent" />

          <div className="space-y-1">
            <Folder name="CCP" desc="Java Core Logic" isOpen={true}>
              <File name="CCP.java" type="java" />
              <File name="MCP.java" type="java" />
              <File name="CommunicationHandler.java" type="java" />
              <File name="UDPCommunicationHandler.java" type="java" />
              <File name="StateManager.java" type="java" />
            </Folder>

            <Folder name="Carriage" desc="Embedded C++ (Arduino)">
              <File name="Carriage.ino" type="cpp" />
              <File name="Command.ino" type="cpp" />
              <File name="Server.ino" type="cpp" />
            </Folder>

            <Folder name="ESP" desc="Hardware Layer (ESP32)">
              <File name="espcode.ino" type="cpp" />
            </Folder>

            <Folder name="website/code" desc="Next.js Dashboard">
              <File name="src/app/page.tsx" type="react" />
              <File name="tailwind.config.ts" type="ts" />
            </Folder>

            <Folder name="docs" desc="System Archives">
              <File name="ARCHITECTURE.md" type="md" />
              <File name="legacy/MethodDocs.md" type="md" />
            </Folder>

            <Folder name=".vscode" desc="Config">
              <File name="settings.json" type="json" />
            </Folder>
          </div>
        </div>

        {/* Footer Status */}
        <div className="mt-6 pt-4 border-t border-white/10 flex justify-between items-center text-xs font-mono text-gray-600">
          <span>Total Modules: 6</span>
          <span className="text-cyan-600 animate-pulse">● SYNCED</span>
        </div>

      </div>
    </section>
  );
};

// --- Helper Components ---

const Folder: React.FC<{ name: string; desc?: string; children: React.ReactNode; isOpen?: boolean }> = ({ name, desc, children, isOpen = false }) => {
  const [open, setOpen] = useState(isOpen);

  return (
    <div className="relative">
      <div
        onClick={() => setOpen(!open)}
        className="flex items-center gap-2 py-1 cursor-pointer group hover:bg-white/5 rounded px-2 -ml-2 transition-colors"
      >
        <span className={`text-cyan-500 transition-transform ${open ? 'rotate-90' : ''}`}>▶</span>
        <span className="text-blue-300 font-bold group-hover:text-cyan-300 transition-colors">{name}/</span>
        {desc && <span className="text-gray-600 text-[10px] ml-auto opacity-0 group-hover:opacity-100 transition-opacity uppercase tracking-wider">// {desc}</span>}
      </div>

      {open && (
        <motion.div
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: 'auto' }}
          className="ml-5 pl-4 border-l border-cyan-900/30"
        >
          {children}
        </motion.div>
      )}
    </div>
  );
};

const File: React.FC<{ name: string; type: 'java' | 'cpp' | 'ts' | 'react' | 'json' | 'md' | 'doc' }> = ({ name, type }) => {
  const getColor = (t: string) => {
    switch (t) {
      case 'java': return 'text-orange-400';
      case 'cpp': return 'text-blue-400';
      case 'react': return 'text-cyan-300';
      case 'ts': return 'text-blue-300';
      case 'json': return 'text-yellow-300';
      case 'md': return 'text-gray-400';
      default: return 'text-gray-300';
    }
  };

  return (
    <div className="flex items-center gap-2 py-1 hover:text-white transition-colors cursor-default text-gray-400">
      <span className="w-4 h-[1px] bg-gray-700" />
      <span className={`text-xs ${getColor(type)} opacity-80`}>[{type.toUpperCase()}]</span>
      <span>{name}</span>
    </div>
  );
};

export default ProjectStructure;
