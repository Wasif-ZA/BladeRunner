import React from "react";
import ProjectOverview from "./ProjectOverview";
import TableOfContents from "./TableOfContents";
import ProjectStructure from "./ProjectStructure";
import Requirements from "./Requirements";
import InstallationGuide from "./InstallationGuide";

const BladeRunnerPage: React.FC = () => {
  return (
    <div className="bg-gray-50">
      <ProjectOverview />
      <TableOfContents />
      <ProjectStructure />
      <Requirements />
      <InstallationGuide />
      {/* Add more components for other sections as needed */}
    </div>
  );
};

export default BladeRunnerPage;
