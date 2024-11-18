import React from "react";

const ProjectOverview: React.FC = () => {
  return (
    <section className="p-8 bg-white text-gray-900">
      <h1 className="text-4xl font-bold mb-4">BladeRunner Project</h1>
      <p className="text-lg">
        This project simulates a Carriage Control System using an ESP32 microcontroller, MCP (Master Control Processor), and CCP (Carriage Control Processor). It includes multiple modules for handling different aspects of the system, such as controlling the carriage, processing commands, managing network connections, and logging events.
      </p>
    </section>
  );
};

export default ProjectOverview;
