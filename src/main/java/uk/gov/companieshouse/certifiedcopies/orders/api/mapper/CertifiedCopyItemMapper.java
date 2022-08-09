package uk.gov.companieshouse.certifiedcopies.orders.api.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemResponseDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemData;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;

@Mapper(componentModel = "spring")
public interface CertifiedCopyItemMapper {

    CertifiedCopyItem certifiedCopyItemRequestDTOToCertifiedCopyItem
            (CertifiedCopyItemRequestDTO certifiedCopyItemDTO);

    CertifiedCopyItemResponseDTO certifiedCopyItemDataToCertifiedCopyItemResponseDTO
            (CertifiedCopyItemData certifiedCopyItemData);

    @AfterMapping
    default void setDefaults(
            CertifiedCopyItemRequestDTO certifiedCopyItemDTO,
            @MappingTarget CertifiedCopyItem certifiedCopyItem){

        DeliveryMethod deliveryMethod = certifiedCopyItemDTO.getItemOptions().getDeliveryMethod();
        certifiedCopyItem.getData().getItemOptions().setDeliveryMethod(
                deliveryMethod != null ? deliveryMethod : DeliveryMethod.POSTAL);

    }
}
