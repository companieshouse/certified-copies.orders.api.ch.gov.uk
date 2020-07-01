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
import uk.gov.companieshouse.certifiedcopies.orders.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.IdGeneratorService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private static final String ID_1 = "MDAxMTEyNzExOGFkaXF6a2N4";
	private static final String ID_2 = "MzAwOTM2MDg5OWFkaXF6a2N4";
	private static final String ID_3 = "MDE2OTkyOTEwMmFkaXF6a2N4";
	private static final String ID_4 = "MDAyNzI3NTQ4OWFkaXF6a2N4";
	private static final String UNKNOWN_ID = "000000000000000000000000";

	private static final FilingHistoryDocument FILING_1 = new FilingHistoryDocument(
			"1993-04-01",
			"memorandum-articles",
			null,
			ID_1,
			"MEM/ARTS"
	);
	private static final FilingHistoryDocument FILING_2 = new FilingHistoryDocument(
			"2010-02-12",
			"change-person-director-company-with-change-date",
			Stream.of(new Object[][] {
					{ "change_date", "2010-02-12" },
					{ "officer_name", "Thomas David Wheare" }
			}).collect(Collectors.toMap(data -> (String) data[0], data -> data[1])),
			ID_2,
			"CH01"
	);
	private static final FilingHistoryDocument FILING_3 = new FilingHistoryDocument(
			"2006-10-19",
			"legacy",
			Collections.singletonMap("description", "New director appointed"),
			ID_3,
			"288a"
	);
	private static final FilingHistoryDocument FILING_4 = new FilingHistoryDocument(
			"2005-03-21",
			"accounts-with-accounts-type-group",
			Collections.singletonMap("made_up_date", "2004-08-31"),
			ID_4,
			"AA"
	);

	private static class Error {
		private String type;
		private String error;

		private Error(String type, String error) {
			this.type = type;
			this.error = error;
		}

		public String getType() {
			return type;
		}

		public String getError() {
			return error;
		}
	}

	private static class ApiErrorResponsePayload {

		private List<Error> errors;

		private ApiErrorResponsePayload(List<Error> errors) {
			this.errors = errors;
		}

		public List<Error> getErrors() {
			return errors;
		}
	}

	private static final ApiErrorResponsePayload FILING_NOT_FOUND =
			new ApiErrorResponsePayload(singletonList(new Error("ch:service", "filing-history-item-not-found")));

	private static final String CERTIFIED_COPIES_URL = "/orderable/certified-copies";
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
	private IdGeneratorService idGeneratorService;

	@SuppressWarnings("squid:S2699")    // at least one assertion
	@Test
	void contextLoads() {
	}

	@Test
	@SetEnvironmentVariable(key = "CHS_API_KEY", value = "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz")
	@SetEnvironmentVariable(key = "API_URL", value = "http://localhost:" + WIRE_MOCK_PORT)
	@DisplayName("createCertifiedCopy propagates 400 Bad Request for an unknown company get filing request")
	void createCertifiedCopyPropagatesBadRequestForUnknownCompanyFilingRequest() throws JsonProcessingException {

		// Given
		final CertifiedCopyItemRequestDTO certifiedCopyItemDTORequest
				= new CertifiedCopyItemRequestDTO();
		certifiedCopyItemDTORequest.setCompanyNumber(UNKNOWN_COMPANY_NUMBER);
		certifiedCopyItemDTORequest.setCustomerReference(CUSTOMER_REFERENCE);
		certifiedCopyItemDTORequest.setQuantity(QUANTITY);

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
		certifiedCopyItemDTORequest.setItemOptions(certifiedCopyItemOptionsDTORequest);

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
			.body(fromObject(certifiedCopyItemDTORequest))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo("400")
			.jsonPath("$.error").isEqualTo("Bad Request")
			.jsonPath("$.message").isEqualTo("Error getting filing history document 1 for company number 00000000.")
			.jsonPath("$.path").isEqualTo("/orderable/certified-copies");

	}

	@Test
	@SetEnvironmentVariable(key = "CHS_API_KEY", value = "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz")
	@SetEnvironmentVariable(key = "API_URL", value = "http://localhost:" + WIRE_MOCK_PORT)
	@DisplayName("createCertifiedCopy propagates 500 Internal Server Error for connection failure during get filing request")
	void createCertifiedCopyPropagatesInternalServerErrorForFilingRequestConnectionFailure() {

		// Given
		final CertifiedCopyItemRequestDTO certifiedCopyItemDTORequest
				= new CertifiedCopyItemRequestDTO();
		certifiedCopyItemDTORequest.setCompanyNumber(COMPANY_NUMBER);
		certifiedCopyItemDTORequest.setCustomerReference(CUSTOMER_REFERENCE);
		certifiedCopyItemDTORequest.setQuantity(QUANTITY);

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
		certifiedCopyItemDTORequest.setItemOptions(certifiedCopyItemOptionsDTORequest);

		givenThat(get(urlEqualTo("/company/" + COMPANY_NUMBER + "/filing-history/" + FILING_HISTORY_ID))
				.willReturn(aResponse()
						.withFault(Fault.CONNECTION_RESET_BY_PEER)));

		// When and then
		webTestClient.post().uri(CERTIFIED_COPIES_URL)
			.contentType(MediaType.APPLICATION_JSON)
			.header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
			.header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
			.header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
			.body(fromObject(certifiedCopyItemDTORequest))
			.exchange()
			.expectStatus().is5xxServerError()
			.expectBody()
			.jsonPath("$.status").isEqualTo("500")
			.jsonPath("$.error").isEqualTo("Internal Server Error")
			.jsonPath("$.message")
			.isEqualTo("Error sending request to http://localhost:12345/company/00006400/filing-history/1: " +
					"Connection reset")
			.jsonPath("$.path").isEqualTo("/orderable/certified-copies");
	}

	@Test
	@SetEnvironmentVariable(key = "CHS_API_KEY", value = "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz")
	@SetEnvironmentVariable(key = "API_URL", value = "http://localhost:" + WIRE_MOCK_PORT)
	@DisplayName("createCertifiedCopy propagates 500 Internal Server Error for service unavailable during get filing request")
	void createCertifiedCopyPropagatesInternalServerErrorForFilingRequestServiceUnavailable() {

		// Given
		final CertifiedCopyItemRequestDTO certifiedCopyItemDTORequest
				= new CertifiedCopyItemRequestDTO();
		certifiedCopyItemDTORequest.setCompanyNumber(COMPANY_NUMBER);
		certifiedCopyItemDTORequest.setCustomerReference(CUSTOMER_REFERENCE);
		certifiedCopyItemDTORequest.setQuantity(QUANTITY);

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
		certifiedCopyItemDTORequest.setItemOptions(certifiedCopyItemOptionsDTORequest);

		givenThat(get(urlEqualTo("/company/" + COMPANY_NUMBER + "/filing-history/" + FILING_HISTORY_ID))
				.willReturn(serviceUnavailable()));

		// When and then
		webTestClient.post().uri(CERTIFIED_COPIES_URL)
			.contentType(MediaType.APPLICATION_JSON)
			.header(REQUEST_ID_HEADER_NAME, REQUEST_ID_VALUE)
			.header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_OAUTH2_VALUE)
			.header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
			.body(fromObject(certifiedCopyItemDTORequest))
			.exchange()
			.expectStatus().is5xxServerError()
			.expectBody()
			.jsonPath("$.status").isEqualTo("500")
			.jsonPath("$.error").isEqualTo("Internal Server Error")
			.jsonPath("$.message")
			.isEqualTo("Error sending request to http://localhost:12345/company/00006400/filing-history/1: " +
					"Service Unavailable")
			.jsonPath("$.path").isEqualTo("/orderable/certified-copies");
	}

}
