import { useEffect, useState, type FormEvent } from 'react';
import { Alert, Box, Button, MenuItem, Stack, TextField } from '@mui/material';
import { parseApiError } from '../../api/errors';
import type {
  Event,
  EventCategory,
  EventRequest,
  Membership,
  UpdateEventRequest,
} from '../../api/types';

export interface EventFormValues {
  organizationId: string;
  title: string;
  description: string;
  location: string;
  category: EventCategory | '';
  startAt: string;
  endAt: string;
  capacity: string;
  registrationDeadline: string;
}

interface EventFormProps {
  memberships: Membership[];
  event?: Event;
  onSubmit: (organizationId: number, body: EventRequest | UpdateEventRequest) => Promise<Event>;
}

type FieldErrors = Partial<Record<keyof EventFormValues, string>>;

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

function toLocalInput(iso: string | null): string {
  if (!iso) return '';
  const date = new Date(iso);
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60_000);
  return local.toISOString().slice(0, 16);
}

function toUtc(value: string): string {
  return new Date(value).toISOString();
}

function validate(values: EventFormValues): FieldErrors {
  const errors: FieldErrors = {};
  if (!values.organizationId) errors.organizationId = 'Organization is required';
  if (!values.title.trim()) errors.title = 'Title is required';
  if (!values.location.trim()) errors.location = 'Location is required';
  if (!values.category) errors.category = 'Category is required';
  if (!values.startAt) errors.startAt = 'Start is required';
  if (!values.endAt) errors.endAt = 'End is required';
  else if (values.startAt && new Date(values.endAt) <= new Date(values.startAt))
    errors.endAt = 'End must be after start';
  const capacity = Number(values.capacity);
  if (!values.capacity || !Number.isInteger(capacity) || capacity <= 0)
    errors.capacity = 'Capacity must be greater than 0';
  return errors;
}

export function EventForm({ memberships, event, onSubmit }: EventFormProps) {
  const eligible = memberships.filter((m) => m.role === 'ORGANIZER' || m.role === 'PRESIDENT');
  const [values, setValues] = useState<EventFormValues>({
    organizationId: event
      ? String(event.organizationId)
      : eligible.length === 1
        ? String(eligible[0].organizationId)
        : '',
    title: event?.title ?? '',
    description: event?.description ?? '',
    location: event?.location ?? '',
    category: event?.category ?? '',
    startAt: toLocalInput(event?.startAt ?? null),
    endAt: toLocalInput(event?.endAt ?? null),
    capacity: event ? String(event.capacity) : '',
    registrationDeadline: toLocalInput(event?.registrationDeadline ?? null),
  });
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!event) return;
    setValues({
      organizationId: String(event.organizationId),
      title: event.title,
      description: event.description ?? '',
      location: event.location,
      category: event.category,
      startAt: toLocalInput(event.startAt),
      endAt: toLocalInput(event.endAt),
      capacity: String(event.capacity),
      registrationDeadline: toLocalInput(event.registrationDeadline),
    });
  }, [event]);

  const update = (field: keyof EventFormValues) => (e: { target: { value: string } }) =>
    setValues((current) => ({ ...current, [field]: e.target.value }));

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setFormError(null);
    const errors = validate(values);
    setFieldErrors(errors);
    if (Object.keys(errors).length) return;
    setSubmitting(true);
    try {
      const body: EventRequest = {
        title: values.title.trim(),
        description: values.description.trim() || null,
        location: values.location.trim(),
        category: values.category as EventCategory,
        startAt: toUtc(values.startAt),
        endAt: toUtc(values.endAt),
        capacity: Number(values.capacity),
        registrationDeadline: values.registrationDeadline
          ? toUtc(values.registrationDeadline)
          : null,
      };
      await onSubmit(
        Number(values.organizationId),
        event ? { ...body, version: event.version } : body,
      );
    } catch (error) {
      const parsed = parseApiError(error);
      setFieldErrors(parsed.fieldErrors as FieldErrors);
      setFormError(
        parsed.code === 'CONCURRENT_UPDATE'
          ? 'Someone else changed this event, reload'
          : parsed.message,
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box
      component="form"
      noValidate
      onSubmit={handleSubmit}
      aria-label={event ? 'Edit event form' : 'Create event form'}
    >
      <Stack spacing={2}>
        {formError && <Alert severity="error">{formError}</Alert>}
        <TextField
          select
          label="Organization"
          required
          value={values.organizationId}
          onChange={update('organizationId')}
          disabled={Boolean(event)}
          error={Boolean(fieldErrors.organizationId)}
          helperText={fieldErrors.organizationId}
        >
          {eligible.map((m) => (
            <MenuItem key={m.organizationId} value={m.organizationId}>
              {m.organizationName}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          label="Title"
          required
          value={values.title}
          onChange={update('title')}
          error={Boolean(fieldErrors.title)}
          helperText={fieldErrors.title}
        />
        <TextField
          label="Description"
          multiline
          minRows={3}
          value={values.description}
          onChange={update('description')}
          error={Boolean(fieldErrors.description)}
          helperText={fieldErrors.description ?? 'Optional'}
        />
        <TextField
          label="Location"
          required
          value={values.location}
          onChange={update('location')}
          error={Boolean(fieldErrors.location)}
          helperText={fieldErrors.location}
        />
        <TextField
          select
          label="Category"
          required
          value={values.category}
          onChange={update('category')}
          error={Boolean(fieldErrors.category)}
          helperText={fieldErrors.category}
        >
          {categories.map((category) => (
            <MenuItem key={category} value={category}>
              {category.replaceAll('_', ' ')}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          label="Start"
          type="datetime-local"
          required
          InputLabelProps={{ shrink: true }}
          value={values.startAt}
          onChange={update('startAt')}
          error={Boolean(fieldErrors.startAt)}
          helperText={fieldErrors.startAt}
        />
        <TextField
          label="End"
          type="datetime-local"
          required
          InputLabelProps={{ shrink: true }}
          value={values.endAt}
          onChange={update('endAt')}
          error={Boolean(fieldErrors.endAt)}
          helperText={fieldErrors.endAt}
        />
        <TextField
          label="Capacity"
          type="number"
          required
          inputProps={{ min: 1, step: 1 }}
          value={values.capacity}
          onChange={update('capacity')}
          error={Boolean(fieldErrors.capacity)}
          helperText={fieldErrors.capacity}
        />
        <TextField
          label="Registration deadline"
          type="datetime-local"
          InputLabelProps={{ shrink: true }}
          value={values.registrationDeadline}
          onChange={update('registrationDeadline')}
          error={Boolean(fieldErrors.registrationDeadline)}
          helperText={fieldErrors.registrationDeadline ?? 'Optional'}
        />
        <Button
          type="submit"
          variant="contained"
          disabled={submitting || (!event && eligible.length === 0)}
        >
          {submitting ? 'Saving…' : event ? 'Save changes' : 'Create event'}
        </Button>
      </Stack>
    </Box>
  );
}
