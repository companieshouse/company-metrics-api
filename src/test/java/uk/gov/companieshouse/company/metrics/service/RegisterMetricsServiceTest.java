package uk.gov.companieshouse.company.metrics.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.metrics.RegisterApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.api.registers.CompanyRegister;
import uk.gov.companieshouse.api.registers.RegisterListDirectors;
import uk.gov.companieshouse.api.registers.RegisteredItems;
import uk.gov.companieshouse.api.registers.Registers;
import uk.gov.companieshouse.company.metrics.model.RegistersDocument;
import uk.gov.companieshouse.company.metrics.repository.registers.RegistersRepository;
import uk.gov.companieshouse.logging.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
class RegisterMetricsServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    public static final String CONTEXT_ID = "12345";

    private final RegistersApi registerMetrics = new RegistersApi().directors(
            new RegisterApi().registerMovedTo("public-register").movedOn(
                    OffsetDateTime.parse("2024-01-01T00:00Z")));

    @Mock
    private Logger logger;

    @Mock
    private RegistersRepository registersRepository;

    @InjectMocks
    private RegisterMetricsService registerMetricsService;

    @Test
    void shouldRecalculateMetrics() {

        Optional<RegistersDocument> doc = Optional.ofNullable(getRegistersDocument());
        when(registersRepository.findById(COMPANY_NUMBER)).thenReturn(doc);

        RegistersApi registers = registerMetricsService.recalculateMetrics(COMPANY_NUMBER);

        assertThat(registers.getDirectors()).isNotNull();
        assertThat(registers.getDirectors().getRegisterMovedTo()).isEqualTo("public-register");
        assertThat(registers.getDirectors().getMovedOn()).isEqualTo(OffsetDateTime.parse("2024-01-01T00:00:00.000Z"));
    }

    private RegistersDocument getRegistersDocument() {
        List<RegisteredItems> items = new ArrayList<RegisteredItems>();
            items.add(new RegisteredItems().registerMovedTo(RegisteredItems.RegisterMovedToEnum.PUBLIC_REGISTER)
                    .movedOn(LocalDate.parse("2024-01-01")));
         RegisterListDirectors registerListDirectors = new RegisterListDirectors()
                .registerType(RegisterListDirectors.RegisterTypeEnum.DIRECTORS).items(items);
        return new RegistersDocument().setData(new CompanyRegister().registers(new Registers().directors(registerListDirectors)));
    }
}