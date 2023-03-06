package uk.gov.companieshouse.company.metrics.controller;

import static org.springframework.http.HttpHeaders.LOCATION;

import javax.validation.Valid;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MetricsRecalculateApi;
import uk.gov.companieshouse.company.metrics.exceptions.BadRequestException;
import uk.gov.companieshouse.company.metrics.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.company.metrics.service.CompanyMetricsService;
import uk.gov.companieshouse.logging.Logger;

@RestController
public class CompanyMetricsController {

    private final CompanyMetricsService companyMetricsService;
    private final Logger logger;

    public CompanyMetricsController(Logger logger,
            CompanyMetricsService companyMetricsService) {
        this.logger = logger;
        this.companyMetricsService = companyMetricsService;
    }

    /**
     * Retrieve company metrics using a company number.
     *
     * @param companyNumber the company number of the company
     * @return company metrics api
     */
    @GetMapping("/company/{company_number}/metrics")
    public ResponseEntity<MetricsApi> getCompanyMetrics(
            @PathVariable("company_number") String companyNumber) {
        return companyMetricsService.get(companyNumber)
                .map(companyMetricsDocument ->
                        new ResponseEntity<>(
                                companyMetricsDocument.getCompanyMetrics(),
                                HttpStatus.OK))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Post request for company metrics.
     *
     * @param companyNumber the company number for metrics recalculation
     * @param requestBody   the request body containing Instructions to recalculate
     * @return no response
     */
    @PostMapping("/company/{company_number}/metrics/recalculate")
    public ResponseEntity<Void> recalculate(
            @RequestHeader("x-request-id") String contextId,
            @PathVariable("company_number") String companyNumber,
            @Valid @RequestBody MetricsRecalculateApi requestBody) throws BadRequestException {
        logger.info(String.format(
                "Payload Successfully received on POST with context id %s and company number %s",
                contextId,
                companyNumber));

        try {
            BodyBuilder responseBuilder = ResponseEntity.ok();
            companyMetricsService.recalculateMetrics(contextId, companyNumber, requestBody)
                    .ifPresent(companyMetricsDocument -> responseBuilder
                            .header(LOCATION, String.format("/company/%s/metrics", companyNumber)));
            return responseBuilder.build();
        } catch (DataAccessResourceFailureException ex) {
            throw new ServiceUnavailableException("Database unavailable");
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(String.format("Invalid Request Received. %s ",
                    requestBody == null ? null : requestBody.toString()));
        }
    }
}
