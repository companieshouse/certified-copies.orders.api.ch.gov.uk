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
    SAME_DAY,
    DIGITAL{

        @Override
        protected int getCertifiedCopyCost(final CostsConfig costs) {
            return costs.getSameDayCost();
        }

        @Override
        protected int getCertifiedCopyNewIncorporationCost(final CostsConfig costs) {
            return costs.getSameDayNewIncorporationCost();
        }

        @Override
        protected ProductType getIncorporationProductType() {
            return CERTIFIED_COPY_INCORPORATION_SAME_DAY;
        }

        @Override
        protected ProductType getNonIncorporationProductType() {
            return CERTIFIED_COPY_SAME_DAY;
        }

    };

    private static final String FILING_HISTORY_TYPE_NEWINC = "NEWINC";

    @JsonValue
    public String getJsonName() {
        return EnumValueNameConverter.convertEnumValueNameToJson(this);
    }

    public final int getCost(final CostsConfig costs, final String filingHistoryType) {
        return isIncorporation(filingHistoryType) ?
                getCertifiedCopyNewIncorporationCost(costs) :
                getCertifiedCopyCost(costs);
    }

    public final ProductType getProductType(final String filingHistoryType) {
        return isIncorporation(filingHistoryType) ?
                getIncorporationProductType() :
                getNonIncorporationProductType();
    }

    protected int getCertifiedCopyCost(final CostsConfig costs) {
        return costs.getStandardCost();
    }

    protected int getCertifiedCopyNewIncorporationCost(final CostsConfig costs) {
        return costs.getStandardNewIncorporationCost();
    }

    protected ProductType getIncorporationProductType() {
        return CERTIFIED_COPY_INCORPORATION;
    }

    protected ProductType getNonIncorporationProductType() {
        return CERTIFIED_COPY;
    }

    private boolean isIncorporation(final String filingHistoryType) {
        return filingHistoryType.equals(FILING_HISTORY_TYPE_NEWINC);
    }
}
