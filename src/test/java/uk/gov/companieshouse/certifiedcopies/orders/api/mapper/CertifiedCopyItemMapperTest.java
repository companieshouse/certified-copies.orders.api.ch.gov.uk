package uk.gov.companieshouse.certifiedcopies.orders.api.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemOptionsRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.CertifiedCopyItemRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.dto.FilingHistoryDocumentRequestDTO;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemData;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CollectionLocation;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(CertifiedCopyItemMapperTest.Config.class)
public class CertifiedCopyItemMapperTest {
    private static final String COMPANY_NUMBER = "00000000";
    private static final String CUSTOMER_REFERENCE = "Certificate ordered by NJ.";
    private static final int QUANTITY = 5;
    private static final String CONTACT_NUMBER = "0123456789";
    private static final String FORENAME = "Bob";
    private static final String SURNAME = "Jones";
    private static final String FILING_HISTORY_ID = "1";
    private static final String KIND = "item#certified-copy";


    @Configuration
    @ComponentScan(basePackageClasses = CertifiedCopyItemMapperTest.class)
    static class Config {}

    @Autowired
    private CertifiedCopyItemMapper mapperUnderTest;

    @Test
    public void testCertifiedCopyItemRequestDTOToCertifiedCopyItem() {
        final FilingHistoryDocumentRequestDTO filingHistoryDocumentRequestDTO = new FilingHistoryDocumentRequestDTO();
        filingHistoryDocumentRequestDTO.setFilingHistoryId(FILING_HISTORY_ID);

        final CertifiedCopyItemOptionsRequestDTO certifiedCopyItemOptionsRequestDTO = new CertifiedCopyItemOptionsRequestDTO();
        certifiedCopyItemOptionsRequestDTO.setFilingHistoryDocuments(Arrays.asList(filingHistoryDocumentRequestDTO));
        certifiedCopyItemOptionsRequestDTO.setContactNumber(CONTACT_NUMBER);
        certifiedCopyItemOptionsRequestDTO.setDeliveryMethod(DeliveryMethod.POSTAL);
        certifiedCopyItemOptionsRequestDTO.setDeliveryTimescale(DeliveryTimescale.STANDARD);
        certifiedCopyItemOptionsRequestDTO.setCollectionLocation(CollectionLocation.CARDIFF);
        certifiedCopyItemOptionsRequestDTO.setForename(FORENAME);
        certifiedCopyItemOptionsRequestDTO.setSurname(SURNAME);

        final CertifiedCopyItemRequestDTO dto = new CertifiedCopyItemRequestDTO();
        dto.setCompanyNumber(COMPANY_NUMBER);
        dto.setCustomerReference(CUSTOMER_REFERENCE);
        dto.setQuantity(QUANTITY);
        dto.setItemOptions(certifiedCopyItemOptionsRequestDTO);

        final CertifiedCopyItem certifiedCopyItem = mapperUnderTest.certifiedCopyItemRequestDTOToCertifiedCopyItem(dto);

        CertifiedCopyItemData item = certifiedCopyItem.getData();

        assertThat(item, is(notNullValue()));
        assertThat(item.getCompanyNumber(), is(dto.getCompanyNumber()));
        assertThat(item.getCustomerReference(), is(dto.getCustomerReference()));
        assertThat(item.getQuantity(), is(dto.getQuantity()));
        assertThat(item.getItemOptions().getContactNumber(), is(dto.getItemOptions().getContactNumber()));
        assertThat(item.getItemOptions().getDeliveryMethod(), is(dto.getItemOptions().getDeliveryMethod()));
        assertThat(item.getItemOptions().getDeliveryTimescale(), is(dto.getItemOptions().getDeliveryTimescale()));
        assertThat(item.getItemOptions().getCollectionLocation(), is(dto.getItemOptions().getCollectionLocation()));
        assertThat(item.getItemOptions().getForename(), is(dto.getItemOptions().getForename()));
        assertThat(item.getItemOptions().getSurname(), is(dto.getItemOptions().getSurname()));
        assertThat(item.getItemOptions().getFilingHistoryDocuments().get(0).getFilingHistoryId(),
                is(dto.getItemOptions().getFilingHistoryDocuments().get(0).getFilingHistoryId()));
    }
}
