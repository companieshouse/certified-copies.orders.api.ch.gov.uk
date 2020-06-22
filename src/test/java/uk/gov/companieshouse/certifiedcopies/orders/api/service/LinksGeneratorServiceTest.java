package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.Links;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class LinksGeneratorServiceTest {

    private static final String SELF_PATH = "/orderable/certified-copies";
    private static final String ITEM_ID = "CCD-123456-123456";

    @Test
    @DisplayName("Generates links correctly with valid inputs")
    void generatesLinksCorrectlyWithValidInputs() {

        final LinksGeneratorService generatorUnderTest = new LinksGeneratorService(SELF_PATH);

        final Links links = generatorUnderTest.generateLinks(ITEM_ID);

        assertThat(links.getSelf(), is(SELF_PATH + "/" + ITEM_ID));

    }

    @Test
    @DisplayName("Unpopulated certified copy id argument results in an IllegalArgumentException")
    void itemIdMustNotBeBlank() {

        final LinksGeneratorService generatorUnderTest = new LinksGeneratorService(SELF_PATH);

        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class,
                        () -> generatorUnderTest.generateLinks(null));

        assertThat(exception.getMessage(), is("Certified Copy ID not populated!"));

    }

    @Test
    @DisplayName("Unpopulated path to self URI results in an IllegalArgumentException")
    void selfPathMustNotBeBlank() {

        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class,
                        () -> new LinksGeneratorService(null));

        assertThat(exception.getMessage(), is("Path to self URI not configured!"));
    }

}
