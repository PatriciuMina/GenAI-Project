package ro.ubb.events.organization.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.ubb.events.organization.entity.OrganizationMembership;

public interface OrganizationMembershipRepository extends JpaRepository<OrganizationMembership, Long> {

    Optional<OrganizationMembership> findByUserIdAndOrganizationId(Long userId, Long organizationId);

    boolean existsByUserIdAndOrganizationId(Long userId, Long organizationId);

    List<OrganizationMembership> findByOrganizationId(Long organizationId);

    List<OrganizationMembership> findByUserId(Long userId);
}
