package tcss450.uw.edu.chapp;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import tcss450.uw.edu.chapp.AllConnectionsFragment.OnListFragmentInteractionListener;
import tcss450.uw.edu.chapp.connections.Connection;
import tcss450.uw.edu.chapp.model.Credentials;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Contact} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyAllConnectionsRecyclerViewAdapter extends RecyclerView.Adapter<MyAllConnectionsRecyclerViewAdapter.ViewHolder> {

    // used for the ViewHolder's viewType
    // connections sent to us from another person AND NOT VERIFIED YET
    public static final int RECIEVED_NOT_VERIFIED = 2;

    // used for the ViewHolder's viewType
    // verified connections sent to us
    public static final int RECIEVED_VERIFIED = 1;

    // used for the ViewHolder's viewType
    // sent from us
    public static final int SENT = 0;

    private final List<Connection> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Credentials mCredentials;

    public MyAllConnectionsRecyclerViewAdapter(List<Connection> items, OnListFragmentInteractionListener listener, Credentials credentials) {
        mValues = items;
        mListener = listener;
        mCredentials = credentials;
    }

    @Override
    public int getItemViewType(int position) {
        // Return 0 if sent from us
        if (mCredentials.getUsername().equals(mValues.get(position).getUsernameA())) {
            return SENT;
        }

        // 1 for verified connections sent to us
        if (mCredentials.getUsername().equals(mValues.get(position).getUsernameB())
                && mValues.get(position).getVerified() == 1) {
            return RECIEVED_VERIFIED;
        }

        // and 2 if sent to us from another person AND NOT VERIFIED YET
        if (mCredentials.getUsername().equals(mValues.get(position).getUsernameB())
                && mValues.get(position).getVerified() == 0) {
            return RECIEVED_NOT_VERIFIED;
        }

        // shouldn't happen, this means db is messed up and there are bigger problems!
        Log.e("MyAllConnectionsRecyclerViewAdapter", "INVALID VIEW TYPE");
        return SENT;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == SENT) {
            // sent from us
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_all_connections_outgoing, parent, false);
        } else {
            // sent to us
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_all_connections_incoming, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        // TODO: SET CLICK LISTENERS HERE FOR THE IMAGES
        if (holder.getItemViewType() == SENT) {
            // display the username we sent to
            holder.mUsernameView.setText(mValues.get(position).getUsernameB());
        } else {
            // display the username that sent the connection to us
            holder.mUsernameView.setText(mValues.get(position).getUsernameA());
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
        public final TextView mUsernameView;
        public final ImageView mAcceptButton;
        public final ImageView mDeclineCancelButton;
        public Connection mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
          //  mIdView = (TextView) view.findViewById(R.id.item_number);
            mUsernameView = (TextView) view.findViewById(R.id.list_item_connection_name);
            mDeclineCancelButton = (ImageView) view.findViewById(R.id.image_cancel_contact);

            // NOTE accept button can be missing if our viewID SENT or RECIEVED_VERIFIED
            mAcceptButton = (ImageView) view.findViewById(R.id.image_accept_contact);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mUsernameView.getText() + "'";
        }
    }
}
