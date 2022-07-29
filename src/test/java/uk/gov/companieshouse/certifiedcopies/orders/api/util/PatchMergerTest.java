package uk.gov.companieshouse.certifiedcopies.orders.api.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod.COLLECTION;
import static uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod.POSTAL;
import static uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale.SAME_DAY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale.STANDARD;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.certifiedcopies.orders.api.config.ApplicationConfig;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemData;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.FilingHistoryDocument;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(PatchMergerTest.Config.class)
public class PatchMergerTest {

    @Configuration
    static class Config {
        @Bean
        public ObjectMapper objectMapper() {
            return new ApplicationConfig().objectMapper();
        }

        @Bean
        PatchMerger patchMerger() {
            return new PatchMerger(objectMapper());
        }

        @Bean
        TestMergePatchFactory patchFactory() {
            return new TestMergePatchFactory(objectMapper());
        }
    }

    private static final String ORIGINAL_COMPANY_NUMBER = "1234";
    private static final DeliveryTimescale DELIVERY_TIMESCALE = STANDARD;
    private static final DeliveryTimescale UPDATED_DELIVERY_TIMESCALE = SAME_DAY;
    private static final DeliveryMethod DELIVERY_METHOD = POSTAL;
    private static final DeliveryMethod UPDATED_DELIVERY_METHOD = COLLECTION;

    @Autowired
    private PatchMerger patchMergerUnderTest;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TestMergePatchFactory patchFactory;

    @Test
    @DisplayName("Unpopulated source string property does not overwrite populated target field")
    void unpopulatedSourceStringLeavesTargetIntact() throws IOException {
        // Given
        final CertifiedCopyItem original = new CertifiedCopyItem();
        original.setCompanyNumber(ORIGINAL_COMPANY_NUMBER);
        final CertifiedCopyItem empty = new CertifiedCopyItem();

        // When
        final CertifiedCopyItem patched =
                patchMergerUnderTest.mergePatch(patchFactory.patchFromPojo(empty), original, CertifiedCopyItem.class);

        // Then
        assertThat(patched.getData().getCompanyNumber(), is(ORIGINAL_COMPANY_NUMBER));
    }

    @Test
    @DisplayName("Nested level properties are propagated correctly")
    void sourceNestedLevelPropertiesPropagated() throws IOException {
        // Given
        final CertifiedCopyItem original = new CertifiedCopyItem();
        final CertifiedCopyItemOptions originalOptions = new CertifiedCopyItemOptions();
        final List<FilingHistoryDocument> originalFilingHistory = new ArrayList<>();
        final FilingHistoryDocument originalFH = new FilingHistoryDocument();
        original.setItemOptions(originalOptions);
        originalFH.setFilingHistoryId("1");
        originalFilingHistory.add(originalFH);
        originalOptions.setDeliveryMethod(DELIVERY_METHOD);
        originalOptions.setDeliveryTimescale(DELIVERY_TIMESCALE);
        originalOptions.setFilingHistoryDocuments(originalFilingHistory);

        final CertifiedCopyItem delta = new CertifiedCopyItem();
        final CertifiedCopyItemOptions deltaOptions = new CertifiedCopyItemOptions();
        deltaOptions.setDeliveryTimescale(UPDATED_DELIVERY_TIMESCALE);
        delta.setItemOptions(deltaOptions);

        // When
        final CertifiedCopyItem patched =
                patchMergerUnderTest.mergePatch(patchFactory.patchFromPojo(delta), original, CertifiedCopyItem.class);

        // Then
        assertThat(patched.getData().getItemOptions().getDeliveryMethod(), is(DELIVERY_METHOD));
        assertThat(patched.getData().getItemOptions().getDeliveryTimescale(), is(UPDATED_DELIVERY_TIMESCALE));
        assertThat(patched.getData().getItemOptions().getFilingHistoryDocuments().get(0).getFilingHistoryId(),is("1"));
    }

    @Test
    @DisplayName("Nested level properties are propagated correctly")
    void sourceNestedLevelPropertiesPropagatedJsonPatch() throws IOException {
        // Given
        final CertifiedCopyItem original = new CertifiedCopyItem();
        final CertifiedCopyItemOptions originalOptions = new CertifiedCopyItemOptions();
        originalOptions.setDeliveryMethod(DELIVERY_METHOD);
        originalOptions.setDeliveryTimescale(DELIVERY_TIMESCALE);
        original.setItemOptions(originalOptions);

//        final String patchString = "{\"data\":{\"item_options\":{\"delivery_timescale\":\"same-day\"}}}";
        final String patchString = "{\"item_options\":{\"delivery_timescale\":\"same-day\"}}";

        // When
        final CertifiedCopyItem patched =
                patchMergerUnderTest.mergePatch(patchFactory.patchFromJson(patchString), original, CertifiedCopyItem.class);

        // Then
        assertThat(patched.getData().getItemOptions().getDeliveryMethod(), is(DELIVERY_METHOD));
        assertThat(patched.getData().getItemOptions().getDeliveryTimescale(), is(UPDATED_DELIVERY_TIMESCALE));
    }

    @Test
    @DisplayName("root level properties are propagated correctly")
    void sourceRootLevelPropertiesPropagatedJsonPatch() throws IOException {
        // Given
        final CertifiedCopyItem original = new CertifiedCopyItem();
        final CertifiedCopyItemData data = new CertifiedCopyItemData();
        data.setId("1");
        data.setCompanyNumber("00006400");
        original.setData(data);

        final String patchString = "{\"company_number\":\"00006500\"}";
//        final String patchString = "{\"data\":{\"company_number\":\"00006500\"}}";

        // When
        final CertifiedCopyItem patched =
                patchMergerUnderTest.mergePatch(patchFactory.patchFromJson(patchString), original, CertifiedCopyItem.class);

        // Then
        assertThat(patched.getData().getId(), is("1"));
        assertThat(patched.getData().getCompanyName(), is("00006500"));
    }
}
