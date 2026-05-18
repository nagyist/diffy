import { useEffect, useState } from 'react';

export interface Timeseries {
  buckets: { startMs: number; endMs: number; requests: number; diffs: number }[];
  windowStart: number;
  windowEnd: number;
  loading: boolean;
}

interface BucketResult {
  startMs: number;
  endMs: number;
  requests: number;
  diffs: number;
}

interface EndpointMetaPayload {
  total: number;
  differences: number;
}

/**
 * Build a real sparkline from `/api/1/endpoints?start=&end=` queries.
 * The backend doesn't expose per-time-bucket counts directly, so we issue
 * one parallel request per bucket. Backend dedup keeps individual responses small.
 */
export function useTimeseries(buckets = 30, windowMinutes = 40): Timeseries {
  const [data, setData] = useState<Timeseries>({
    buckets: [],
    windowStart: 0,
    windowEnd: 0,
    loading: true,
  });

  useEffect(() => {
    let cancelled = false;
    const now = Date.now();
    const start = now - windowMinutes * 60_000;
    const step = (now - start) / buckets;

    const fetchAll = async () => {
      const reqs: Promise<BucketResult>[] = [];
      for (let i = 0; i < buckets; i++) {
        const bStart = Math.floor(start + i * step);
        const bEnd = Math.floor(start + (i + 1) * step);
        reqs.push(
          fetch(`/api/1/endpoints?exclude_noise=false&start=${bStart}&end=${bEnd}`)
            .then((r) => r.json())
            .then((payload: Record<string, EndpointMetaPayload>) => {
              let requests = 0, diffs = 0;
              for (const meta of Object.values(payload || {})) {
                requests += meta.total || 0;
                diffs += meta.differences || 0;
              }
              return { startMs: bStart, endMs: bEnd, requests, diffs };
            })
            .catch(() => ({ startMs: bStart, endMs: bEnd, requests: 0, diffs: 0 })),
        );
      }
      const results = await Promise.all(reqs);
      if (!cancelled) {
        setData({ buckets: results, windowStart: start, windowEnd: now, loading: false });
      }
    };

    fetchAll();
    return () => {
      cancelled = true;
    };
  }, [buckets, windowMinutes]);

  return data;
}
