package ro.ubb.events.organizing.mapper;

import org.springframework.stereotype.Component;
import ro.ubb.events.organizing.dto.AnnouncementResponse;
import ro.ubb.events.organizing.entity.Announcement;

/** Entity to DTO conversion for announcements; used only by the service layer. */
@Component
public class AnnouncementMapper {

    public AnnouncementResponse toResponse(Announcement announcement) {
        return new AnnouncementResponse(announcement.getId(), announcement.getTitle(), announcement.getBody(),
                announcement.getAuthor().getFullName(), announcement.getCreatedAt());
    }
}
