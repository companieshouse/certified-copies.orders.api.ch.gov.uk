package uk.gov.companieshouse.certifiedcopies.orders.api.model;

/**
 * Values of this represent the possible product types.
 */
public enum ProductType {
    CERTIFIED_COPY("certified-copy"),
    CERTIFIED_COPY_SAME_DAY("certified-copy-same-day");

    private String value;

    private ProductType(String value) { this.value = value; }

    public String getValue() { return this.value; }
}
