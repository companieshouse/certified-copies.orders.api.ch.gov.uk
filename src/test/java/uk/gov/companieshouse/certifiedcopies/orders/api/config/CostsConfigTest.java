package uk.gov.companieshouse.certifiedcopies.orders.api.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.CERTIFIED_COPY_COST;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.CERTIFIED_COPY_NEW_INCORPORATION_COST;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.SAME_DAY_CERTIFIED_COPY_COST;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.SAME_DAY_CERTIFIED_COPY_NEW_INCORPORATION_COST;

/**
 * Unit tests the {@link CostsConfig} class.
 */
@SpringBootTest
class CostsConfigTest {

    @Autowired
    private CostsConfig configUnderTest;

    @Test
    @DisplayName("The configured costs have their expected values")
    void costsAreConfiguredCorrectly() {
        assertThat(configUnderTest.getStandardCost(), is(CERTIFIED_COPY_COST));
        assertThat(configUnderTest.getSameDayCost(), is(SAME_DAY_CERTIFIED_COPY_COST));
        assertThat(configUnderTest.getStandardNewIncorporationCost(), is(CERTIFIED_COPY_NEW_INCORPORATION_COST));
        assertThat(configUnderTest.getSameDayNewIncorporationCost(), is(SAME_DAY_CERTIFIED_COPY_NEW_INCORPORATION_COST));
    }

}
