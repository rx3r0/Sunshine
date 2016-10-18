package com.example.android.sunshine.app;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment {

    // Log tag used for debugging purposes
    public static final String LOG_TAG = ForecastFragment.class.getSimpleName();

    public ForecastFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);

        // Create dummy data for the ListView
        String[] dummyData = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };

        // Create ArrayList and add string array with dummy data
        List<String> weatherForecastList = new ArrayList<>(Arrays.asList(dummyData));

        // Create adapter and initialise adapter with dummy data
        // Adapter takes data from the array list and uses it to populate forecast list view
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview,
                weatherForecastList);

        // Find reference to list view
        ListView forecastListView = (ListView) rootView.findViewById(R.id.listView_forecast);

        // Set adapter on list view
        forecastListView.setAdapter(adapter);

        return rootView;
    }

    private class FetchWeatherTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            // These need to be declared outside the try/catch block so that they can be closed in
            // the finally block.  The connection and buffered stream reader will be initialised later
            // in the try block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Contain raw JSON response string
            String forescastStr = null;

            try {
                // Construct the URL for Open Weather Map's query.
                // Possible parameters are available at Open Weather Map's forecast API page:
                // http://openweathermap.org/API#forecast

                // The URL is too long.  For readability, break it up into smaller pieces and
                // concatenated it.
                final String URL_BASE = "http://api.openweathermap.org/data/2.5/forecast/daily";
                final String LOCATION_QUERY = "?q=spanish%20town,jm";
                final String MODE = "&mode=json";
                final String UNITS = "&units=metric";
                final String COUNT = "&cnt=7";
                final String API_ID = BuildConfig.OPEN_WEATHER_MAP_API_KEY;

                URL url = new URL(URL_BASE + LOCATION_QUERY + MODE + UNITS + COUNT + API_ID);

                // Create connection to Open Weather Map and open it.
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Create input stream and read into string buffer
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                // If the input stream is null, then there's nothing to do, so return null and end.
                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                // Break stream into individual lines by appending \n to each line
                String line;

                while ((line = reader.readLine()) != null) {
                    // Adding a new line to the JSON data is not necessary as it will not affect the
                    // parsing.  However, it does make debugging a lot easier if the completed buffer
                    // is printed.
                    buffer.append(line + "\n");
                }

                // If the stream was empty then there's no point in parsing.
                if (buffer.length() == 0) {
                    forescastStr = null;
                }

                forescastStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);

                // If there was no data returned there is no reason it should be parsed, therefore,
                // just return null.
                forescastStr = null;
            } finally {
                // If still connected, then disconnect.
                if (urlConnection != null) {
                    urlConnection.disconnect();

                    // If the stream buffer is still open, then close it.
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Error closing stream", e);
                        }

                    }
                }
            }

            return null;
        }
    }

}
