package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;

@Service
public class EtagGeneratorService {

    public String generateEtag() {
        return GenerateEtagUtil.generateEtag();
    }

}
