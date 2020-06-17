package uk.gov.companieshouse.certifiedcopies.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.certifiedcopies.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.api.repository.CertifiedCopyItemRepository;

@Service
public class CertifiedCopyItemService {

    private final CertifiedCopyItemRepository repository;

    public CertifiedCopyItemService(final CertifiedCopyItemRepository repository) {
        this.repository = repository;
    }

    public CertifiedCopyItem createCertifiedCopyItem(final CertifiedCopyItem certifiedCopyItem) {
        final CertifiedCopyItem itemSaved = repository.save(certifiedCopyItem);
        return itemSaved;
    }
}