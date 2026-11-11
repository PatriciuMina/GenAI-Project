import { useState, type FormEvent } from 'react';
import { Alert, Box, Button, Link, Stack, TextField } from '@mui/material';
import { Link as RouterLink } from 'react-router';
import { parseApiError } from '../../api/errors';
import type { CreateOrganizationRequest, Organization } from '../../api/types';
import { paths } from '../../routes/paths';

export interface CreateOrganizationFormValues {
  name: string;
  description: string;
  contactEmail: string;
  presidentEmail: string;
}

interface CreateOrganizationFormProps {
  /** Sends the request: resolves with the created organization, rejects with the backend error. */
  onSubmit: (request: CreateOrganizationRequest) => Promise<Organization>;
}

type FieldErrors = Partial<Record<keyof CreateOrganizationFormValues, string>>;

const EMPTY_VALUES: CreateOrganizationFormValues = {
  name: '',
  description: '',
  contactEmail: '',
  presidentEmail: '',
};

const EMAIL_PATTERN = /^\S+@\S+\.\S+$/;

// Same limits as the backend CreateOrganizationRequest; the backend still validates everything.
function validate(values: CreateOrganizationFormValues): FieldErrors {
  const errors: FieldErrors = {};
  const name = values.name.trim();
  if (!name) errors.name = 'Name is required';
  else if (name.length > 150) errors.name = 'At most 150 characters';
  const contactEmail = values.contactEmail.trim();
  if (contactEmail && !EMAIL_PATTERN.test(contactEmail))
    errors.contactEmail = 'Enter a valid email';
  const presidentEmail = values.presidentEmail.trim();
  if (!presidentEmail) errors.presidentEmail = 'President email is required';
  else if (!EMAIL_PATTERN.test(presidentEmail)) errors.presidentEmail = 'Enter a valid email';
  return errors;
}

/** Trimmed values; empty optional fields are sent as null. */
function toRequest(values: CreateOrganizationFormValues): CreateOrganizationRequest {
  const optional = (value: string) => value.trim() || null;
  return {
    name: values.name.trim(),
    description: optional(values.description),
    contactEmail: optional(values.contactEmail),
    presidentEmail: values.presidentEmail.trim(),
  };
}

/**
 * Admin form to create an organization and appoint its president (T-09). Backend field errors
 * (400) are shown under the matching field; other errors (404 USER_NOT_FOUND, 409
 * ORGANIZATION_NAME_TAKEN, ...) above the form. On success it shows a link to the new
 * organization and clears the form.
 */
export function CreateOrganizationForm({ onSubmit }: CreateOrganizationFormProps) {
  const [values, setValues] = useState<CreateOrganizationFormValues>(EMPTY_VALUES);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [created, setCreated] = useState<Organization | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setFormError(null);
    setCreated(null);
    const errors = validate(values);
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) return;

    setSubmitting(true);
    try {
      const organization = await onSubmit(toRequest(values));
      setCreated(organization);
      setValues(EMPTY_VALUES);
    } catch (error) {
      const parsed = parseApiError(error);
      setFieldErrors(parsed.fieldErrors);
      setFormError(parsed.message);
    } finally {
      setSubmitting(false);
    }
  };

  const update =
    (field: keyof CreateOrganizationFormValues) => (e: { target: { value: string } }) =>
      setValues((v) => ({ ...v, [field]: e.target.value }));

  return (
    <Box component="form" noValidate onSubmit={handleSubmit} aria-label="Create organization form">
      <Stack spacing={2}>
        {created && (
          <Alert severity="success">
            Organization <strong>{created.name}</strong> created.{' '}
            <Link component={RouterLink} to={paths.organization(created.id)}>
              Open {created.name}
            </Link>
          </Alert>
        )}
        {formError && <Alert severity="error">{formError}</Alert>}
        <TextField
          label="Name"
          required
          value={values.name}
          onChange={update('name')}
          error={Boolean(fieldErrors.name)}
          helperText={fieldErrors.name ?? 'Unique, at most 150 characters'}
          fullWidth
        />
        <TextField
          label="Description"
          multiline
          minRows={3}
          value={values.description}
          onChange={update('description')}
          error={Boolean(fieldErrors.description)}
          helperText={fieldErrors.description ?? 'Optional'}
          fullWidth
        />
        <TextField
          label="Contact email"
          type="email"
          value={values.contactEmail}
          onChange={update('contactEmail')}
          error={Boolean(fieldErrors.contactEmail)}
          helperText={fieldErrors.contactEmail ?? 'Optional'}
          fullWidth
        />
        <TextField
          label="President email"
          type="email"
          required
          value={values.presidentEmail}
          onChange={update('presidentEmail')}
          error={Boolean(fieldErrors.presidentEmail)}
          helperText={fieldErrors.presidentEmail ?? 'An existing account; it becomes the president'}
          fullWidth
        />
        <Box>
          <Button type="submit" variant="contained" disabled={submitting}>
            {submitting ? 'Creating…' : 'Create organization'}
          </Button>
        </Box>
      </Stack>
    </Box>
  );
}
