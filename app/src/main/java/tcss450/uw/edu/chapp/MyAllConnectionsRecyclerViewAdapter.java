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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.chapp.AllConnectionsFragment.OnListFragmentInteractionListener;
import tcss450.uw.edu.chapp.chat.Message;
import tcss450.uw.edu.chapp.connections.Connection;
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Connection} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyAllConnectionsRecyclerViewAdapter extends RecyclerView.Adapter<MyAllConnectionsRecyclerViewAdapter.ViewHolder> {

    public static final String PROPERTY_CONNECTIONS_CHANGED = "connections changed}{";

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
    private final OnListFragmentInteractionListener mListener;
    private Credentials mCredentials;
    private Context mContext;
    private String mJwToken;

    public MyAllConnectionsRecyclerViewAdapter(List<Connection> items, OnListFragmentInteractionListener listener,
                                               Credentials credentials, String jwToken, Context context) {
        mValues = items;
        mListener = listener;
        mCredentials = credentials;
        mContext = context;
        mJwToken = jwToken;
        myPcs = new PropertyChangeSupport(this);
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
        if (viewType == SENT || viewType == RECIEVED_VERIFIED) {
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

        if (holder.getItemViewType() == SENT) {
            // display the username we sent to
            holder.mUsernameView.setText(mValues.get(position).getUsernameB());
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

    public void updateItems(List<Connection> theConnections) {
        mValues = theConnections;
        notifyDataSetChanged();
    }

    private void handleDeclineCancelContact(View view) {
        View parent = (View) view.getParent();
        String otherUsername = ((TextView)parent.findViewById(R.id.list_item_connection_name)).getText().toString();

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
                .onCancelled(error -> Log.e("MyAllConnectionsRecyclerViewAdapter", error))
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
                .onCancelled(error -> Log.e("MyAllConnectionsRecyclerViewAdapter", error))
                .addHeaderField("authorization", mJwToken)
                .build().execute();

        Log.e("CONTACTSBUTTONCLICKED", "ACCEPT CLICKED ON "
                + ((TextView)parent.findViewById(R.id.list_item_connection_name)).getText().toString());
    }

    private void handleConnectionsChangePostExecute(String result) {
        // Our list changed, notify listeners that we need to be refreshed
        myPcs.firePropertyChange(PROPERTY_CONNECTIONS_CHANGED, null, result);

        // run the runnable, let someone else handle it
//        mUpdateRunnable.run();


        // We successfully accepted/rejected/something. Update the list
//        String requestUsername = "";
//        String declineCancelUsername = null;
//        String acceptUsername = null;
//
//        try {
//            JSONObject root = new JSONObject(result);
//            // We only get request username back if successful, so check for this
//            if (root.has(mContext.getString(R.string.keys_json_connections_request_username))) {
//
//                requestUsername = root.getString(mContext.getString(R.string.keys_json_connections_request_username));
//                if (root.has(mContext.getString(R.string.keys_json_connections_decline_cancel_username))) {
//                    declineCancelUsername = root.getString(mContext.getString(R.string.keys_json_connections_decline_cancel_username));
//                }
//                if (root.has(mContext.getString(R.string.keys_json_connections_accept_username))) {
//                    acceptUsername = root.getString(mContext.getString(R.string.keys_json_connections_accept_username));
//                }
//
//            } else {
//                Log.e("ERROR!", "invalid response");
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Log.e("ERROR!", e.getMessage());
//        }
//
//        int clickedIndex = 0;
//        // find the element that matches the request
//        for (int i = 0; i < mValues.size(); i++) {
//            if (mValues.get(i).getUsernameA().equals(requestUsername)) {
//                clickedIndex = i;
//            }
//        }
//
//        if (Objects.nonNull(declineCancelUsername)) {
//            mValues.remove(clickedIndex);
//        } else if (Objects.nonNull(acceptUsername)) {
//
//        }
//        notifyDataSetChanged();
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
