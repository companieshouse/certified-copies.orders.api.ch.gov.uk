package uk.gov.companieshouse.certifiedcopies.orders.api.model;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.companieshouse.certifiedcopies.orders.api.config.CostsConfig;
import uk.gov.companieshouse.certifiedcopies.orders.api.converter.EnumValueNameConverter;

public enum DeliveryTimescale {
    STANDARD("standard"),
    SAME_DAY("same-day") {

        @Override
        public int getCertifiedCopyCost(final CostsConfig costs) {
            return costs.getSameDayCost();
        }

        @Override
        public int getCertifiedCopyNewIncorporationCost(final CostsConfig costs) {
            return costs.getSameDayNewIncorporationCost();
        }

        @Override
        public ProductType getProductType() {
            return ProductType.CERTIFIED_COPY_SAME_DAY;
        }
    };
    private String value;
    private DeliveryTimescale(String value) {
        this.value = value;
    }
    public String getValue(){
        return this.value;
    }

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

    public ProductType getProductType() {
        return ProductType.CERTIFIED_COPY;
    }
}
