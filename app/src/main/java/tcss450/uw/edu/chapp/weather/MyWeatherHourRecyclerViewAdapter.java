package tcss450.uw.edu.chapp.weather;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import tcss450.uw.edu.chapp.weather.WeatherHourContent.WeatherHourItem;


import java.util.List;

import tcss450.uw.edu.chapp.R;

public class MyWeatherHourRecyclerViewAdapter extends RecyclerView.Adapter<MyWeatherHourRecyclerViewAdapter.ViewHolder> {

    private final List<WeatherHourItem> mValues;
    private Context mContext;

    public MyWeatherHourRecyclerViewAdapter(List<WeatherHourItem> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_weatherhour, parent, false);
        mContext = view.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mWeatherTimeText.setText(mValues.get(position).time);

        String temp = mValues.get(position).temp + "°";
        holder.mWeatherTempText.setText(temp);

        int resId = mContext.getResources().getIdentifier(mValues.get(position).icon, "drawable", mContext.getPackageName());
        holder.mWeatherIcon.setImageResource(resId);

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;

        public final TextView mWeatherTimeText;
        public final TextView mWeatherTempText;
        public final ImageView mWeatherIcon;
        public WeatherHourItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mWeatherTimeText = (TextView) view.findViewById(R.id.weather_hour_time);
            mWeatherTempText = (TextView) view.findViewById(R.id.weather_hour_temp);
            mWeatherIcon = (ImageView) view.findViewById(R.id.weather_hour_icon);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mWeatherTimeText.getText() + "'";
        }
    }
}
