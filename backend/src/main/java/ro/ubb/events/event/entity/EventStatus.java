package ro.ubb.events.event.entity;

/** Event lifecycle (W2). Transitions are enforced in the event service, not in the DB. */
public enum EventStatus {
    DRAFT,
    PUBLISHED,
    REGISTRATION_CLOSED,
    ONGOING,
    COMPLETED,
    CANCELLED
}
