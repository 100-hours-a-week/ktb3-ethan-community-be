package org.restapi.springrestapi.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerWebMvcTest {

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("잘못된 JSON 본문은 HttpMessageNotReadableException으로 400이 반환된다")
    void invalidJson_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/test/payload")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COMMON001"));
    }

    @Test
    @DisplayName("필수 요청 파라미터 누락 시 400을 반환한다")
    void missingRequestParam_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/test/param"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COMMON001"));
    }

    @Test
    @DisplayName("타입 미스매치 파라미터는 400을 반환한다")
    void typeMismatch_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/test/param").param("id", "not-number"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COMMON001"));
    }

    @Test
    @DisplayName("존재하지 않는 라우팅은 404를 반환한다")
    void noHandlerFound_returnsNotFound() throws Exception {
        mockMvc.perform(get("/test/unknown"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("COMMON000"));
    }

    @RestController
    static class TestController {
        record Payload(@NotBlank String name) {}

        @PostMapping("/test/payload")
        void payload(@Valid @RequestBody Payload payload) {}

        @GetMapping("/test/param")
        void param(@RequestParam Long id) {}

        @GetMapping("/test/unknown")
        void unknown() throws Exception {
            throw new org.springframework.web.servlet.NoHandlerFoundException("GET", "/test/unknown", null);
        }
    }
}
