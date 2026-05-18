import { useEffect, useRef, useState } from 'react';
import { Icons } from './Icons';
import { cx, StatusDot } from './primitives';
import { Run } from '../features/runs/runsApiSlice';
import { View } from '../features/selections/selectionsSlice';

const NAV: { id: View; label: string; icon: keyof typeof Icons }[] = [
  { id: 'overview', label: 'Overview', icon: 'Activity' },
  { id: 'endpoints', label: 'Endpoints', icon: 'Compare' },
  { id: 'noise', label: 'Noise', icon: 'Mute' },
  { id: 'transforms', label: 'Transformations', icon: 'Transform' },
  { id: 'runs', label: 'All runs', icon: 'Stack' },
];

export function Sidebar({
  runs,
  activeRunId,
  onPickRun,
  activeView,
  onPickView,
  endpointBadge,
  proxyPort,
}: {
  runs: Run[];
  activeRunId: string;
  onPickRun: (id: string) => void;
  activeView: View;
  onPickView: (v: View) => void;
  endpointBadge: number;
  proxyPort?: string;
}) {
  const [open, setOpen] = useState(false);
  const activeRun = runs.find((r) => r.id === activeRunId) || runs[0];
  const switcherRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function onDoc(e: MouseEvent) {
      if (switcherRef.current && !switcherRef.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener('mousedown', onDoc);
    return () => document.removeEventListener('mousedown', onDoc);
  }, []);

  return (
    <aside className="diffy-sidebar">
      <div className="diffy-brand">
        <span className="diffy-brand-mark"><Icons.Cleave size={30} dual /></span>
        <span className="diffy-brand-word">diffy</span>
      </div>

      <div className="diffy-run-switcher" ref={switcherRef}>
        <button className="diffy-run-pill" onClick={() => setOpen((v) => !v)}>
          {activeRun && (
            <StatusDot verdict={activeRun.status === 'running' ? 'running' : activeRun.verdict} />
          )}
          <div className="diffy-run-pill-text">
            <div className="diffy-run-pill-label">{activeRun?.label || 'No run'}</div>
            <div className="diffy-run-pill-sub">
              <Icons.Branch size={11} /> {activeRun?.branch || '—'}
            </div>
          </div>
          <Icons.ChevronDown
            size={14}
            style={{ opacity: 0.6, transform: open ? 'rotate(180deg)' : 'none', transition: 'transform .15s' }}
          />
        </button>

        {open && (
          <div className="diffy-run-menu">
            <div className="diffy-run-menu-label">Active runs</div>
            {runs
              .filter((r) => r.status === 'running')
              .map((r) => (
                <button
                  key={r.id}
                  className={cx('diffy-run-menu-item', r.id === activeRunId && 'is-active')}
                  onClick={() => {
                    onPickRun(r.id);
                    setOpen(false);
                  }}
                >
                  <StatusDot verdict="running" />
                  <div className="diffy-run-menu-text">
                    <div>{r.label}</div>
                    <div className="diffy-run-menu-sub">{r.author} · {r.startedAt}</div>
                  </div>
                </button>
              ))}
            {runs.filter((r) => r.status !== 'running').length > 0 && (
              <>
                <div className="diffy-run-menu-label" style={{ marginTop: 6 }}>Recent</div>
                {runs
                  .filter((r) => r.status !== 'running')
                  .slice(0, 4)
                  .map((r) => (
                    <button
                      key={r.id}
                      className={cx('diffy-run-menu-item', r.id === activeRunId && 'is-active')}
                      onClick={() => {
                        onPickRun(r.id);
                        setOpen(false);
                      }}
                    >
                      <StatusDot verdict={r.verdict} />
                      <div className="diffy-run-menu-text">
                        <div>{r.label}</div>
                        <div className="diffy-run-menu-sub">{r.author} · {r.startedAt}</div>
                      </div>
                    </button>
                  ))}
              </>
            )}
            <div className="diffy-run-menu-divider" />
            <button className="diffy-run-menu-item diffy-run-menu-new">
              <span className="diffy-run-menu-new-icon"><Icons.Plus size={12} /></span>
              <div className="diffy-run-menu-text"><div>Start new run…</div></div>
            </button>
          </div>
        )}
      </div>

      <nav className="diffy-nav">
        {NAV.map((item) => {
          const I = Icons[item.icon] as (p: { size?: number }) => JSX.Element;
          const active = item.id === activeView;
          const count = item.id === 'endpoints' ? endpointBadge : 0;
          return (
            <button
              key={item.id}
              className={cx('diffy-nav-item', active && 'is-active')}
              onClick={() => onPickView(item.id)}
            >
              <I size={15} />
              <span>{item.label}</span>
              {count > 0 && <span className="diffy-nav-count">{count}</span>}
            </button>
          );
        })}
      </nav>

      <div className="diffy-sidebar-foot">
        <div className="diffy-foot-row">
          <span className="diffy-foot-key">PROXY</span>
          <span className="diffy-foot-val">{proxyPort ? `:${proxyPort}` : '—'}</span>
        </div>
        <div className="diffy-foot-row">
          <span className="diffy-foot-key">RUN</span>
          <span className="diffy-foot-val">{activeRun?.duration || '—'}</span>
        </div>
      </div>
    </aside>
  );
}

const TITLES: Record<View, string> = {
  overview: 'Overview',
  endpoints: 'Endpoints',
  noise: 'Noise rules',
  transforms: 'Transformations',
  runs: 'All runs',
};

export function Topbar({
  activeRun,
  activeView,
  search,
  onSearch,
}: {
  activeRun?: Run;
  activeView: View;
  search: string;
  onSearch: (s: string) => void;
}) {
  const head = activeRun?.label.split(' · ')[0] || 'Diffy';
  return (
    <header className="diffy-topbar">
      <div className="diffy-crumbs">
        <span className="diffy-crumb-muted">{head}</span>
        <Icons.Chevron size={12} style={{ opacity: 0.35 }} />
        <span className="diffy-crumb-muted">{activeRun?.id || ''}</span>
        <Icons.Chevron size={12} style={{ opacity: 0.35 }} />
        <span className="diffy-crumb-current">{TITLES[activeView]}</span>
      </div>

      <div className="diffy-topbar-spacer" />

      <div className="diffy-search">
        <Icons.Search size={13} />
        <input
          placeholder="Search endpoints, fields, requests…"
          value={search}
          onChange={(e) => onSearch(e.target.value)}
        />
        <kbd className="diffy-kbd">⌘K</kbd>
      </div>

      <button className="diffy-icon-btn" title="Notifications">
        <Icons.Bell size={15} />
        <span className="diffy-icon-btn-dot" />
      </button>

      <div className="diffy-avatar" title="local">D</div>
    </header>
  );
}
