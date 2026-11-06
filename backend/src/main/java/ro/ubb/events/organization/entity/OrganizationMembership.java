package ro.ubb.events.organization.entity;

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
import ro.ubb.events.user.entity.User;

/** Membership of a user in an organization. Unique per (user, organization). */
@Entity
@Table(name = "organization_memberships")
public class OrganizationMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @Column(name = "org_role", nullable = false, length = 20)
    private OrganizationRole orgRole;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    protected OrganizationMembership() {
        // for JPA
    }

    public OrganizationMembership(User user, Organization organization, OrganizationRole orgRole) {
        this.user = user;
        this.organization = organization;
        this.orgRole = orgRole;
    }

    @PrePersist
    void onCreate() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Organization getOrganization() {
        return organization;
    }

    public OrganizationRole getOrgRole() {
        return orgRole;
    }

    public void setOrgRole(OrganizationRole orgRole) {
        this.orgRole = orgRole;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
