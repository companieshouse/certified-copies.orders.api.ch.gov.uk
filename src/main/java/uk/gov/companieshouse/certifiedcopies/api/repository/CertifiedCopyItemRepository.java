package uk.gov.companieshouse.certifiedcopies.api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.companieshouse.certifiedcopies.api.model.CertifiedCopyItem;

@RepositoryRestResource
public interface CertifiedCopyItemRepository extends MongoRepository<CertifiedCopyItem, String> { }
