import React from "react";

const ProjectStructure: React.FC = () => {
  return (
    <section className="p-8 bg-white text-gray-900">
      <h2 className="text-2xl font-semibold mb-4">Project Structure</h2>
      <pre className="bg-gray-100 p-4 rounded-md overflow-x-auto">
        {`BladeRunner/
├── CCP/
│   ├── lib/
│   │   └── json-20240303.jar
│   ├── CCP.java
│   ├── CommunicationHandler.java
│   ├── JSONProcessor.java
│   ├── MCP.java
│   ├── MessageListener.java
│   ├── StateManager.java
│   └── UDPCommunicationHandler.java
│
├── DOCs/
│   ├── MethodDocs.md
│   ├── Setup.md
│   ├── T3_C1_Design_Document.docx
│   └── T3_C1_Scoping_Document.docx
│
└── .vscode/
    └── settings.json`}
      </pre>
      <p className="mt-4">
        The project is organized into directories for Java source files, documentation, and VS Code settings.
      </p>
    </section>
  );
};

export default ProjectStructure;
