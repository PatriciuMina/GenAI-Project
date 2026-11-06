package ro.ubb.events.organization.service;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ubb.events.common.exception.NotFoundException;
import ro.ubb.events.organization.dto.OrganizationResponse;
import ro.ubb.events.organization.mapper.OrganizationMapper;
import ro.ubb.events.organization.repository.OrganizationRepository;

/** Read operations on organizations (W1). Returns DTOs only; entities stay in this layer. */
@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    public OrganizationService(OrganizationRepository organizationRepository, OrganizationMapper organizationMapper) {
        this.organizationRepository = organizationRepository;
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
                .orElseThrow(() -> new NotFoundException("Organization " + id + " not found"));
    }
}
