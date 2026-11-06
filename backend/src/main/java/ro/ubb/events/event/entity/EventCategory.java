package ro.ubb.events.event.entity;

/** Fixed list of event categories; adding one requires a migration updating ck_events_category. */
public enum EventCategory {
    ACADEMIC,
    CAREER,
    CULTURAL,
    SOCIAL,
    SPORTS,
    VOLUNTEERING,
    WORKSHOP,
    OTHER
}
