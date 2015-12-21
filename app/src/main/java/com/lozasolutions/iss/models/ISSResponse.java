package com.lozasolutions.iss.models;


import android.location.Location;

import java.util.List;


public class ISSResponse {

    Location location;

    List<ISSPass> responses;

    public ISSResponse(Location location, List<ISSPass> responses) {
        this.location = location;
        this.responses = responses;
    }

    public List<ISSPass> getResponses() {
        return responses;
    }

    public void setResponses(List<ISSPass> responses) {
        this.responses = responses;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }




}
