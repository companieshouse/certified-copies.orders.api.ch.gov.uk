package uk.gov.companieshouse.certifiedcopies.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.certifiedcopies.api.dto.CertifiedCopyItemDTO;
import uk.gov.companieshouse.certifiedcopies.api.mapper.CertifiedCopyItemMapper;
import uk.gov.companieshouse.certifiedcopies.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.api.service.CertifiedCopyItemService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.companieshouse.certifiedcopies.api.logging.LoggingUtils.APPLICATION_NAMESPACE;
import static uk.gov.companieshouse.certifiedcopies.api.logging.LoggingUtils.REQUEST_ID_HEADER_NAME;

@RestController
public class CertifiedCopiesItemController {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    private final CertifiedCopyItemMapper mapper;
    private final CertifiedCopyItemService certifiedCopyItemService;

    public CertifiedCopiesItemController(final CertifiedCopyItemMapper mapper, final CertifiedCopyItemService certifiedCopyItemService) {
        this.mapper = mapper;
        this.certifiedCopyItemService = certifiedCopyItemService;
    }

    @PostMapping("${uk.gov.companieshouse.certified.copies.api.home}")
    public ResponseEntity<?> createCertifiedCopy(final @Valid @RequestBody CertifiedCopyItemDTO certificateItemDTO,
                                                 HttpServletRequest request,
                                                 final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {
        CertifiedCopyItem certifiedCopyItem = mapper.certifiedCopyItemDTOToCertifiedCopyItem(certificateItemDTO);

        CertifiedCopyItem createdCertifiedCopyItem = certifiedCopyItemService.createCertifiedCopyItem(certifiedCopyItem);
        System.out.println(createdCertifiedCopyItem);
        return ResponseEntity.status(CREATED).body("Hello");
    }
}
