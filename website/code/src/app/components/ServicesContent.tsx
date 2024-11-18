"use client";
import React, { useRef } from "react";
import { motion, useScroll, useTransform } from "framer-motion";
import { FiArrowUpRight } from "react-icons/fi";

// Props Interface for individual content items
interface ContentPagesProps {
  imgUrl: string;
  subheading: string;
  heading: string;
  description: string;
  buttonText: string;
  extraText?: string;
  href?: string;
}

// Props Interface for the main ServicesContent component
interface ServicesContentProps {
  title: string;
  contentData: ContentPagesProps[];
}

const IMG_PADDING = 12;

// Reusable TextParallaxContent Component
const TextParallaxContent: React.FC<ContentPagesProps> = ({
  imgUrl,
  subheading,
  heading,
  description,
  buttonText,
  extraText,
  href,
}) => {
  return (
    <div style={{ paddingLeft: IMG_PADDING, paddingRight: IMG_PADDING }}>
      <div className="relative h-[150vh]">
        <StickyImage imgUrl={imgUrl} />
        <OverlayCopy heading={heading} subheading={subheading} />
      </div>
      <ExampleContent
        description={description}
        buttonText={buttonText}
        extraText={extraText}
        href={href}
      />
    </div>
  );
};

// StickyImage Component
interface StickyImageProps {
  imgUrl: string;
}

const StickyImage: React.FC<StickyImageProps> = ({ imgUrl }) => {
  const targetRef = useRef<HTMLDivElement>(null);
  const { scrollYProgress } = useScroll({
    target: targetRef,
    offset: ["end end", "end start"],
  });

  const scale = useTransform(scrollYProgress, [0, 1], [1, 0.85]);
  const opacity = useTransform(scrollYProgress, [0, 1], [1, 0]);

  return (
    <motion.div
      style={{
        backgroundImage: `url(${imgUrl})`,
        backgroundSize: "cover",
        backgroundPosition: "center",
        height: `calc(100vh - ${IMG_PADDING * 2}px)`,
        top: IMG_PADDING,
        scale,
      }}
      ref={targetRef}
      className="sticky z-0 overflow-hidden rounded-3xl"
    >
      <motion.div className="absolute inset-0 bg-neutral-950/70" style={{ opacity }} />
    </motion.div>
  );
};

// OverlayCopy Component
interface OverlayCopyProps {
  subheading: string;
  heading: string;
}

const OverlayCopy: React.FC<OverlayCopyProps> = ({ subheading, heading }) => {
  const targetRef = useRef<HTMLDivElement>(null);
  const { scrollYProgress } = useScroll({
    target: targetRef,
    offset: ["start end", "end start"],
  });

  const y = useTransform(scrollYProgress, [0, 1], [250, -250]);
  const opacity = useTransform(scrollYProgress, [0.25, 0.5, 0.75], [0, 1, 0]);

  return (
    <motion.div
      style={{ y, opacity }}
      ref={targetRef}
      className="absolute left-0 top-0 flex h-screen w-full flex-col items-center justify-center text-white"
    >
      <p className="mb-2 text-center text-xl md:mb-4 md:text-3xl">{subheading}</p>
      <p className="text-center text-4xl font-bold md:text-7xl">{heading}</p>
    </motion.div>
  );
};

// ExampleContent Component with Dynamic Props
interface ExampleContentProps {
  description: string;
  buttonText: string;
  extraText?: string;
  href?: string;
}

const ExampleContent: React.FC<ExampleContentProps> = ({
  description,
  buttonText,
  extraText,
  href,
}) => (
  <div className="mx-auto grid max-w-5xl grid-cols-1 gap-8 px-4 pb-24 pt-12 md:grid-cols-12">
    <h2 className="col-span-1 text-3xl font-bold md:col-span-4">
      {extraText || "Additional content explaining the above card here"}
    </h2>
    <div className="col-span-1 md:col-span-8">
      <p className="mb-4 text-xl text-neutral-600 md:text-2xl">{description}</p>
      <a href={href} className="w-full rounded bg-neutral-900 px-9 py-4 text-xl text-white transition-colors hover:bg-neutral-700 md:w-fit">
        {buttonText} <FiArrowUpRight className="inline" />
      </a>
    </div>
  </div>
);

const ServicesContent: React.FC<ServicesContentProps> = ({ title, contentData }) => {
  return (
    <section
      className="relative w-full overflow-hidden bg-black"
      style={{
        backgroundImage: `url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32' width='32' height='32' fill='none' stroke='%23171717'%3e%3cpath d='M0 .5H31.5V32'/%3e%3c/svg%3e")`,
        backgroundSize: "32px 32px",
      }}
    >
      <h1 className="text-white text-4xl text-center py-8">{title}</h1>
      <div className="container mx-auto">
        {contentData.map((content, index) => (
          <TextParallaxContent key={index} {...content} />
        ))}
      </div>
    </section>
  );
};

export default ServicesContent;
