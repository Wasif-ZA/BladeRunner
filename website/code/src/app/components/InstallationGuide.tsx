import React from "react";

const InstallationGuide: React.FC = () => {
  return (
    <section className="p-8 bg-white text-gray-900">
      <h2 className="text-2xl font-semibold mb-4">Installation</h2>
      <ol className="list-decimal ml-6 space-y-2">
        <li>Open your terminal and run: <code>git clone https://github.com/Wasif-ZA/BladeRunner.git</code></li>
        <li>Navigate to the CCP directory: <code>cd BladeRunner/CCP</code></li>
        <li>Open the project in VS Code and install the Java Extension Pack.</li>
        <li>Add the JSON library to the classpath.</li>
      </ol>
    </section>
  );
};

export default InstallationGuide;
