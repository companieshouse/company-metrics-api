package uk.gov.companieshouse.company.metrics.logging;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.filter.GenericFilterBean;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.LogContextProperties;
import uk.gov.companieshouse.logging.util.RequestLogger;

public class StructuredLoggingFilter extends GenericFilterBean
        implements RequestLogger {

    private final Logger log;

    public StructuredLoggingFilter(Logger logger) {
        this.log = logger;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        try {
            logStartRequestProcessing((jakarta.servlet.http.HttpServletRequest) req, log);

            DataMapHolder.initialise(Optional
                    .ofNullable(req.getHeader(LogContextProperties.REQUEST_ID.value()))
                    .orElse(UUID.randomUUID().toString()));

            chain.doFilter(request, response);

        } finally {
            logEndRequestProcessing((jakarta.servlet.http.HttpServletRequest) req,
                    (jakarta.servlet.http.HttpServletResponse) resp, log);
            DataMapHolder.clear();
        }
    }

    @Override
    public void doFilter(jakarta.servlet.ServletRequest servletRequest,
                         jakarta.servlet.ServletResponse servletResponse,
                         jakarta.servlet.FilterChain filterChain)
            throws IOException, jakarta.servlet.ServletException {

    }
}
