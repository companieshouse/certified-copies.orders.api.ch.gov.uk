package uk.gov.companieshouse.certifiedcopies.orders.api.model;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.companieshouse.certifiedcopies.orders.api.config.CostsConfig;
import uk.gov.companieshouse.certifiedcopies.orders.api.converter.EnumValueNameConverter;

import static uk.gov.companieshouse.certifiedcopies.orders.api.model.ProductType.CERTIFIED_COPY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.model.ProductType.CERTIFIED_COPY_INCORPORATION;
import static uk.gov.companieshouse.certifiedcopies.orders.api.model.ProductType.CERTIFIED_COPY_INCORPORATION_SAME_DAY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.model.ProductType.CERTIFIED_COPY_SAME_DAY;

public enum DeliveryTimescale {
    STANDARD,
    SAME_DAY {

        @Override
        public int getCertifiedCopyCost(final CostsConfig costs) {
            return costs.getSameDayCost();
        }

        @Override
        public int getCertifiedCopyNewIncorporationCost(final CostsConfig costs) {
            return costs.getSameDayNewIncorporationCost();
        }

        @Override
        public ProductType getProductType(final String filingHistoryType) {
            return filingHistoryType.equals(FILING_HISTORY_TYPE_NEWINC) ?
                    CERTIFIED_COPY_INCORPORATION_SAME_DAY :
                    CERTIFIED_COPY_SAME_DAY;
        }
    };

    // TODO GCI-1321 Duplicates constant on CertifiedCopyCostCalculatorService
    private static final String FILING_HISTORY_TYPE_NEWINC = "NEWINC";

    @JsonValue
    public String getJsonName() {
        return EnumValueNameConverter.convertEnumValueNameToJson(this);
    }

    public int getCertifiedCopyCost(final CostsConfig costs) {
        return costs.getStandardCost();
    }

    public int getCertifiedCopyNewIncorporationCost(final CostsConfig costs) {
        return costs.getStandardNewIncorporationCost();
    }

    public ProductType getProductType(final String filingHistoryType) {
        return filingHistoryType.equals(FILING_HISTORY_TYPE_NEWINC) ?
                CERTIFIED_COPY_INCORPORATION :
                CERTIFIED_COPY;
    }
}
