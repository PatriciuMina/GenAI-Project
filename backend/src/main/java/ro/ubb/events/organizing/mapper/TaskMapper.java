package ro.ubb.events.organizing.mapper;

import org.springframework.stereotype.Component;
import ro.ubb.events.organization.entity.OrganizationMembership;
import ro.ubb.events.organizing.dto.TaskAssigneeResponse;
import ro.ubb.events.organizing.dto.TaskResponse;
import ro.ubb.events.organizing.entity.EventTask;

/** Entity to DTO conversion for tasks; used only by the service layer. */
@Component
public class TaskMapper {

    public TaskResponse toResponse(EventTask task) {
        OrganizationMembership assignee = task.getAssignee();
        TaskAssigneeResponse assigneeResponse = assignee == null
                ? null
                : new TaskAssigneeResponse(assignee.getId(), assignee.getUser().getFullName());
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getStatus(),
                task.getDueAt(), assigneeResponse, task.getCreatedAt());
    }
}
