package uk.gov.companieshouse.certifiedcopies.orders.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemResponseDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.certifiedcopies.orders.api.mapper.CertifiedCopyItemMapper;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.CertifiedCopyItemService;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.CompanyService;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.FilingHistoryDocumentService;
import uk.gov.companieshouse.certifiedcopies.orders.api.validator.CreateCertifiedCopyItemRequestValidator;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.CERTIFIED_COPY_ID_LOG_KEY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.COMPANY_NUMBER_LOG_KEY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.STATUS_LOG_KEY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.USER_ID_LOG_KEY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.createLoggingDataMap;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.logErrorsWithStatus;

@RestController
public class CertifiedCopiesItemController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingUtils.APPLICATION_NAMESPACE);

    private final CreateCertifiedCopyItemRequestValidator createCertifiedCopyItemRequestValidator;
    private final CertifiedCopyItemMapper mapper;
    private final CertifiedCopyItemService certifiedCopyItemService;
    private final CompanyService companyService;
    private final FilingHistoryDocumentService filingHistoryDocumentService;

    public CertifiedCopiesItemController(final CreateCertifiedCopyItemRequestValidator createCertifiedCopyItemRequestValidator,
                                         final CertifiedCopyItemMapper mapper,
                                         final CertifiedCopyItemService certifiedCopyItemService,
                                         final CompanyService companyService,
                                         final FilingHistoryDocumentService filingHistoryDocumentService) {
        this.createCertifiedCopyItemRequestValidator = createCertifiedCopyItemRequestValidator;
        this.mapper = mapper;
        this.certifiedCopyItemService = certifiedCopyItemService;
        this.companyService = companyService;
        this.filingHistoryDocumentService = filingHistoryDocumentService;
    }

    @PostMapping("${uk.gov.companieshouse.certifiedcopies.orders.api.home}")
    public ResponseEntity<Object> createCertifiedCopy(
            final @Valid @RequestBody CertifiedCopyItemRequestDTO certifiedCopyItemRequestDTO,
            HttpServletRequest request,
            final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {

        Map<String, Object> logMap = LoggingUtils.createLoggingDataMap(requestId);
        LoggingUtils.getLogger().infoRequest(request, "create certified copy item request", logMap);

        final List<String> errors = createCertifiedCopyItemRequestValidator.getValidationErrors(certifiedCopyItemRequestDTO);
        if (!errors.isEmpty()) {
            logErrorsWithStatus(logMap, errors, BAD_REQUEST);
            LOGGER.errorRequest(request, "create certified copy item validation errors", logMap);
            return ResponseEntity.status(BAD_REQUEST).body(new ApiError(BAD_REQUEST, errors));
        }

        CertifiedCopyItem certifiedCopyItem = mapper
                .certifiedCopyItemRequestDTOToCertifiedCopyItem(certifiedCopyItemRequestDTO);

        final String companyName = companyService.getCompanyName(certifiedCopyItemRequestDTO.getCompanyNumber());

        certifiedCopyItem.getData().setCompanyName(companyName);
        certifiedCopyItem.setUserId(AuthorisationUtil.getAuthorisedIdentity(request));
        final List<FilingHistoryDocument> filings =
                filingHistoryDocumentService.getFilingHistoryDocuments(
                        certifiedCopyItem.getData().getCompanyNumber(),
                        certifiedCopyItem.getData().getItemOptions().getFilingHistoryDocuments());
        certifiedCopyItem.getData().getItemOptions().setFilingHistoryDocuments(filings);

        CertifiedCopyItem createdCertifiedCopyItem = certifiedCopyItemService
                .createCertifiedCopyItem(certifiedCopyItem);

        logMap.put(USER_ID_LOG_KEY, createdCertifiedCopyItem.getUserId());
        logMap.put(COMPANY_NUMBER_LOG_KEY, createdCertifiedCopyItem.getData().getCompanyNumber());
        logMap.put(CERTIFIED_COPY_ID_LOG_KEY, createdCertifiedCopyItem.getId());
        logMap.put(STATUS_LOG_KEY, CREATED);
        LoggingUtils.getLogger().infoRequest(request, "certified copy item created", logMap);

        CertifiedCopyItemResponseDTO certifiedCopyItemResponseDTO = mapper
                .certifiedCopyItemDataToCertifiedCopyItemResponseDTO(
                        createdCertifiedCopyItem.getData());

        return ResponseEntity.status(CREATED).body(certifiedCopyItemResponseDTO);
    }

    @GetMapping("${uk.gov.companieshouse.certifiedcopies.orders.api.home}/{id}")
    public ResponseEntity<Object> getCertifiedCopy(final @PathVariable String id,
                                                   final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId)
    {
        final Map<String, Object> logMap = createLoggingDataMap(requestId);
        logMap.put(CERTIFIED_COPY_ID_LOG_KEY, id);
        LOGGER.info("get certified copy item request", logMap);
        final Optional<CertifiedCopyItem> item = certifiedCopyItemService.getCertifiedCopyItemById(id);
        if (item.isPresent()) {
            final CertifiedCopyItemResponseDTO retrievedCertifiedCopyItemDTO =
                    mapper.certifiedCopyItemDataToCertifiedCopyItemResponseDTO(item.get().getData());
            logMap.put(STATUS_LOG_KEY, OK);
            LOGGER.info("certified copy item found", logMap);
            return ResponseEntity.status(OK).body(retrievedCertifiedCopyItemDTO);
        } else {
            final String errorMsg = "certified copy resource not found";
            final List<String> errors = new ArrayList<>();
            errors.add(errorMsg);
            logErrorsWithStatus(logMap, errors, NOT_FOUND);
            LOGGER.error(errorMsg, logMap);
            return ResponseEntity.status(NOT_FOUND).body(new ApiError(NOT_FOUND, errors));
        }
    }

}
