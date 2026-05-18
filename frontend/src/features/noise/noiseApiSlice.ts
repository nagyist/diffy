import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';

export interface EndpointMeta {
  total: number;
  differences: number;
}

export interface FieldsResponse {
  endpoint: unknown;
  fields: Record<string, unknown>;
}

export interface DifferencesQueryArgs {
  selectedEndpoint: string;
  selectedFieldPrefix: string;
  includeWeights: boolean;
  excludeNoise: boolean;
  start: number;
  end: number;
}

export interface FieldsQueryArgs {
  selectedEndpoint: string;
  includeWeights: boolean;
  excludeNoise: boolean;
  start: number;
  end: number;
}

export interface EndpointFieldPrefix {
  endpoint: string;
  fieldPrefix: string;
}

interface RawDifference {
  type: string;
  left: unknown;
  right: unknown;
}

export interface DifferenceResults {
  endpoint: string;
  path: string;
  requests: { id: string; differences: Record<string, RawDifference> }[];
}

export const apiNoiseSlice = createApi({
  reducerPath: 'noiseApi',
  tagTypes: ['Noise'],
  baseQuery: fetchBaseQuery({ baseUrl: '/api/1' }),
  endpoints(builder) {
    return {
      fetchNoise: builder.query<string[], string>({
        query(endpointName) {
          return `/noise/${endpointName}`;
        },
        providesTags: ['Noise'],
      }),
      postNoise: builder.mutation<boolean, EndpointFieldPrefix & { isNoise: boolean }>({
        query({ endpoint, fieldPrefix, isNoise }) {
          return {
            url: `/noise/${endpoint}/prefix/${fieldPrefix}`,
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ isNoise }),
          };
        },
        invalidatesTags: ['Noise'],
      }),
      fetchEndpoints: builder.query<Record<string, EndpointMeta>, { excludeNoise: boolean; start: number; end: number }>({
        query({ excludeNoise, start, end }) {
          return `/endpoints?exclude_noise=${excludeNoise}&start=${start}&end=${end}`;
        },
        providesTags: ['Noise'],
      }),
      fetchFields: builder.query<FieldsResponse, FieldsQueryArgs>({
        query({ selectedEndpoint, includeWeights, excludeNoise, start, end }) {
          return `/endpoints/${selectedEndpoint}/stats?include_weights=${includeWeights}&exclude_noise=${excludeNoise}&start=${start}&end=${end}`;
        },
        providesTags: ['Noise'],
      }),
      fetchDifferences: builder.query<DifferenceResults, DifferencesQueryArgs>({
        query(args) {
          return `/endpoints/${args.selectedEndpoint}/fields/${args.selectedFieldPrefix}/results?include_weights=${args.includeWeights}&exclude_noise=${args.excludeNoise}&start=${args.start}&end=${args.end}`;
        },
        providesTags: ['Noise'],
      }),
    };
  },
});

export const {
  useFetchNoiseQuery,
  usePostNoiseMutation,
  useFetchEndpointsQuery,
  useFetchFieldsQuery,
  useFetchDifferencesQuery,
} = apiNoiseSlice;
