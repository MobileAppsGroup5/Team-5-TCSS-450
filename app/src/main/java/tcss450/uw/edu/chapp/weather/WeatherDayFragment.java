package tcss450.uw.edu.chapp.weather;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import tcss450.uw.edu.chapp.R;
import tcss450.uw.edu.chapp.WaitFragment;
import tcss450.uw.edu.chapp.weather.WeatherDayContent.WeatherDayItem;

/**
 * WeatherDayFragment which can take a list of WeatherDayItems and then set the recycler view to
 * the list.
 */
public class WeatherDayFragment extends Fragment {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WeatherDayFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weatherday_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            if (getArguments() != null) {
                Bundle args = getArguments();
                ArrayList<WeatherDayItem> weatherDayItems = (ArrayList<WeatherDayItem>)
                        args.getSerializable(getString(R.string.keys_weather_day_list_arg));
                recyclerView.setAdapter(new MyWeatherDayRecyclerViewAdapter(weatherDayItems));
            } else {
                recyclerView.setAdapter(new MyWeatherDayRecyclerViewAdapter(WeatherDayContent.ITEMS));
            }

        }
        return view;
    }

}
