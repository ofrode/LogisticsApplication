package com.logisticsapplication.exception;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @Mock
    private ConstraintViolation<Object> constraintViolation;

    @Mock
    private Path propertyPath;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setValidator(validator)
                .build();
    }

    @Test
    void handlesMethodArgumentNotValid() throws Exception {
        mockMvc.perform(post("/validated-body")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").value("must not be blank"));
    }

    @Test
    void handlesHandlerMethodValidationException() throws Exception {
        mockMvc.perform(get("/validated-parameter").param("count", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void handlesConstraintViolationException() throws Exception {
        org.mockito.Mockito.when(constraintViolation.getPropertyPath()).thenReturn(propertyPath);
        org.mockito.Mockito.when(propertyPath.toString()).thenReturn("shipment.customerId");
        org.mockito.Mockito.when(constraintViolation.getMessage()).thenReturn("must be positive");

        mockMvc = MockMvcBuilders.standaloneSetup(new TestController(Set.of(constraintViolation)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();

        mockMvc.perform(get("/constraint-violation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['shipment.customerId']").value("must be positive"));
    }

    @Test
    void handlesResponseStatusException() throws Exception {
        mockMvc.perform(get("/response-status"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("shipment missing"))
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.name()));
    }

    @Test
    void handlesBadRequestVariants() throws Exception {
        mockMvc.perform(get("/type-mismatch/not-a-number"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/missing-param"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/malformed-json")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handlesDataIntegrityViolation() throws Exception {
        mockMvc.perform(get("/data-integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Conflict: resource already exists or violates constraints"));
    }

    @Test
    void handlesUnhandledExceptions() throws Exception {
        mockMvc.perform(get("/unhandled"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected internal server error"));
    }

    @RestController
    @RestControllerAdvice
    static class TestController {

        private final Set<ConstraintViolation<?>> violations;

        TestController() {
            this(Set.of());
        }

        TestController(Set<ConstraintViolation<?>> violations) {
            this.violations = violations;
        }

        @PostMapping("/validated-body")
        String validatedBody(@Valid @RequestBody TestBody body) {
            return body.name();
        }

        @GetMapping("/validated-parameter")
        String validatedParameter(@RequestParam @Positive Integer count) {
            return String.valueOf(count);
        }

        @GetMapping("/constraint-violation")
        String constraintViolation() {
            throw new ConstraintViolationException(violations);
        }

        @GetMapping("/response-status")
        String responseStatus() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "shipment missing");
        }

        @GetMapping("/type-mismatch/{id}")
        String typeMismatch(@org.springframework.web.bind.annotation.PathVariable Long id) {
            return String.valueOf(id);
        }

        @GetMapping("/missing-param")
        String missingParam(@RequestParam String query) {
            return query;
        }

        @PostMapping("/malformed-json")
        String malformedJson(@RequestBody TestBody body) {
            return body.name();
        }

        @GetMapping("/data-integrity")
        String dataIntegrity() {
            throw new DataIntegrityViolationException("duplicate key");
        }

        @GetMapping("/unhandled")
        String unhandled() {
            throw new IllegalStateException("boom");
        }
    }

    record TestBody(@NotBlank String name) {
    }
}
