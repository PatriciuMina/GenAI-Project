-- DEV-ONLY demo data. Loaded only by the 'dev' profile (spring.flyway.locations in
-- application-dev.yml); never in tests or production.
-- Repeatable migration: Flyway re-runs it whenever this file changes, so every insert is
-- idempotent (ON CONFLICT / NOT EXISTS) and rows are looked up by natural keys, not ids.
-- Dates are relative to now() at the time the seed is (re)applied.
-- All accounts have the password "password" (BCrypt, cost 10).

-- ---------------------------------------------------------------- users
INSERT INTO users (email, password_hash, full_name, global_role)
VALUES
    ('admin@ubbevents.ro',            '$2a$10$a3B/umTo8/gr/F.dJ9zkVuIWv59EwC2le2wvkc5tLQop6uCIcyz1y', 'Admin UBB Events', 'ADMIN'),
    ('ana.popescu@stud.ubbcluj.ro',   '$2a$10$a3B/umTo8/gr/F.dJ9zkVuIWv59EwC2le2wvkc5tLQop6uCIcyz1y', 'Ana Popescu',      'STUDENT'),
    ('mihai.ionescu@stud.ubbcluj.ro', '$2a$10$a3B/umTo8/gr/F.dJ9zkVuIWv59EwC2le2wvkc5tLQop6uCIcyz1y', 'Mihai Ionescu',    'STUDENT'),
    ('elena.marin@stud.ubbcluj.ro',   '$2a$10$a3B/umTo8/gr/F.dJ9zkVuIWv59EwC2le2wvkc5tLQop6uCIcyz1y', 'Elena Marin',      'STUDENT'),
    ('radu.pop@stud.ubbcluj.ro',      '$2a$10$a3B/umTo8/gr/F.dJ9zkVuIWv59EwC2le2wvkc5tLQop6uCIcyz1y', 'Radu Pop',         'STUDENT'),
    ('ioana.rusu@stud.ubbcluj.ro',    '$2a$10$a3B/umTo8/gr/F.dJ9zkVuIWv59EwC2le2wvkc5tLQop6uCIcyz1y', 'Ioana Rusu',       'STUDENT'),
    ('andrei.toma@stud.ubbcluj.ro',   '$2a$10$a3B/umTo8/gr/F.dJ9zkVuIWv59EwC2le2wvkc5tLQop6uCIcyz1y', 'Andrei Toma',      'STUDENT'),
    ('maria.dobre@stud.ubbcluj.ro',   '$2a$10$a3B/umTo8/gr/F.dJ9zkVuIWv59EwC2le2wvkc5tLQop6uCIcyz1y', 'Maria Dobre',      'STUDENT')
ON CONFLICT (lower(email)) DO NOTHING;

-- ---------------------------------------------------------------- organizations
INSERT INTO organizations (name, description, contact_email)
VALUES
    ('UBB Computer Science Society',
     'Student society for workshops, hackathons and career events in computer science.',
     'cs.society@stud.ubbcluj.ro'),
    ('UBB Theatre Club',
     'Student theatre club: improv nights, readings and performances.',
     'theatre.club@stud.ubbcluj.ro')
ON CONFLICT (lower(name)) DO NOTHING;

-- ---------------------------------------------------------------- memberships
INSERT INTO organization_memberships (user_id, organization_id, org_role)
SELECT u.id, o.id, v.org_role
FROM (VALUES
    ('ana.popescu@stud.ubbcluj.ro',   'UBB Computer Science Society', 'PRESIDENT'),
    ('elena.marin@stud.ubbcluj.ro',   'UBB Computer Science Society', 'ORGANIZER'),
    ('ioana.rusu@stud.ubbcluj.ro',    'UBB Computer Science Society', 'MEMBER'),
    ('mihai.ionescu@stud.ubbcluj.ro', 'UBB Theatre Club',             'PRESIDENT'),
    ('radu.pop@stud.ubbcluj.ro',      'UBB Theatre Club',             'ORGANIZER'),
    ('maria.dobre@stud.ubbcluj.ro',   'UBB Theatre Club',             'MEMBER')
) AS v(email, org_name, org_role)
JOIN users u ON lower(u.email) = lower(v.email)
JOIN organizations o ON lower(o.name) = lower(v.org_name)
ON CONFLICT (user_id, organization_id) DO NOTHING;

-- ---------------------------------------------------------------- events
-- One event per lifecycle status (PUBLISHED twice). (organization, title) identifies a seed event.
INSERT INTO events (organization_id, title, description, location, category, start_at, end_at,
                    capacity, registration_deadline, status, created_by)
SELECT o.id, v.title, v.description, v.location, v.category,
       now() + v.start_offset, now() + v.start_offset + v.duration,
       v.capacity, now() + v.deadline_offset, v.status, u.id
FROM (VALUES
    ('UBB Computer Science Society', 'Spring Hackathon 2027',
     '24h hackathon, teams of 3-4. Details still being finalized.',
     'FSEGA Building, Hall A', 'ACADEMIC',
     interval '60 days', interval '24 hours', 100, interval '55 days', 'DRAFT', 'ana.popescu@stud.ubbcluj.ro'),
    ('UBB Computer Science Society', 'Intro to Spring Boot Workshop',
     'Hands-on workshop: build a REST API with Spring Boot in two hours.',
     'Room 2/I, Mathematica Building', 'WORKSHOP',
     interval '7 days', interval '2 hours', 30, interval '6 days', 'PUBLISHED', 'elena.marin@stud.ubbcluj.ro'),
    ('UBB Theatre Club', 'Improv Night',
     'Open improv evening; everyone can join on stage.',
     'Student Culture House, Studio', 'CULTURAL',
     interval '14 days', interval '3 hours', 50, NULL, 'PUBLISHED', 'radu.pop@stud.ubbcluj.ro'),
    ('UBB Computer Science Society', 'Tech Career Fair Prep',
     'CV review and mock interviews before the career fair. Small group.',
     'Room 5/I, Mathematica Building', 'CAREER',
     interval '2 days', interval '2 hours', 3, interval '-1 hour', 'REGISTRATION_CLOSED', 'ana.popescu@stud.ubbcluj.ro'),
    ('UBB Theatre Club', 'Shakespeare Reading Marathon',
     'Reading "A Midsummer Night''s Dream" together, in shifts.',
     'Central University Library, Room 3', 'CULTURAL',
     interval '-1 hour', interval '4 hours', 40, interval '-2 hours', 'ONGOING', 'mihai.ionescu@stud.ubbcluj.ro'),
    ('UBB Computer Science Society', 'Campus Cleanup Day',
     'Volunteering: cleaning the botanical garden alleys.',
     'UBB Botanical Garden, main gate', 'VOLUNTEERING',
     interval '-10 days', interval '3 hours', 25, interval '-11 days', 'COMPLETED', 'elena.marin@stud.ubbcluj.ro'),
    ('UBB Theatre Club', 'Board Games Evening',
     'Board games and tea. Cancelled because the room is unavailable.',
     'Student Culture House, Room 1', 'SOCIAL',
     interval '5 days', interval '3 hours', 20, interval '4 days', 'CANCELLED', 'mihai.ionescu@stud.ubbcluj.ro')
) AS v(org_name, title, description, location, category, start_offset, duration,
       capacity, deadline_offset, status, creator_email)
JOIN organizations o ON lower(o.name) = lower(v.org_name)
JOIN users u ON lower(u.email) = lower(v.creator_email)
WHERE NOT EXISTS (SELECT 1 FROM events e WHERE e.organization_id = o.id AND e.title = v.title);

-- ---------------------------------------------------------------- registrations
-- checked_in_offset is NULL when the participant has not checked in.
INSERT INTO registrations (event_id, user_id, status, registered_at, checked_in_at)
SELECT e.id, u.id, v.status,
       LEAST(now(), e.start_at) - v.registered_before_start,
       CASE WHEN v.checked_in THEN e.start_at + interval '5 minutes' END
FROM (VALUES
    ('Intro to Spring Boot Workshop', 'ioana.rusu@stud.ubbcluj.ro',  'CONFIRMED', interval '2 days', false),
    ('Intro to Spring Boot Workshop', 'andrei.toma@stud.ubbcluj.ro', 'CONFIRMED', interval '1 day',  false),
    ('Intro to Spring Boot Workshop', 'maria.dobre@stud.ubbcluj.ro', 'CANCELLED', interval '3 days', false),
    ('Improv Night',                  'maria.dobre@stud.ubbcluj.ro', 'CONFIRMED', interval '1 day',  false),
    ('Improv Night',                  'ioana.rusu@stud.ubbcluj.ro',  'CONFIRMED', interval '2 hours', false),
    ('Tech Career Fair Prep',         'ioana.rusu@stud.ubbcluj.ro',  'CONFIRMED', interval '3 days', false),
    ('Tech Career Fair Prep',         'andrei.toma@stud.ubbcluj.ro', 'CONFIRMED', interval '3 days', false),
    ('Tech Career Fair Prep',         'maria.dobre@stud.ubbcluj.ro', 'CONFIRMED', interval '2 days', false),
    ('Shakespeare Reading Marathon',  'andrei.toma@stud.ubbcluj.ro', 'CONFIRMED', interval '2 days', true),
    ('Shakespeare Reading Marathon',  'maria.dobre@stud.ubbcluj.ro', 'CONFIRMED', interval '1 day',  false),
    ('Campus Cleanup Day',            'ioana.rusu@stud.ubbcluj.ro',  'CONFIRMED', interval '5 days', true),
    ('Campus Cleanup Day',            'andrei.toma@stud.ubbcluj.ro', 'CONFIRMED', interval '4 days', true),
    ('Campus Cleanup Day',            'maria.dobre@stud.ubbcluj.ro', 'CONFIRMED', interval '4 days', false),
    ('Board Games Evening',           'andrei.toma@stud.ubbcluj.ro', 'CANCELLED', interval '2 days', false)
) AS v(event_title, email, status, registered_before_start, checked_in)
JOIN events e ON e.title = v.event_title
JOIN users u ON lower(u.email) = lower(v.email)
WHERE NOT EXISTS (
    SELECT 1 FROM registrations r
    WHERE r.event_id = e.id AND r.user_id = u.id AND r.status = v.status
);

-- ---------------------------------------------------------------- tasks
INSERT INTO event_tasks (event_id, title, description, assignee_membership_id, status, due_at)
SELECT e.id, v.title, v.description, m.id, v.status, e.start_at - v.due_before_start
FROM (VALUES
    ('Intro to Spring Boot Workshop', 'Book the room', 'Confirm Room 2/I with the faculty secretariat.',
     'elena.marin@stud.ubbcluj.ro', 'DONE', interval '5 days'),
    ('Intro to Spring Boot Workshop', 'Prepare slides and starter repo', NULL,
     'ana.popescu@stud.ubbcluj.ro', 'IN_PROGRESS', interval '2 days'),
    ('Intro to Spring Boot Workshop', 'Order snacks', 'Coffee and snacks for 30 people.',
     NULL, 'TODO', interval '1 day'),
    ('Improv Night', 'Print posters', 'Put posters in the main faculty buildings.',
     'radu.pop@stud.ubbcluj.ro', 'TODO', interval '7 days')
) AS v(event_title, title, description, assignee_email, status, due_before_start)
JOIN events e ON e.title = v.event_title
LEFT JOIN users u ON lower(u.email) = lower(v.assignee_email)
LEFT JOIN organization_memberships m ON m.user_id = u.id AND m.organization_id = e.organization_id
WHERE NOT EXISTS (SELECT 1 FROM event_tasks t WHERE t.event_id = e.id AND t.title = v.title);

-- ---------------------------------------------------------------- announcements
INSERT INTO announcements (event_id, author_id, title, body, created_at)
SELECT e.id, u.id, v.title, v.body, LEAST(now(), e.start_at) - v.before_start
FROM (VALUES
    ('Intro to Spring Boot Workshop', 'elena.marin@stud.ubbcluj.ro', 'Bring your laptop',
     'Please install JDK 21 and an IDE before the workshop.', interval '1 day'),
    ('Shakespeare Reading Marathon', 'radu.pop@stud.ubbcluj.ro', 'We have started!',
     'Come to Room 3 any time, the next shift starts every hour.', interval '0 minutes'),
    ('Board Games Evening', 'mihai.ionescu@stud.ubbcluj.ro', 'Event cancelled',
     'Sorry, the room is no longer available. We will announce a new date soon.', interval '1 day')
) AS v(event_title, author_email, title, body, before_start)
JOIN events e ON e.title = v.event_title
JOIN users u ON lower(u.email) = lower(v.author_email)
WHERE NOT EXISTS (SELECT 1 FROM announcements a WHERE a.event_id = e.id AND a.title = v.title);
