package uk.gov.companieshouse.certifiedcopies.orders.api.mapper;

import org.mapstruct.Mapper;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;

@Mapper(componentModel = "spring")
public interface CertifiedCopyItemMapper {
    CertifiedCopyItem certifiedCopyItemRequestDTOToCertifiedCopyItem(CertifiedCopyItemRequestDTO certifiedCopyItemDTO);
}
