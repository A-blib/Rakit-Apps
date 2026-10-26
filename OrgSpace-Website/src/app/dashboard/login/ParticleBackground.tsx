"use client";

import { useEffect, useRef } from "react";
import Script from "next/script";

declare global {
  interface Window {
    tsParticles?: {
      load: (id: string, options: unknown) => Promise<unknown>;
      domItem: (index: number) => { destroy?: () => void } | undefined;
    };
  }
}

function getParticleConfig(isDark: boolean) {
  if (isDark) {
    return {
      background: {
        color: {
          value: "transparent",
        },
      },
      fpsLimit: 60,
      interactivity: {
        events: {
          onHover: {
            enable: true,
            mode: "grab",
          },
          resize: true,
        },
        modes: {
          grab: {
            distance: 140,
            links: {
              opacity: 0.5,
              color: "#06b6d4",
            },
          },
        },
      },
      particles: {
        color: {
          value: ["#06b6d4", "#3b82f6", "#ffffff"],
        },
        links: {
          color: "#334155", // slate-700
          distance: 150,
          enable: true,
          opacity: 0.4,
          width: 1,
        },
        move: {
          direction: "none",
          enable: true,
          outModes: {
            default: "bounce",
          },
          random: false,
          speed: 1.5,
          straight: false,
        },
        number: {
          density: {
            enable: true,
            area: 800,
          },
          value: 80,
        },
        opacity: {
          value: 0.5,
        },
        shape: {
          type: "circle",
        },
        size: {
          value: { min: 1, max: 3 },
        },
      },
      detectRetina: true,
    };
  }

  // Light Mode Config (sesuai spesifikasi user)
  return {
    background: {
      color: {
        value: "transparent",
      },
    },
    fpsLimit: 60,
    interactivity: {
      events: {
        onHover: {
          enable: true,
          mode: "grab",
        },
        resize: true,
      },
      modes: {
        grab: {
          distance: 160,
          links: {
            opacity: 0.8,
            color: "#2563eb", /* Vibrant blue grab connection */
          },
        },
      },
    },
    particles: {
      color: {
        value: ["#2563eb", "#334155", "#0ea5e9"], /* Vibrant Blue, Slate, Sky Blue */
      },
      links: {
        color: "#2563eb", /* Vibrant Blue for general links */
        distance: 160,
        enable: true,
        opacity: 0.6, /* Increased opacity for light mode */
        width: 1.5, /* Slightly thicker lines */
      },
      move: {
        direction: "none",
        enable: true,
        outModes: {
          default: "bounce",
        },
        random: false,
        speed: 1.2,
        straight: false,
      },
      number: {
        density: {
          enable: true,
          area: 800,
        },
        value: 70,
      },
      opacity: {
        value: 0.6,
      },
      shape: {
        type: "circle",
      },
      size: {
        value: { min: 1, max: 3 },
      },
    },
    detectRetina: true,
  };
}

export function ParticleBackground() {
  const currentDarkRef = useRef<boolean>(true);

  const initParticles = (isDark?: boolean) => {
    if (typeof window !== "undefined" && window.tsParticles) {
      const dark = isDark !== undefined ? isDark : document.documentElement.classList.contains("dark");
      currentDarkRef.current = dark;
      window.tsParticles.load("tsparticles", getParticleConfig(dark));
    }
  };

  useEffect(() => {
    // Inisialisasi awal
    const isDark = document.documentElement.classList.contains("dark");
    initParticles(isDark);

    // Pantau perubahan tema pada <html> untuk reload partikel sesuai mode
    const observer = new MutationObserver((mutations) => {
      for (const mutation of mutations) {
        if (mutation.type === "attributes" && mutation.attributeName === "class") {
          const newIsDark = document.documentElement.classList.contains("dark");
          if (newIsDark !== currentDarkRef.current) {
            initParticles(newIsDark);
          }
        }
      }
    });

    observer.observe(document.documentElement, { attributes: true, attributeFilter: ["class"] });

    return () => {
      observer.disconnect();
    };
  }, []);

  return (
    <>
      <div id="tsparticles" className="pointer-events-auto absolute inset-0 z-0 h-full w-full" />
      <Script
        src="https://cdn.jsdelivr.net/npm/tsparticles@2/tsparticles.bundle.min.js"
        strategy="afterInteractive"
        onLoad={() => initParticles()}
      />
    </>
  );
}
