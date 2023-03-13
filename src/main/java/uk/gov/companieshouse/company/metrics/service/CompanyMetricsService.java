package uk.gov.companieshouse.company.metrics.service;

import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.metrics.AppointmentsApi;
import uk.gov.companieshouse.api.metrics.CountsApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MetricsRecalculateApi;
import uk.gov.companieshouse.api.metrics.MortgageApi;
import uk.gov.companieshouse.api.metrics.PscApi;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.Updated;
import uk.gov.companieshouse.company.metrics.repository.metrics.CompanyMetricsRepository;
import uk.gov.companieshouse.logging.Logger;

@Service
public class CompanyMetricsService {

    private static final String COMPANY_METRICS_TYPE = "company_metrics";

    private final Logger logger;

    private final ChargesCountService chargesCountService;
    private final AppointmentsCountService appointmentsCountService;
    private final PscCountService pscCountService;
    private final CompanyMetricsRepository companyMetricsRepository;

    /**
     * Constructor.
     */
    public CompanyMetricsService(Logger logger,
                                 ChargesCountService chargesCountService,
                                 AppointmentsCountService appointmentsCountService,
                                 PscCountService pscCountService,
                                 CompanyMetricsRepository companyMetricsRepository) {
        this.logger = logger;
        this.chargesCountService = chargesCountService;
        this.appointmentsCountService = appointmentsCountService;
        this.pscCountService = pscCountService;
        this.companyMetricsRepository = companyMetricsRepository;
    }

    /**
     * Retrieve company metrics using its company number.
     *
     * @param companyNumber the company number
     * @return company metrics, if the company number exists, otherwise an empty optional
     */
    public Optional<CompanyMetricsDocument> get(String companyNumber) {
        return companyMetricsRepository.findById(companyNumber);
    }

    /**
     * Save or Update company_metrics.
     *
     * @param contextId          Request context ID
     * @param companyNumber      The ID of the company to update metrics for
     * @param recalculateRequest Request data that determines which metrics to update
     */
    public Optional<CompanyMetricsDocument> recalculateMetrics(String contextId,
            String companyNumber,
            MetricsRecalculateApi recalculateRequest) {

        CompanyMetricsDocument companyMetricsDocument = get(companyNumber)
                .orElseGet(() -> getCompanyMetricsDocument(companyNumber));
        MetricsApi metrics = Optional.ofNullable(companyMetricsDocument.getCompanyMetrics())
                .orElse(new MetricsApi());

        if (BooleanUtils.isTrue(recalculateRequest.getPersonsWithSignificantControl())) {

            recalculatePscs(contextId, companyNumber, metrics);

        }

        if (BooleanUtils.isTrue(recalculateRequest.getMortgage())) {

            recalculateCharges(contextId, companyNumber, metrics);

        }

        if (BooleanUtils.isTrue(recalculateRequest.getAppointments())) {

            recalculateAppointments(contextId, companyNumber, metrics);

        }

        if (!BooleanUtils.isTrue(recalculateRequest.getPersonsWithSignificantControl())
                && !BooleanUtils.isTrue(recalculateRequest.getMortgage())
                && !BooleanUtils.isTrue(recalculateRequest.getAppointments())) {
            throw new IllegalArgumentException(String.format(
                    "Unable to process payload with context id %s and company number %s",
                    contextId, companyNumber));
        }

        logger.info("metrics successfully found");

        MetricsApi updatedMetrics = cleanupMetricsContent(metrics);
        if (updatedMetrics.getCounts() != null //NOSONAR
                || updatedMetrics.getMortgage() != null
                || updatedMetrics.getRegisters() != null) {

            updatedMetrics.setEtag(GenerateEtagUtil.generateEtag());
            logger.info("etag set");
            companyMetricsDocument.setCompanyMetrics(updatedMetrics);

            logger.info("metrics set");
            String updatedBy = recalculateRequest.getInternalData() != null
                    ? recalculateRequest.getInternalData().getUpdatedBy() : null;
            logger.info("updated by set");
            companyMetricsDocument.setUpdated(populateUpdated(
                    updatedBy != null ? updatedBy : String.format("contextId:%s", contextId)));

            logger.info(String.format("Company metrics updated for context id %s with id %s",
                    contextId, companyMetricsDocument.getId()));
            companyMetricsRepository.save(companyMetricsDocument);
            return Optional.of(companyMetricsDocument);
        } else {
            companyMetricsRepository.delete(companyMetricsDocument);
            logger.info(String.format("Empty company metrics deleted for context id %s with id %s",
                    contextId, companyMetricsDocument.getId()));
            return Optional.empty();
        }
    }

    private void recalculateAppointments(String contextId, String companyNumber,
            MetricsApi metrics) {
        AppointmentsApi appointments = appointmentsCountService.recalculateMetrics(contextId,
                companyNumber);
        if (appointments.getTotalCount() > 0) {
            CountsApi counts = Optional.ofNullable(metrics.getCounts())
                    .orElse(new CountsApi());
            counts.setAppointments(appointments);
            metrics.setCounts(counts);
        } else if (metrics.getCounts() != null) { // NOSONAR
            metrics.getCounts().setAppointments(null);
        }
    }

    private void recalculateCharges(String contextId, String companyNumber, MetricsApi metrics) {
        MortgageApi mortgages = chargesCountService.recalculateMetrics(contextId, companyNumber);
        metrics.setMortgage(mortgages.getTotalCount() > 0 ? mortgages : null);  //NOSONAR
    }

    private MetricsApi cleanupMetricsContent(MetricsApi metrics) {
        CountsApi counts = metrics.getCounts();

        if (counts != null && counts.getAppointments() == null  // NOSONAR
                && counts.getPersonsWithSignificantControl() == null) { //NOSONAR
            metrics.setCounts(null);
        }

        return metrics;
    }

    private void recalculatePscs(String contextId, String companyNumber, MetricsApi metrics) {
        logger.info("recalculating pcs metrics");
        PscApi pscs = pscCountService.recalculateMetrics(contextId, companyNumber);
        metrics.getCounts().setPersonsWithSignificantControl(pscs);
        CountsApi counts = Optional.ofNullable(metrics.getCounts())
                .orElse(new CountsApi());
        counts.setPersonsWithSignificantControl(pscs);
        metrics.setCounts(counts);
    }

    private CompanyMetricsDocument getCompanyMetricsDocument(String companyNumber) {

        CompanyMetricsDocument companyMetricsDocument = get(companyNumber)
                .orElseGet(() -> {
                    CompanyMetricsDocument document = new CompanyMetricsDocument();
                    document.setId(companyNumber);
                    return document;
                });
        MetricsApi metricsApi = Optional.ofNullable(companyMetricsDocument.getCompanyMetrics())
                .orElse(new MetricsApi());
        companyMetricsDocument.setCompanyMetrics(metricsApi);
        return companyMetricsDocument;
    }

    private Updated populateUpdated(String updatedBy) {

        Updated updated = new Updated();
        updated.setBy(updatedBy);
        updated.setAt(LocalDateTime.now());
        updated.setType(COMPANY_METRICS_TYPE);
        return updated;
    }
}
