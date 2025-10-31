package uk.gov.companieshouse.company.metrics.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class ExceptionHandlerConfigTest {

    @Mock
    private WebRequest request;

    private ExceptionHandlerConfig exceptionHandlerConfig;

    @BeforeEach
    void setUp() {
        this.exceptionHandlerConfig = new ExceptionHandlerConfig(mock(Logger.class));
    }

    @Test
    void testHandleException() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        WebRequest webRequest = new ServletWebRequest(servletRequest);
        ResponseEntity<Object> entity = exceptionHandlerConfig.
                handleException(new Exception(), webRequest);

        assertNotNull(entity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());
    }

    @Test
    @DisplayName("Handle Generic Exception")
    void handleGenericExceptionTest() {
        Exception exp = new Exception("some error");
        ResponseEntity<Object> response = exceptionHandlerConfig.handleException(exp, request);
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    @DisplayName("Handle HttpMessageNotReadableException thrown when payload not deserialised")
    void handleHttpMessageNotReadableExceptionTest() {
        HttpInputMessage inputMessage = new HttpInputMessage() {
            @Override
            public HttpHeaders getHeaders() {
                return null;
            }

            @Override
            public InputStream getBody() throws IOException {
                return null;
            }
        };
        HttpMessageNotReadableException exp = new HttpMessageNotReadableException("some error", inputMessage);
        ResponseEntity<Object> response = exceptionHandlerConfig.handleBadRequestException(exp, request);
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    @DisplayName("Handle mongo data API exception")
    void handleExceptionKafkaNon200ResponseTest() {
        InvalidDataAccessApiUsageException exp = new InvalidDataAccessApiUsageException("Mongo test exception");
        ResponseEntity<Object> response = exceptionHandlerConfig.handleServiceUnavailableException(exp, request);
        assertEquals(503, response.getStatusCodeValue());
    }

    @Test
    @DisplayName("Handle DataAccessException")
    void handleCausedByDataAccessExceptionTest() {
        DataAccessResourceFailureException exp = new DataAccessResourceFailureException("Test exception");
        ResponseEntity<Object> response = exceptionHandlerConfig.handleServiceUnavailableException(exp, request);
        assertEquals(503, response.getStatusCodeValue());
    }
}