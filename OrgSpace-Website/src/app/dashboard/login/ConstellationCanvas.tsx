"use client";

import { useEffect, useRef } from "react";

interface Particle {
  x: number;
  y: number;
  vx: number;
  vy: number;
  radius: number;
  alpha: number;
}

/**
 * Canvas partikel konstelasi yang sangat dioptimalkan:
 * - Menghindari pemanggilan ctx.shadowBlur yang boros GPU/CPU
 * - Otomatis berhenti saat tab diminimalkan (Page Visibility API)
 * - Menyesuaikan jumlah partikel secara cerdas dengan ukuran layar untuk menjaga 60fps di semua perangkat
 */
export function ConstellationCanvas() {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d", { alpha: true });
    if (!ctx) return;

    let animId: number = 0;
    let isRunning = true;
    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = window.innerHeight);

    // Deteksi reduced motion untuk aksesibilitas & hemat daya
    const prefersReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    // Batasi partikel: 22 di mobile, maksimal 40 di desktop besar
    const isMobile = width < 768;
    const PARTICLE_COUNT = isMobile ? 22 : Math.min(38, Math.floor((width * height) / 38000));
    const particles: Particle[] = [];

    for (let i = 0; i < PARTICLE_COUNT; i++) {
      particles.push({
        x: Math.random() * width,
        y: Math.random() * height,
        vx: (Math.random() - 0.5) * 0.35,
        vy: (Math.random() - 0.5) * 0.35,
        radius: Math.random() * 1.5 + 1,
        alpha: Math.random() * 0.4 + 0.3,
      });
    }

    let mouseX = -1000;
    let mouseY = -1000;

    const handleMouseMove = (e: MouseEvent) => {
      mouseX = e.clientX;
      mouseY = e.clientY;
    };

    const handleMouseLeave = () => {
      mouseX = -1000;
      mouseY = -1000;
    };

    const handleResize = () => {
      if (!canvas) return;
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
    };

    const handleVisibilityChange = () => {
      if (document.hidden) {
        isRunning = false;
        cancelAnimationFrame(animId);
      } else {
        if (!isRunning && !prefersReducedMotion) {
          isRunning = true;
          animId = requestAnimationFrame(render);
        }
      }
    };

    window.addEventListener("mousemove", handleMouseMove, { passive: true });
    window.addEventListener("mouseleave", handleMouseLeave, { passive: true });
    window.addEventListener("resize", handleResize, { passive: true });
    document.addEventListener("visibilitychange", handleVisibilityChange);

    const maxDist = isMobile ? 90 : 120;
    const maxDistSq = maxDist * maxDist;
    const mouseRangeSq = 140 * 140;

    const render = () => {
      if (!isRunning) return;

      ctx.clearRect(0, 0, width, height);

      for (let i = 0; i < particles.length; i++) {
        const p1 = particles[i];

        if (!prefersReducedMotion) {
          p1.x += p1.vx;
          p1.y += p1.vy;

          if (p1.x < 0) p1.x = width;
          else if (p1.x > width) p1.x = 0;

          if (p1.y < 0) p1.y = height;
          else if (p1.y > height) p1.y = 0;
        }

        // Garis interaktif ke kursor mouse (tanpa shadow)
        const dxM = p1.x - mouseX;
        const dyM = p1.y - mouseY;
        const distMSq = dxM * dxM + dyM * dyM;
        if (distMSq < mouseRangeSq) {
          const alphaM = (1 - Math.sqrt(distMSq) / 140) * 0.35;
          ctx.beginPath();
          ctx.moveTo(p1.x, p1.y);
          ctx.lineTo(mouseX, mouseY);
          ctx.strokeStyle = `rgba(0, 201, 242, ${alphaM})`;
          ctx.lineWidth = 0.75;
          ctx.stroke();
        }

        // Garis koneksi antarpartikel
        for (let j = i + 1; j < particles.length; j++) {
          const p2 = particles[j];
          const dx = p1.x - p2.x;
          const dy = p1.y - p2.y;
          const distSq = dx * dx + dy * dy;

          if (distSq < maxDistSq) {
            const lineAlpha = (1 - distSq / maxDistSq) * 0.2;
            ctx.beginPath();
            ctx.moveTo(p1.x, p1.y);
            ctx.lineTo(p2.x, p2.y);
            ctx.strokeStyle = `rgba(0, 201, 242, ${lineAlpha})`;
            ctx.lineWidth = 0.55;
            ctx.stroke();
          }
        }

        // Titik simpul (lingkaran solid murni, tanpa shadowBlur yang memicu lag)
        ctx.beginPath();
        ctx.arc(p1.x, p1.y, p1.radius, 0, Math.PI * 2);
        ctx.fillStyle = `rgba(0, 229, 255, ${p1.alpha})`;
        ctx.fill();
      }

      if (!prefersReducedMotion && isRunning) {
        animId = requestAnimationFrame(render);
      }
    };

    render();

    return () => {
      isRunning = false;
      cancelAnimationFrame(animId);
      window.removeEventListener("mousemove", handleMouseMove);
      window.removeEventListener("mouseleave", handleMouseLeave);
      window.removeEventListener("resize", handleResize);
      document.removeEventListener("visibilitychange", handleVisibilityChange);
    };
  }, []);

  return (
    <canvas
      ref={canvasRef}
      className="pointer-events-none absolute inset-0 z-0 h-full w-full opacity-70 will-change-transform"
      aria-hidden="true"
    />
  );
}
