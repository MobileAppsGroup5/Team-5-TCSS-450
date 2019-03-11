package tcss450.uw.edu.chapp.weather;

import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tcss450.uw.edu.chapp.R;
import tcss450.uw.edu.chapp.weather.WeatherLocationFragment.OnListFragmentInteractionListener;
import tcss450.uw.edu.chapp.weather.WeatherLocationContent.WeatherLocationItem;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * {@link RecyclerView.Adapter} that can display a {@link WeatherLocationItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyWeatherLocationRecyclerViewAdapter extends RecyclerView.Adapter<MyWeatherLocationRecyclerViewAdapter.ViewHolder> {

    private final List<WeatherLocationItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final WeatherLocationFragment mLocationFragment;

    public MyWeatherLocationRecyclerViewAdapter(List<WeatherLocationItem> items, OnListFragmentInteractionListener listener,
                                                WeatherLocationFragment locationFragment) {
        mValues = items;
        mListener = listener;
        mLocationFragment = locationFragment;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_weatherlocation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mCityView.setText(mValues.get(position).city);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    /*
                    * delete, cancel, or ok. then if user presses delete, then remove the locaiton
                    * from the shared preferences and update the list view.
                    * if user presses accept then call the onListFragmentInteraction,
                    * and let the main WeatherFragment set the weather equal to the weatherLocationItem.
                    */
                    mLocationFragment.makeAlertDialogOnItemClick(mListener, position, holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mCityView;
        public WeatherLocationItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mCityView = (TextView) view.findViewById(R.id.weather_location_city_text);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mCityView.getText() + "'";
        }
    }
}
