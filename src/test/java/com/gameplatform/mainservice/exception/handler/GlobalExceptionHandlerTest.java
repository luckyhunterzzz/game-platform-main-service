package com.gameplatform.mainservice.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.exception.exceptions.DictionaryItemInUseException;
import com.gameplatform.mainservice.exception.exceptions.InvalidAuthenticationException;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.hero.dto.response.HeroUsageReferenceResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private Validator validator;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.afterPropertiesSet();
        validator = validatorFactoryBean;

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestExceptionController(validator))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validatorFactoryBean)
                .build();
    }

    @Test
    void shouldReturnBadRequestForBusinessValidationException() throws Exception {
        mockMvc.perform(get("/test/business-validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Business rule failed"))
                .andExpect(jsonPath("$.path").value("/test/business-validation"));
    }

    @Test
    void shouldReturnNotFoundForNotFoundException() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Hero not found: 42"))
                .andExpect(jsonPath("$.path").value("/test/not-found"));
    }

    @Test
    void shouldReturnConflictForDictionaryItemInUseException() throws Exception {
        mockMvc.perform(get("/test/dictionary-in-use"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("ENTITY_IN_USE"))
                .andExpect(jsonPath("$.heroes[0].id").value(7))
                .andExpect(jsonPath("$.heroes[0].slug").value("khufu"))
                .andExpect(jsonPath("$.path").value("/test/dictionary-in-use"));
    }

    @Test
    void shouldReturnUnauthorizedForInvalidAuthenticationException() throws Exception {
        mockMvc.perform(get("/test/invalid-auth"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication is missing in SecurityContext"))
                .andExpect(jsonPath("$.path").value("/test/invalid-auth"));
    }

    @Test
    void shouldReturnForbiddenForAccessDeniedException() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value("/test/access-denied"));
    }

    @Test
    void shouldReturnBadRequestForMethodArgumentNotValidException() throws Exception {
        mockMvc.perform(post("/test/request-body-validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new InvalidBodyRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("name: must not be blank"))
                .andExpect(jsonPath("$.path").value("/test/request-body-validation"));
    }

    @Test
    void shouldReturnBadRequestForConstraintViolationException() throws Exception {
        mockMvc.perform(get("/test/constraint-validation").param("value", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("value: must be greater than or equal to 1"))
                .andExpect(jsonPath("$.path").value("/test/constraint-validation"));
    }

    @Test
    void shouldReturnBadRequestForMethodArgumentTypeMismatchException() throws Exception {
        mockMvc.perform(get("/test/type-mismatch").param("mode", "wrong"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("mode: invalid value 'wrong'"))
                .andExpect(jsonPath("$.path").value("/test/type-mismatch"));
    }

    @Test
    void shouldReturnBadRequestForHttpMessageNotReadableException() throws Exception {
        mockMvc.perform(post("/test/request-body-validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed request body"))
                .andExpect(jsonPath("$.path").value("/test/request-body-validation"));
    }

    @Test
    void shouldReturnInternalServerErrorForUnexpectedException() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected internal server error"))
                .andExpect(jsonPath("$.path").value("/test/unexpected"));
    }

    @RestController
    @Validated
    @RequestMapping("/test")
    static class TestExceptionController {

        private final Validator validator;

        TestExceptionController(Validator validator) {
            this.validator = validator;
        }

        @GetMapping("/business-validation")
        void businessValidation() {
            throw new BusinessValidationException("Business rule failed");
        }

        @GetMapping("/not-found")
        void notFound() {
            throw new NotFoundException("Hero not found: 42");
        }

        @GetMapping("/dictionary-in-use")
        void dictionaryInUse() {
            throw new DictionaryItemInUseException(
                    "Passive skill is used by one or more heroes and cannot be deleted",
                    List.of(new HeroUsageReferenceResponse(
                            7L,
                            "khufu",
                            new LocalizedTextJson("Khufu", "Khufu"),
                            "PUBLISHED"
                    ))
            );
        }

        @GetMapping("/invalid-auth")
        void invalidAuth() {
            throw new InvalidAuthenticationException("Authentication is missing in SecurityContext");
        }

        @GetMapping("/access-denied")
        void accessDenied() {
            throw new AccessDeniedException("Access denied");
        }

        @PostMapping("/request-body-validation")
        void requestBodyValidation(@RequestBody @Valid InvalidBodyRequest request) {
        }

        @GetMapping("/constraint-validation")
        void constrained(@RequestParam @Min(1) int value) {
            ConstrainedRequest request = new ConstrainedRequest(value);
            var violations = validator.validate(request);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }

        @GetMapping("/type-mismatch")
        void typeMismatch(@RequestParam TestMode mode) {
        }

        @GetMapping("/unexpected")
        void unexpected() {
            throw new RuntimeException("boom");
        }
    }

    record InvalidBodyRequest(@NotBlank String name) {
    }

    record ConstrainedRequest(@Min(1) int value) {
    }

    enum TestMode {
        FAST,
        SLOW
    }
}

