package com.flex.mind.tech.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating or updating event configuration")
public class EventConfigRequestDto {

    @Schema(description = "Event configuration ID (auto-generated if not provided)", example = "507f1f77bcf86cd799439011")
    private String id;

    @NotBlank(message = "Event type cannot be blank")
    @Schema(description = "Type of the event", example = "USER_REGISTRATION", required = true)
    private String eventType;

    @NotBlank(message = "Source cannot be blank")
    @Schema(description = "Source system of the event", example = "auth-service", required = true)
    private String source;

    @NotNull(message = "Enabled flag cannot be null")
    @Schema(description = "Whether the event configuration is enabled", example = "true", required = true)
    private Boolean enabled;
}
