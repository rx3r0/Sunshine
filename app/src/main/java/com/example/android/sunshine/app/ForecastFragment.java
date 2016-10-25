package com.example.android.sunshine.app;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment {

    // Log tag used for debugging purposes
    public static final String LOG_TAG = ForecastFragment.class.getSimpleName();

    // Making the forecast list view and adapter a member variable makes it possible to access and
    // update them from within onPostExecute in FetchWeatherTask.
    ListView forecastListView;
    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tell fragment to handle menu options
        setHasOptionsMenu(true);
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
        List<String> dummyForecastList = new ArrayList<>(Arrays.asList(dummyData));

        // Create adapter and initialise adapter with dummy data
        // Adapter takes data from the array list and uses it to populate forecast list view
        mForecastAdapter = new ArrayAdapter<>(
                getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview,
                dummyForecastList);

        // Find reference to list view
        forecastListView = (ListView) rootView.findViewById(R.id.listView_forecast);

        // Set adapter on list view
        forecastListView.setAdapter(mForecastAdapter);

        // Add click listener to weather forecast list view items to show weather forecast details
        forecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String forecast = mForecastAdapter.getItem(i);
                Toast.makeText(getContext(), forecast,
                        Toast.LENGTH_LONG).show();
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(LOG_TAG, "onCreateOptionsMenu");

        // Inflate the menu; this adds items to the bar if the bar is present
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "onOptionsItemSelected");

        // Handle action bar item clicks.  The action bar will automatically handle clicks on
        // Home/Up button, if parent activity is specified in AndroidManifest.xml
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            new FetchWeatherTask().execute("spanish town, jm");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        /**
         * Convert UNIX time to readable formatted string.
         * @param time
         * @return
         */
        private String getReadableDateString(Date time) {
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EE MM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         * @param high
         * @param low
         * @return
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            return roundedHigh + "/" + roundedLow;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and pull out the
         * data needed to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy: constructor takes the JSON string and converts it into
         * an Object hierarchy for us.
         * @param forecastJsonStr
         * @param numDays
         * @return
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {
            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // Create Gregorian Calendar which is current date
                GregorianCalendar gregorianCalendar = new GregorianCalendar();

                // Add 'i' dates to current date of calendar
                gregorianCalendar.add(GregorianCalendar.DATE, i);

                // Get that date, format it
                Date time = gregorianCalendar.getTime();
                day = getReadableDateString(time);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            /*for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }*/
            return resultStrs;
        }

        @Override
        protected String[] doInBackground(String... params) {
            Log.d(LOG_TAG, "doInBackground method");
            // These need to be declared outside the try/catch block so that they can be closed in
            // the finally block.  The connection and buffered stream reader will be initialised
            // later in the try block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Contain raw JSON response string
            String forescastJsonStr = null;

            // Values for MODE, UNITS, and COUNT parameters in Open Weather Map query
            String format = "json";
            String units = "metric";
            int days = 7;

            try {
                // Construct the URL for Open Weather Map's query.
                // Possible parameters are available at Open Weather Map's forecast API page:
                // http://openweathermap.org/API#forecast

                // Build URI using UriBuilder absolute URI scheme

                // Open Weather Map's parameters for use in UriBuilder
                final String URL_BASE = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String LOCATION_QUERY = "q";
                final String MODE = "mode";
                final String UNITS = "units";
                final String COUNT = "cnt";
                final String APP_ID = "appid";

                Uri owmUri = Uri.parse(URL_BASE)
                        .buildUpon()
                        .appendQueryParameter(LOCATION_QUERY, params[0])
                        .appendQueryParameter(MODE, format)
                        .appendQueryParameter(UNITS, units)
                        .appendQueryParameter(COUNT, Integer.toString(days))
                        .appendQueryParameter(APP_ID, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build();


                URL url = new URL(owmUri.toString());
                Log.v(LOG_TAG, "Open Weather Map Query URL: " + url.toString());

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
                    // Adding a new line to the JSON data is not necessary as it will not affect
                    // the parsing.  However, it does make debugging a lot easier if the completed
                    // buffer is printed.
                    buffer.append(line + "\n");
                }

                // If the stream was empty then there's no point in parsing.
                if (buffer.length() == 0) {
                    forescastJsonStr = null;
                }

                forescastJsonStr = buffer.toString();

                //Log.v(LOG_TAG, "Forecast JSON String: " + forescastJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);

                // If there was no data returned there is no reason it should be parsed, therefore,
                // just return null.
                forescastJsonStr = null;
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

            // Return formatted date
            try {
                return getWeatherDataFromJson(forescastJsonStr, days);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] results) {
            // If there is weather forecast data, clear previous contents of the array adapter,
            // then add the new data

            if (results != null) {
                mForecastAdapter.clear();

                // Add the forecast strings to the forecast array adapter
                mForecastAdapter.addAll(results);
            }
        }
    }

}
