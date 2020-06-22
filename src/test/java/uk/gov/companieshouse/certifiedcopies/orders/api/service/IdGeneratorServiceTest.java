package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertTrue;

class IdGeneratorServiceTest {

    @Test
    @DisplayName("autoGenerateId returns in the format CCD-######-######")
    void autoGenerateIdGenerateIdInCorrectFormat() {

        final IdGeneratorService idGeneratorService = new IdGeneratorService();

        final String id = idGeneratorService.autoGenerateId();

        assertTrue(id.matches("^CCD-\\d{6}-\\d{6}$")); ;

    }

}
