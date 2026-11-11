package ro.ubb.events.organization.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.ubb.events.organization.entity.OrganizationMembership;

public interface OrganizationMembershipRepository extends JpaRepository<OrganizationMembership, Long> {

    Optional<OrganizationMembership> findByUserIdAndOrganizationId(Long userId, Long organizationId);

    boolean existsByUserIdAndOrganizationId(Long userId, Long organizationId);

    /** Loads the user in the same query, so mapping to a DTO needs no extra query per row. */
    @EntityGraph(attributePaths = "user")
    List<OrganizationMembership> findByOrganizationId(Long organizationId);

    /** Loads the organization in the same query, so mapping to a DTO needs no extra query per row. */
    @EntityGraph(attributePaths = "organization")
    List<OrganizationMembership> findByUserId(Long userId);
}
