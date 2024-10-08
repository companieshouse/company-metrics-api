package uk.gov.companieshouse.company.metrics.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.metrics.AppointmentsApi;
import uk.gov.companieshouse.api.metrics.CountsApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.MetricsRecalculateApi;
import uk.gov.companieshouse.api.metrics.MortgageApi;
import uk.gov.companieshouse.api.metrics.PscApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.company.metrics.logging.DataMapHolder;
import uk.gov.companieshouse.company.metrics.model.CompanyMetricsDocument;
import uk.gov.companieshouse.company.metrics.model.UnversionedCompanyMetricsDocument;
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
    private final RegisterMetricsService registerMetricsService;
    private final CompanyMetricsRepository companyMetricsRepository;
    private final MongoTemplate mongoTemplate;

    /**
     * Constructor.
     */
    public CompanyMetricsService(Logger logger,
            ChargesCountService chargesCountService,
            AppointmentsCountService appointmentsCountService,
            PscCountService pscCountService,
            RegisterMetricsService registerMetricsService,
            CompanyMetricsRepository companyMetricsRepository,
            MongoTemplate mongoTemplate) {
        this.logger = logger;
        this.chargesCountService = chargesCountService;
        this.appointmentsCountService = appointmentsCountService;
        this.pscCountService = pscCountService;
        this.registerMetricsService = registerMetricsService;
        this.companyMetricsRepository = companyMetricsRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Retrieve company metrics using its company number.
     *
     * @param companyNumber the company number
     * @return company metrics, if the company number exists, otherwise an empty optional
     */
    public Optional<CompanyMetricsDocument> getMetrics(String companyNumber) {
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
        CompanyMetricsDocument companyMetricsDocument = getCompanyMetricsDocument(companyNumber);
        MetricsApi metrics = Optional.ofNullable(companyMetricsDocument.getCompanyMetrics())
                .orElse(new MetricsApi());

        if (BooleanUtils.isTrue(recalculateRequest.getPersonsWithSignificantControl())) {

            recalculatePscs(companyNumber, metrics);

        }

        if (BooleanUtils.isTrue(recalculateRequest.getMortgage())) {

            recalculateCharges(companyNumber, metrics);

        }

        if (BooleanUtils.isTrue(recalculateRequest.getAppointments())) {

            recalculateAppointments(companyNumber, metrics);

        }

        if (BooleanUtils.isTrue(recalculateRequest.getRegisters())) {

            recalculateRegisters(companyNumber, metrics);

        }

        if (!BooleanUtils.isTrue(recalculateRequest.getPersonsWithSignificantControl())
                && !BooleanUtils.isTrue(recalculateRequest.getMortgage())
                && !BooleanUtils.isTrue(recalculateRequest.getAppointments())
                && !BooleanUtils.isTrue(recalculateRequest.getRegisters())) {
            throw new IllegalArgumentException(String.format(
                    "Unable to process payload with context id %s and company number %s",
                    contextId, companyNumber));
        }

        MetricsApi updatedMetrics = cleanupMetricsContent(metrics);
        if (updatedMetrics.getCounts() != null //NOSONAR
                || updatedMetrics.getMortgage() != null
                || updatedMetrics.getRegisters() != null) {

            updatedMetrics.setEtag(GenerateEtagUtil.generateEtag());
            companyMetricsDocument.setCompanyMetrics(updatedMetrics);

            String updatedBy = recalculateRequest.getInternalData() != null
                    ? recalculateRequest.getInternalData().getUpdatedBy() : null;
            companyMetricsDocument.setUpdated(populateUpdated(
                    updatedBy != null ? updatedBy : String.format("contextId:%s", contextId)));

            logger.info("Company metrics updated", DataMapHolder.getLogMap());
            if (companyMetricsDocument.getId() == null) { // A new document
                companyMetricsDocument.setId(companyNumber);
                companyMetricsRepository.insert(companyMetricsDocument);
            } else if (companyMetricsDocument.getVersion() == null) {
                mongoTemplate.save(new UnversionedCompanyMetricsDocument(companyMetricsDocument));
            } else {
                companyMetricsRepository.save(companyMetricsDocument);
            }
            return Optional.of(companyMetricsDocument);
        } else {
            companyMetricsRepository.delete(companyMetricsDocument);
            logger.info("Empty company metrics deleted", DataMapHolder.getLogMap());
            return Optional.empty();
        }
    }

    private void recalculateAppointments(String companyNumber,
            MetricsApi metrics) {
        AppointmentsApi appointments = appointmentsCountService.recalculateMetrics(
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

    private void recalculateCharges(String companyNumber, MetricsApi metrics) {
        MortgageApi mortgages = chargesCountService.recalculateMetrics(companyNumber);
        metrics.setMortgage(mortgages.getTotalCount() > 0 ? mortgages : null);  //NOSONAR
    }

    private void recalculateRegisters(String companyNumber, MetricsApi metrics) {
        RegistersApi registerMetrics = registerMetricsService.recalculateMetrics(companyNumber);

        if (registerMetrics != null) {
            logger.info("Company register metrics set", DataMapHolder.getLogMap());
            metrics.setRegisters(registerMetrics);  //NOSONAR
            return;
        }
        logger.info("Company register metrics unset", DataMapHolder.getLogMap());
        metrics.setRegisters(null);  //NOSONAR
    }

    private MetricsApi cleanupMetricsContent(MetricsApi metrics) {
        CountsApi counts = metrics.getCounts();

        if (counts != null && counts.getAppointments() == null  // NOSONAR
                && counts.getPersonsWithSignificantControl() == null) { //NOSONAR
            metrics.setCounts(null);
        }

        return metrics;
    }

    private void recalculatePscs(String companyNumber, MetricsApi metrics) {
        PscApi pscs = pscCountService.recalculateMetrics(companyNumber);
        if (pscs.getTotalCount() > 0) { //NOSONAR
            CountsApi counts = Optional.ofNullable(metrics.getCounts())
                    .orElse(new CountsApi());
            counts.setPersonsWithSignificantControl(pscs);
            metrics.setCounts(counts);
        } else if (metrics.getCounts() != null) { //NOSONAR
            metrics.getCounts().setPersonsWithSignificantControl(null);
        }
    }

    private CompanyMetricsDocument getCompanyMetricsDocument(String companyNumber) {

        CompanyMetricsDocument companyMetricsDocument = getMetrics(companyNumber)
                .orElse(new CompanyMetricsDocument());
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
