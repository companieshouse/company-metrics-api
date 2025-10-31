package uk.gov.companieshouse.company.metrics.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import uk.gov.companieshouse.company.metrics.exceptions.BadRequestException;
import uk.gov.companieshouse.company.metrics.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.company.metrics.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;

@ControllerAdvice
public class ExceptionHandlerConfig {

    private static final String X_REQUEST_ID_HEADER = "x-request-id";
    private final Logger logger;

    @Autowired
    public ExceptionHandlerConfig(Logger logger) {
        this.logger = logger;
    }

    private void populateResponseBody(Map<String, Object> responseBody, String correlationId) {
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", String.format("Exception occurred while processing the API"
                + " request with Correlation ID: %s", correlationId));
    }

    private void errorLogException(Exception ex) {
        logger.errorContext("Exception occurred while processing the "
                + "API request", ex, DataMapHolder.getLogMap());
    }

    private Map<String, Object> responseAndLogBuilderHandler(Exception ex, WebRequest request) {
        var correlationId = request.getHeader(X_REQUEST_ID_HEADER);

        if (StringUtils.isEmpty(correlationId)) {
            correlationId = generateShortCorrelationId();
        }
        Map<String, Object> responseBody = new LinkedHashMap<>();
        populateResponseBody(responseBody, correlationId);
        errorLogException(ex);

        return responseBody;
    }

    /**
     * Runtime exception handler. Acts as the catch-all scenario.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Object> handleException(Exception ex,
            WebRequest request) {
        return new ResponseEntity<>(responseAndLogBuilderHandler(ex, request),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * BadRequestException exception handler. Thrown when data is given in the wrong format.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {BadRequestException.class, DateTimeParseException.class,
            HttpMessageNotReadableException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Object> handleBadRequestException(Exception ex,
            WebRequest request) {
        return new ResponseEntity<>(responseAndLogBuilderHandler(ex, request),
                HttpStatus.BAD_REQUEST);

    }

    /**
     * MethodNotAllowedException exception handler.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<Object> handleMethodNotAllowedException(Exception ex,
            WebRequest request) {
        return new ResponseEntity<>(responseAndLogBuilderHandler(ex, request),
                HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * ServiceUnavailableException exception handler. To be thrown when there are connection issues.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {ServiceUnavailableException.class, DataAccessException.class,
            DataAccessResourceFailureException.class})
    public ResponseEntity<Object> handleServiceUnavailableException(Exception ex,
            WebRequest request) {
        return new ResponseEntity<>(responseAndLogBuilderHandler(ex, request),
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    private String generateShortCorrelationId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
