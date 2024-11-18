// src/app/layout.tsx
import React from 'react';
import './globals.css';

const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const childrenArray = React.Children.toArray(children);

  return (
    <html lang="en" className="h-full w-full bg-white">
      <head>
        <title>My App</title>
        <meta name="description" content="My Next.js App" />
      </head>
      <body className="h-full w-full bg-white text-black overflow-x-hidden">
        {/* Full-width Hero Component without constraints */}
        <div className="w-full">{childrenArray[0]}</div>

        {/* Constrained layout for the rest of the content */}
        <main className="w-full max-w-7xl mx-auto px-5 sm:px-10 md:px-12 lg:px-5">
          {childrenArray.slice(1)}
        </main>
      </body>
    </html>
  );
};

export default Layout;
