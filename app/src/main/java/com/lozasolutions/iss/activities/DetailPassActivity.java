package com.lozasolutions.iss.activities;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.lozasolutions.iss.R;
import com.lozasolutions.iss.application.BaseApplication;
import com.lozasolutions.iss.models.ISSPass;
import com.lozasolutions.iss.utils.controls.Constants;

import java.util.Date;

import de.greenrobot.event.EventBus;

public class DetailPassActivity extends AppCompatActivity {

    MaterialDialog progress;
    TextView txtAddress,txtNumbersAPI,txtCountDown,txtDuration;
    ISSPass issPass;
    CountDownTimer countDownTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pass);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //Title and subtitle
        toolbar.setTitle(getString(R.string.location_activity_toolbar_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Data
        Bundle data = getIntent().getExtras();
        issPass = (ISSPass) data.getParcelable(Constants.ISSPASS);

        //TxtAddress
        txtAddress = (TextView) findViewById(R.id.txtAddress);
        if(issPass.getAddress() != null){
            txtAddress.setText(issPass.getAddress());
        }

        //Countdown
        txtCountDown = (TextView) findViewById(R.id.txtCountDown);

        Date currentDate = new Date();

        Date issDate = new Date(issPass.getRisetime()*1000);

        long seconds = (issDate.getTime()-currentDate.getTime())/1000;

        String secondsValue = String.valueOf(seconds);
        String secondsTxt = getString(R.string.detail_pass_seconds);

        String countdownSeconds = secondsValue + secondsTxt;
        SpannableString styledString = new SpannableString(countdownSeconds);
        //Seconds
        styledString.setSpan(new StyleSpan(Typeface.BOLD), countdownSeconds.indexOf(secondsValue), countdownSeconds.indexOf(secondsValue)+secondsValue.length(), 0);

        //Seconds txt
        styledString.setSpan(new RelativeSizeSpan(0.5f), countdownSeconds.indexOf(secondsTxt), countdownSeconds.indexOf(secondsTxt)+secondsTxt.length(), 0);
        styledString.setSpan(new StyleSpan(Typeface.NORMAL), countdownSeconds.indexOf(secondsTxt), countdownSeconds.indexOf(secondsTxt)+secondsTxt.length(), 0);
        styledString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.gray)), countdownSeconds.indexOf(secondsTxt), countdownSeconds.indexOf(secondsTxt) + secondsTxt.length(), 0);

        txtCountDown.setText(styledString);

        if(countDownTimer == null) {

            countDownTimer = new CountDownTimer(seconds * 1000, 1000) {

                public void onTick(long millisUntilFinished) {

                    String secondsValue = String.valueOf(millisUntilFinished / 1000);
                    String secondsTxt = getString(R.string.detail_pass_seconds);

                    String countdownSeconds = secondsValue + secondsTxt;
                    SpannableString styledString = new SpannableString(countdownSeconds);
                    //Seconds
                    styledString.setSpan(new StyleSpan(Typeface.BOLD), countdownSeconds.indexOf(secondsValue), countdownSeconds.indexOf(secondsValue) + secondsValue.length(), 0);

                    //Seconds txt
                    styledString.setSpan(new RelativeSizeSpan(0.5f), countdownSeconds.indexOf(secondsTxt), countdownSeconds.indexOf(secondsTxt) + secondsTxt.length(), 0);
                    styledString.setSpan(new StyleSpan(Typeface.NORMAL), countdownSeconds.indexOf(secondsTxt), countdownSeconds.indexOf(secondsTxt) + secondsTxt.length(), 0);
                    styledString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.gray)), countdownSeconds.indexOf(secondsTxt), countdownSeconds.indexOf(secondsTxt) + secondsTxt.length(), 0);

                    txtCountDown.setText(styledString);

                }

                public void onFinish() {

                    String secondsValue = String.valueOf(0);
                    String secondsTxt = getString(R.string.detail_pass_seconds);

                    String countdownSeconds = secondsValue + secondsTxt;
                    SpannableString styledString = new SpannableString(countdownSeconds);
                    //Seconds
                    styledString.setSpan(new StyleSpan(Typeface.BOLD), countdownSeconds.indexOf(secondsValue), countdownSeconds.indexOf(secondsValue) + secondsValue.length(), 0);

                    //Seconds txt
                    styledString.setSpan(new RelativeSizeSpan(0.5f), countdownSeconds.indexOf(secondsTxt), countdownSeconds.indexOf(secondsTxt) + secondsTxt.length(), 0);
                    styledString.setSpan(new StyleSpan(Typeface.NORMAL), countdownSeconds.indexOf(secondsTxt), countdownSeconds.indexOf(secondsTxt) + secondsTxt.length(), 0);
                    styledString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.gray)), countdownSeconds.indexOf(secondsTxt), countdownSeconds.indexOf(secondsTxt) + secondsTxt.length(), 0);

                    txtCountDown.setText(styledString);

                }
            }.start();

            countDownTimer.start();
        }


        //Txt Duration
        txtDuration = (TextView) findViewById(R.id.txtDuration);

        Long durationMinutes = issPass.getDuration()/60L;
        Long durationSeconds = issPass.getDuration()%60;

        txtDuration.setText(String.format(getResources().getConfiguration().locale,getString(R.string.row_next_pass_duration),durationMinutes,durationSeconds));

        //Numbers API
        txtNumbersAPI = (TextView) findViewById(R.id.txtNumbersAPI);

        getNumbersData(issPass.getDuration());


    }


    @Override
    protected void onStart() {
        super.onStart();

        //Default Bus Event
        EventBus.getDefault().register(this);

        //Register
        BaseApplication.getInstance().getBusWrapper().register(this);

    }

    @Override
    protected void onStop() {

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

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    ///-------------------------------EVENTS OF EVENT BUS ----------------------------------------------------


    public void onEvent(ConnectivityChanged event) {
        ConnectivityStatus status = event.getConnectivityStatus();

        if (status.equals(ConnectivityStatus.MOBILE_CONNECTED) || status.equals(ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET)) {

            BaseApplication.getInstance().setInternetConnection(true);

            //If BaseApplication.getInstance().isInternetConnection() == null then is de first time of this event (always fires at start)
            if(BaseApplication.getInstance().isInternetConnection()!= null ){

                BaseApplication.getInstance().setInternetConnection(true);
                new MaterialDialog.Builder(this)
                        .widgetColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .title(R.string.dialog_network_title)
                        .content(R.string.dialog_network_content_activate)
                        .positiveText(getString(R.string.dialog_network_accept))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                getNumbersData(issPass.getDuration());
                            }
                        })
                        .show();

            }


        } else {


        }

    }

    /**
     * This method request to Numbers API a fact of a number and set it to textview.
     *
     * @param number location to search.
     */
    private void getNumbersData(Long number) {

        //Dialog
        progress = new MaterialDialog.Builder(this)
                .widgetColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                .title(R.string.dialog_loading_numbers_title)
                .content(R.string.dialog_loading_numbers_content)
                .progress(true, 0)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        //TODO: Some message to user?
                    }
                })
                .show();

        //Check network status
        if(BaseApplication.getInstance().isInternetConnection() != null && BaseApplication.getInstance().isInternetConnection()) {

            //Numbers API
            //TODO: Call Numbers API

            String uri = String.format( Constants.NUMBERS_URL, number);

            //ISS API INFO

            StringRequest getRequest = new StringRequest(Request.Method.GET,uri,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {


                                txtNumbersAPI.setText(response);

                            progress.dismiss();

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {


                            Log.e("Err", error.networkResponse.data.toString());
                            error.printStackTrace();


                        }
                    }
            );
            BaseApplication.getInstance().getRequestQueue().add(getRequest);





        }else{

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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }



}
