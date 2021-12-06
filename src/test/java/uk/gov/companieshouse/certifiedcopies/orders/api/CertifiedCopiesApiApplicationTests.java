package uk.gov.companieshouse.certifiedcopies.orders.api;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.TOKEN_PERMISSION_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.ClassRule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.env.Environment;
import org.springframework.test.web.reactive.server.WebTestClient;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemOptionsRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.FilingHistoryDocumentRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.CompanyService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class CertifiedCopiesApiApplicationTests {

    @ClassRule
    public static final EnvironmentVariables ENVIRONMENT_VARIABLES = new EnvironmentVariables();

    private static final String CUSTOMER_REFERENCE = "Certified Copy ordered by NJ.";
    private static final int QUANTITY = 5;
    private static final String CONTACT_NUMBER = "0123456789";
    private static final String FORENAME = "Bob";
    private static final String SURNAME = "Jones";
    private static final String FILING_HISTORY_ID = "1";
    
    private final String ITEMS_DATABASE = "ITEMS_DATABASE";
    private final String MONGODB_URL = "MONGODB_URL";
    private final String CHS_API_KEY = "CHS_API_KEY";
    private final String API_URL = "API_URL";
    private final String PAYMENTS_API_URL = "PAYMENTS_API_URL";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CompanyService companyService;

    @Autowired
    private Environment environment;

    @SuppressWarnings("squid:S2699")    // at least one assertion
    @Test
    void contextLoads() {
    }

    @Test
    @DisplayName("Checking environment variables when all variables are set should return true")
    void checkEnvironmentVariablesAllPresentReturnsTrue() {
        ENVIRONMENT_VARIABLES.set(ITEMS_DATABASE, ITEMS_DATABASE);
        ENVIRONMENT_VARIABLES.set(MONGODB_URL, MONGODB_URL);
        ENVIRONMENT_VARIABLES.set(CHS_API_KEY, CHS_API_KEY);
        ENVIRONMENT_VARIABLES.set(PAYMENTS_API_URL, PAYMENTS_API_URL);
        ENVIRONMENT_VARIABLES.set(API_URL, API_URL);
        
        boolean present = CertifiedCopiesApiApplication.checkEnvironmentVariables();
        assertTrue(present);
        ENVIRONMENT_VARIABLES.clear(ITEMS_DATABASE, MONGODB_URL, API_URL, CHS_API_KEY, PAYMENTS_API_URL);
    }
    
    @Test
    @DisplayName("Checking environment variables when ITEMS_DATABASE is not set should return false")
    void checkEnvironmentVariablesItemsDatabaseMissingReturnsFalse() {
        ENVIRONMENT_VARIABLES.clear(ITEMS_DATABASE, MONGODB_URL, API_URL, CHS_API_KEY, PAYMENTS_API_URL);
        ENVIRONMENT_VARIABLES.set(MONGODB_URL, MONGODB_URL);
        ENVIRONMENT_VARIABLES.set(CHS_API_KEY, CHS_API_KEY);
        ENVIRONMENT_VARIABLES.set(PAYMENTS_API_URL, PAYMENTS_API_URL);
        ENVIRONMENT_VARIABLES.set(API_URL, API_URL);
        boolean present = CertifiedCopiesApiApplication.checkEnvironmentVariables();
        assertFalse(present);
        ENVIRONMENT_VARIABLES.clear(MONGODB_URL, API_URL, CHS_API_KEY, PAYMENTS_API_URL);
    }
    
    @Test
    @DisplayName("Checking environment variables when MONGODB_URL is not set should return false")
    void checkEnvironmentVariablesMongoDbMissingReturnsFalse() {
        ENVIRONMENT_VARIABLES.clear(ITEMS_DATABASE, MONGODB_URL, API_URL, CHS_API_KEY, PAYMENTS_API_URL);
        ENVIRONMENT_VARIABLES.set(ITEMS_DATABASE, ITEMS_DATABASE);
        ENVIRONMENT_VARIABLES.set(CHS_API_KEY, CHS_API_KEY);
        ENVIRONMENT_VARIABLES.set(PAYMENTS_API_URL, PAYMENTS_API_URL);
        ENVIRONMENT_VARIABLES.set(API_URL, API_URL);
        boolean present = CertifiedCopiesApiApplication.checkEnvironmentVariables();
        assertFalse(present);
        ENVIRONMENT_VARIABLES.clear(ITEMS_DATABASE, API_URL, CHS_API_KEY, PAYMENTS_API_URL);
    }
    
    @Test
    @DisplayName("Checking environment variables when API_URL is not set should return false")
    void checkEnvironmentVariablesApiUrlMissingReturnsFalse() {
        ENVIRONMENT_VARIABLES.clear(ITEMS_DATABASE, MONGODB_URL, API_URL, CHS_API_KEY, PAYMENTS_API_URL);
        ENVIRONMENT_VARIABLES.set(ITEMS_DATABASE, ITEMS_DATABASE);
        ENVIRONMENT_VARIABLES.set(MONGODB_URL, MONGODB_URL);
        ENVIRONMENT_VARIABLES.set(CHS_API_KEY, CHS_API_KEY);
        ENVIRONMENT_VARIABLES.set(PAYMENTS_API_URL, PAYMENTS_API_URL);
        
        boolean present = CertifiedCopiesApiApplication.checkEnvironmentVariables();
        assertFalse(present);
        ENVIRONMENT_VARIABLES.clear(ITEMS_DATABASE, MONGODB_URL, CHS_API_KEY, PAYMENTS_API_URL);
    }
    
    @Test
    @DisplayName("Checking environment variables when CHS_API_KEY is not set should return false")
    void checkEnvironmentVariablesChsApiKeyMissingReturnsFalse() {
        ENVIRONMENT_VARIABLES.clear(ITEMS_DATABASE, MONGODB_URL, API_URL, CHS_API_KEY, PAYMENTS_API_URL);
        ENVIRONMENT_VARIABLES.set(ITEMS_DATABASE, ITEMS_DATABASE);
        ENVIRONMENT_VARIABLES.set(MONGODB_URL, MONGODB_URL);
        ENVIRONMENT_VARIABLES.set(PAYMENTS_API_URL, PAYMENTS_API_URL);
        ENVIRONMENT_VARIABLES.set(API_URL, API_URL);
        
        boolean present = CertifiedCopiesApiApplication.checkEnvironmentVariables();
        assertFalse(present);
        ENVIRONMENT_VARIABLES.clear(ITEMS_DATABASE, MONGODB_URL, API_URL, PAYMENTS_API_URL);
    }
    
    @Test
    @DisplayName("Checking environment variables when PAYMENTS_API_URL is not set should return false")
    void checkEnvironmentVariablesPaymentsApiUrlMissingReturnsFalse() {
        ENVIRONMENT_VARIABLES.clear(ITEMS_DATABASE, MONGODB_URL, API_URL, CHS_API_KEY, PAYMENTS_API_URL);
        ENVIRONMENT_VARIABLES.set(ITEMS_DATABASE, ITEMS_DATABASE);
        ENVIRONMENT_VARIABLES.set(MONGODB_URL, MONGODB_URL);
        ENVIRONMENT_VARIABLES.set(CHS_API_KEY, CHS_API_KEY);
        ENVIRONMENT_VARIABLES.set(API_URL, API_URL);
        
        boolean present = CertifiedCopiesApiApplication.checkEnvironmentVariables();
        assertFalse(present);
        ENVIRONMENT_VARIABLES.clear(ITEMS_DATABASE, MONGODB_URL, API_URL, CHS_API_KEY);
    }

    /**
     * Factory method to create a valid create certified copy item request DTO for testing purposes.
     * @param companyNumber the company number to be used in the certified copy item creation request
     * @return the {@link CertifiedCopyItemRequestDTO} created
     */
    private CertifiedCopyItemRequestDTO buildCreateCertifiedCopyItemRequest(final String companyNumber) {
        final CertifiedCopyItemRequestDTO request = new CertifiedCopyItemRequestDTO();
        request.setCompanyNumber(companyNumber);
        request.setCustomerReference(CUSTOMER_REFERENCE);
        request.setQuantity(QUANTITY);

        final FilingHistoryDocumentRequestDTO filingHistoryDocumentRequestDTO
                = new FilingHistoryDocumentRequestDTO();
        filingHistoryDocumentRequestDTO.setFilingHistoryId(FILING_HISTORY_ID);

        final CertifiedCopyItemOptionsRequestDTO certifiedCopyItemOptionsDTORequest
                = new CertifiedCopyItemOptionsRequestDTO();
        certifiedCopyItemOptionsDTORequest.setContactNumber(CONTACT_NUMBER);
        certifiedCopyItemOptionsDTORequest.setDeliveryMethod(DeliveryMethod.POSTAL);
        certifiedCopyItemOptionsDTORequest.setDeliveryTimescale(DeliveryTimescale.STANDARD);
        certifiedCopyItemOptionsDTORequest.setForename(FORENAME);
        certifiedCopyItemOptionsDTORequest.setSurname(SURNAME);

        certifiedCopyItemOptionsDTORequest
                .setFilingHistoryDocuments(singletonList(filingHistoryDocumentRequestDTO));
        request.setItemOptions(certifiedCopyItemOptionsDTORequest);
        return request;
    }
}
