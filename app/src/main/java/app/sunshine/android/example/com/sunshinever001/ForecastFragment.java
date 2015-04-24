package app.sunshine.android.example.com.sunshinever001;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }

    ArrayAdapter<String> listForecastAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] dataDummy = {
                "Today - Sunny - 88/63",
                "Tomorrow - Sunny - 88/63",
                "Weds - Foggy - 70/43",
                "Thurs - Sunny - 88/63",
                "Fri - Sunny - 88/63",
                "Sat - Foggy - 70/43"
                //"Mon - Sunny - 100/90"
        };

        List<String> listDataDummy = new ArrayList<String>(Arrays.asList(dataDummy));
            /*
            ArrayList ini berisikan data yang tipe datanya array of string
            ketika dimasukkkan data kemudian data diubah menjadi raw data kemudian
            dikirimkan ke array adapter untuk ditampilkan.
            misal banyak data ada 50 tapi device screen hnya mampu untuk tampilkan 10 data
            maka 10 data yang diproses dulu sisanya setelah di scroll dari data 11-seterusnya
             */

        listForecastAdapter = new ArrayAdapter<String>(
                getActivity(),//menggunakan getActiity karena class menggunakan fragment
                R.layout.list_item_forecast,//layout dari listview
                R.id.list_item_forecast_textview,//id text yg dipke bwt diisikan nanti
                listDataDummy//data yang mau diisikan
        );

            /*
            lakukan reference view dari data pada adapterArray
             */
        ListView listLayout = (ListView) rootView.findViewById(R.id.listview_forecast);
        listForecastAdapter.setDropDownViewResource(R.layout.list_item_forecast);
        listLayout.setAdapter(listForecastAdapter);

        return rootView;
    }

    @Override
    //first time to execute when running before oncreateView
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //this line for declaring the menu that has initialized in forecastFrament menu xml
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem itemMenu){
        int id = itemMenu.getItemId();
        if(id == R.id.action_refresh){
            //when this action refresh execute, it will execute thread that have created named fetchWeatherTask
            //with parameter 94043 as zip code.
            new FetchWeatherTask().execute("94043");
            return true;
        }
        return super.onOptionsItemSelected(itemMenu);
    }

    class FetchWeatherTask extends AsyncTask<String, Void, String[]> {//<parameter, progress, result>

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private String getReadableDataString(long time){
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");

            return shortenedDateFormat.format(time);
        }

        private String formatHighLows(double high, double low){
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + " / " + roundedLow;
            return highLowStr;
        }

        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays) throws JSONException{
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            Time dayTime = new Time();
            dayTime.setToNow();

            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i<weatherArray.length(); i++){
                String day;
                String description;
                String highAndLow;

                JSONObject dayForecast = weatherArray.getJSONObject(i);

                long dateTime;
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDataString(dateTime);

                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);

                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day +" - "+ description +" - "+ highAndLow;
            }

            for(String s : resultStrs){
                Log.d(LOG_TAG, "Forecast Entry: "+s);
            }

            return resultStrs;
        }

        @Override
        protected String[] doInBackground(String... params) {

            if(params.length == 0){
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String mode = "json";
            String units = "metric";
            int cnt = 7;

            try {

                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNIT_PARAM = "unit";
                final String DAY_PARAM = "cnt";

                Uri buildUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, mode)
                        .appendQueryParameter(UNIT_PARAM, units)
                        .appendQueryParameter(DAY_PARAM, Integer.toString(cnt))
                        .build();

                URL url = new URL(buildUri.toString());

                Log.d(LOG_TAG, "Built URI "+buildUri.toString());

                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q="+params[0]+"&mode=json&units=metric&cnt=7");

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.d(LOG_TAG, "Forecast string "+forecastJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            try{
                return getWeatherDataFromJson(forecastJsonStr, cnt);
            }catch (JSONException n){
                Log.e(LOG_TAG, n.getMessage(), n);
                n.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String[] result){
            if(result != null){
                listForecastAdapter.clear();
                for(String dayForecastStr : result){
                    listForecastAdapter.add(dayForecastStr);
                }
            }
        }
    }
}