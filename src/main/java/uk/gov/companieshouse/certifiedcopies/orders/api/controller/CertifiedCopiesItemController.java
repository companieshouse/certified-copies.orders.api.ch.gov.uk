package uk.gov.companieshouse.certifiedcopies.orders.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.mapper.CertifiedCopyItemMapper;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.CertifiedCopyItemService;
import uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
public class CertifiedCopiesItemController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingUtils.APPLICATION_NAMESPACE);

    private final CertifiedCopyItemMapper mapper;
    private final CertifiedCopyItemService certifiedCopyItemService;

    public CertifiedCopiesItemController(final CertifiedCopyItemMapper mapper, final CertifiedCopyItemService certifiedCopyItemService) {
        this.mapper = mapper;
        this.certifiedCopyItemService = certifiedCopyItemService;
    }

    @PostMapping("${uk.gov.companieshouse.certified.copies.api.home}")
    public ResponseEntity<?> createCertifiedCopy(final @Valid @RequestBody CertifiedCopyItemDTO certificateItemDTO,
                                                 HttpServletRequest request,
                                                 final @RequestHeader(LoggingUtils.REQUEST_ID_HEADER_NAME) String requestId) {
        CertifiedCopyItem certifiedCopyItem = mapper.certifiedCopyItemDTOToCertifiedCopyItem(certificateItemDTO);

        CertifiedCopyItem createdCertifiedCopyItem = certifiedCopyItemService.createCertifiedCopyItem(certifiedCopyItem);
        System.out.println(createdCertifiedCopyItem);
        return ResponseEntity.status(CREATED).body("Hello");
    }
}
