import Image from "next/image";

interface LogoProps {
  className?: string;
  variant?: "full" | "icon";
  size?: "sm" | "md" | "lg";
}

/**
 * Komponen Logo Resmi OrgSpace.
 * Mendukung pergantian otomatis dan instan antara Mode Terang dan Mode Gelap:
 * - Mode Terang: Logo dengan teks biru korporat gelap kontras di atas latar putih (.logo-light).
 * - Mode Gelap: Logo khusus di mana teks "ORGSPACE" berwarna putih bersih (#FFFFFF) di atas header gelap (.logo-dark).
 */
export function Logo({ className = "", variant = "full", size = "md" }: LogoProps) {
  if (variant === "icon") {
    const dim = size === "sm" ? 24 : size === "lg" ? 40 : 32;
    return (
      <div className={`relative inline-flex items-center justify-center shrink-0 ${className}`}>
        {/* Varian Icon Mode Terang */}
        <Image
          src="/orgspace-icon.png"
          alt="OrgSpace"
          width={dim}
          height={dim}
          priority
          className="object-contain logo-light"
        />
        {/* Varian Icon Mode Gelap */}
        <Image
          src="/orgspace-icon-dark.png"
          alt="OrgSpace"
          width={dim}
          height={dim}
          priority
          className="object-contain logo-dark"
        />
      </div>
    );
  }

  // Full logo (icon + text "ORGSPACE")
  const height = size === "sm" ? 28 : size === "lg" ? 44 : 34;
  const width = Math.round(height * (830 / 500));

  return (
    <div className={`relative inline-flex items-center shrink-0 ${className}`}>
      {/* Varian Full Logo Mode Terang (Teks Biru Korporat) */}
      <Image
        src="/orgspace-logo.png"
        alt="OrgSpace"
        width={width}
        height={height}
        priority
        className="object-contain logo-light"
      />
      {/* Varian Full Logo Khusus Mode Gelap (Teks Putih Bersih) */}
      <Image
        src="/orgspace-logo-dark.png"
        alt="OrgSpace"
        width={width}
        height={height}
        priority
        className="object-contain logo-dark"
      />
    </div>
  );
}
