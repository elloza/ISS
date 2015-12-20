package com.lozasolutions.iss.models.events;

import android.location.Location;

/**
 * Created by Loza on 19/12/2015.
 */
public class ReverseGeocodingEvent {

    public final String address;
    public final Integer result;
    public final Location location;

    public ReverseGeocodingEvent(Location location, String address,Integer result) {
        this.location = location;
        this.address = address;
        this.result = result;
    }

}
