import Image from "next/image";

interface LogoProps {
  className?: string;
  variant?: "full" | "icon";
  size?: "sm" | "md" | "lg";
  withSubtitle?: boolean;
}

export function Logo({
  className = "",
  variant = "full",
  size = "md",
  withSubtitle = false,
}: LogoProps) {
  const iconDim = size === "sm" ? 28 : size === "lg" ? 40 : 32;
  const textSize = size === "sm" ? "text-base" : size === "lg" ? "text-2xl" : "text-xl";

  if (variant === "icon") {
    return (
      <div className={`relative inline-flex items-center justify-center shrink-0 ${className}`}>
        <Image
          src="/images/orgspace-emblem.png"
          alt="OrgSpace"
          width={iconDim}
          height={iconDim}
          priority
          className="size-auto object-contain select-none drop-shadow-xs"
        />
      </div>
    );
  }

  // Full logo: Emblem 3D transparan + Tipografi modern yang menyatu dengan background
  return (
    <div className={`relative inline-flex items-center gap-2.5 shrink-0 ${className}`}>
      <div className="relative flex shrink-0 items-center justify-center">
        <Image
          src="/images/orgspace-emblem.png"
          alt="Logo OrgSpace"
          width={iconDim}
          height={iconDim}
          priority
          className="size-auto object-contain select-none drop-shadow-xs"
        />
      </div>
      <div className="flex flex-col min-w-0">
        <span className={`${textSize} font-black tracking-tight text-foreground leading-none`}>
          OrgSpace
        </span>
        {withSubtitle && (
          <span className="text-[10px] font-semibold text-muted tracking-wider uppercase mt-1 leading-none">
            Portal Internal
          </span>
        )}
      </div>
    </div>
  );
}
