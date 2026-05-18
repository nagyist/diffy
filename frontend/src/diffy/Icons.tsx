import { CSSProperties, ReactNode } from 'react';

interface IconProps {
  size?: number;
  stroke?: number;
  fill?: string;
  style?: CSSProperties;
  d?: string;
  children?: ReactNode;
}

const Icon = ({ d, size = 16, stroke = 1.5, fill = 'none', children, style }: IconProps) => (
  <svg
    width={size}
    height={size}
    viewBox="0 0 24 24"
    fill={fill}
    stroke="currentColor"
    strokeWidth={stroke}
    strokeLinecap="round"
    strokeLinejoin="round"
    style={style}
  >
    {d ? <path d={d} /> : children}
  </svg>
);

// Cleave — the primary brand mark. A "D" letterform whose bowl is split
// horizontally with a hairline seam — the seam encodes the diff. Dual-color
// mode tints the lower half with --accent.
function Cleave({ size = 32, dual = true }: { size?: number; dual?: boolean }) {
  const cx = 9, cy = 16, r = 11;
  const gap = 1.4;
  const bowl = `M ${cx} 5 A ${r} ${r} 0 0 1 ${cx} 27 Z`;
  const idTop = `cleave-top-${size}`;
  const idBot = `cleave-bot-${size}`;
  return (
    <svg width={size} height={size} viewBox="0 0 32 32" aria-hidden="true">
      <defs>
        <clipPath id={idTop} clipPathUnits="userSpaceOnUse">
          <rect x="0" y="0" width="32" height={cy - gap / 2} />
        </clipPath>
        <clipPath id={idBot} clipPathUnits="userSpaceOnUse">
          <rect x="0" y={cy + gap / 2} width="32" height={32 - (cy + gap / 2)} />
        </clipPath>
      </defs>
      <rect x="5" y="5" width="4" height="22" rx="1" fill="currentColor" />
      <path d={bowl} fill="currentColor" clipPath={`url(#${idTop})`} />
      <path d={bowl} fill={dual ? 'var(--accent)' : 'currentColor'} clipPath={`url(#${idBot})`} />
    </svg>
  );
}

export const Icons = {
  // Brand
  Logo: Cleave,
  Cleave,
  Activity: (p: IconProps) => <Icon {...p} d="M3 12h4l3-8 4 16 3-8h4" />,
  Runs: (p: IconProps) => (
    <Icon {...p}>
      <circle cx="12" cy="12" r="9" />
      <path d="M9 9l6 3-6 3z" fill="currentColor" />
    </Icon>
  ),
  Endpoints: (p: IconProps) => (
    <Icon {...p}>
      <path d="M4 7h16M4 12h16M4 17h10" />
    </Icon>
  ),
  Noise: (p: IconProps) => (
    <Icon {...p}>
      <path d="M3 12h2l2-6 4 12 3-9 2 6h5" />
    </Icon>
  ),
  Transform: (p: IconProps) => (
    <Icon {...p}>
      <path d="M4 7h11l-3-3M20 17H9l3 3" />
    </Icon>
  ),
  Search: (p: IconProps) => (
    <Icon {...p}>
      <circle cx="11" cy="11" r="7" />
      <path d="M20 20l-3.5-3.5" />
    </Icon>
  ),
  Chevron: (p: IconProps) => <Icon {...p} d="M9 6l6 6-6 6" />,
  ChevronDown: (p: IconProps) => <Icon {...p} d="M6 9l6 6 6-6" />,
  Close: (p: IconProps) => <Icon {...p} d="M6 6l12 12M18 6L6 18" />,
  Play: (p: IconProps) => (
    <Icon {...p}>
      <path d="M7 5l12 7-12 7z" fill="currentColor" />
    </Icon>
  ),
  Pause: (p: IconProps) => (
    <Icon {...p}>
      <rect x="6" y="5" width="4" height="14" fill="currentColor" />
      <rect x="14" y="5" width="4" height="14" fill="currentColor" />
    </Icon>
  ),
  Pass: (p: IconProps) => <Icon {...p} d="M5 12l4 4 10-10" />,
  Fail: (p: IconProps) => (
    <Icon {...p}>
      <circle cx="12" cy="12" r="9" />
      <path d="M9 9l6 6M15 9l-6 6" />
    </Icon>
  ),
  Dot: (p: IconProps) => (
    <Icon {...p}>
      <circle cx="12" cy="12" r="4" fill="currentColor" stroke="none" />
    </Icon>
  ),
  Plus: (p: IconProps) => <Icon {...p} d="M12 5v14M5 12h14" />,
  Replay: (p: IconProps) => (
    <Icon {...p}>
      <path d="M3 12a9 9 0 1 0 3-6.7L3 8" />
      <path d="M3 3v5h5" />
    </Icon>
  ),
  Bell: (p: IconProps) => (
    <Icon {...p}>
      <path d="M6 9a6 6 0 1 1 12 0c0 6 2 7 2 7H4s2-1 2-7z" />
      <path d="M10 19a2 2 0 0 0 4 0" />
    </Icon>
  ),
  Branch: (p: IconProps) => (
    <Icon {...p}>
      <circle cx="6" cy="5" r="2" />
      <circle cx="6" cy="19" r="2" />
      <circle cx="18" cy="9" r="2" />
      <path d="M6 7v10M18 11v1a4 4 0 0 1-4 4H8" />
    </Icon>
  ),
  Sparkle: (p: IconProps) => (
    <Icon {...p}>
      <path d="M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8z" />
    </Icon>
  ),
  Copy: (p: IconProps) => (
    <Icon {...p}>
      <rect x="8" y="8" width="12" height="12" rx="2" />
      <path d="M16 8V5a1 1 0 0 0-1-1H5a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h3" />
    </Icon>
  ),
  ArrowRight: (p: IconProps) => <Icon {...p} d="M5 12h14M13 6l6 6-6 6" />,
  Stack: (p: IconProps) => (
    <Icon {...p}>
      <path d="M12 3l9 5-9 5-9-5z" />
      <path d="M3 13l9 5 9-5M3 18l9 5 9-5" />
    </Icon>
  ),
  Spinner: ({ size = 14 }: { size?: number }) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" style={{ animation: 'diffy-spin 0.9s linear infinite' }}>
      <circle cx="12" cy="12" r="9" stroke="currentColor" strokeOpacity=".25" strokeWidth="2.5" />
      <path d="M21 12a9 9 0 0 0-9-9" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
    </svg>
  ),

  // ─── Diffy domain icons (from branding/iconset.jsx) ──────────────────────
  Compare: (p: IconProps) => (
    <Icon {...p}>
      <rect x="3" y="5" width="13" height="6" rx="1.5" />
      <rect x="8" y="13" width="13" height="6" rx="1.5" />
    </Icon>
  ),
  Diff: (p: IconProps) => (
    <Icon {...p}>
      <path d="M10 4a8 8 0 0 0 0 16" />
      <path d="M14 4a8 8 0 0 1 0 16" />
      <path d="M12 8v8" strokeDasharray="1 2" />
    </Icon>
  ),
  Multicast: (p: IconProps) => (
    <Icon {...p}>
      <circle cx="4" cy="12" r="2" />
      <circle cx="20" cy="5" r="2" />
      <circle cx="20" cy="12" r="2" />
      <circle cx="20" cy="19" r="2" />
      <path d="M6 12h2M18 5L8 11M18 19L8 13" />
    </Icon>
  ),
  Primary: (p: IconProps) => (
    <Icon {...p}>
      <circle cx="12" cy="12" r="9" />
      <circle cx="12" cy="12" r="3" fill="currentColor" stroke="none" />
    </Icon>
  ),
  Secondary: (p: IconProps) => (
    <Icon {...p}>
      <circle cx="12" cy="12" r="9" />
      <circle cx="12" cy="12" r="3" />
    </Icon>
  ),
  Candidate: (p: IconProps) => (
    <Icon {...p}>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7l5 5-5 5-5-5z" />
    </Icon>
  ),
  Regression: (p: IconProps) => (
    <Icon {...p}>
      <path d="M4 7l5 6 4-3 7 7" />
      <path d="M20 5v4M20 11.4v.2" />
    </Icon>
  ),
  Match: (p: IconProps) => (
    <Icon {...p}>
      <path d="M5 9h14M5 15h14" />
      <path d="M3 6v12M21 6v12" opacity="0.45" />
    </Icon>
  ),
  Diverge: (p: IconProps) => (
    <Icon {...p}>
      <path d="M12 4v6" />
      <path d="M12 10L6 18M9 18H6v-3M12 10l6 8M15 18h3v-3" />
    </Icon>
  ),
  Threshold: (p: IconProps) => (
    <Icon {...p}>
      <path d="M3 16a9 9 0 0 1 18 0" />
      <path d="M12 16l4-5" />
      <circle cx="12" cy="16" r="1.2" fill="currentColor" stroke="none" />
    </Icon>
  ),
  Heat: (p: IconProps) => (
    <Icon {...p}>
      <rect x="4" y="4" width="7" height="7" rx="1" />
      <rect x="13" y="4" width="7" height="7" rx="1" fill="currentColor" stroke="none" opacity="0.85" />
      <rect x="4" y="13" width="7" height="7" rx="1" fill="currentColor" stroke="none" opacity="0.35" />
      <rect x="13" y="13" width="7" height="7" rx="1" />
    </Icon>
  ),
  Run: (p: IconProps) => (
    <Icon {...p}>
      <circle cx="12" cy="12" r="9" />
      <path d="M9 12h6M13 9l3 3-3 3" />
    </Icon>
  ),
  Channel: (p: IconProps) => (
    <Icon {...p}>
      <rect x="3" y="4" width="4" height="16" rx="1" />
      <rect x="10" y="4" width="4" height="16" rx="1" />
      <rect x="17" y="4" width="4" height="16" rx="1" />
    </Icon>
  ),
  Proxy: (p: IconProps) => (
    <Icon {...p}>
      <rect x="3" y="9" width="18" height="6" rx="3" />
      <path d="M7 6v12M17 6v12" opacity="0.5" />
      <path d="M9 12h6" />
    </Icon>
  ),
  Snapshot: (p: IconProps) => (
    <Icon {...p}>
      <rect x="7" y="5" width="12" height="14" rx="1.5" />
      <rect x="4" y="8" width="12" height="14" rx="1.5" fill="currentColor" fillOpacity="0.08" />
    </Icon>
  ),
  Json: (p: IconProps) => (
    <Icon {...p}>
      <path d="M8 4c-2 0-3 1-3 3v3c0 1-1 2-2 2 1 0 2 1 2 2v3c0 2 1 3 3 3" />
      <path d="M16 4c2 0 3 1 3 3v3c0 1 1 2 2 2-1 0-2 1-2 2v3c0 2-1 3-3 3" />
    </Icon>
  ),
  Mute: (p: IconProps) => (
    <Icon {...p}>
      <path d="M3 12h2l3-7 4 14 3-10 2 5h4" />
      <path d="M4 4l16 16" strokeWidth={2} />
    </Icon>
  ),
};

export type IconName = keyof typeof Icons;
