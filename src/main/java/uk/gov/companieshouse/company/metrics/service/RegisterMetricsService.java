package uk.gov.companieshouse.company.metrics.service;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.apache.commons.lang3.mutable.MutableBoolean;
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

        MutableBoolean registerDataFound = new MutableBoolean(false);

        mapDirectorsRegister(registers, registerMetrics, registerDataFound);

        mapSecretariesRegister(registers, registerMetrics, registerDataFound);

        mapMembersRegister(registers, registerMetrics, registerDataFound);

        mapPscRegister(registers, registerMetrics, registerDataFound);

        mapUraRegister(registers, registerMetrics, registerDataFound);

        mapLlpMembersRegister(registers, registerMetrics, registerDataFound);

        mapLlpUraRegister(registers, registerMetrics, registerDataFound);

        if (registerDataFound.isTrue()) {
            logger.trace(String.format("Recalculating registers metrics registerMetrics=[%s]",
                    registerMetrics), DataMapHolder.getLogMap());
            return registerMetrics;
        }
        logger.trace("Recalculating registers metrics register data found but no metrics set",
                DataMapHolder.getLogMap());
        return null;
    }

    private void mapDirectorsRegister(Registers registers, RegistersApi registerMetrics,
            MutableBoolean registerDataFound) {
        if (registers.getDirectors() != null
                && !registers.getDirectors().getItems().isEmpty()) {
            RegisteredItems currentRegister = registers.getDirectors()
                    .getItems().get(0);
            RegisterApi register = new RegisterApi();
            register.setRegisterMovedTo(currentRegister.getRegisterMovedTo().getValue());
            register.setMovedOn(convertMovedOn(currentRegister));
            registerMetrics.setDirectors(register);
            registerDataFound.setTrue();
        }
    }

    private void mapSecretariesRegister(Registers registers, RegistersApi registerMetrics,
            MutableBoolean registerDataFound) {
        if (registers.getSecretaries() != null
                && !registers.getSecretaries().getItems().isEmpty()) {
            RegisteredItems currentRegister = registers.getSecretaries()
                    .getItems().get(0);
            RegisterApi register = new RegisterApi();
            register.setRegisterMovedTo(currentRegister.getRegisterMovedTo().getValue());
            register.setMovedOn(convertMovedOn(currentRegister));
            registerMetrics.setSecretaries(register);
            registerDataFound.setTrue();
        }
    }

    private void mapMembersRegister(Registers registers, RegistersApi registerMetrics,
            MutableBoolean registerDataFound) {
        if (registers.getMembers() != null
                && !registers.getMembers().getItems().isEmpty()) {
            RegisteredItems currentRegister = registers.getMembers()
                    .getItems().get(0);
            RegisterApi register = new RegisterApi();
            register.setRegisterMovedTo(currentRegister.getRegisterMovedTo().getValue());
            register.setMovedOn(convertMovedOn(currentRegister));
            registerMetrics.setMembers(register);
            registerDataFound.setTrue();
        }
    }

    private void mapPscRegister(Registers registers, RegistersApi registerMetrics,
            MutableBoolean registerDataFound) {
        if (registers.getPersonsWithSignificantControl() != null
                && !registers.getPersonsWithSignificantControl().getItems().isEmpty()) {
            RegisteredItems currentRegister = registers.getPersonsWithSignificantControl()
                    .getItems().get(0);
            RegisterApi register = new RegisterApi();
            register.setRegisterMovedTo(currentRegister.getRegisterMovedTo().getValue());
            register.setMovedOn(convertMovedOn(currentRegister));
            registerMetrics.setPersonsWithSignificantControl(register);
            registerDataFound.setTrue();
        }
    }

    private void mapUraRegister(Registers registers, RegistersApi registerMetrics,
            MutableBoolean registerDataFound) {
        if (registers.getUsualResidentialAddress() != null
                && !registers.getUsualResidentialAddress().getItems().isEmpty()) {
            RegisteredItems currentRegister = registers.getUsualResidentialAddress()
                    .getItems().get(0);
            RegisterApi register = new RegisterApi();
            register.setRegisterMovedTo(currentRegister.getRegisterMovedTo().getValue());
            register.setMovedOn(convertMovedOn(currentRegister));
            registerMetrics.setUsualResidentialAddress(register);
            registerDataFound.setTrue();
        }
    }

    private void mapLlpMembersRegister(Registers registers, RegistersApi registerMetrics,
            MutableBoolean registerDataFound) {
        if (registers.getLlpMembers() != null
                && !registers.getLlpMembers().getItems().isEmpty()) {
            RegisteredItems currentRegister = registers.getLlpMembers().getItems().get(0);
            RegisterApi register = new RegisterApi();
            register.setRegisterMovedTo(currentRegister.getRegisterMovedTo().getValue());
            register.setMovedOn(convertMovedOn(currentRegister));
            registerMetrics.setLlpMembers(register);
            registerDataFound.setTrue();
        }
    }

    private void mapLlpUraRegister(Registers registers, RegistersApi registerMetrics,
            MutableBoolean registerDataFound) {
        if (registers.getLlpUsualResidentialAddress() != null
                && !registers.getLlpUsualResidentialAddress().getItems().isEmpty()) {
            RegisteredItems currentRegister = registers.getLlpUsualResidentialAddress()
                    .getItems().get(0);
            RegisterApi register = new RegisterApi();
            register.setRegisterMovedTo(currentRegister.getRegisterMovedTo().getValue());
            register.setMovedOn(convertMovedOn(currentRegister));
            registerMetrics.setLlpUsualResidentialAddress(register);
            registerDataFound.setTrue();
        }
    }

    private OffsetDateTime convertMovedOn(RegisteredItems currentRegister) {
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
