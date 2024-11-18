import React from "react";

const TableOfContents: React.FC = () => {
  return (
    <nav className="p-8 bg-gray-100 text-gray-900">
      <h2 className="text-2xl font-semibold mb-4">Table of Contents</h2>
      <ul className="list-disc ml-6">
        <li><a href="#project-structure">Project Structure</a></li>
        <li><a href="#hardware-requirements">Hardware Requirements</a></li>
        <li><a href="#software-requirements">Software Requirements</a></li>
        <li><a href="#installation">Installation</a></li>
        <li><a href="#usage">Usage</a></li>
        <li><a href="#running-the-program">Running the Program</a></li>
        <li><a href="#troubleshooting">Troubleshooting</a></li>
        <li><a href="#java-integration">Java Integration for JSON Processing</a></li>
        <li><a href="#license">License</a></li>
      </ul>
    </nav>
  );
};

export default TableOfContents;
