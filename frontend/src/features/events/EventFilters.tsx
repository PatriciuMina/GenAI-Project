import { MenuItem, Stack, TextField } from '@mui/material';
import type { EventCategory, Organization } from '../../api/types';
import { categoryLabel } from './eventFormat';

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

export interface EventFilterValues {
  q: string;
  category: string;
  organizationId: string;
  from: string;
  to: string;
}

interface EventFiltersProps {
  values: EventFilterValues;
  organizations: Organization[];
  onChange: (name: keyof EventFilterValues, value: string) => void;
}

export function EventFilters({ values, organizations, onChange }: EventFiltersProps) {
  return (
    <Stack
      component="form"
      direction={{ xs: 'column', sm: 'row' }}
      spacing={2}
      onSubmit={(event) => event.preventDefault()}
      sx={{ mb: 3, flexWrap: 'wrap' }}
    >
      <TextField
        label="Search events"
        value={values.q}
        onChange={(event) => onChange('q', event.target.value)}
        size="small"
        sx={{ flex: '1 1 220px' }}
      />
      <TextField
        select
        label="Category"
        value={values.category}
        onChange={(event) => onChange('category', event.target.value)}
        size="small"
        sx={{ flex: '1 1 160px' }}
      >
        <MenuItem value="">All categories</MenuItem>
        {categories.map((category) => (
          <MenuItem key={category} value={category}>
            {categoryLabel(category)}
          </MenuItem>
        ))}
      </TextField>
      <TextField
        select
        label="Organization"
        value={values.organizationId}
        onChange={(event) => onChange('organizationId', event.target.value)}
        size="small"
        sx={{ flex: '1 1 200px' }}
      >
        <MenuItem value="">All organizations</MenuItem>
        {values.organizationId &&
          !organizations.some((organization) => String(organization.id) === values.organizationId) && (
            <MenuItem value={values.organizationId}>Organization {values.organizationId}</MenuItem>
          )}
        {organizations.map((organization) => (
          <MenuItem key={organization.id} value={String(organization.id)}>
            {organization.name}
          </MenuItem>
        ))}
      </TextField>
      <TextField
        label="From"
        type="date"
        value={values.from}
        onChange={(event) => onChange('from', event.target.value)}
        size="small"
        slotProps={{ inputLabel: { shrink: true } }}
        sx={{ flex: '1 1 150px' }}
      />
      <TextField
        label="To"
        type="date"
        value={values.to}
        onChange={(event) => onChange('to', event.target.value)}
        size="small"
        slotProps={{ inputLabel: { shrink: true } }}
        sx={{ flex: '1 1 150px' }}
      />
    </Stack>
  );
}
