package fr.insa.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChooseRequest {
    @JsonProperty("Demand_ID")
    private int demand_ID;
    
    @JsonProperty("Helper_ID")
    private int helper_ID;

    public int getDemand_ID() {
        return demand_ID;
    }

    public void setDemand_ID(int demand_ID) {
        this.demand_ID = demand_ID;
    }

    public int getHelper_ID() {
        return helper_ID;
    }

    public void setHelper_ID(int helper_ID) {
        this.helper_ID = helper_ID;
    }
}
