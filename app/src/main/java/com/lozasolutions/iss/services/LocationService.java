package com.lozasolutions.iss.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.lozasolutions.iss.models.events.NewLocationEvent;
import com.lozasolutions.iss.models.events.ReverseGeocodingEvent;
import com.lozasolutions.iss.utils.controls.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


/**
 * Location Service
 */
public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //Tag for debug
    public static final String TAG = "LocationService";

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;

    private int DISTANCE_BETWEEN_LOCATIONS = 100;

    //Previous location
    private Location previousLocation;

    @Override
    public void onCreate() {

        super.onCreate();

        Log.d(TAG, "Start Service");

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {

            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {
            Log.e(TAG, "unable to connect to google play services.");
        }


    }

    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand");
        return START_NOT_STICKY;
    }


    @Override
    public void onLocationChanged(Location location) {

        //New location

        if (location != null) {

            //TODO: Check if distance > DISTANCE_BETWEEN_LOCATIONS
            EventBus.getDefault().post(new NewLocationEvent(location));

        }

        previousLocation = location;

        Log.d(TAG, "position: " + location.getLatitude() + ", " + location.getLongitude() + " accuracy: " + location.getAccuracy());


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    /**
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000); // milliseconds
        locationRequest.setFastestInterval(1000); // the fastest rate in milliseconds at which your app can handle location updates
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (lastLocation != null) {
            previousLocation = lastLocation;
            EventBus.getDefault().post(new NewLocationEvent(lastLocation));
        } else {
            //Send null location to request user active some location mode
            EventBus.getDefault().post(new NewLocationEvent(lastLocation));
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
        stopLocationUpdates();
        stopSelf();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "GoogleApiClient connection has been suspend");
    }


    public Location getPreviousLocation() {
        return previousLocation;
    }

    public void setPreviousLocation(Location previousLocation) {
        this.previousLocation = previousLocation;
    }


    /**
     * Function that performs reverse geocoding of a location. This function generates a ReverseGeocodingEvent.
     *
     * @param location
     */
    public void reverseGeocoding(Location location) {

        ReverseGeocodingTask task = new ReverseGeocodingTask(location, this);
        task.execute();

    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public LocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationService.this;
        }
    }


    private class ReverseGeocodingTask extends AsyncTask<String, Void, Void> {

        Location loc;
        Context con;

        ReverseGeocodingTask(Location loc, Context con) {
            this.loc = loc;
            this.con = con;
        }

        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */
        protected Void doInBackground(String... urls) {

            reverseGeocoding(loc);

            return null;
        }

        public Void reverseGeocoding(Location location) {

            Boolean success = false;

            if (Geocoder.isPresent()) {

                String errorMessage = "";

                // Errors could still arise from using the Geocoder (for example, if there is no
                // connectivity, or if the Geocoder is given illegal location data). Or, the Geocoder may
                // simply not have an address for a location. In all these cases, we communicate with the
                // receiver using a resultCode indicating failure. If an address is found, we use a
                // resultCode indicating success.

                // The Geocoder used in this sample. The Geocoder's responses are localized for the given
                // Locale, which represents a specific geographical or linguistic region. Locales are used
                // to alter the presentation of information such as numbers or dates to suit the conventions
                // in the region they describe.

                Geocoder geocoder = new Geocoder(con, con.getResources().getConfiguration().locale);

                // Address found using the Geocoder.
                List<Address> addresses = null;

                try {
                    // Using getFromLocation() returns an array of Addresses for the area immediately
                    // surrounding the given latitude and longitude. The results are a best guess and are
                    // not guaranteed to be accurate.
                    addresses = geocoder.getFromLocation(
                            location.getLatitude(),
                            location.getLongitude(),
                            // In this sample, we get just a single address.
                            1);
                } catch (IOException ioException) {
                    // Catch network or other I/O problems.
                    Log.e(TAG, errorMessage, ioException);

                    EventBus.getDefault().post(new ReverseGeocodingEvent(location, "", Constants.REVERSE_GEOCODING_ERROR));


                } catch (IllegalArgumentException illegalArgumentException) {
                    // Catch invalid latitude or longitude values.
                    EventBus.getDefault().post(new ReverseGeocodingEvent(location, "", Constants.REVERSE_GEOCODING_ERROR));
                }

                // Handle case where no address was found.
                if (addresses == null || addresses.size() == 0) {
                    if (errorMessage.isEmpty()) {
                        EventBus.getDefault().post(new ReverseGeocodingEvent(location, "", Constants.REVERSE_GEOCODING_NOT_FOUND));
                    }

                } else {
                    Address address = addresses.get(0);
                    ArrayList<String> addressFragments = new ArrayList<String>();

                    // Fetch the address lines using {@code getAddressLine},
                    // join them, and send them to the thread. The {@link android.location.address}
                    // class provides other options for fetching address details that you may prefer
                    // to use. Here are some examples:
                    // getLocality() ("Mountain View", for example)
                    // getAdminArea() ("CA", for example)
                    // getPostalCode() ("94043", for example)
                    // getCountryCode() ("US", for example)
                    // getCountryName() ("United States", for example)
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        addressFragments.add(address.getAddressLine(i));
                    }

                    EventBus.getDefault().post(new ReverseGeocodingEvent(location, TextUtils.join(",", addressFragments), Constants.REVERSE_GEOCODING_SUCCESS));


                }

            } else {
                EventBus.getDefault().post(new ReverseGeocodingEvent(location, "", Constants.REVERSE_GEOCODING_ERROR));

            }

            return null;
        }
    }


}


