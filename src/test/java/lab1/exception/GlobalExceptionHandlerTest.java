package lab1.exception;

import lab1.exception.GlobalExceptionHandler;
import lab1.exception.ResourceNotFoundException;
import lab1.controller.ProductController;
import lab1.dto.ProductDetailsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationErrorsShouldReturnBadRequestWithFieldDetails() throws Exception {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new ProductDetailsDTO(), "productDetailsDTO");
        bindingResult.addError(new FieldError("productDetailsDTO", "name", "must not be null"));

        MethodParameter parameter = new MethodParameter(
                ProductController.class.getMethod("createProduct", ProductDetailsDTO.class), 0);

        ProblemDetail problem = handler.handleValidationErrors(
                new MethodArgumentNotValidException(parameter, bindingResult));

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Validation failed", problem.getTitle());
        assertEquals("Field 'name' must not be null", problem.getDetail());
    }

    @Test
    void handleDeliveryNotFoundShouldReturnNotFound() {
        HttpClientErrorException.NotFound ex = (HttpClientErrorException.NotFound)
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        HttpHeaders.EMPTY, new byte[0], null);

        ProblemDetail problem = handler.handleDeliveryNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        assertEquals("Delivery not found", problem.getTitle());
    }

    @Test
    void handleNotFoundShouldUseDefaultDetailWhenMessageMissing() {
        ProblemDetail problem = handler.handleNotFound(new ResourceNotFoundException(null));

        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        assertEquals("Requested resource was not found.", problem.getDetail());
    }

    @Test
    void handleResponseStatusShouldKeepStatusAndReason() {
        ProblemDetail problem = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "bad page"));

        assertEquals(400, problem.getStatus());
        assertEquals("Bad Request", problem.getTitle());
        assertEquals("bad page", problem.getDetail());
    }

    @Test
    void handleUnexpectedShouldHideInternalDetails() {
        ProblemDetail problem = handler.handleUnexpected(new IllegalStateException("db password leaked"));

        assertEquals(500, problem.getStatus());
        assertFalse(problem.getDetail().contains("password"));
    }

    @Test
    void handleConflictShouldReturn409() {
        ProblemDetail problem = handler.handleConflict(new DataIntegrityViolationException("duplicate"));

        assertEquals(409, problem.getStatus());
        assertEquals("Data conflict", problem.getTitle());
        assertFalse(problem.getDetail().contains("duplicate"));
    }

    @Test
    void handleIllegalArgumentShouldReturn400() {
        ProblemDetail problem = handler.handleIllegalArgument(new IllegalArgumentException("bad limit"));

        assertEquals(400, problem.getStatus());
        assertEquals("bad limit", problem.getDetail());
    }
}
