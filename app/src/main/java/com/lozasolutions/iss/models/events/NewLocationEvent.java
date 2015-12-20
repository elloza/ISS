package com.lozasolutions.iss.models.events;

import android.location.Location;

/**
 * Created by Loza on 19/12/2015.
 */
public class NewLocationEvent {

    public final Location location;

    public NewLocationEvent(Location location) {
        this.location = location;
    }

}
