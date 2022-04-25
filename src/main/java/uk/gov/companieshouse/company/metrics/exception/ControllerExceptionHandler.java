package uk.gov.companieshouse.company.metrics.exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.company.metrics.repository.charges.ChargesRepository;
import uk.gov.companieshouse.company.metrics.repository.metrics.CompanyMetricsRepository;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

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

    private void populateResponseBody(Map<String, Object> responseBody , String correlationId){
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

    @ExceptionHandler(value = {DataAccessResourceFailureException.class})
    public ResponseEntity<Object> handleException(DataAccessResourceFailureException ex, WebRequest request) {
        var correlationId = generateShortCorrelationId();
        logger.error(String.format("Started: handleException: %s Generating error response ",
            correlationId), ex);
        Map<String, Object> responseBody = new LinkedHashMap<>();
        populateResponseBody(responseBody, correlationId);
        request.setAttribute("javax.servlet.error.exception", ex, 0);
        Throwable cause = ex.getCause();

        return new ResponseEntity<>(responseBody, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(value = {DataAccessException.class})
    public ResponseEntity<Object> handleException(DataAccessException ex, WebRequest request) {
        var correlationId = generateShortCorrelationId();
        logger.error(String.format("Started: handleException: %s Generating error response ",
            correlationId), ex);
        Map<String, Object> responseBody = new LinkedHashMap<>();
        populateResponseBody(responseBody, correlationId);
        request.setAttribute("javax.servlet.error.exception", ex, 0);

        return new ResponseEntity<>(responseBody, HttpStatus.NOT_EXTENDED);
    }

    @ExceptionHandler(value = {HttpMessageNotReadableException.class})
    public ResponseEntity<Object> handleException(HttpMessageNotReadableException ex, WebRequest request) {
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