import { ReactNode } from 'react';

export function cx(...xs: (string | false | null | undefined)[]): string {
  return xs.filter(Boolean).join(' ');
}

export function fmtN(n: number | null | undefined): string {
  if (n == null) return '—';
  if (n >= 1000) return (n / 1000).toFixed(n >= 10000 ? 0 : 1) + 'k';
  return String(n);
}

export type Verdict = 'pass' | 'fail' | 'running' | null;

export function verdictColor(v: Verdict): string {
  if (v === 'pass') return 'var(--ok)';
  if (v === 'fail') return 'var(--bad)';
  if (v === 'running') return 'var(--accent)';
  return 'var(--text-2)';
}

export function verdictLabel(v: Verdict): string {
  return (v && { pass: 'Passed', fail: 'Failed', running: 'Running' }[v]) || '—';
}

export function StatusDot({ verdict, size = 8 }: { verdict: Verdict; size?: number }) {
  const c = verdictColor(verdict);
  if (verdict === 'running') {
    return (
      <span className="diffy-status-dot" style={{ width: size, height: size, background: c }}>
        <span className="diffy-status-pulse" style={{ background: c }} />
      </span>
    );
  }
  return <span style={{ width: size, height: size, borderRadius: 99, background: c, display: 'inline-block' }} />;
}

export function MethodPill({ method }: { method: string }) {
  const m = (method || 'GET').toUpperCase();
  return <span className={`diffy-method is-${m.toLowerCase()}`}>{m}</span>;
}

type TagTone = 'default' | 'bad' | 'noise' | 'accent';
type TagSize = 'xs' | 'sm';

export function Tag({ children, tone = 'default', size = 'sm' }: { children: ReactNode; tone?: TagTone; size?: TagSize }) {
  return <span className={`diffy-tag is-${tone} is-${size}`}>{children}</span>;
}

export function Stat({ label, value, sub, accent }: { label: string; value: ReactNode; sub?: ReactNode; accent?: string }) {
  return (
    <div className="diffy-stat">
      <div className="diffy-stat-label">{label}</div>
      <div className="diffy-stat-value" style={accent ? { color: accent } : undefined}>{value}</div>
      {sub && <div className="diffy-stat-sub">{sub}</div>}
    </div>
  );
}

export function Sparkbars({
  values,
  height = 36,
  color = 'var(--accent)',
  gap = 1.5,
}: {
  values: number[];
  height?: number;
  color?: string;
  gap?: number;
}) {
  const w = 200;
  const max = Math.max(1, ...values);
  const bw = (w - gap * (values.length - 1)) / values.length;
  return (
    <svg viewBox={`0 0 ${w} ${height}`} preserveAspectRatio="none" width="100%" height={height} style={{ display: 'block' }}>
      {values.map((v, i) => {
        const h = Math.max(1.5, (v / max) * (height - 2));
        return (
          <rect
            key={i}
            x={i * (bw + gap)}
            y={height - h}
            width={bw}
            height={h}
            rx={1}
            fill={color}
            opacity={0.55 + 0.45 * (v / max)}
          />
        );
      })}
    </svg>
  );
}

const METHODS = ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS'];

export function splitMethodPath(p: string): { method: string; path: string } {
  for (const sep of [' ', ':']) {
    const i = p.indexOf(sep);
    if (i > 0) {
      const head = p.slice(0, i).toUpperCase();
      if (METHODS.includes(head)) return { method: head, path: p.slice(i + 1) };
    }
  }
  return { method: 'GET', path: p };
}
