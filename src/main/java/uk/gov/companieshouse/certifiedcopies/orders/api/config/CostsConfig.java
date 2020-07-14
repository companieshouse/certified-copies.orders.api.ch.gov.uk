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
    private int standardCost;
    @Min(1)
    private int standardNewIncorporationCost;
    @Min(1)
    private int sameDayCost;
    @Min(1)
    private int sameDayNewIncorporationCost;

    public int getStandardCost() {
        return standardCost;
    }

    public void setStandardCost(int standardCost) {
        this.standardCost = standardCost;
    }

    public int getStandardNewIncorporationCost() {
        return standardNewIncorporationCost;
    }

    public void setStandardNewIncorporationCost(int standardNewIncorporationCost) {
        this.standardNewIncorporationCost = standardNewIncorporationCost;
    }

    public int getSameDayCost() {
        return sameDayCost;
    }

    public void setSameDayCost(int sameDayCost) {
        this.sameDayCost = sameDayCost;
    }

    public int getSameDayNewIncorporationCost() {
        return sameDayNewIncorporationCost;
    }

    public void setSameDayNewIncorporationCost(int sameDayNewIncorporationCost) {
        this.sameDayNewIncorporationCost = sameDayNewIncorporationCost;
    }
}
