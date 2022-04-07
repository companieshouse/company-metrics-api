package uk.gov.companieshouse.company.metrics.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import uk.gov.companieshouse.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ControllerExceptionHandlerTest {

    private ControllerExceptionHandler controllerExceptionHandler;

    @Mock
    Logger logger;

    @BeforeEach
    void setUp() {
        this.controllerExceptionHandler = new ControllerExceptionHandler(logger);
    }

    @Test
    void testHandleException() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        WebRequest webRequest = new ServletWebRequest(servletRequest);
        ResponseEntity<Object> entity = controllerExceptionHandler.
                handleException(new Exception(), webRequest);

        assertNotNull(entity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());
    }
}