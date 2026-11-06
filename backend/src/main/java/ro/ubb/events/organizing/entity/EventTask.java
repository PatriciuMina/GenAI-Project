package ro.ubb.events.organizing.entity;

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
import ro.ubb.events.organization.entity.OrganizationMembership;

/**
 * An organizer task for an event. The assignee is a membership of the event's organization
 * (checked in the service layer); it becomes null if that membership is deleted.
 */
@Entity
@Table(name = "event_tasks")
public class EventTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false, updatable = false)
    private Event event;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_membership_id")
    private OrganizationMembership assignee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.TODO;

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected EventTask() {
        // for JPA
    }

    public EventTask(Event event, String title) {
        this.event = event;
        this.title = title;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OrganizationMembership getAssignee() {
        return assignee;
    }

    public void setAssignee(OrganizationMembership assignee) {
        this.assignee = assignee;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public void setDueAt(Instant dueAt) {
        this.dueAt = dueAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
