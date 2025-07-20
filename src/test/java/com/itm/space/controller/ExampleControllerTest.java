package com.itm.space.controller;

import com.itm.space.BaseIntegrationTest;
import com.itm.space.constant.RoleConstant;
import com.itm.space.model.request.ExampleRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExampleControllerTest extends BaseIntegrationTest {

    @Test
    @WithMockUser(authorities = RoleConstant.USER)
    @DisplayName("POST /api/v1/example - ответ 200")
    void testExampleRequest() throws Exception {
        // given
        ExampleRequest request = new ExampleRequest("test message");

        mockMvc.perform(post("/api/v1/example")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Example response: test message"));
    }
}
