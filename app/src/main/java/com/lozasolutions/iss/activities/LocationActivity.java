package com.lozasolutions.iss.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.lozasolutions.iss.R;

public class LocationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //Title and subtitle
        toolbar.setTitle(getString(R.string.location_activity_toolbar_title));
        setSupportActionBar(toolbar);

        //TODO: Toolbar
        //http://www.hermosaprogramacion.com/2015/06/toolbar-en-android-creacion-de-action-bar-en-material-design/
        //http://stackoverflow.com/questions/26651602/display-back-arrow-on-toolbar-android
        //https://www.youtube.com/watch?v=4XfDDfa3rv8
        //http://developer.android.com/intl/es/training/appbar/up-action.html
        //http://www.sgoliver.net/blog/actionbar-appbar-toolbars-en-android-i/


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
