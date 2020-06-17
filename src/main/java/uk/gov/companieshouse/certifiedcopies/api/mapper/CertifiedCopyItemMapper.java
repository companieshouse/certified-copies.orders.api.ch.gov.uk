package uk.gov.companieshouse.certifiedcopies.api.mapper;

import org.mapstruct.Mapper;
import uk.gov.companieshouse.certifiedcopies.api.dto.CertifiedCopyItemDTO;
import uk.gov.companieshouse.certifiedcopies.api.model.CertifiedCopyItem;

@Mapper(componentModel = "spring")
public interface CertifiedCopyItemMapper {
    CertifiedCopyItem certifiedCopyItemDTOToCertifiedCopyItem(CertifiedCopyItemDTO certifiedCopyItemDTO);
}
