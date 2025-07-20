package com.flex.mind.tech.service;

import com.flex.mind.tech.model.request.EventConfigRequestDto;

public interface ServiceEventConfig {
    EventConfigRequestDto createEventConfig(EventConfigRequestDto eventDto);
}
