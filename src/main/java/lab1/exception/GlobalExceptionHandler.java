package lab1.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * Глобальна обробка помилок у форматі RFC 9457 (application/problem+json).
 * Поле "instance" Spring заповнює шляхом запиту автоматично.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String ERRORS_BASE = "https://cosmo-cats.market/errors/";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> "Field '" + error.getField() + "' " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return problem(HttpStatus.BAD_REQUEST, "validation", "Validation failed", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "malformed-request", "Malformed request body",
                "Request body is missing or contains invalid JSON.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return problem(HttpStatus.BAD_REQUEST, "validation", "Validation failed",
                "Parameter '" + ex.getName() + "' has invalid value '" + ex.getValue() + "'.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatus(ResponseStatusException ex) {
        HttpStatusCode status = ex.getStatusCode();
        String title = status.isError() && status instanceof HttpStatus httpStatus
                ? httpStatus.getReasonPhrase()
                : "Request failed";
        return problem(status, "validation", title, ex.getReason());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        String detail = ex.getMessage() != null ? ex.getMessage() : "Requested resource was not found.";
        return problem(HttpStatus.NOT_FOUND, "not-found", "Resource not found", detail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "validation", "Validation failed", ex.getMessage());
    }

    /** Порушення обмежень БД, наприклад унікальність назви товару в межах категорії. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleConflict(DataIntegrityViolationException ex) {
        return problem(HttpStatus.CONFLICT, "conflict", "Data conflict",
                "Request violates data constraints (e.g. product name must be unique within its category).");
    }

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ProblemDetail handleDeliveryNotFound(HttpClientErrorException.NotFound ex) {
        return problem(HttpStatus.NOT_FOUND, "delivery-not-found", "Delivery not found",
                "Delivery information not found");
    }

    /** Остання лінія оборони: жодних stack trace назовні, лише лог. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal", "Internal server error",
                "Unexpected error occurred. Please try again later.");
    }

    private static ProblemDetail problem(HttpStatusCode status, String type, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(ERRORS_BASE + type));
        problem.setTitle(title);
        return problem;
    }
}
