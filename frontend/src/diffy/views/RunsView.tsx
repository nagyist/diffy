import { Icons } from '../Icons';
import { cx, fmtN, StatusDot } from '../primitives';
import { Run } from '../../features/runs/runsApiSlice';

export function RunsView({
  runs,
  activeRunId,
  onPickRun,
}: {
  runs: Run[];
  activeRunId: string;
  onPickRun: (id: string) => void;
}) {
  return (
    <div className="diffy-page">
      <section className="diffy-card">
        <div className="diffy-card-head">
          <div>
            <div className="diffy-card-title">All runs</div>
            <div className="diffy-card-sub">Comparison sessions across all services</div>
          </div>
          <button className="diffy-btn is-primary">
            <Icons.Plus size={13} /> New run
          </button>
        </div>

        <div className="diffy-table">
          <div className="diffy-table-head">
            <div style={{ width: 16 }} />
            <div style={{ flex: 2 }}>Run</div>
            <div style={{ flex: 1 }}>Branch</div>
            <div style={{ width: 90, textAlign: 'right' }}>Requests</div>
            <div style={{ width: 80, textAlign: 'right' }}>Real diffs</div>
            <div style={{ width: 100, textAlign: 'right' }}>Diff rate</div>
            <div style={{ width: 110, textAlign: 'right' }}>Duration</div>
            <div style={{ width: 100, textAlign: 'right' }}>Started</div>
          </div>
          {runs.map((r) => (
            <button
              key={r.id}
              className={cx('diffy-table-row', r.id === activeRunId && 'is-active')}
              onClick={() => onPickRun(r.id)}
            >
              <div style={{ width: 16, display: 'flex', alignItems: 'center' }}>
                <StatusDot verdict={r.status === 'running' ? 'running' : r.verdict} />
              </div>
              <div style={{ flex: 2, minWidth: 0 }}>
                <div className="diffy-runrow-label">{r.label}</div>
                <div className="diffy-runrow-sub diffy-mono">
                  {r.id} · {r.candidate.split('@')[1] || r.candidate}
                </div>
              </div>
              <div style={{ flex: 1, minWidth: 0 }} className="diffy-mono diffy-text-muted">
                <Icons.Branch size={11} style={{ marginRight: 4, verticalAlign: -1, opacity: 0.6 }} />
                {r.branch}
              </div>
              <div style={{ width: 90, textAlign: 'right' }} className="diffy-mono">
                {fmtN(r.totalReqs)}
              </div>
              <div
                className="diffy-mono"
                style={{
                  width: 80,
                  textAlign: 'right',
                  color: r.realDiffs > 0 ? 'var(--bad)' : 'var(--text-2)',
                }}
              >
                {r.realDiffs.toLocaleString()}
              </div>
              <div style={{ width: 100, textAlign: 'right' }} className="diffy-mono">
                {r.diffRate.toFixed(2)}%
              </div>
              <div style={{ width: 110, textAlign: 'right' }} className="diffy-mono diffy-text-muted">
                {r.duration}
              </div>
              <div style={{ width: 100, textAlign: 'right' }} className="diffy-text-muted">
                {r.startedAt}
              </div>
            </button>
          ))}
          {runs.length === 0 && (
            <div className="diffy-empty">
              <div>No runs to show yet.</div>
            </div>
          )}
        </div>
      </section>
    </div>
  );
}
