package tcss450.uw.edu.chapp;

import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.chapp.connections.Connection;
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Connection}
 */
public class MyConnectionsRecyclerViewAdapter extends RecyclerView.Adapter<MyConnectionsRecyclerViewAdapter.ViewHolder> {

    // used for the ViewHolder's viewType
    // connections sent to us from another person AND NOT VERIFIED YET
    public static final int RECIEVED_NOT_VERIFIED = 2;

    // used for the ViewHolder's viewType
    // verified connections sent to us
    public static final int RECIEVED_VERIFIED = 1;

    // used for the ViewHolder's viewType
    // sent from us
    public static final int SENT = 0;

    // Property change support for firing events when users click on the fragments. Allows
    // other fragments to easily listen for events and update accordingly. (For example, call
    // to a web service and refresh our data)
    private PropertyChangeSupport myPcs;

    private List<Connection> mValues;
    private Credentials mCredentials;
    private Context mContext;
    private String mJwToken;
    private boolean mCompactMode;

    public MyConnectionsRecyclerViewAdapter(List<Connection> items, Credentials credentials,
                                            String jwToken, Context context, boolean compactMode) {
        Collections.reverse(items);
        mValues = items;
        mCredentials = credentials;
        mContext = context;
        mJwToken = jwToken;
        myPcs = new PropertyChangeSupport(this);
        mCompactMode = compactMode;
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
        Log.e("MyConnectionsRecyclerViewAdapter", "INVALID VIEW TYPE");
        return SENT;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_empty, parent, false);
        // if in compact mode, only show requests
        if (mCompactMode) {
            if (viewType == RECIEVED_NOT_VERIFIED) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_connections_incoming, parent, false);
            }
        } else {
            // display everything
            if (viewType == SENT || viewType == RECIEVED_VERIFIED) {
                // sent from us
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_connections_outgoing, parent, false);
            } else {
                // sent to us
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_connections_incoming, parent, false);
            }
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        if (mCompactMode) {
            if (holder.getItemViewType() == RECIEVED_NOT_VERIFIED) {
                holder.mUsernameView.setText(mValues.get(position).getUsernameA());
                holder.mView.findViewById(R.id.image_accept_contact).setOnClickListener(this::handleAcceptContact);
                holder.mView.findViewById(R.id.image_cancel_contact).setOnClickListener(this::handleDeclineCancelContact);
            }
        } else {
            if (holder.getItemViewType() == SENT) {
                // display the username we sent to
                Log.e("VERIFIED", Integer.toString(mValues.get(position).getVerified()));
            if (mValues.get(position).getVerified() == 1) {
                    holder.mUsernameView.setText(mValues.get(position).getUsernameB());
                } else {
                    holder.mUsernameView.setText(mValues.get(position).getUsernameB() + "\n(not accepted)");
                }
            } else {
                // display the username that sent the connection to us
                holder.mUsernameView.setText(mValues.get(position).getUsernameA());
                // Accept only shows up if this is recieved and not verified
                if (holder.getItemViewType() == RECIEVED_NOT_VERIFIED) {
                    holder.mView.findViewById(R.id.image_accept_contact).setOnClickListener(this::handleAcceptContact);
                }
            }
            // All adapter items will have a cancel button, handle it with one listener
            holder.mView.findViewById(R.id.image_cancel_contact).setOnClickListener(this::handleDeclineCancelContact);
        }

    }

    public void updateItems(List<Connection> theConnections) {
        mValues = theConnections;
        notifyDataSetChanged();
    }

    private void handleDeclineCancelContact(View view) {
        View parent = (View) view.getParent();
        String temp = ((TextView)parent.findViewById(R.id.list_item_connection_name)).getText().toString();
        // trim if necessary
        if (temp.endsWith("\n(not accepted)")) {
            temp = temp.substring(0, temp.indexOf("\n(not accepted)"));
            Log.e("TRIMMED", temp);
        }
        final String otherUsername = temp;

        // confirm with the user
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Are you sure you want to cancel/delete " + otherUsername + "?")
                .setTitle("Delete/Cancel?")
                .setPositiveButton("YES", (dialog, which) -> {
                    deleteContact(otherUsername);
                })
                .setNegativeButton("CANCEL", (dialog, which) -> {});
        AlertDialog dialog = builder.create();
        dialog.show();

        Log.e("CONTACTSBUTTONCLICKED", "DECLINE CLICKED ON "
                + ((TextView)parent.findViewById(R.id.list_item_connection_name)).getText().toString());
    }

    private void deleteContact(String otherUsername) {
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("decliningUsername", mCredentials.getUsername());
            messageJson.put("requestUsername", otherUsername);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(mContext.getString(R.string.ep_base_url))
                .appendPath(mContext.getString(R.string.ep_connections_base))
                .appendPath(mContext.getString(R.string.ep_connections_decline_request))
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                .onPostExecute(this::handleConnectionsChangePostExecute)
                .onCancelled(error -> Log.e("MyConnectionsRecyclerViewAdapter", error))
                .addHeaderField("authorization", mJwToken)
                .build().execute();
    }

    private void handleAcceptContact(View view) {
        View parent = (View) view.getParent();
        String otherUsername = ((TextView)parent.findViewById(R.id.list_item_connection_name)).getText().toString();
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("acceptingUsername", mCredentials.getUsername());
            messageJson.put("requestUsername", otherUsername);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(mContext.getString(R.string.ep_base_url))
                .appendPath(mContext.getString(R.string.ep_connections_base))
                .appendPath(mContext.getString(R.string.ep_connections_accept_request))
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                .onPostExecute(this::handleConnectionsChangePostExecute)
                .onCancelled(error -> Log.e("MyConnectionsRecyclerViewAdapter", error))
                .addHeaderField("authorization", mJwToken)
                .build().execute();

        Log.e("CONTACTSBUTTONCLICKED", "ACCEPT CLICKED ON "
                + ((TextView)parent.findViewById(R.id.list_item_connection_name)).getText().toString());
    }

    private void handleConnectionsChangePostExecute(String result) {
        // Our list changed, notify listeners that we need to be refreshed
        myPcs.firePropertyChange(ConnectionsContainerFragment.REFRESH_CONNECTIONS, null, result);
        Log.e("RESULT", result);
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
            mUsernameView = view.findViewById(R.id.list_item_connection_name);
            mDeclineCancelButton = view.findViewById(R.id.image_cancel_contact);

            // NOTE accept button can be missing if our viewID SENT or RECIEVED_VERIFIED
            mAcceptButton = view.findViewById(R.id.image_accept_contact);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mUsernameView.getText() + "'";
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        myPcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        myPcs.removePropertyChangeListener(listener);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onWaitFragmentInteractionShow();
        void onWaitFragmentInteractionHide();
    }
}
