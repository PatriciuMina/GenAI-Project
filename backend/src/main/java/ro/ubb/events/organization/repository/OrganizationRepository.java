package ro.ubb.events.organization.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.ubb.events.organization.entity.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
