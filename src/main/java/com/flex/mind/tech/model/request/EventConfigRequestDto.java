package com.flex.mind.tech.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating or updating event configuration")
public class EventConfigRequestDto {

    @NotBlank(message = "Event type cannot be blank")
    @Schema(description = "Type of the event", example = "USER_REGISTRATION", required = true)
    private String eventType;

    @NotBlank(message = "Source cannot be blank")
    @Schema(description = "Source system of the event", example = "auth-service", required = true)
    private String source;

    @NotNull(message = "Enabled flag cannot be null")
    @Schema(description = "Whether the event configuration is enabled", example = "true", required = true)
    private Boolean enabled;

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    @Override
    public String toString() {
        return "EventConfigRequestDto{" +
                "eventType='" + eventType + '\'' +
                ", source='" + source + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
