package ro.ubb.events.organization.entity;

/**
 * Role of a user inside one organization. Declared from highest to lowest rank: the members list
 * is sorted by this order ({@code MembershipService}), so do not reorder the constants.
 */
public enum OrganizationRole {
    PRESIDENT,
    ORGANIZER,
    MEMBER
}
