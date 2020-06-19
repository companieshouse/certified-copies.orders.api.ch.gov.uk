package uk.gov.companieshouse.certifiedcopies.orders.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemResponseDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.mapper.CertifiedCopyItemMapper;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.CertifiedCopyItemService;
import uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.CERTIFIED_COPY_ID_LOG_KEY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.COMPANY_NUMBER_LOG_KEY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.REQUEST_ID_LOG_KEY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.STATUS_LOG_KEY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.USER_ID_LOG_KEY;

@RestController
public class CertifiedCopiesItemController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingUtils.APPLICATION_NAMESPACE);

    private final CertifiedCopyItemMapper mapper;
    private final CertifiedCopyItemService certifiedCopyItemService;

    public CertifiedCopiesItemController(final CertifiedCopyItemMapper mapper, final CertifiedCopyItemService certifiedCopyItemService) {
        this.mapper = mapper;
        this.certifiedCopyItemService = certifiedCopyItemService;
    }

    @PostMapping("${uk.gov.companieshouse.certifiedcopies.orders.api.home}")
    public ResponseEntity<?> createCertifiedCopy(final @Valid @RequestBody CertifiedCopyItemRequestDTO certificateItemRequestDTO,
                                                 HttpServletRequest request,
                                                 final @RequestHeader(LoggingUtils.REQUEST_ID_HEADER_NAME) String requestId) {
        Map<String, Object> logMap = createLoggingDataMap(requestId);
        LOGGER.infoRequest(request, "create certified copy item request", logMap);

        CertifiedCopyItem certifiedCopyItem = mapper.certifiedCopyItemRequestDTOToCertifiedCopyItem(certificateItemRequestDTO);
        certifiedCopyItem.setUserId(AuthorisationUtil.getAuthorisedIdentity(request));

        CertifiedCopyItem createdCertifiedCopyItem = certifiedCopyItemService.createCertifiedCopyItem(certifiedCopyItem);

        logMap.put(USER_ID_LOG_KEY, createdCertifiedCopyItem.getUserId());
        logMap.put(COMPANY_NUMBER_LOG_KEY, createdCertifiedCopyItem.getData().getCompanyNumber());
        logMap.put(CERTIFIED_COPY_ID_LOG_KEY, createdCertifiedCopyItem.getId());
        logMap.put(STATUS_LOG_KEY, CREATED);
        LOGGER.infoRequest(request, "certificate item created", logMap);

        CertifiedCopyItemResponseDTO certifiedCopyItemResponseDTO = mapper.certifiedCopyItemDataToCertifiedCopyItemResponseDTO(createdCertifiedCopyItem.getData());

        return ResponseEntity.status(CREATED).body(certifiedCopyItemResponseDTO);
    }

    /**
     * method to set up a map for logging purposes and add a value for the
     * request id
     *
     * @param requestId
     * @return
     */
    private Map<String, Object> createLoggingDataMap(final String requestId) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(REQUEST_ID_LOG_KEY, requestId);
        return logMap;
    }
}
