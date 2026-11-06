package ro.ubb.events.common.dto;

/** A single validation error on a request field. */
public record FieldErrorDto(String field, String message) {
}
