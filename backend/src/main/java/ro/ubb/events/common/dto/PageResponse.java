package ro.ubb.events.common.dto;

import java.util.List;

/** A page of results with the paging metadata exposed by the API. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
}
