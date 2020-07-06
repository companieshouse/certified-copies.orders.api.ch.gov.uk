package uk.gov.companieshouse.certifiedcopies.orders.api.model;

/**
 * Values of this represent the possible product types.
 */
public enum ProductType {
    CERTIFICATE("certificate"),
    CERTIFICATE_SAME_DAY("certificate-same-day"),
    CERTIFICATE_ADDITIONAL_COPY("certificate-additional-copy"),
    SCAN_UPON_DEMAND("scan_upon-demand"),
    CERTIFIED_COPY("certified-copy"),
    CERTIFIED_COPY_SAME_DAY("certified-copy-same-day");

    private String value;

    private ProductType(String value) { this.value = value; }

    public String getValue() { return this.value; }
}
