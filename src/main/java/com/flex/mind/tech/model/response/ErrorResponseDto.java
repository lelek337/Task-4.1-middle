package com.flex.mind.tech.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response structure")
public class ErrorResponseDto {

    @Schema(description = "Error code", example = "VALIDATION_ERROR")
    private String code;

    @Schema(description = "Error message", example = "Invalid request parameters")
    private String message;

    @Schema(description = "Detailed error description", example = "Field 'eventType' is required")
    private String details;

    @Schema(description = "Timestamp when error occurred", example = "2025-07-20T15:30:45")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "Request path where error occurred", example = "/api/v1/event-configs")
    private String path;
}
