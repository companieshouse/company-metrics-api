package uk.gov.companieshouse.company.metrics.service;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.metrics.RegisterApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.api.registers.RegisteredItems;
import uk.gov.companieshouse.api.registers.Registers;
import uk.gov.companieshouse.company.metrics.logging.DataMapHolder;
import uk.gov.companieshouse.company.metrics.model.RegistersDocument;
import uk.gov.companieshouse.company.metrics.repository.registers.RegistersRepository;
import uk.gov.companieshouse.logging.Logger;

@Service
public class RegisterMetricsService {

    private final Logger logger;
    private final RegistersRepository registersRepository;

    /**
     * Constructor.
     */
    public RegisterMetricsService(Logger logger, RegistersRepository registersRepository) {
        this.logger = logger;
        this.registersRepository = registersRepository;
    }

    /**
     * Save or Update company_metrics for registers.
     *
     * @param companyNumber The ID of the company to update metrics for
     * @return Recalculated registers metrics
     */
    public RegistersApi recalculateMetrics(String companyNumber) {
        Registers registers = getRegisters(companyNumber);

        logger.trace(String.format("Company register metrics being calculated from registers=[%s]",
                registers), DataMapHolder.getLogMap());

        RegistersApi registerMetrics = new RegistersApi();

        boolean registerDataFound = false;
        if (registers.getDirectors() != null
                && !registers.getDirectors().getItems().isEmpty()) {
            RegisteredItems currentRegister = registers.getDirectors()
                    .getItems().get(0);
            RegisterApi register = new RegisterApi();
            register.setRegisterMovedTo(currentRegister.getRegisterMovedTo().getValue());
            register.setMovedOn(convertMovedOn(currentRegister));
            registerMetrics.setDirectors(register);
            registerDataFound = true;
        }

        if (registers.getSecretaries() != null
                && !registers.getSecretaries().getItems().isEmpty()) {
            RegisteredItems currentRegister = registers.getSecretaries()
                    .getItems().get(0);
            RegisterApi register = new RegisterApi();
            register.setRegisterMovedTo(currentRegister.getRegisterMovedTo().getValue());
            register.setMovedOn(convertMovedOn(currentRegister));
            registerMetrics.setSecretaries(register);
            registerDataFound = true;
        }

        if (registers.getMembers() != null
                && !registers.getMembers().getItems().isEmpty()) {
            RegisteredItems currentRegister = registers.getMembers()
                    .getItems().get(0);
            RegisterApi register = new RegisterApi();
            register.setRegisterMovedTo(currentRegister.getRegisterMovedTo().getValue());
            register.setMovedOn(convertMovedOn(currentRegister));
            registerMetrics.setMembers(register);
            registerDataFound = true;
        }

        if (registers.getPersonsWithSignificantControl() != null
                && !registers.getPersonsWithSignificantControl().getItems().isEmpty()) {
            RegisteredItems currentRegister = registers.getPersonsWithSignificantControl()
                    .getItems().get(0);
            RegisterApi register = new RegisterApi();
            register.setRegisterMovedTo(currentRegister.getRegisterMovedTo().getValue());
            register.setMovedOn(convertMovedOn(currentRegister));
            registerMetrics.setPersonsWithSignificantControl(register);
            registerDataFound = true;
        }

        if (registers.getUsualResidentialAddress() != null
                && !registers.getUsualResidentialAddress().getItems().isEmpty()) {
            RegisteredItems currentRegister = registers.getUsualResidentialAddress()
                    .getItems().get(0);
            RegisterApi register = new RegisterApi();
            register.setRegisterMovedTo(currentRegister.getRegisterMovedTo().getValue());
            register.setMovedOn(convertMovedOn(currentRegister));
            registerMetrics.setUsualResidentialAddress(register);
            registerDataFound = true;
        }

        if (registers.getLlpMembers() != null
                && !registers.getLlpMembers().getItems().isEmpty()) {
            RegisteredItems currentRegister = registers.getLlpMembers().getItems().get(0);
            RegisterApi register = new RegisterApi();
            register.setRegisterMovedTo(currentRegister.getRegisterMovedTo().getValue());
            register.setMovedOn(convertMovedOn(currentRegister));
            registerMetrics.setLlpMembers(register);
            registerDataFound = true;
        }

        if (registers.getLlpUsualResidentialAddress() != null
                && !registers.getLlpUsualResidentialAddress().getItems().isEmpty()) {
            RegisteredItems currentRegister = registers.getLlpUsualResidentialAddress()
                    .getItems().get(0);
            RegisterApi register = new RegisterApi();
            register.setRegisterMovedTo(currentRegister.getRegisterMovedTo().getValue());
            register.setMovedOn(convertMovedOn(currentRegister));
            registerMetrics.setLlpUsualResidentialAddress(register);
            registerDataFound = true;
        }

        if (registerDataFound) {
            logger.trace(String.format("Recalculating registers metrics registerMetrics=[%s]",
                    registerMetrics), DataMapHolder.getLogMap());
            return registerMetrics;
        }
        logger.trace("Recalculating registers metrics register data found but no metrics set",
                DataMapHolder.getLogMap());
        return null;
    }

    private static OffsetDateTime convertMovedOn(RegisteredItems currentRegister) {
        return OffsetDateTime.of(currentRegister.getMovedOn(), LocalTime.MIDNIGHT, ZoneOffset.UTC);
    }

    private Registers getRegisters(String companyNumber) {
        logger.info("Recalculating registers metrics",
                DataMapHolder.getLogMap());
        Optional<RegistersDocument> registersDocument = registersRepository.findById(companyNumber);
        if (registersDocument.isPresent()) {
            logger.info("Recalculating registers metrics, registers found",
                    DataMapHolder.getLogMap());
            return registersDocument.get().getData().getRegisters();
        }
        logger.info("Recalculating registers metrics, registers not found",
                DataMapHolder.getLogMap());
        return new Registers();
    }
}
