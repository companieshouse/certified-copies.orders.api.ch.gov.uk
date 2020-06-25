package uk.gov.companieshouse.certifiedcopies.orders.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemResponseDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.mapper.CertifiedCopyItemMapper;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.CertifiedCopyItemService;
import uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.CompanyService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import java.util.*;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.*;

@RestController
public class CertifiedCopiesItemController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingUtils.APPLICATION_NAMESPACE);

    private final CertifiedCopyItemMapper mapper;
    private final CertifiedCopyItemService certifiedCopyItemService;
    private final CompanyService companyService;

    public CertifiedCopiesItemController(final CertifiedCopyItemMapper mapper,
                                         final CertifiedCopyItemService certifiedCopyItemService,
                                         final CompanyService companyService) {
        this.mapper = mapper;
        this.certifiedCopyItemService = certifiedCopyItemService;
        this.companyService = companyService;
    }

    @PostMapping("${uk.gov.companieshouse.certifiedcopies.orders.api.home}")
    public ResponseEntity<CertifiedCopyItemResponseDTO> createCertifiedCopy(
            final @Valid @RequestBody CertifiedCopyItemRequestDTO certifiedCopyItemRequestDTO,
            HttpServletRequest request,
            final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {

        Map<String, Object> logMap = LoggingUtils.createLoggingDataMap(requestId);
        LoggingUtils.getLogger().infoRequest(request, "create certified copy item request", logMap);

        CertifiedCopyItem certifiedCopyItem = mapper
                .certifiedCopyItemRequestDTOToCertifiedCopyItem(certifiedCopyItemRequestDTO);

        final String companyName = companyService.getCompanyName(certifiedCopyItemRequestDTO.getCompanyNumber());

        certifiedCopyItem.getData().setCompanyName(companyName);
        certifiedCopyItem.setUserId(AuthorisationUtil.getAuthorisedIdentity(request));

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
