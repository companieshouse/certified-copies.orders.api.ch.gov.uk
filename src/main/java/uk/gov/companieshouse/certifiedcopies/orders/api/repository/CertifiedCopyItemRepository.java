package uk.gov.companieshouse.certifiedcopies.orders.api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;

@Repository
public interface CertifiedCopyItemRepository extends MongoRepository<CertifiedCopyItem, String> { }
