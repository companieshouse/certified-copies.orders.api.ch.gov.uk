package uk.gov.companieshouse.certifiedcopies.orders.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.Fault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemOptionsRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.FilingHistoryDocumentRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.CompanyService;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Collections.singletonList;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY_TYPE;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.*;

@AutoConfigureWireMock(port = CertifiedCopiesApiApplicationTests.WIRE_MOCK_PORT)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CertifiedCopiesApiApplicationTests {

	// Junit 5 Pioneer @SetEnvironmentVariable cannot evaluate properties/environment variables
	// such as {wire.mock.port}, hence we seem to be forced to hard wire the port value. Not ideal.
	static final int WIRE_MOCK_PORT = 12345;

	private static final String COMPANY_NUMBER = "00006400";
	private static final String UNKNOWN_COMPANY_NUMBER = "00000000";


	private static final String CUSTOMER_REFERENCE = "Certified Copy ordered by NJ.";
	private static final int QUANTITY = 5;
	private static final String CONTACT_NUMBER = "0123456789";
	private static final String FORENAME = "Bob";
	private static final String SURNAME = "Jones";
	private static final String FILING_HISTORY_ID = "1";

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private CompanyService companyService;

	@SuppressWarnings("squid:S2699")    // at least one assertion
	@Test
	void contextLoads() {
	}

	@Test
	@SetEnvironmentVariable(key = "CHS_API_KEY", value = "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz")
	@SetEnvironmentVariable(key = "API_URL", value = "http://localhost:" + WIRE_MOCK_PORT)
	@SetEnvironmentVariable(key = "PAYMENTS_API_URL", value = "http://localhost:" + WIRE_MOCK_PORT)
	@DisplayName("createCertifiedCopy propagates 400 Bad Request for an unknown company get filing request")
	void createCertifiedCopyPropagatesBadRequestForUnknownCompanyFilingRequest() throws JsonProcessingException {

		// Given
		givenThat(get(urlEqualTo("/company/" + UNKNOWN_COMPANY_NUMBER + "/filing-history/" + FILING_HISTORY_ID))
			.willReturn(badRequest()
					.withHeader("Content-Type", "application/json")
					.withBody(objectMapper.writeValueAsString(FILING_NOT_FOUND))));

		// When and then
		webTestClient.post().uri(CERTIFIED_COPIES_URL)
			.contentType(MediaType.APPLICATION_JSON)
			.header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
			.header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
			.header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
			.body(fromObject(buildCreateCertifiedCopyItemRequest(UNKNOWN_COMPANY_NUMBER)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo("400")
			.jsonPath("$.error").isEqualTo("Bad Request")
			.jsonPath("$.message").isEqualTo("Error getting filing history document 1 for company number 00000000.")
			.jsonPath("$.path").isEqualTo(CERTIFIED_COPIES_URL);

	}

	@Test
	@SetEnvironmentVariable(key = "CHS_API_KEY", value = "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz")
	@SetEnvironmentVariable(key = "API_URL", value = "http://localhost:" + WIRE_MOCK_PORT)
	@SetEnvironmentVariable(key = "PAYMENTS_API_URL", value = "http://localhost:" + WIRE_MOCK_PORT)
	@DisplayName("createCertifiedCopy propagates 500 Internal Server Error for connection failure during get filing request")
	void createCertifiedCopyPropagatesInternalServerErrorForFilingRequestConnectionFailure() {

		// Given
		givenThat(get(urlEqualTo("/company/" + COMPANY_NUMBER + "/filing-history/" + FILING_HISTORY_ID))
			.willReturn(aResponse()
					.withFault(Fault.CONNECTION_RESET_BY_PEER)));

		// When and then
		webTestClient.post().uri(CERTIFIED_COPIES_URL)
			.contentType(MediaType.APPLICATION_JSON)
			.header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
			.header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
			.header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
			.body(fromObject(buildCreateCertifiedCopyItemRequest(COMPANY_NUMBER)))
			.exchange()
			.expectStatus().is5xxServerError()
			.expectBody()
			.jsonPath("$.status").isEqualTo("500")
			.jsonPath("$.error").isEqualTo("Internal Server Error")
			.jsonPath("$.message")
			.isEqualTo("Error sending request to http://localhost:12345/company/00006400/filing-history/1: " +
					"Connection reset")
			.jsonPath("$.path").isEqualTo(CERTIFIED_COPIES_URL);
	}

	@Test
	@SetEnvironmentVariable(key = "CHS_API_KEY", value = "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz")
	@SetEnvironmentVariable(key = "API_URL", value = "http://localhost:" + WIRE_MOCK_PORT)
	@SetEnvironmentVariable(key = "PAYMENTS_API_URL", value = "http://localhost:" + WIRE_MOCK_PORT)
	@DisplayName("createCertifiedCopy propagates 500 Internal Server Error for service unavailable during get filing request")
	void createCertifiedCopyPropagatesInternalServerErrorForFilingRequestServiceUnavailable() {

		// Given
		givenThat(get(urlEqualTo("/company/" + COMPANY_NUMBER + "/filing-history/" + FILING_HISTORY_ID))
			.willReturn(serviceUnavailable()));

		// When and then
		webTestClient.post().uri(CERTIFIED_COPIES_URL)
			.contentType(MediaType.APPLICATION_JSON)
			.header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
			.header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
			.header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
			.body(fromObject(buildCreateCertifiedCopyItemRequest(COMPANY_NUMBER)))
			.exchange()
			.expectStatus().is5xxServerError()
			.expectBody()
			.jsonPath("$.status").isEqualTo("500")
			.jsonPath("$.error").isEqualTo("Internal Server Error")
			.jsonPath("$.message")
			.isEqualTo("Error sending request to http://localhost:12345/company/00006400/filing-history/1: " +
					"Service Unavailable")
			.jsonPath("$.path").isEqualTo(CERTIFIED_COPIES_URL);
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
