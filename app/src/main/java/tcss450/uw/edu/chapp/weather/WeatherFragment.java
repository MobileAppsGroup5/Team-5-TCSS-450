package tcss450.uw.edu.chapp.weather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tcss450.uw.edu.chapp.R;
import tcss450.uw.edu.chapp.WaitFragment;
import tcss450.uw.edu.chapp.utils.GetAsyncTask;
import tcss450.uw.edu.chapp.weather.WeatherHourContent.WeatherHourItem;
import tcss450.uw.edu.chapp.weather.WeatherDayContent.WeatherDayItem;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WeatherFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class WeatherFragment extends Fragment {

    /*
     * desired interval for location updates, inexact. updates may be more or less frequent
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 2;

    /*
     * fastest rate for active location updates. exact. updates will never be more frequent than
     * this interval
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS/2;

    private static final int MY_PERMISSION_LOCATIONS = 8414;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    private OnFragmentInteractionListener mListener;

    public WeatherFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_weather, container, false);

        //default to current device location to display weather upon initial creation

        //ask for permission for location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSION_LOCATIONS);

        } else { //user already allowed locations
            //requestLocation();
            createLocationRequest();
            setLocationCallback();
            startLocationUpdates();
        }

        return v;
    }

    private void setWeatherHourFragment(ArrayList<WeatherHourItem> weatherItemList) {
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.keys_weather_hour_list_arg), weatherItemList);

        WeatherHourFragment wf = new WeatherHourFragment();
        wf.setArguments(args);

        //put the weatherHourRecyclerView in the weatherHourFrameLayout
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.weather_hour_frame_layout, wf)
                .commit();

    }

    private void setWeatherDayFragment(ArrayList<WeatherDayItem> weatherItemList) {
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.keys_weather_day_list_arg), weatherItemList);

        WeatherDayFragment wf = new WeatherDayFragment();
        wf.setArguments(args);

        //put the weatherDayRecylcerView in the weatherDayFrameLayout
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.weather_day_frame_layout, wf)
                .commit();
    }

    /**
     * Method that will create the top left menu, which seems to be the apps standard for
     * selecting special options
     * Will have different options for selecting weather.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Add menu entries
        MenuItem setWeatherZip = menu.add("Select Weather by Zipcode");
        setWeatherZip.setOnMenuItemClickListener(this::setWeatherByZipcode);

        MenuItem setWeatherMap = menu.add("Select Weather on a Map");
        setWeatherMap.setOnMenuItemClickListener(this::setWeatherByMap);

        MenuItem setWeatherCurrent = menu.add("Select Weather by Current Location");
        setWeatherCurrent.setOnMenuItemClickListener(this::setWeatherByCurrent);

        MenuItem saveLocation = menu.add("Save Location");
        saveLocation.setOnMenuItemClickListener(this::saveLocation);

        MenuItem loadLocation = menu.add("Load Location");
        loadLocation.setOnMenuItemClickListener(this::loadLocation);

        // NOTE: this super call adds the logout button so we don't have to worry about that
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Method that will call setWeather and pass in a zipcode to use
     */
    private boolean setWeatherByZipcode(MenuItem menuItem) {
        Log.i("WEATHER_OPTIONS_SELECT", "zipcode selected");
        return true;
    }

    /**
     * Method that will open a map Activity then the map should call set weather.
     */
    private boolean setWeatherByMap(MenuItem menuItem) {
        Log.i("WEATHER_OPTIONS_SELECT", "map selected");
        return true;
    }

    /**
     * Method that will get the current location then set the weather based on
     * the current location.
     */
    private boolean setWeatherByCurrent(MenuItem menuItem) {
        Log.i("WEATHER_OPTIONS_SELECT", "current selected");
        return true;
    }

    /**
     * Method that will save the current location to the shared preferences.
     * pop up a snack bar saying that the location has been saved.
     */
    private boolean saveLocation(MenuItem menuItem) {
        Log.i("WEATHER_OPTION_SELECT", "save location");
        return true;
    }

    /**
     * Method that will open a list of all the locations saved in the shared preferences.
     * Then when the user clicks on one of the locations saved, then set the weather using
     * that location
     */
    private boolean loadLocation(MenuItem menuItem) {
        Log.i("WEATHER_OPTION_SELECT", "load location");
        return true;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void setLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    //Update UI with location data
                    //...
                    stopLocationUpdates();
                    setWeather(location);

                }
            }
        };
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /*looper */);
        }

    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    /**
     * method that will figure out what to do when the users presses yes or no to
     * allow access to current location
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch(requestCode) {
            case MY_PERMISSION_LOCATIONS: {
                //if reeult is cancelled, the resultArrays are empty/
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //permission was granted
                    //do the location task needed to do
                    //requestLocation();
                    createLocationRequest();
                    setLocationCallback();
                    startLocationUpdates();

                } else {
                    //permission denied
                    //either display error message or do nothing with fragment
                }
                return;
            }
            //other permissions the app might ask for.
        }
    }

    private void setWeather(Location location) {
        if (location != null) {
            setCurrentWeather(location);
            setHourWeather(location);
            setDayWeather(location);

        } else {
            Log.e("WEATHER_ERROR", "location is null");
        }
    }
    

    private void setCurrentWeather(Location location) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather_base))
                .appendPath(getString(R.string.ep_weather_current))
                .appendQueryParameter("lat", Double.toString(location.getLatitude()))
                .appendQueryParameter("lon", Double.toString(location.getLongitude()))
                .build();


        new GetAsyncTask.Builder(uri.toString())
                .onPreExecute(this::handleGetWeatherOnPre)
                .onCancelled(this::handleGetWeatherError)
                .onPostExecute(this::handleGetCurrentWeatherOnPost)
                .build().execute();
    }

    private void setHourWeather(Location location) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather_base))
                .appendPath(getString(R.string.ep_weather_24h))
                .appendQueryParameter("lat", Double.toString(location.getLatitude()))
                .appendQueryParameter("lon", Double.toString(location.getLongitude()))
                .build();


        new GetAsyncTask.Builder(uri.toString())
                .onCancelled(this::handleGetWeatherError)
                .onPostExecute(this::handleGetHourWeatherOnPost)
                .build().execute();
    }

    private void setDayWeather(Location location) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather_base))
                .appendPath(getString(R.string.ep_weather_10d))
                .appendQueryParameter("lat", Double.toString(location.getLatitude()))
                .appendQueryParameter("lon", Double.toString(location.getLongitude()))
                .build();


        new GetAsyncTask.Builder(uri.toString())
                .onCancelled(this::handleGetWeatherError)
                .onPostExecute(this::handleGetDayWeatherOnPost)
                .build().execute();
    }


    private void handleGetWeatherOnPre() {
        mListener.onWaitFragmentInteractionShow();
    }

    private void handleGetWeatherError(String result) {
        Log.e("WEATHER_ASYNC_ERROR", result);
    }

    private void handleGetCurrentWeatherOnPost(String result) {
        try {
            JSONObject weatherResultJSON = new JSONObject(result);
            if (weatherResultJSON.has("data")) {
                //array of weather json objects
                JSONArray weatherDataJSON = weatherResultJSON.getJSONArray("data");
                // object of weather information
                JSONObject weatherInfoJSON = weatherDataJSON.getJSONObject(0);
                //object that has the icon and weather description
                JSONObject weatherJSON = weatherInfoJSON.getJSONObject("weather");

                //get visual displays of the current weather
                TextView cityText = getActivity().findViewById(R.id.weather_city_text);
                TextView conditionText = getActivity().findViewById(R.id.weather_condition_text);
                TextView tempText = getActivity().findViewById(R.id.weather_temp_text);
                ImageView iconView = getActivity().findViewById(R.id.weather_current_icon);

                //populate each view with information of the current weather.
                //populate city text
                String city = weatherInfoJSON.getString("city_name") + ", " +
                        weatherInfoJSON.getString("country_code");
                cityText.setText(city);
                //populate condition view
                conditionText.setText(weatherJSON.getString("description"));
                //populate the temperature view
                String temp = weatherInfoJSON.getDouble("temp") + "°";
                tempText.setText(temp);
                //populate the icon
                int resId = getResources().getIdentifier(weatherJSON.getString("icon")
                        , "drawable", getActivity().getPackageName());
                iconView.setImageResource(resId);


            } else {
                Log.e("WEATHER_JSON_ERROR", "Weather JSON result does not have data field");
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void handleGetHourWeatherOnPost(String result) {
        try {
            JSONObject weatherResultJSON = new JSONObject(result);
            //get weatherDataJSON array
            JSONArray weatherDataJSON = weatherResultJSON.getJSONArray("data");
            //initialize list of weather items for the recycler view
            ArrayList<WeatherHourItem> weatherList = new ArrayList<WeatherHourItem>();
            //date formats for getting the hour on the time stamp.
            SimpleDateFormat timeStampFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            SimpleDateFormat timeHourFormat = new SimpleDateFormat("h a", Locale.US);

            for (int i = 0; i < weatherDataJSON.length(); i++) {
                JSONObject weatherInfoJSON = weatherDataJSON.getJSONObject(i);
                JSONObject weatherJSON = weatherInfoJSON.getJSONObject("weather");

                //formatting time stamp for display
                Date timeStamp = timeStampFormat.parse(weatherInfoJSON.getString("timestamp_local"));
                String hourTime = timeHourFormat.format(timeStamp);

                //temp
                double temp = weatherInfoJSON.getDouble("temp");

                //icon
                String icon = weatherJSON.getString("icon");

                WeatherHourItem weatherItem = new WeatherHourItem(Integer.toString(i+1), hourTime, temp, icon);
                weatherList.add(weatherItem);
            }
            //create the weather hour fragment with the list of WeatherHourItems
            setWeatherHourFragment(weatherList);



        } catch(JSONException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void handleGetDayWeatherOnPost(String result) {
        try {
            JSONObject weatherResultJSON = new JSONObject(result);
            if (weatherResultJSON.has("data")) {
                JSONArray weatherDataJSON = weatherResultJSON.getJSONArray("data");

                SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                SimpleDateFormat outputDateFormat = new SimpleDateFormat("EEEE", Locale.US);

                ArrayList<WeatherDayItem> weatherDayList = new ArrayList<WeatherDayItem>();
                for (int i = 0; i < weatherDataJSON.length(); i++) {
                    JSONObject weatherInfoJSON = weatherDataJSON.getJSONObject(i);
                    JSONObject weatherJSON = weatherInfoJSON.getJSONObject("weather");

                    //day
                    Date date = inputDateFormat.parse(weatherInfoJSON.getString("datetime"));
                    String day = outputDateFormat.format(date);
                    //icon
                    String icon = weatherJSON.getString("icon");
                    //high temp
                    double highTemp = weatherInfoJSON.getDouble("max_temp");
                    //low temp
                    double lowTemp = weatherInfoJSON.getDouble("min_temp");

                    WeatherDayItem weatherDayItem = new WeatherDayItem(Integer.toString(i+1),
                            day, icon, highTemp, lowTemp);

                    weatherDayList.add(weatherDayItem);
                }
                setWeatherDayFragment(weatherDayList);


            } else {
                Log.e("WEATHER_ERROR", "resultJSON doesnt have data object");
            }
        } catch(JSONException | ParseException e) {
            e.printStackTrace();
        }

        mListener.onWaitFragmentInteractionHide();
    }


    /**
     * Interface just useful for displaying the wait fragment.
     */
    public interface OnFragmentInteractionListener extends WaitFragment.OnFragmentInteractionListener {}
}
