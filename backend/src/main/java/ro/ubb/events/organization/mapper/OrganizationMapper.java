package ro.ubb.events.organization.mapper;

import org.springframework.stereotype.Component;
import ro.ubb.events.organization.dto.OrganizationResponse;
import ro.ubb.events.organization.entity.Organization;

/** Converts entities to DTOs. Hand-written on purpose: easy to read, debug and test. */
@Component
public class OrganizationMapper {

    public OrganizationResponse toResponse(Organization organization) {
        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getDescription(),
                organization.getContactEmail(),
                organization.getCreatedAt());
    }
}
