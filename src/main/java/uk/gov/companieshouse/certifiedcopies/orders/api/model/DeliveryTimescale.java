package uk.gov.companieshouse.certifiedcopies.orders.api.model;

import uk.gov.companieshouse.certifiedcopies.orders.api.config.CostsConfig;

public enum DeliveryTimescale {
    STANDARD,
    SAME_DAY {

        @Override
        public int getCertifiedCopyCost(final CostsConfig costs) {
            return costs.getSameDayCertifiedCopy();
        }

        @Override
        public int getCertifiedCopyNewIncorporationCost(final CostsConfig costs) {
            return costs.getSameDayCertifiedCopyNewIncorporation();
        }

        @Override
        public ProductType getProductType() {
            return ProductType.CERTIFIED_COPY_SAME_DAY;
        }
    };

    public int getCertifiedCopyCost(final CostsConfig costs) {
        return costs.getCertifiedCopy();
    }

    public int getCertifiedCopyNewIncorporationCost(final CostsConfig costs) {
        return costs.getCertifiedCopyNewIncorporation();
    }

    public ProductType getProductType() {
        return ProductType.CERTIFIED_COPY;
    }
}
