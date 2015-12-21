package com.lozasolutions.iss.application;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.github.pwittchen.networkevents.library.BusWrapper;
import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.NetworkEvents;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.lozasolutions.iss.utils.controls.Utils;

import de.greenrobot.event.EventBus;


public class BaseApplication extends Application {

    private RequestQueue requestQueue;
    private static BaseApplication instance;
    public static final String TAG = BaseApplication.class.getSimpleName();
    private Boolean internetConnection;
    private String previousInternetState;

    //Network connection
    private BusWrapper busWrapper;
    private NetworkEvents networkEvents;

    @Override
    public void onCreate() {
        super.onCreate();

        //Singleton
        instance = this;

        //Network Bus
        final EventBus bus = new EventBus();
        busWrapper = Utils.getGreenRobotBusWrapper(bus);
        networkEvents = new NetworkEvents(this, busWrapper).enableInternetCheck();

        //Register
        BaseApplication.getInstance().getBusWrapper().register(this);
        BaseApplication.getInstance().getNetworkEvents().register();

    }



    public void onEvent(ConnectivityChanged event) {
        ConnectivityStatus status = event.getConnectivityStatus();

        if (status.equals(ConnectivityStatus.MOBILE_CONNECTED) || status.equals(ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET)) {

            setInternetConnection(true);

        } else {

            setInternetConnection(false);
        }

    }

    /**
     * Returns the application instance.
     *
     * @return The application instance.
     */
    public static synchronized BaseApplication getInstance() {
        return instance;
    }

    /**
     * Returns the Volley requests queue, initializing it if it doesn't exist
     *
     * @return The requests queue
     */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return requestQueue;
    }

    /**
     * Allows to add a Volley request to the requests global queue, setting a tag in order to
     * identify the added request.
     *
     * @param req The request to be added
     * @param tag The tag which identifies the request.
     * @param <T>
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    /**
     * Allows to add a Volley request to the requests global queue
     *
     * @param req The request to be added
     * @param <T>
     */
    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    /**
     * Cancels an specified volley request
     *
     * @param tag tag of the request to be canceled.
     */
    public void cancelPendingRequests(Object tag) {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }

    public Boolean isInternetConnection() {
        return internetConnection;
    }

    public void setInternetConnection(Boolean internetConnection) {
        this.internetConnection = internetConnection;
    }
    public String getPreviousInternetState() {
        return previousInternetState;
    }

    public void setPreviousInternetState(String previousInternetState) {
        this.previousInternetState = previousInternetState;
    }

    public BusWrapper getBusWrapper() {
        return busWrapper;
    }

    public void setBusWrapper(BusWrapper busWrapper) {
        this.busWrapper = busWrapper;
    }

    public NetworkEvents getNetworkEvents() {
        return networkEvents;
    }

    public void setNetworkEvents(NetworkEvents networkEvents) {
        this.networkEvents = networkEvents;
    }



}
