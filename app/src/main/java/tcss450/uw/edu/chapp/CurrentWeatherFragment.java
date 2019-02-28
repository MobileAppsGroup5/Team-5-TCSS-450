package tcss450.uw.edu.chapp;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.chapp.utils.GetAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 * If there is an error, such as locations not being alowed.
 * Then make all text invisible and make the error text field visible.
 */
public class CurrentWeatherFragment extends Fragment {

    /*
     * desired interval for location updates, inexact. updates may be more or less frequent
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /*
     * fastest rate for active location updates. exact. updates will never be more frequent than
     * this interval
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS/2;

    private OnCurrentWeatherFragmentInteractionListener mListener;

    private static final int MY_PERMISSION_LOCATIONS = 8414;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    private TextView mCurrentCityText;
    private TextView mCurrentConditionText;
    private TextView mCurrentTempText;
    private TextView mCurrentErrorText;
    private ImageView mCurrentIcon;

    public CurrentWeatherFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_current_weather, container, false);

        //get layout texts and icon.
        mCurrentCityText = v.findViewById(R.id.weather_current_city_text);
        mCurrentConditionText = v.findViewById(R.id.weather_current_condition_text);
        mCurrentTempText = v.findViewById(R.id.weather_current_temp_text);
        mCurrentErrorText = v.findViewById(R.id.weather_current_error_text);
        mCurrentIcon = v.findViewById(R.id.weather_current_icon);

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
            requestLocation();
        }

//        mLocationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                if (locationResult == null) {
//                    return;
//                }
//                for (Location location : locationResult.getLocations()) {
//                    //Update UI with location data
//                    //...
//                    //setLocation(location);
//                    Log.d("WEATHER LOCATION UPDATE!", location.toString());
//                }
//            }
//        };
//
//        createLocationRequest();
//
//        //if the currentLocation is not null
//        if (mCurrentLocation != null) {
//            setWeather();
//        }




        //get the current location
        //call GetAsyncTask to call the weather endpoint for current weather,
        //pass in current location lat and lon
        //display weather received from endpoint
        //display icon based on weather code.

        return v;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        startLocationUpdates();
//    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        stopLocationUpdates();
//    }

    private void startLocationUpdates() {
        Log.wtf("WEATHER", "start location updates");
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.wtf("WEATHER", "start update permission granted");
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /*looper */);
        }
        Log.wtf("WEATHER start location update", Boolean.toString(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED));
        Log.wtf("WEATHER fine location", Integer.toString(
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)));
    }

    private void stopLocationUpdates() {
        Log.wtf("WEATHER", "stop location updates");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
                    requestLocation();

                } else {
                    //permission denied
                    //either display error message or do nothing with fragment.
                    mCurrentErrorText.setVisibility(TextView.VISIBLE);
                    mCurrentErrorText.setText(getString(R.string.current_weather_no_location_permission_error));
                }
                return;
            }
            //other permissions the app might ask for.
        }
    }

    /**
     * helper method that gets device location from the api
     */
    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //user did not allow permission for location
        } else {
            //user allowed permission for location
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            //got last known location
                            if (location != null) {
                                Log.wtf("WEATHER", "onSuccess: " + location.toString());
                                mCurrentLocation = location;
                                setWeather(location);
                            }
                        }
                    });

        }
    }

    /**
     * helper method that will set the current location.
     * @param location
     */
    private void setLocation(final Location location) {
        mCurrentLocation = location;
    }

    /**
     * Helper method that will set the weather by calling
     * weather endpoint with a GetAsyncTask
     */
    private void setWeather(Location location) {
        //build uri to the weather backend
        if (mCurrentLocation != null) {
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
                    .onPostExecute(this::handleGetWeatherOnPost)
                    .build().execute();
        }
        else {
            Log.e("WEATHER", "Can't get weather, mCurrentLocation is null");
        }

    }

    /**
     * Helper method to set the icon based on the icon string
     * @param icon the icon string
     */
    private void setIcon(String icon) {
        int resId = getResources().getIdentifier(icon, "drawable", getActivity().getPackageName());
        mCurrentIcon.setImageResource(resId);

    }

    private void setCity(String city) {
        mCurrentCityText.setText(city);
    }

    private void setCondition(String condition) {
        mCurrentConditionText.setText(condition);
    }

    private void setTemperature(double temp) {
        String tempString = temp + "°";
        mCurrentTempText.setText(tempString);
    }


    private void handleGetWeatherOnPre() {
        mListener.onWaitFragmentInteractionShow();
    }

    private void handleGetWeatherError(String error) {
        Log.e("WEATHER", "error with getting weather from backend: " + error);
    }

    private void handleGetWeatherOnPost(String result) {
        try {
            JSONObject resultJSON = new JSONObject(result);

            if (resultJSON.has("data")) {
                JSONArray weatherData = resultJSON.getJSONArray("data");
                if (resultJSON.has("count")) {
                    int count = resultJSON.getInt("count");
                    //current weather only ever has on JSONObject in the data array
                    JSONObject weatherInfo = weatherData.getJSONObject(0);
                    JSONObject weather = weatherInfo.getJSONObject("weather");


                    setCity(weatherInfo.getString("city_name"));
                    setCondition(weather.getString("description"));
                    setTemperature(weatherInfo.getDouble("temp"));
                    setIcon(weather.getString("icon"));

                    mListener.onWaitFragmentInteractionHide();

                } else {
                    Log.e("WEATHER", "weather result does not have count");
                }
            } else {
                Log.e("WEATHER", "weather result does not have weather data");
            }
        } catch(JSONException e) {
            Log.e("WEATHER", e.toString());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CurrentWeatherFragment.OnCurrentWeatherFragmentInteractionListener) {
            mListener = (CurrentWeatherFragment.OnCurrentWeatherFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCurrentWeatherFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnCurrentWeatherFragmentInteractionListener extends
            WaitFragment.OnFragmentInteractionListener {

    }

}
