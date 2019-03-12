package tcss450.uw.edu.chapp.weather;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import tcss450.uw.edu.chapp.R;
import tcss450.uw.edu.chapp.weather.WeatherDayContent.WeatherDayItem;

import java.util.List;

/**
 * class which will take a list of weather day items and display them in a list view.
 *
 * @author Mike Osborne, Trung Thai, Michael Josten, Jessica Medrzycki
 */
public class MyWeatherDayRecyclerViewAdapter extends RecyclerView.Adapter<MyWeatherDayRecyclerViewAdapter.ViewHolder> {

    private final List<WeatherDayItem> mValues;
    private Context mContext;

    public MyWeatherDayRecyclerViewAdapter(List<WeatherDayItem> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_weatherday, parent, false);

        mContext = view.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        //set day text
        holder.mDayView.setText(mValues.get(position).day);
        //set icon
        int resId = mContext.getResources().getIdentifier(mValues.get(position).icon, "drawable", mContext.getPackageName());
        holder.mIconView.setImageResource(resId);
        //set high temp
        String highT = mValues.get(position).highTemp + "°";
        holder.mHighTemp.setText(highT);
        //set low temp
        String lowT = mValues.get(position).lowTemp + "°";
        holder.mLowTemp.setText(lowT);


    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    /**
     * class that holds the text view of the recycler view.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mDayView;
        public final ImageView mIconView;
        public final TextView mHighTemp;
        public final TextView mLowTemp;

        public WeatherDayItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDayView = view.findViewById(R.id.weather_day_text);
            mIconView = view.findViewById(R.id.weather_day_icon);
            mHighTemp = view.findViewById(R.id.weather_day_high_temp_text);
            mLowTemp = view.findViewById(R.id.weather_day_low_temp_text);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mDayView.getText() + "'";
        }
    }
}
