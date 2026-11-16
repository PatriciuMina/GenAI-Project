import { Grid, Pagination, Paper, Stack, Typography } from '@mui/material';
import { useSearchParams } from 'react-router';
import { ErrorAlert } from '../../components/ErrorAlert';
import { LoadingState } from '../../components/LoadingState';
import type { EventCategory } from '../../api/types';
import { useOrganizations } from '../organizations/queries';
import { EventCard } from './EventCard';
import { EventFilters, type EventFilterValues } from './EventFilters';
import { useSearchEvents } from './queries';

const PAGE_SIZE = 20;
const categories: EventCategory[] = [
  'ACADEMIC',
  'CAREER',
  'CULTURAL',
  'SOCIAL',
  'SPORTS',
  'VOLUNTEERING',
  'WORKSHOP',
  'OTHER',
];

function isDateValue(value: string): boolean {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return false;
  const [year, month, day] = value.split('-').map(Number);
  const date = new Date(year, month - 1, day);
  return date.getFullYear() === year && date.getMonth() === month - 1 && date.getDate() === day;
}

function localDateBound(value: string, endOfDay: boolean): string | undefined {
  if (!isDateValue(value)) return undefined;
  const [year, month, day] = value.split('-').map(Number);
  const date = endOfDay
    ? new Date(year, month - 1, day + 1, 0, 0, 0, -1)
    : new Date(year, month - 1, day);
  return date.toISOString();
}

function positiveInteger(value: string | null): number | undefined {
  if (!value || !/^\d+$/.test(value)) return undefined;
  const parsed = Number(value);
  return Number.isSafeInteger(parsed) && parsed > 0 ? parsed : undefined;
}

export function EventsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const values: EventFilterValues = {
    q: searchParams.get('q') ?? '',
    category: searchParams.get('category') ?? '',
    organizationId: searchParams.get('organizationId') ?? '',
    from: searchParams.get('from') ?? '',
    to: searchParams.get('to') ?? '',
  };
  const rawPage = positiveInteger(searchParams.get('page'));
  const page = rawPage ? rawPage - 1 : 0;
  const category = categories.includes(values.category as EventCategory)
    ? (values.category as EventCategory)
    : undefined;
  const organizationId = positiveInteger(values.organizationId);
  const search = useSearchEvents({
    q: values.q || undefined,
    category,
    organizationId,
    from: localDateBound(values.from, false),
    to: localDateBound(values.to, true),
    page,
    size: PAGE_SIZE,
  });
  const organizations = useOrganizations();

  function updateFilter(name: keyof EventFilterValues, value: string) {
    setSearchParams(
      (current) => {
        const next = new URLSearchParams(current);
        if (value) next.set(name, value);
        else next.delete(name);
        next.delete('page');
        return next;
      },
      { replace: true },
    );
  }

  function changePage(_: React.ChangeEvent<unknown>, selectedPage: number) {
    setSearchParams(
      (current) => {
        const next = new URLSearchParams(current);
        if (selectedPage === 1) next.delete('page');
        else next.set('page', String(selectedPage));
        return next;
      },
      { replace: true },
    );
  }

  return (
    <Stack spacing={2}>
      <Typography component="h1" variant="h4">
        Events
      </Typography>

      {organizations.isError && <ErrorAlert error={organizations.error} />}
      <EventFilters
        values={values}
        organizations={organizations.data ?? []}
        onChange={updateFilter}
      />

      {search.isPending && <LoadingState label="Loading events…" />}
      {search.isError && <ErrorAlert error={search.error} onRetry={() => void search.refetch()} />}

      {search.data && search.data.content.length === 0 && (
        <Paper variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
          <Typography color="text.secondary">No events match your filters</Typography>
        </Paper>
      )}

      {search.data && search.data.content.length > 0 && (
        <>
          <Grid container spacing={2}>
            {search.data.content.map((event) => (
              <Grid key={event.id} size={{ xs: 12, sm: 6, md: 4 }}>
                <EventCard event={event} />
              </Grid>
            ))}
          </Grid>
          {search.data.totalPages > 1 && (
            <Pagination
              aria-label="Event pages"
              count={search.data.totalPages}
              page={page + 1}
              onChange={changePage}
              sx={{ alignSelf: 'center', py: 2 }}
            />
          )}
        </>
      )}
    </Stack>
  );
}
