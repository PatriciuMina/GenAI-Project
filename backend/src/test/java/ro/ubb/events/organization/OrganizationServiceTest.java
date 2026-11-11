package ro.ubb.events.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
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
import ro.ubb.events.organization.service.OrganizationService;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;
import ro.ubb.events.user.service.UserService;

/** Unit test: repositories and UserService are mocked, the (simple) mapper is real. No Spring, no database. */
@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMembershipRepository membershipRepository;

    @Mock
    private UserService userService;

    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        organizationService = new OrganizationService(
                organizationRepository, membershipRepository, userService, new OrganizationMapper());
    }

    @Test
    void findAllReturnsDtosSortedByName() {
        Organization cs = new Organization("CS Society", "Workshops", "cs@test.ro");
        Organization theatre = new Organization("Theatre Club", null, null);
        when(organizationRepository.findAll(Sort.by("name"))).thenReturn(List.of(cs, theatre));

        List<OrganizationResponse> result = organizationService.findAll();

        assertThat(result).extracting(OrganizationResponse::name).containsExactly("CS Society", "Theatre Club");
        assertThat(result.get(0).description()).isEqualTo("Workshops");
        assertThat(result.get(0).contactEmail()).isEqualTo("cs@test.ro");
    }

    @Test
    void getByIdReturnsDto() {
        when(organizationRepository.findById(5L))
                .thenReturn(Optional.of(new Organization("CS Society", "Workshops", "cs@test.ro")));

        OrganizationResponse result = organizationService.getById(5L);

        assertThat(result.name()).isEqualTo("CS Society");
    }

    @Test
    void getByIdThrowsNotFoundForUnknownId() {
        when(organizationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizationService.getById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Organization 99 not found");
    }

    // --- create: POST /organizations (T-08) ---

    @Test
    void createSavesTheOrganizationAndMakesThePresidentAMember() {
        User president = new User("andrei@test.ro", "hash", "Andrei Toma", GlobalRole.STUDENT);
        Instant createdAt = Instant.parse("2026-11-11T12:00:00Z");
        when(organizationRepository.existsByNameIgnoreCase("UBB Robotics Club")).thenReturn(false);
        when(userService.findByEmail("andrei@test.ro")).thenReturn(Optional.of(president));
        when(organizationRepository.saveAndFlush(any(Organization.class))).thenAnswer(invocation -> {
            Organization toSave = invocation.getArgument(0);
            ReflectionTestUtils.setField(toSave, "id", 3L);
            ReflectionTestUtils.setField(toSave, "createdAt", createdAt);
            return toSave;
        });

        OrganizationResponse result = organizationService.create(new CreateOrganizationRequest(
                "  UBB Robotics Club ", "Robots and coffee", "robotics@ubbcluj.ro", "andrei@test.ro"));

        assertThat(result).isEqualTo(new OrganizationResponse(
                3L, "UBB Robotics Club", "Robots and coffee", "robotics@ubbcluj.ro", createdAt));
        ArgumentCaptor<OrganizationMembership> membership = ArgumentCaptor.forClass(OrganizationMembership.class);
        verify(membershipRepository).save(membership.capture());
        assertThat(membership.getValue().getUser()).isSameAs(president);
        assertThat(membership.getValue().getOrganization().getId()).isEqualTo(3L);
        assertThat(membership.getValue().getOrgRole()).isEqualTo(OrganizationRole.PRESIDENT);
    }

    @Test
    void createStoresBlankOptionalFieldsAsNull() {
        when(organizationRepository.existsByNameIgnoreCase("Chess Club")).thenReturn(false);
        when(userService.findByEmail("andrei@test.ro"))
                .thenReturn(Optional.of(new User("andrei@test.ro", "hash", "Andrei Toma", GlobalRole.STUDENT)));
        when(organizationRepository.saveAndFlush(any(Organization.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrganizationResponse result = organizationService.create(
                new CreateOrganizationRequest("Chess Club", "  ", "", "andrei@test.ro"));

        assertThat(result.description()).isNull();
        assertThat(result.contactEmail()).isNull();
    }

    @Test
    void createThrowsNameTakenWhenTheNameExistsIgnoringCase() {
        when(organizationRepository.existsByNameIgnoreCase("ubb theatre club")).thenReturn(true);

        assertThatThrownBy(() -> organizationService.create(
                new CreateOrganizationRequest("ubb theatre club", null, null, "andrei@test.ro")))
                .isInstanceOfSatisfying(ConflictException.class,
                        e -> assertThat(e.getCode()).isEqualTo("ORGANIZATION_NAME_TAKEN"));
        verify(organizationRepository, never()).saveAndFlush(any());
        verifyNoInteractions(userService, membershipRepository);
    }

    @Test
    void createThrowsUserNotFoundForUnknownPresidentAndSavesNothing() {
        when(organizationRepository.existsByNameIgnoreCase("UBB Robotics Club")).thenReturn(false);
        when(userService.findByEmail("nobody@test.ro")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizationService.create(
                new CreateOrganizationRequest("UBB Robotics Club", null, null, "nobody@test.ro")))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(e.getCode()).isEqualTo("USER_NOT_FOUND");
                });
        verify(organizationRepository, never()).saveAndFlush(any());
        verifyNoInteractions(membershipRepository);
    }

    @Test
    void createMapsUniqueNameViolationToNameTaken() {
        // two admins create the same name at the same moment: both pass the check, the unique index rejects one
        when(organizationRepository.existsByNameIgnoreCase("UBB Robotics Club")).thenReturn(false);
        when(userService.findByEmail("andrei@test.ro"))
                .thenReturn(Optional.of(new User("andrei@test.ro", "hash", "Andrei Toma", GlobalRole.STUDENT)));
        when(organizationRepository.saveAndFlush(any(Organization.class)))
                .thenThrow(new DataIntegrityViolationException("ux_organizations_name_lower"));

        assertThatThrownBy(() -> organizationService.create(
                new CreateOrganizationRequest("UBB Robotics Club", null, null, "andrei@test.ro")))
                .isInstanceOfSatisfying(ConflictException.class,
                        e -> assertThat(e.getCode()).isEqualTo("ORGANIZATION_NAME_TAKEN"));
        verifyNoInteractions(membershipRepository);
    }
}
