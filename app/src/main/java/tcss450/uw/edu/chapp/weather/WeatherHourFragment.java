package tcss450.uw.edu.chapp.weather;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tcss450.uw.edu.chapp.R;
import tcss450.uw.edu.chapp.weather.WeatherHourContent.WeatherHourItem;


public class WeatherHourFragment extends Fragment {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WeatherHourFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weatherhour_list, container, false);


        // Set the adapter
        if (view instanceof RecyclerView) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext(),
                    LinearLayoutManager.HORIZONTAL, false);

            RecyclerView recyclerView = (RecyclerView) view;

            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(new MyWeatherHourRecyclerViewAdapter(WeatherHourContent.ITEMS));

        }
        return view;
    }
}
