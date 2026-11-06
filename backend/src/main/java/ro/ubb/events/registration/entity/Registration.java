package ro.ubb.events.registration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.user.entity.User;

/**
 * A user's registration to an event. At most one CONFIRMED registration per (event, user)
 * (partial unique index); cancelled rows are kept as history.
 */
@Entity
@Table(name = "registrations")
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false, updatable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status = RegistrationStatus.CONFIRMED;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    protected Registration() {
        // for JPA
    }

    public Registration(Event event, User user) {
        this.event = event;
        this.user = user;
    }

    @PrePersist
    void onCreate() {
        if (registeredAt == null) {
            registeredAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public User getUser() {
        return user;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public Instant getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(Instant checkedInAt) {
        this.checkedInAt = checkedInAt;
    }
}
