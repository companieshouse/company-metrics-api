package uk.gov.companieshouse.company.metrics.controller;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MetricsRecalculateApi;
import uk.gov.companieshouse.company.metrics.exception.BadRequestException;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.service.CompanyMetricsService;



@RestController
public class CompanyMetricsController {

    private final CompanyMetricsService companyMetricsService;
    private static final String SATISFIED_STATUS = "satisfied";
    private static final String FULLY_SATISFIED_STATUS = "fully-satisfied";
    private static final String PART_SATISFIED_STATUS = "part-satisfied";

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
    public ResponseEntity<MetricsApi> getCompanyMetrics(
            @PathVariable("company_number") String companyNumber) {
        return companyMetricsService.get(companyNumber)
                .map(companyMetricsDocument ->
                        new ResponseEntity<>(
                                companyMetricsDocument.getCompanyMetrics(),
                                HttpStatus.OK))
                .orElse(ResponseEntity.status(HttpStatus.GONE).build());
    }

    /**
     * Post request for company metrics.
     *
     * @param  companyNumber  the company number for metrics recalculation
     * @param  requestBody  the request body containing Instructions to recalculate
     * @return  no response
     */
    @PostMapping ("/company/{company_number}/metrics/recalculate")
    public ResponseEntity<Void> recalculate(
             @PathVariable("company_number") String companyNumber,
             @Valid @RequestBody MetricsRecalculateApi requestBody
    ) throws JsonProcessingException, BadRequestException {

        // Check to see if mortgages flag is true then process further
        if (requestBody != null && BooleanUtils.isTrue(requestBody.getMortgage())) {
            // query the mongodb to get a charges counts from company_mortgages
            int totalCount  = companyMetricsService.queryCompanyMortgages(companyNumber, "none");
            int satisfiedCount =   companyMetricsService.queryCompanySatisfiedMortgages(
                     companyNumber, SATISFIED_STATUS,FULLY_SATISFIED_STATUS);
            int partSatisfiedCount = companyMetricsService.queryCompanyMortgages(
                     companyNumber, PART_SATISFIED_STATUS);
            String updatedBy =  requestBody.getInternalData() != null
                      ? requestBody.getInternalData().getUpdatedBy() : null;

            Optional<CompanyMetricsDocument> companyMetricsDocument =
                    companyMetricsService.get(companyNumber);

            companyMetricsDocument.ifPresentOrElse(
                    companyMetrics -> companyMetricsService.upsertMetrics(totalCount,
                            satisfiedCount, partSatisfiedCount,
                            updatedBy, companyMetrics),
                    () -> companyMetricsService.insertMetrics(companyNumber,totalCount,
                            satisfiedCount,partSatisfiedCount,updatedBy)
            );

            return ResponseEntity.status(HttpStatus.CREATED).build();

        } else {
            // mortgage flag is null or false in payload hence throwing BadRequestException
            throw new BadRequestException(String.format("Invalid Request Received. %s ",
                    requestBody.toString()));

        }
    }

}
