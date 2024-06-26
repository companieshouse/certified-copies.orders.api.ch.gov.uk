package uk.gov.companieshouse.certifiedcopies.orders.api.environment;

public enum RequiredEnvironmentVariables {
    
    ITEMS_DATABASE("ITEMS_DATABASE"),
    MONGODB_URL("MONGODB_URL"),
    CHS_API_KEY("CHS_API_KEY"),
    API_URL("API_URL"),
    PAYMENTS_API_URL("PAYMENTS_API_URL");

    private final String name;
    
    RequiredEnvironmentVariables(String name){
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }

}
