package tcss450.uw.edu.phishapp;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tcss450.uw.edu.phishapp.SetListFragment.OnListFragmentInteractionListener;
import tcss450.uw.edu.phishapp.setlist.SetList;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link tcss450.uw.edu.phishapp.setlist.SetList} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MySetListRecyclerViewAdapter extends RecyclerView.Adapter<MySetListRecyclerViewAdapter.ViewHolder> {

    private final List<SetList> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MySetListRecyclerViewAdapter(List<SetList> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_setlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mLongDateView.setText(mValues.get(position).getLongDate());
        holder.mLocationView.setText(mValues.get(position).getLocation());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.mVenueView.setText(Html.fromHtml(mValues.get(position).getVenue(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.mVenueView.setText(Html.fromHtml(mValues.get(position).getVenue()));
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
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
        public final TextView mLongDateView;
        public final TextView mLocationView;
        public final TextView mVenueView;
        public SetList mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mLongDateView = view.findViewById(R.id.text_view_long_date);
            mLocationView = view.findViewById(R.id.text_view_location);
            mVenueView = view.findViewById(R.id.text_view_venue);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mLongDateView.getText() + "'";
        }
    }
}
