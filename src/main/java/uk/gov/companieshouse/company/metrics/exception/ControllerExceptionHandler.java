package uk.gov.companieshouse.company.metrics.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import uk.gov.companieshouse.logging.Logger;

@ControllerAdvice
public class ControllerExceptionHandler {
    public static final String TARGET = "-";
    public static final String REPLACEMENT = "";

    private final Logger logger;

    /**
     * Constructor.
     */
    public ControllerExceptionHandler(Logger logger) {
        this.logger = logger;
    }

    private void populateResponseBody(Map<String, Object> responseBody , String correlationId) {
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", "There is issue completing the request.");
        responseBody.put("correlationId", correlationId);
    }

    /**
     * Runtime exception handler.
     *
     * @param ex      exception to handle.
     * @return error response to return.
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleException(Exception ex, WebRequest request) {
        String correlationId = generateShortCorrelationId();
        Map<String, Object> responseBody = new LinkedHashMap<>();
        populateResponseBody(responseBody, correlationId);
        request.setAttribute("javax.servlet.error.exception", ex, 0);
        logger.error("correlationId = " + correlationId, ex);

        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * DataAccessResourceFailureException handler.
     *
     * @param ex      exception to handle.
     * @return error response to return.
     */
    @ExceptionHandler(value = {DataAccessResourceFailureException.class})
    public ResponseEntity<Object> handleException(DataAccessResourceFailureException ex,
            WebRequest request) {
        String correlationId = generateShortCorrelationId();
        logger.error(String.format("Started: handleException: %s Generating error response ",
                correlationId), ex);
        Map<String, Object> responseBody = new LinkedHashMap<>();
        populateResponseBody(responseBody, correlationId);
        request.setAttribute("javax.servlet.error.exception", ex, 0);
        Throwable cause = ex.getCause();

        return new ResponseEntity<>(responseBody, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * DataAccessException handler.
     *
     * @param ex      exception to handle.
     * @return error response to return.
     */
    @ExceptionHandler(value = {DataAccessException.class})
    public ResponseEntity<Object> handleException(DataAccessException ex,
            WebRequest request) {
        String correlationId = generateShortCorrelationId();
        logger.error(String.format("Started: handleException: %s Generating error response ",
                correlationId), ex);
        Map<String, Object> responseBody = new LinkedHashMap<>();
        populateResponseBody(responseBody, correlationId);
        request.setAttribute("javax.servlet.error.exception", ex, 0);

        return new ResponseEntity<>(responseBody, HttpStatus.NOT_EXTENDED);
    }

    /**
     * HttpMessageNotReadableException handler.
     *
     * @param ex      exception to handle.
     * @return error response to return.
     */
    @ExceptionHandler(value = {HttpMessageNotReadableException.class})
    public ResponseEntity<Object> handleException(HttpMessageNotReadableException ex,
            WebRequest request) {
        var correlationId = generateShortCorrelationId();
        logger.error(String.format("Started: handleException: %s Generating error response ",
                correlationId), ex);
        Map<String, Object> responseBody = new LinkedHashMap<>();
        populateResponseBody(responseBody, correlationId);
        request.setAttribute("javax.servlet.error.exception", ex, 0);

        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    private String generateShortCorrelationId() {
        return UUID.randomUUID().toString().replace(TARGET, REPLACEMENT).substring(0,8);
    }
}