package com.lozasolutions.iss.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.lozasolutions.iss.R;
import com.lozasolutions.iss.adapters.PassesAdapter;
import com.lozasolutions.iss.application.BaseApplication;
import com.lozasolutions.iss.models.ISSPass;
import com.lozasolutions.iss.models.events.NewLocationEvent;
import com.lozasolutions.iss.models.events.ReverseGeocodingEvent;
import com.lozasolutions.iss.services.LocationService;
import com.lozasolutions.iss.utils.SimpleDividerItemDecoration;
import com.lozasolutions.iss.utils.controls.Constants;
import com.lozasolutions.iss.utils.controls.RecyclerViewEmptySupport;
import com.lozasolutions.iss.utils.controls.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;

public class HomeActivity extends AppCompatActivity {

    RecyclerViewEmptySupport recyclerView;
    MaterialDialog progress;
    TextView txtAddress;

    //LOCATION SERVICE
    LocationService locationService;
    static boolean locationServiceBound = false;

    //Current location
    //TODO: Change statics vars
    private static Location currentLocation;
    private static String address;
    private static PassesAdapter adapter;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection locationConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationService = binder.getService();
            locationServiceBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

            locationServiceBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //Title and subtitle
        toolbar.setTitle(getString(R.string.location_activity_toolbar_title));
        setSupportActionBar(toolbar);

        //Recyclerview
        recyclerView = (RecyclerViewEmptySupport) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setEmptyView(findViewById(R.id.txtEmpty));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));

        if (adapter != null) {

            recyclerView.setAdapter(adapter);

        } else {

            List<ISSPass> ISSPasses = new ArrayList<>();
            adapter = new PassesAdapter(ISSPasses, this);
            recyclerView.setAdapter(adapter);

        }


        //TxtAddress
        txtAddress = (TextView) findViewById(R.id.txtAddress);

        if (address != null) {
            txtAddress.setText(address);
        }

        //LOCATION ENABLE DEVICE
        if (!Utils.isLocationEnabled(this)) {

            requestUserEnableLocation();

        }

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onStart() {
        super.onStart();

        //Default Bus Event
        EventBus.getDefault().register(this);

        //Register
        BaseApplication.getInstance().getBusWrapper().register(this);

        //BIND LOCATION SERVICE (This service is alive until the application is closed)
        BaseApplication.getInstance().bindService(new Intent(this, LocationService.class), locationConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {

        //Unregister events
        BaseApplication.getInstance().getBusWrapper().unregister(this);
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent context in AndroidManifest.xml.
        int id = item.getItemId();

        /* Ready for the settings

        if (id == R.id.action_settings) {
            return true;
        }

        */

        return super.onOptionsItemSelected(item);
    }



    ///-------------------------------EVENTS OF EVENT BUS ----------------------------------------------------


    /**
     * Event called when an operation of reverse geocoding is called.
     *
     * @param event Parameter containing the result of the operation.
     */
    public void onEventMainThread(ReverseGeocodingEvent event) {

        switch (event.result) {

            case Constants.REVERSE_GEOCODING_ERROR:

                if (currentLocation.equals(event.location)) {
                    Toast.makeText(getBaseContext(), getString(R.string.location_activity_address_error), Toast.LENGTH_LONG).show();

                    address = getString(R.string.location_activity_address_not_found);
                    if (txtAddress != null) {
                        txtAddress.setText(getString(R.string.location_activity_address_not_found));
                    }

                    if (progress != null && progress.isShowing()) {
                        progress.dismiss();
                    }
                }

                break;

            case Constants.REVERSE_GEOCODING_NOT_FOUND:

                if (currentLocation.equals(event.location)) {
                    Toast.makeText(getBaseContext(), getString(R.string.location_activity_address_not_found), Toast.LENGTH_LONG).show();

                    address = getString(R.string.location_activity_address_not_found);
                    if (txtAddress != null) {
                        txtAddress.setText(getString(R.string.location_activity_address_not_found));
                    }

                    if (progress != null && progress.isShowing()) {
                        progress.dismiss();
                    }
                }

                break;

            case Constants.REVERSE_GEOCODING_SUCCESS:

                if (currentLocation.equals(event.location)) {

                    address = event.address;
                    if (txtAddress != null) {
                        txtAddress.setText(address);
                    }

                    if (progress != null && progress.isShowing()) {
                        progress.dismiss();
                    }

                }

                break;

        }

    }

    /**
     * Event called when a new location is available. This event is executed in the MainThread.
     *
     * @param event Parameter containing the new location.
     */
    public void onEventMainThread(NewLocationEvent event) {

        //TODO Request to user to update the data
        if (event.location != null) {

            if (currentLocation == null) {

                currentLocation = event.location;
                updateCurrentData(event.location);

            } else {

                //Toast.makeText(getBaseContext(), "newlocation", Toast.LENGTH_LONG).show();
                //TODO ASK to user if want to update the data (if location is far away)

            }
        } else {

            //TODO NO LOCATION AVAILABLE Inform user


        }

    }


    /**
     *
     * This Event is raised when a change in internet connection is produced
     *
     * @param event
     */
    public void onEvent(ConnectivityChanged event) {
        ConnectivityStatus status = event.getConnectivityStatus();

        if (status.equals(ConnectivityStatus.MOBILE_CONNECTED) || status.equals(ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET)) {

            BaseApplication.getInstance().setInternetConnection(true);

            //If BaseApplication.getInstance().isInternetConnection() == null then is de first time of this event (always fires at start)
            if (currentLocation != null && BaseApplication.getInstance().isInternetConnection() != null) {

                //TODO: Ask to user or update info directly? (Check if the current info displayed is valid)

                BaseApplication.getInstance().setInternetConnection(true);
                new MaterialDialog.Builder(this)
                        .widgetColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .title(R.string.dialog_network_title)
                        .content(R.string.dialog_network_content_activate)
                        .positiveText(getString(R.string.dialog_network_accept))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                updateCurrentData(currentLocation);
                            }
                        })
                        .show();

            }


        } else {


        }

    }


    ///-------------------------------OTHER FUNCTIONS ----------------------------------------------------


    /**
     * This method perform a reverse geocoding operation and a request to  ISS API to get passes.
     *
     * @param loc location to search.
     */
    private void updateCurrentData(Location loc) {

        //Dialog
        progress = new MaterialDialog.Builder(this)
                .widgetColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                .title(R.string.dialog_loading_iss_title)
                .content(R.string.dialog_loading_iss_content)
                .progress(true, 0)
                .show();

        //Check network status
        if (BaseApplication.getInstance().isInternetConnection() != null && BaseApplication.getInstance().isInternetConnection()) {

            //REVERSE GEODING
            locationService.reverseGeocoding(loc);

            String uri = String.format(Constants.ISS_URL, String.format(Locale.US, "%.6f", loc.getLatitude()), String.format(Locale.US, "%.6f", loc.getLongitude()));

            //ISS API INFO

            StringRequest getRequest = new StringRequest(Request.Method.GET, uri,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                //TODO Use GSON or Jackson to parse responses
                                JSONObject jsonResponse = new JSONObject(response);
                                String message = jsonResponse.getString("message");
                                JSONObject request = jsonResponse.getJSONObject("request");
                                Double longitude = request.getDouble("longitude");
                                Double latitude = request.getDouble("latitude");
                                JSONArray responseArray = jsonResponse.getJSONArray("response");

                                List<ISSPass> issPasses = new ArrayList<>();

                                for (int i = 0; i < responseArray.length(); i++) {

                                    JSONObject iss = (JSONObject) responseArray.get(i);
                                    Long duration = iss.getLong("duration");
                                    Long risetime = iss.getLong("risetime");

                                    ISSPass issPass = new ISSPass(duration, risetime, longitude.floatValue(), latitude.floatValue(), address);
                                    issPasses.add(issPass);

                                }

                                adapter = new PassesAdapter(issPasses, getApplicationContext());

                                recyclerView.setAdapter(adapter);

                                adapter.notifyDataSetChanged();

                                if(progress.isShowing()){
                                    progress.dismiss();
                                }


                            } catch (JSONException e) {

                                e.printStackTrace();

                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            //TODO: Inform error to user
                            Log.e("Err", error.networkResponse.data.toString());
                            error.printStackTrace();


                        }
                    }
            );

            BaseApplication.getInstance().getRequestQueue().add(getRequest);


        } else {

            EventBus.getDefault().post(new ReverseGeocodingEvent(loc, "", Constants.REVERSE_GEOCODING_NOT_FOUND));

            progress.dismiss();

            new MaterialDialog.Builder(this)
                    .widgetColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                    .title(R.string.dialog_network_title)
                    .content(R.string.dialog_network_content_error)
                    .positiveText(getString(R.string.dialog_network_accept))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .show();

        }

    }


    public void requestUserEnableLocation() {

        new MaterialDialog.Builder(this)
                .widgetColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                .title(R.string.dialog_enable_location_title)
                .content(R.string.dialog_enable_location_content)
                .positiveText(R.string.dialog_enable_location_enable)
                .negativeText(R.string.dialog_enable_location_cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        //Go to monitor
                        dialog.dismiss();

                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {

                        Intent callGPSSettingIntent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                })
                .show();

    }



}
