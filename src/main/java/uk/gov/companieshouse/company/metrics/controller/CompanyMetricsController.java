package uk.gov.companieshouse.company.metrics.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.service.CompanyMetricsService;

@RestController
public class CompanyMetricsController {

    private final CompanyMetricsService companyMetricsService;

    public CompanyMetricsController(CompanyMetricsService companyMetricsService) {
        this.companyMetricsService = companyMetricsService;
    }

    /**
     * Retrieve company metrics using a company number.
     *
     * @param companyNumber the company number of the company
     * @return company metrics api
     */
    @GetMapping("/company/{company_number}/metrics")
    public ResponseEntity<CompanyMetricsDocument> getCompanyMetrics(
            @PathVariable("company_number") String companyNumber) {
        return companyMetricsService.get(companyNumber)
                .map(companyMetricsDocument ->
                        new ResponseEntity<>(
                                companyMetricsDocument,
                                HttpStatus.OK))
                .orElse(ResponseEntity.notFound().build());
    }

}
