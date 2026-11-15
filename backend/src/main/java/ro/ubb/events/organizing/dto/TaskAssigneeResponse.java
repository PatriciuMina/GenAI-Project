package ro.ubb.events.organizing.dto;

/** The assignee of a task: an organization membership and the member's name. */
public record TaskAssigneeResponse(Long membershipId, String fullName) { }
