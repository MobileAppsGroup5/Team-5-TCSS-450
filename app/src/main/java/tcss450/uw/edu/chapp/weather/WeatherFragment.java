package tcss450.uw.edu.chapp.weather;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import tcss450.uw.edu.chapp.MapsActivity;
import tcss450.uw.edu.chapp.R;
import tcss450.uw.edu.chapp.WaitFragment;
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.GetAsyncTask;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;
import tcss450.uw.edu.chapp.weather.WeatherHourContent.WeatherHourItem;
import tcss450.uw.edu.chapp.weather.WeatherDayContent.WeatherDayItem;


/**
 * Class that holds the functionality for the weather of the app.
 * will set the weather to the current device location on create and
 * then the user will be able to select different modes of weather selection,
 * such as entering a zipcode, choosing a place on the map, and saving and loading a
 * previous location
 *
 * @author Mike Osborne, Trung Thai, Michael Josten, Jessica Medrzycki
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

    private Location mCurrentLocation = null;
    private String mZipcode = "";
    private String mCurrentCity = "";
    private Location mMapLocation = null;
    private Location mMapStartLocation = null;
    private Location mLoadLocation = null;
    private Credentials mCreds;
    private String mJwt;

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

        mCreds = (Credentials) getArguments().getSerializable(getString(R.string.key_credentials));
        mJwt = getArguments().getString(getString(R.string.keys_intent_jwt));

        //default to current device location to display weather upon initial creation
        //ask for permission for location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        //get location from map
        Bundle args = getArguments();
        mMapLocation = args.getParcelable(getString(R.string.keys_weather_location_from_map));
        mLoadLocation = args.getParcelable(getString(R.string.keys_weather_location_load));
        mZipcode = args.getInt(getString(R.string.keys_weather_location_zip)) + "";
        Log.wtf("WEATHER", mZipcode);
        if (mZipcode.equals("0"))
            mZipcode = "";

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_LOCATIONS);

        } else { //user already allowed locations
            //requestLocation();
            createLocationRequest();
            setLocationCallback();
            //startLocationUpdates();
        }

        return v;
    }

    /**
     * onStart call back method in the fragment lifecycle,
     * if map location exists then set the weather to that location,
     * otherwise wait a cycle and set the weather to the current device location
     * In onstart because back pressing on map would mess things up, should work now.
     */
    @Override
    public void onStart() {
        super.onStart();
        Log.wtf("WEATHER ZIP", mZipcode);
        if (mLoadLocation != null && (mLoadLocation.getLatitude() != 0 && mLoadLocation.getLongitude() != 0)) {
                setWeather(mLoadLocation);
        } else if (!mZipcode.isEmpty()) {
            Log.wtf("WEATHER ZIP IS NOT EMPTY", mZipcode);
           setWeather(mZipcode);
        } else if (mMapLocation != null) {
            //map location exists
            setWeather(mMapLocation);
        } else {
            //map location does not exist
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                //have permission to use location services
                startLocationUpdates();
            }
        }


    }

    /**
     * method that will create a fragment that will display the weather hour item list
     * @param weatherItemList
     */
    private void setWeatherHourFragment(ArrayList<WeatherHourItem> weatherItemList) {
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.keys_weather_hour_list_arg), weatherItemList);

        WeatherHourFragment wf = new WeatherHourFragment();
        wf.setArguments(args);

        //put the weatherHourRecyclerView in the weatherHourFrameLayout
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.weather_hour_frame_layout, wf, getString(R.string.weather_hour_fragment_tag))
                .commit();

    }

    /**
     * method that will set the weather day fragment with the list of weather items passed.
     * @param weatherItemList list of weather items passed which each weather item
     *                        represents a day.
     */
    private void setWeatherDayFragment(ArrayList<WeatherDayItem> weatherItemList) {
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.keys_weather_day_list_arg), weatherItemList);

        WeatherDayFragment wf = new WeatherDayFragment();
        wf.setArguments(args);

        //put the weatherDayRecylcerView in the weatherDayFrameLayout
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.weather_day_frame_layout, wf, getString(R.string.weather_day_fragment_tag))
                .commit();
    }

    /**
     * helper method that will set the current weather fragment visible or invisible
     * based on the boolean passed.
     * @param visible boolean if current weather fragment is visible
     */
    private void setWeatherCurrentVisible(boolean visible) {
        TextView cityText = getActivity().findViewById(R.id.weather_city_text);
        TextView condText = getActivity().findViewById(R.id.weather_condition_text);
        TextView tempText = getActivity().findViewById(R.id.weather_temp_text);
        ImageView iconView = getActivity().findViewById(R.id.weather_current_icon);

        if (visible) {
            cityText.setVisibility(View.VISIBLE);
            condText.setVisibility(View.VISIBLE);
            tempText.setVisibility(View.VISIBLE);
            iconView.setVisibility(View.VISIBLE);
        } else {
            cityText.setVisibility(View.INVISIBLE);
            condText.setVisibility(View.INVISIBLE);
            tempText.setVisibility(View.INVISIBLE);
            iconView.setVisibility(View.INVISIBLE);
        }
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

        //make a dialog with a edittext
        final EditText editText = new EditText(getActivity());

        //make alert for the user to enter a zipcode
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Enter a Zipcode")
                .setView(editText)
                .setPositiveButton("Enter", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button pButton = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                pButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String zipcode = editText.getText().toString();
                        //check if zipcode is valid.
                        if (zipcode.isEmpty()) {
                            editText.setError(getString(R.string.weather_dialog_postal_code_error));
                        } else {
                            mZipcode = zipcode;
                            setWeather(zipcode);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        dialog.show();
        return true;
    }

    /**
     * Method that will open a map Activity then the map should call set weather.
     */
    private boolean setWeatherByMap(MenuItem menuItem) {
        Log.i("WEATHER_OPTIONS_SELECT", "map selected");
        new AlertDialog.Builder(getActivity())
                .setTitle("Open Map?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Ok", (i, d) -> {
                    setWeatherCurrentVisible(false);
                    Fragment hourFrag = getChildFragmentManager().findFragmentByTag(getString(R.string.weather_hour_fragment_tag));
                    Fragment dayFrag = getChildFragmentManager().findFragmentByTag(getString(R.string.weather_day_fragment_tag));
                    if (hourFrag != null) {
                        getChildFragmentManager()
                                .beginTransaction()
                                .remove(hourFrag)
                                .commit();
                    }
                    if (dayFrag != null) {
                        getChildFragmentManager()
                                .beginTransaction()
                                .remove(dayFrag)
                                .commit();
                    }


                    Intent intent = new Intent(getActivity(), MapsActivity.class);
                    //pass current location to the map activity
                    //Log.e("WEATHER_DEBUG", mCurrentLocation.toString());
                    intent.putExtra(getString(R.string.keys_weather_map_intent_location), mMapStartLocation);
                    Bundle args = getArguments();
                    if (args != null) {
                        intent.putExtras(args);
                    }
                    startActivity(intent);
                })
                .show();



        return true;
    }

    /**
     * Method that will get the current location then set the weather based on
     * the current location.
     */
    private boolean setWeatherByCurrent(MenuItem menuItem) {
        Log.i("WEATHER_OPTIONS_SELECT", "current selected");
        startLocationUpdates();
        return true;
    }

    /**
     * Method that will save the current location to the shared preferences.
     * pop up a snack bar saying that the location has been saved.
     */
    private boolean saveLocation(MenuItem menuItem) {
        Log.i("WEATHER_OPTION_SELECT", "save location");
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather_base))
                .appendPath(getString(R.string.ep_weather_save))
                .build();

        try {
            JSONObject saveJSON = new JSONObject();
            saveJSON.put("username", mCreds.getUsername());
            saveJSON.put("city", mCurrentCity);
            if (!mZipcode.isEmpty()) {
                saveJSON.put("zip", mZipcode);
            } else if (mCurrentLocation != null) {
                saveJSON.put("lat", mCurrentLocation.getLatitude());
                saveJSON.put("lon", mCurrentLocation.getLongitude());
            }
            new SendPostAsyncTask.Builder(uri.toString(), saveJSON)
                    .onPostExecute((result) -> {
                        try {
                            JSONObject resultJSON = new JSONObject(result);
                            View parentView = getActivity().findViewById(R.id.weather_parent_layout);
                            if (resultJSON.getBoolean("success")) {
                                Snackbar.make(parentView, "Saved Location " + mCurrentCity + "!", Snackbar.LENGTH_SHORT)
                                        .show();
                            } else {
                                Snackbar.make(parentView, "Location Already Saved", Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        } catch(JSONException e) {
                            e.printStackTrace();
                        }

                    })
                    .build()
                    .execute();

        } catch(JSONException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Method that will open a list of all the locations saved in the shared preferences.
     * Then when the user clicks on one of the locations saved, then set the weather using
     * that location
     */
    private boolean loadLocation(MenuItem menuItem) {
        Log.i("WEATHER_OPTION_SELECT", "load location");
        mListener.onLoadWeatherClicked();
        return true;
    }

    /**
     * method that will set the fragment interaction listener to the
     * the activity that this fragment is being attached to.
     * @param context
     */
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

    /**
     * method that will set the interface listener to null
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * method that will specify the locationRequest
     */
    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * method that sets the location callback,
     * call back will stop the updates and set the weather based on the current device location
     */
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

    /**
     * method that will start location updates.
     */
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
                    //startLocationUpdates();

                } else {
                    //permission denied
                    //either display error message or do nothing with fragment
                }
                return;
            }
            //other permissions the app might ask for.
        }
    }

    /**
     * method that will call other set weather methods with a location
     * @param location of the weather
     */
    private void setWeather(Location location) {
        if (location != null) {
            mCurrentLocation = location;
            mZipcode = "";
            setWeatherCurrentVisible(true);
            setCurrentWeather(location);
            setHourWeather(location);
            setDayWeather(location);

        } else {
            Log.e("WEATHER_ERROR", "location is null");
        }
    }

    /**
     * Method that will call the other set weather methods with the zipcode
     * @param zipcode of the weather location
     */
    private void setWeather(String zipcode) {
        if (!zipcode.isEmpty()) {
            mZipcode = zipcode;
            mCurrentLocation = null;
            setWeatherCurrentVisible(true);
            setCurrentWeather(zipcode);
            setHourWeather(zipcode);
            setDayWeather(zipcode);
        } else {
            Log.e("WEATHER_ERROR", "zipcode is empty");
        }
    }

    /**
     * Method that will build a uri to the backend that will take the
     * lat and lon of a location and create a async task to handle the results
     * @param location location of the weather
     */
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

    /**
     * Method that will create a uri using a zipcode to get weather from the webservice.
     * then will build async task to handle weather.
     * @param zipcode of the weather
     */
    private void setCurrentWeather(String zipcode) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather_base))
                .appendPath(getString(R.string.ep_weather_current))
                .appendQueryParameter("postal_code", zipcode)
                .build();

        new GetAsyncTask.Builder(uri.toString())
                .onPreExecute(this::handleGetWeatherOnPre)
                .onCancelled(this::handleGetWeatherError)
                .onPostExecute(this::handleGetCurrentWeatherOnPost)
                .build().execute();
    }

    /**
     * Method that will set the hourWeather based on the location
     * by calling the webservice and making a get request using the location
     * @param location is the location of the weather
     */
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

    /**
     * method that will set the hour weather based on the zipcode
     * @param zipcode of the weather
     */
    private void setHourWeather(String zipcode) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather_base))
                .appendPath(getString(R.string.ep_weather_24h))
                .appendQueryParameter("postal_code", zipcode)
                .build();


        new GetAsyncTask.Builder(uri.toString())
                .onCancelled(this::handleGetWeatherError)
                .onPostExecute(this::handleGetHourWeatherOnPost)
                .build().execute();
    }

    /**
     * set the day weather based on the location passed
     * by calling a webservice and making a get request with the location
     * @param location is the location of the weather
     */
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

    /**
     * Method that will set the day weather based on the zipcode
     * @param zipcode of the weather
     */
    private void setDayWeather(String zipcode) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather_base))
                .appendPath(getString(R.string.ep_weather_10d))
                .appendQueryParameter("postal_code", zipcode)
                .build();


        new GetAsyncTask.Builder(uri.toString())
                .onCancelled(this::handleGetWeatherError)
                .onPostExecute(this::handleGetDayWeatherOnPost)
                .build().execute();
    }


    /**
     * method that will show the wait fragment
     */
    private void handleGetWeatherOnPre() {
        mListener.onWaitFragmentInteractionShow();
    }

    /**
     * method that logs an error in the async tasks
     * @param result error message
     */
    private void handleGetWeatherError(String result) {
        Log.e("WEATHER_ASYNC_ERROR", result);
    }

    /**
     * Method that will set the views of the fragment to the result of the
     * get request of the current weather.
     * @param result is a JSON result from the webservice
     */
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
                mCurrentCity = city;
                mMapStartLocation = new Location("");
                mMapStartLocation.setLatitude(weatherInfoJSON.getDouble("lat"));
                mMapStartLocation.setLongitude(weatherInfoJSON.getDouble("lon"));




            } else {
                Log.e("WEATHER_JSON_ERROR", "Weather JSON result does not have data field");
            }



        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("WEATHER_GET_ERROR", "error with get current on post");
            //display dialog which informs user
            new AlertDialog.Builder(getActivity())
                    .setTitle("Error with Getting Weather")
                    .setPositiveButton("Ok", null)
                    .create()
                    .show();
        }


    }


    /**
     * Method that will set the views of the fragment to the result from the get hour weather
     * request
     * @param result is a JSON result from the webservice
     */
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

    /**
     * Method that set the views of the fragment to the result of the get day weather request
     * @param result JSON result of the get request from the webservice.
     */
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
     * Interface to communicate with parent activity
     */
    public interface OnFragmentInteractionListener extends WaitFragment.OnFragmentInteractionListener {
        void onLoadWeatherClicked();
    }
}
