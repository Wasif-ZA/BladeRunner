import React from "react";

const Requirements: React.FC = () => {
  return (
    <section className="p-8 bg-gray-100 text-gray-900">
      <h2 className="text-2xl font-semibold mb-4">Requirements</h2>
      <div className="mb-4">
        <h3 className="text-xl font-medium">Hardware Requirements</h3>
        <ul className="list-disc ml-6 mt-2">
          <li>ESP32: The primary hardware device for carriage control and networking.</li>
          <li>Sensors: As required (e.g., ultrasonic sensors or IR photodiodes for alignment).</li>
        </ul>
      </div>
      <div>
        <h3 className="text-xl font-medium">Software Requirements</h3>
        <ul className="list-disc ml-6 mt-2">
          <li>Java Development Kit (JDK) version 11 or later.</li>
          <li>Visual Studio Code (VS Code) with Java Extension Pack.</li>
          <li>ESP32 development tools (if programming the ESP32).</li>
          <li>Wi-Fi network for communication.</li>
          <li><code>org.json</code> library for JSON processing.</li>
        </ul>
      </div>
    </section>
  );
};

export default Requirements;
