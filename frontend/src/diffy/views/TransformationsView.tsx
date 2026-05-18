import { useEffect, useMemo, useState } from 'react';
import { Icons } from '../Icons';
import { cx } from '../primitives';
import {
  useFetchOverrideQuery,
  useUpdateOverrideMutation,
  useDeleteOverrideMutation,
} from '../../features/overrides/transformationsApiSlice';

const BACKEND_NOOP = '(request) => (request)';
const DEFAULT_BODY = `// Diffy applies this JS at the chosen injection point.
// The function takes the parsed request and returns a (possibly modified) request.
(request) => {
  return request;
}
`;

type Edge = 'all' | 'primary' | 'secondary' | 'candidate' | 'none';

const EDGES: { id: Edge; label: string; sub: string }[] = [
  { id: 'all', label: 'all', sub: 'every outgoing request' },
  { id: 'primary', label: 'primary', sub: 'requests to the primary upstream' },
  { id: 'secondary', label: 'secondary', sub: 'requests to the secondary upstream' },
  { id: 'candidate', label: 'candidate', sub: 'requests to the candidate upstream' },
  { id: 'none', label: 'none', sub: 'never injected — useful for staging a draft' },
];

export function TransformationsView() {
  const [activeEdge, setActiveEdge] = useState<Edge>('candidate');

  const overrideRes = useFetchOverrideQuery(activeEdge);
  const remoteBody = (overrideRes.data as { transformationJs?: string } | undefined)?.transformationJs;
  const isConfigured = !!remoteBody && remoteBody.trim() !== BACKEND_NOOP;

  const [bodyText, setBodyText] = useState<string>(DEFAULT_BODY);
  const [previewErr, setPreviewErr] = useState<string | null>(null);

  useEffect(() => {
    setBodyText(isConfigured ? (remoteBody as string) : DEFAULT_BODY);
  }, [remoteBody, isConfigured, activeEdge]);

  useEffect(() => {
    try {
      // eslint-disable-next-line no-new-func
      const fn = new Function(`return (${bodyText})`)();
      if (typeof fn !== 'function') {
        // Fall back to named function form
        // eslint-disable-next-line no-new-func
        const named = new Function(`${bodyText}\n;return typeof transform==='function' ? transform : null;`)();
        if (typeof named !== 'function') throw new Error('Body must evaluate to a function: `(request) => request` or `function transform(req) { ... }`');
      }
      setPreviewErr(null);
    } catch (e) {
      setPreviewErr((e as Error).message);
    }
  }, [bodyText]);

  const [updateOverride] = useUpdateOverrideMutation();
  const [deleteOverride] = useDeleteOverrideMutation();

  const enabled = isConfigured;
  const toggleEnabled = async () => {
    if (enabled) {
      await deleteOverride({ injectionPoint: activeEdge, transformationJs: '' });
    } else {
      await updateOverride({ injectionPoint: activeEdge, transformationJs: bodyText });
    }
  };
  const save = async () => {
    await updateOverride({ injectionPoint: activeEdge, transformationJs: bodyText });
  };

  const preview = useMemo(() => {
    try {
      // eslint-disable-next-line no-new-func
      let fn: any = new Function(`return (${bodyText})`)();
      if (typeof fn !== 'function') {
        // eslint-disable-next-line no-new-func
        fn = new Function(`${bodyText}\n;return typeof transform==='function' ? transform : null;`)();
      }
      if (typeof fn !== 'function') return null;
      const before = { method: 'POST', uri: '/json', headers: { 'Content-Type': 'application/json' }, body: { name: 'sample' } };
      const after = fn(JSON.parse(JSON.stringify(before)));
      return { before, after };
    } catch {
      return null;
    }
  }, [bodyText]);

  return (
    <div className="diffy-page">
      <div className="diffy-tx-layout">
        <div className="diffy-card diffy-tx-list">
          <div className="diffy-card-head">
            <div>
              <div className="diffy-card-title">Injection points</div>
              <div className="diffy-card-sub">JS applied to outgoing requests</div>
            </div>
          </div>
          <div className="diffy-pane-list">
            {EDGES.map((e) => {
              const active = activeEdge === e.id;
              return (
                <button
                  key={e.id}
                  className={cx('diffy-tx-item', active && 'is-active')}
                  onClick={() => setActiveEdge(e.id)}
                >
                  <div className="diffy-tx-item-top">
                    <span className={cx('diffy-tx-dot', active && enabled && 'is-on')} />
                    <span className="diffy-mono">{e.label}</span>
                  </div>
                  <div className="diffy-tx-item-bot diffy-text-muted">{e.sub}</div>
                </button>
              );
            })}
          </div>
        </div>

        <div className="diffy-tx-main">
          <div className="diffy-card diffy-tx-editor-card">
            <div className="diffy-card-head">
              <div>
                <div className="diffy-card-title diffy-mono">{activeEdge}</div>
                <div className="diffy-card-sub">{enabled ? 'transform active' : 'no transform stored — saving will install one'}</div>
              </div>
              <div className="diffy-card-head-r">
                <label className="diffy-toggle">
                  <input type="checkbox" checked={enabled} onChange={toggleEnabled} />
                  <span className="diffy-toggle-track"><span className="diffy-toggle-thumb" /></span>
                  <span>{enabled ? 'Enabled' : 'Disabled'}</span>
                </label>
                <button className="diffy-btn is-primary is-sm" onClick={save}>
                  Save
                </button>
              </div>
            </div>
            <div className="diffy-editor">
              <div className="diffy-editor-gutter diffy-mono">
                {bodyText.split('\n').map((_, i) => (
                  <div key={i}>{i + 1}</div>
                ))}
              </div>
              <textarea
                className="diffy-editor-text diffy-mono"
                value={bodyText}
                onChange={(e) => setBodyText(e.target.value)}
                spellCheck={false}
              />
            </div>
            {previewErr && (
              <div className="diffy-editor-err diffy-mono">
                <Icons.Fail size={12} /> {previewErr}
              </div>
            )}
          </div>

          <div className="diffy-card">
            <div className="diffy-card-head">
              <div>
                <div className="diffy-card-title">Live preview</div>
                <div className="diffy-card-sub">
                  Sample request shape. Replace with a captured request after a Save.
                </div>
              </div>
              <div className="diffy-segmented">
                <button className="is-active">Diff</button>
                <button>Before</button>
                <button>After</button>
              </div>
            </div>
            <div className="diffy-tx-preview-body">
              <TxPreview before={preview?.before} after={preview?.after} />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function TxPreview({ before, after }: { before?: unknown; after?: unknown }) {
  if (!before || !after) {
    return <div className="diffy-empty">Fix the transform body to see results.</div>;
  }
  const beforeS = JSON.stringify(before, null, 2).split('\n');
  const afterS = JSON.stringify(after, null, 2).split('\n');
  const max = Math.max(beforeS.length, afterS.length);
  const rows = [];
  for (let i = 0; i < max; i++) {
    const b = beforeS[i] || '';
    const a = afterS[i] || '';
    rows.push({ b, a, changed: b !== a });
  }
  return (
    <div className="diffy-tx-preview diffy-mono">
      <div className="diffy-tx-preview-col">
        <div className="diffy-tx-preview-col-head">
          <span className="diffy-col-pill is-candidate">Before</span>
        </div>
        <div className="diffy-tx-preview-col-body">
          {rows.map((l, i) => (
            <div key={i} className={cx('diffy-jsl', l.changed && 'is-del')}>
              {l.b || ' '}
            </div>
          ))}
        </div>
      </div>
      <div className="diffy-tx-preview-col">
        <div className="diffy-tx-preview-col-head">
          <span className="diffy-col-pill is-primary">After</span>
        </div>
        <div className="diffy-tx-preview-col-body">
          {rows.map((l, i) => (
            <div key={i} className={cx('diffy-jsl', l.changed && 'is-add')}>
              {l.a || ' '}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
