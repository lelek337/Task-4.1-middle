package com.flex.mind.tech.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response DTO for event configuration")
public class EventConfigResponseDto {

    @Schema(description = "Unique identifier", example = "507f1f77bcf86cd799439011")
    @JsonProperty("id")
    private String id;

    @Schema(description = "Type of the event", example = "USER_REGISTRATION")
    @JsonProperty("eventType")
    private String eventType;

    @Schema(description = "Source system of the event", example = "auth-service")
    @JsonProperty("source")
    private String source;

    @Schema(description = "Whether the event configuration is enabled", example = "true")
    @JsonProperty("enabled")
    private Boolean enabled;

    @Schema(description = "Creation timestamp", example = "2024-01-15T10:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2024-01-15T10:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}
