package com.example.android.sunshine.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class DetailActivity extends AppCompatActivity {
    private final String LOG_TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate called ...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Add Up action to action bar
        try {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Trying to set up action returns null?", e);
            e.printStackTrace();
        }

        Log.d(LOG_TAG, "Attaching DetailFragment");
        // Attach Details fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_container, new DetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; the adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks.  The action bar will automatically handle
        // clicks on the Home/Up button, as long as the parent activity is specified in
        // the AndroidManifest.xml file.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
