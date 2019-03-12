package tcss450.uw.edu.chapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tcss450.uw.edu.chapp.MessagingFragment.OnListFragmentInteractionListener;
import tcss450.uw.edu.chapp.chat.Message;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Message}.
 */
public class MyMessagingRecyclerViewAdapter extends RecyclerView.Adapter<MyMessagingRecyclerViewAdapter.ViewHolder> {

    private final List<Message> mValues;
    private final OnListFragmentInteractionListener mListener;
    private String mUsername;

    public MyMessagingRecyclerViewAdapter(List<Message> items, OnListFragmentInteractionListener listener, String username) {
        mValues = items;
        mListener = listener;
        mUsername = username;
    }

    @Override
    public int getItemViewType(int position) {
        // Return 0 if sent from us, return 1 if from another person
        return mUsername.equals(mValues.get(position).getUsername()) ? 0 : 1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            // sent from us
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_message_outgoing, parent, false);
        } else {
            // sent from them
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_message_incoming, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mMessageView.setText(mValues.get(position).getMessage());
        holder.mSenderView.setText(mValues.get(position).getUsername());
        holder.mTimeStampView.setText(mValues.get(position).getTimestamp());

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
        public final TextView mMessageView;
        public final TextView mSenderView;
        public final TextView mTimeStampView;
        public Message mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mMessageView = view.findViewById(R.id.text_view_message);
            mSenderView = view.findViewById(R.id.text_view_sender);
            mTimeStampView = view.findViewById(R.id.text_view_timestamp);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mMessageView.getText() + "'";
        }
    }
}
