package dev.magurno.dockplanner.common;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Maps domain exceptions to RFC 9457 problem details. Validation errors are
 * handled by the base class and also returned as problem details.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	/** Bean Validation errors, with one message per invalid field. */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> errors = new TreeMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.putIfAbsent(error.getField(), error.getDefaultMessage());
		}
		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "validation", "Validation failed",
				"One or more fields are invalid.");
		problem.setProperty("errors", errors);
		return handleExceptionInternal(ex, problem, headers, status, request);
	}

	@ExceptionHandler(NotFoundException.class)
	ProblemDetail notFound(NotFoundException ex) {
		return problem(HttpStatus.NOT_FOUND, "not-found", "Resource not found", ex.getMessage());
	}

	@ExceptionHandler(ConflictException.class)
	ProblemDetail conflict(ConflictException ex) {
		ProblemDetail problem = problem(HttpStatus.CONFLICT, "conflict", "Scheduling conflict", ex.getMessage());
		if (!ex.getConflictingBookingIds().isEmpty()) {
			problem.setProperty("conflictingBookingIds", ex.getConflictingBookingIds());
		}
		return problem;
	}

	@ExceptionHandler(BusinessRuleException.class)
	ProblemDetail businessRule(BusinessRuleException ex) {
		return problem(HttpStatus.UNPROCESSABLE_CONTENT, "business-rule", "Business rule violated", ex.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ProblemDetail badRequest(IllegalArgumentException ex) {
		return problem(HttpStatus.BAD_REQUEST, "bad-request", "Invalid request", ex.getMessage());
	}

	/** Last line of defence: the database exclusion constraints caught a concurrent overlap. */
	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail integrity(DataIntegrityViolationException ex) {
		return problem(HttpStatus.CONFLICT, "conflict", "Scheduling conflict",
				"The request conflicts with data saved in the meantime. Reload and try again.");
	}

	private static ProblemDetail problem(HttpStatus status, String type, String title, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setType(URI.create("urn:dock-planner:problem:" + type));
		problem.setTitle(title);
		return problem;
	}

}
