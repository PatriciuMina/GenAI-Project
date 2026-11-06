package ro.ubb.events.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ro.ubb.events.common.exception.NotFoundException;
import ro.ubb.events.organization.dto.OrganizationResponse;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.mapper.OrganizationMapper;
import ro.ubb.events.organization.repository.OrganizationRepository;
import ro.ubb.events.organization.service.OrganizationService;

/** Unit test: repository is mocked, the (simple) mapper is real. No Spring, no database. */
@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        organizationService = new OrganizationService(organizationRepository, new OrganizationMapper());
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
}
