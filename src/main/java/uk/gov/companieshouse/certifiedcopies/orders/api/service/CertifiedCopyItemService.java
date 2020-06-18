package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.repository.CertifiedCopyItemRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Calendar;

@Service
public class CertifiedCopyItemService {

    private final CertifiedCopyItemRepository repository;
    private final EtagGeneratorService etagGenerator;
    private final LinksGeneratorService linksGenerator;

    public CertifiedCopyItemService(final CertifiedCopyItemRepository repository, final EtagGeneratorService etagGenerator, final LinksGeneratorService linksGenerator) {
        this.repository = repository;
        this.etagGenerator = etagGenerator;
        this.linksGenerator = linksGenerator;
    }

    private String autoGenerateId() {
        SecureRandom random = new SecureRandom();
        byte[] values = new byte[4];
        random.nextBytes(values);
        String rand = String.format("%04d", random.nextInt(9999));
        String time = String.format("%08d", Calendar.getInstance().getTimeInMillis() / 100000L);
        String rawId = rand + time;
        String[] tranId = rawId.split("(?<=\\G.{6})");
        return "CCD-" + String.join("-", tranId);
    }

    public CertifiedCopyItem createCertifiedCopyItem(final CertifiedCopyItem certifiedCopyItem) {
        final LocalDateTime now = LocalDateTime.now();
        certifiedCopyItem.setCreatedAt(now);
        certifiedCopyItem.setUpdatedAt(now);
        certifiedCopyItem.setId(autoGenerateId());
        certifiedCopyItem.setEtag(etagGenerator.generateEtag());
        certifiedCopyItem.setLinks(linksGenerator.generateLinks(certifiedCopyItem.getId()));

        if(certifiedCopyItem.getData().getItemOptions().getDeliveryMethod().equals(DeliveryMethod.POSTAL)) {
            certifiedCopyItem.getData().setPostalDelivery(Boolean.TRUE);
        } else {
            certifiedCopyItem.getData().setPostalDelivery(Boolean.FALSE);
        }
        certifiedCopyItem.getData().setKind("item#certified-copy");

        final CertifiedCopyItem itemSaved = repository.save(certifiedCopyItem);
        return itemSaved;
    }
}
