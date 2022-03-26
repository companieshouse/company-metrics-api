package uk.gov.companieshouse.company.metrics.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@ControllerAdvice
public class ControllerExceptionHandler {
    public static final String APPLICATION_NAME_SPACE = "company-metrics-api";
    public static final String TARGET = "-";
    public static final String REPLACEMENT = "";

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    /**
     * Runtime exception handler.
     *
     * @param ex      exception to handle.
     * @return error response to return.
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleException(Exception ex, WebRequest request) {
        LOGGER.error(ex.getMessage(), ex);
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", "Error processing the request");
        responseBody.put("correlationId", generateShortCorrelationId());
        request.setAttribute("javax.servlet.error.exception", ex, 0);
        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String generateShortCorrelationId() {
        return UUID.randomUUID().toString().replace(TARGET, REPLACEMENT).substring(0,8);
    }
}