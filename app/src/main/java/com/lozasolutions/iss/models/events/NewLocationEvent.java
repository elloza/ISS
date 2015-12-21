package com.lozasolutions.iss.models.events;

import android.location.Location;


public class NewLocationEvent {

    public final Location location;

    public NewLocationEvent(Location location) {
        this.location = location;
    }

}
