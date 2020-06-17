package uk.gov.companieshouse.certifiedcopies.orders.api.converter;

public class EnumValueNameConverter {
    private EnumValueNameConverter() { }

    public static String convertEnumValueJsonToName(final String enumValueJson) {
        return enumValueJson.toUpperCase().replace("-", "_");
    }

    public static String convertEnumValueNameToJson(final Enum value) {
        return value.name().toLowerCase().replace("_", "-");
    }
}
