package uk.gov.companieshouse.company.metrics.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
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
            logStartRequestProcessing(req, log);

            DataMapHolder.initialise(Optional
                    .ofNullable(req.getHeader(LogContextProperties.REQUEST_ID.value()))
                    .orElse(UUID.randomUUID().toString()));

            chain.doFilter(request, response);

        } finally {
            logEndRequestProcessing(req, resp, log);
            DataMapHolder.clear();
        }
    }
}
