import Image from "next/image";

interface LogoProps {
  className?: string;
  variant?: "full" | "icon";
  size?: "sm" | "md" | "lg";
}

export function Logo({ className = "", variant = "full", size = "md" }: LogoProps) {
  if (variant === "icon") {
    const dim = size === "sm" ? 24 : size === "lg" ? 40 : 32;
    return (
      <div className={`relative inline-flex items-center justify-center shrink-0 ${className}`}>
        <Image
          src="/orgspace-icon.png"
          alt="OrgSpace"
          width={dim}
          height={dim}
          priority
          className="object-contain"
        />
      </div>
    );
  }

  // Full logo (icon + text "ORGSPACE")
  const height = size === "sm" ? 28 : size === "lg" ? 44 : 34;
  const width = Math.round(height * (830 / 500));

  return (
    <div className={`relative inline-flex items-center shrink-0 ${className}`}>
      <Image
        src="/orgspace-logo.png"
        alt="OrgSpace"
        width={width}
        height={height}
        priority
        className="object-contain"
      />
    </div>
  );
}
