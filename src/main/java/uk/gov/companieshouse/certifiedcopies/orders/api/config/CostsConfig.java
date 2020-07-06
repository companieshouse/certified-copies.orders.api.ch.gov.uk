package uk.gov.companieshouse.certifiedcopies.orders.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Configuration
@Component
@PropertySource(value = "classpath:costs.properties")
@ConfigurationProperties(prefix = "costs")
@Validated
public class CostsConfig {
    @Min(1)
    private int certifiedCopy;
    @Min(1)
    private int certifiedCopyNewIncorporation;
    @Min(1)
    private int sameDayCertifiedCopy;
    @Min(1)
    private int sameDayCertifiedCopyNewIncorporation;

    public int getCertifiedCopy() {
        return certifiedCopy;
    }

    public void setCertifiedCopy(int certifiedCopy) {
        this.certifiedCopy = certifiedCopy;
    }

    public int getCertifiedCopyNewIncorporation() {
        return certifiedCopyNewIncorporation;
    }

    public void setCertifiedCopyNewIncorporation(int certifiedCopyNewIncorporation) {
        this.certifiedCopyNewIncorporation = certifiedCopyNewIncorporation;
    }

    public int getSameDayCertifiedCopy() {
        return sameDayCertifiedCopy;
    }

    public void setSameDayCertifiedCopy(int sameDayCertifiedCopy) {
        this.sameDayCertifiedCopy = sameDayCertifiedCopy;
    }

    public int getSameDayCertifiedCopyNewIncorporation() {
        return sameDayCertifiedCopyNewIncorporation;
    }

    public void setSameDayCertifiedCopyNewIncorporation(int sameDayCertifiedCopyNewIncorporation) {
        this.sameDayCertifiedCopyNewIncorporation = sameDayCertifiedCopyNewIncorporation;
    }
}
