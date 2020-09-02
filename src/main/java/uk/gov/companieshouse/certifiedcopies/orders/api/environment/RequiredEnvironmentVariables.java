package uk.gov.companieshouse.certifiedcopies.orders.api.environment;

public enum RequiredEnvironmentVariables {
    
    ITEMS_DATABASE("ITEMS_DATABASE"),
    MONGODB_URL("MONGODB_URL");
    
    private String name;
    
    RequiredEnvironmentVariables(String name){
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }

}
