package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.FilingHistoryDocument;

import java.util.Collections;
import java.util.List;

/**
 * Integrations tests the {@link FilingHistoryDocumentService}. Uses JUnit4 to take advantage of the
 * system-rules {@link EnvironmentVariables} class rule. The JUnit5 system-extensions equivalent does not
 * seem to have been released.
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@SpringJUnitConfig(FilingHistoryDocumentServiceIntegrationTest.Config.class)
public class FilingHistoryDocumentServiceIntegrationTest {

    @ClassRule
    public static final EnvironmentVariables ENVIRONMENT_VARIABLES = new EnvironmentVariables();

    // TODO GCI-1209 Have more than one filing
    private static final FilingHistoryDocument FILING;
    static {
        FILING = new FilingHistoryDocument();
        FILING.setFilingHistoryId("MDAxMTEyNzExOGFkaXF6a2N4");
    }

    @Configuration
    @ComponentScan(basePackageClasses = FilingHistoryDocumentServiceIntegrationTest.class)
    static class Config {}

    @Autowired
    private FilingHistoryDocumentService serviceUnderTest;

    @MockBean
    private CertifiedCopyItemService certifiedCopyItemService;

    @Test
    public void getFilingHistoryDocuments() {

        // Given
        ENVIRONMENT_VARIABLES.set("CHS_API_KEY", "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz");
        ENVIRONMENT_VARIABLES.set("API_URL", "http://api.chs-dev.internal:4001"); // TODO GCI-1209 Move to WireMock

        final List<FilingHistoryDocument> docs =
                serviceUnderTest.getFilingHistoryDocuments("00006400", Collections.singletonList(FILING));
        // TODO GCI-1209 Remove this
        docs.forEach(doc -> System.out.println(">" + doc.getFilingHistoryId() + ":" + doc.getFilingHistoryDescription()));
    }

}
