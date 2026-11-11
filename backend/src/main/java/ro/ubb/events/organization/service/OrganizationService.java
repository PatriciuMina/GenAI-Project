package ro.ubb.events.organization.service;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ubb.events.common.exception.ApiException;
import ro.ubb.events.common.exception.ConflictException;
import ro.ubb.events.common.exception.NotFoundException;
import ro.ubb.events.organization.dto.CreateOrganizationRequest;
import ro.ubb.events.organization.dto.OrganizationResponse;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.entity.OrganizationMembership;
import ro.ubb.events.organization.entity.OrganizationRole;
import ro.ubb.events.organization.mapper.OrganizationMapper;
import ro.ubb.events.organization.repository.OrganizationMembershipRepository;
import ro.ubb.events.organization.repository.OrganizationRepository;
import ro.ubb.events.user.entity.User;
import ro.ubb.events.user.service.UserService;

/** Organizations (W1). Returns DTOs only; entities stay in this layer. */
@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final UserService userService;
    private final OrganizationMapper organizationMapper;

    public OrganizationService(OrganizationRepository organizationRepository,
                               OrganizationMembershipRepository membershipRepository,
                               UserService userService,
                               OrganizationMapper organizationMapper) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.userService = userService;
        this.organizationMapper = organizationMapper;
    }

    /** All organizations, sorted by name. */
    @Transactional(readOnly = true)
    public List<OrganizationResponse> findAll() {
        return organizationRepository.findAll(Sort.by("name")).stream()
                .map(organizationMapper::toResponse)
                .toList();
    }

    /** One organization, or 404 NOT_FOUND. */
    @Transactional(readOnly = true)
    public OrganizationResponse getById(Long id) {
        return organizationRepository.findById(id)
                .map(organizationMapper::toResponse)
                .orElseThrow(() -> notFound(id));
    }

    /**
     * The organization entity, for the service layer of other features only (e.g. an event
     * belongs to an organization); the entity must not reach controllers. 404 NOT_FOUND if unknown.
     */
    @Transactional(readOnly = true)
    public Organization getEntityById(Long id) {
        return organizationRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    /**
     * Creates an organization and makes the given account its PRESIDENT, in one transaction (both
     * or nothing). The name is stored without leading/trailing spaces; blank optional fields are
     * stored as null. Who may call this is checked in the controller (ADMIN only).
     *
     * @throws ConflictException 409 ORGANIZATION_NAME_TAKEN if the name exists, ignoring case
     * @throws ApiException 404 USER_NOT_FOUND if no account has the president's email
     */
    @Transactional
    public OrganizationResponse create(CreateOrganizationRequest request) {
        String name = request.name().strip();
        if (organizationRepository.existsByNameIgnoreCase(name)) {
            throw nameTaken(name);
        }
        User president = userService.findByEmail(request.presidentEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND",
                        "No account found with email " + request.presidentEmail()));
        Organization organization;
        try {
            organization = organizationRepository.saveAndFlush(
                    new Organization(name, blankToNull(request.description()), blankToNull(request.contactEmail())));
        } catch (DataIntegrityViolationException e) {
            // two requests with the same name at the same time: ux_organizations_name_lower wins
            throw nameTaken(name);
        }
        membershipRepository.save(new OrganizationMembership(president, organization, OrganizationRole.PRESIDENT));
        return organizationMapper.toResponse(organization);
    }

    private static NotFoundException notFound(Long id) {
        return new NotFoundException("Organization " + id + " not found");
    }

    private static ConflictException nameTaken(String name) {
        return new ConflictException("ORGANIZATION_NAME_TAKEN", "An organization named '" + name + "' already exists");
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
