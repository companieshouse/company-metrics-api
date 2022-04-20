package uk.gov.companieshouse.company.metrics.exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", "Error processing the request");
        responseBody.put("correlationId", correlationId);
        request.setAttribute("javax.servlet.error.exception", ex, 0);
        logger.error("correlationId = " + correlationId, ex);

        if (ex instanceof ResponseStatusException){
            ResponseStatusException rse = (ResponseStatusException)ex;
            Throwable cause = rse.getCause();
            if (cause instanceof IOException){
                return new ResponseEntity(responseBody, HttpStatus.SERVICE_UNAVAILABLE);
            }
            if ("invokeChsKafkaApi".equals(rse.getReason())){
                return new ResponseEntity(responseBody, HttpStatus.NOT_EXTENDED);
            }
        }
        if (ex instanceof HttpMessageNotReadableException){
            return new ResponseEntity(responseBody, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String generateShortCorrelationId() {
        return UUID.randomUUID().toString().replace(TARGET, REPLACEMENT).substring(0,8);
    }
}