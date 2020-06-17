package uk.gov.companieshouse.certifiedcopies.orders.api.model;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.companieshouse.certifiedcopies.orders.api.converter.EnumValueNameConverter;

public enum DeliveryTimescale {
    STANDARD,
    SAME_DAY;

    @JsonValue
    public String getJsonName() {
        return EnumValueNameConverter.convertEnumValueNameToJson(this);
    }
}