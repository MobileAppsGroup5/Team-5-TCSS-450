package tcss450.uw.edu.chapp.weather;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import tcss450.uw.edu.chapp.R;
import tcss450.uw.edu.chapp.WaitFragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WeatherFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class WeatherFragment extends Fragment {

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

        //put the weatherHourRecyclerView in the weatherHourFrameLayout
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.weather_hour_frame_layout, new WeatherHourFragment())
                .commit();


        return v;
    }

    /**
     * Method that will create the top left menu, which seems to be the apps standard for
     * selecting special options
     * Will have different options for selecting weather.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Add menu entries
        MenuItem setWeatherZip = menu.add("Select Weather by zipcode");
        setWeatherZip.setOnMenuItemClickListener(this::setWeatherByZipcode);

        MenuItem setWeatherMap = menu.add("Select Weather on a Map");
        setWeatherMap.setOnMenuItemClickListener(this::setWeatherByMap);

        MenuItem setWeatherCurrent = menu.add("Select Weather by current location");
        setWeatherCurrent.setOnMenuItemClickListener(this::setWeatherByCurrent);

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


    /**
     * Interface just useful for displaying the wait fragment.
     */
    public interface OnFragmentInteractionListener extends WaitFragment.OnFragmentInteractionListener {}
}
