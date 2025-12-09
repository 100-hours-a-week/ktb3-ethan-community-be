package org.restapi.springrestapi.integration;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthCheckIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("CSRF 토큰 엔드포인트는 토큰 정보와 쿠키를 반환한다")
    void csrf_returnsTokenAndCookie() throws Exception {
        mockMvc.perform(get("/csrf"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.header_name").value("X-XSRF-TOKEN"))
            .andExpect(jsonPath("$.parameter_name").value("_csrf"))
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(header().string("Set-Cookie", containsString("XSRF-TOKEN=")));
    }
}
