package uk.gov.companieshouse.company.metrics.auth;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.companieshouse.logging.Logger;

@SpringBootTest
public class EricTokenAuthenticationFilterTest {

    @Mock
    Logger logger;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    FilterChain filterChain;

    @Test
    @DisplayName("EricTokenAuthenticationFilter doFilterInternal Test successfully calling filterchain method")
    void doFilterInternal() throws ServletException, IOException {
        EricTokenAuthenticationFilter ericTokenAuthenticationFilter = new EricTokenAuthenticationFilter(logger);

        when(request.getHeader("ERIC-Identity")).thenReturn("SOME-IDENTITY");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("OAUTH2");

        ericTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("EricTokenAuthenticationFilter doFilterInternal Test not filter chain method when not passing proper Eric headers")
    void doFilterInternalNoCallToFilterChain() throws ServletException, IOException {
        EricTokenAuthenticationFilter ericTokenAuthenticationFilter = new EricTokenAuthenticationFilter(logger);

        when(request.getHeader("ERIC-Identity")).thenReturn("SOME-IDENTITY");

        ericTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(0)).doFilter(request, response);
    }
}