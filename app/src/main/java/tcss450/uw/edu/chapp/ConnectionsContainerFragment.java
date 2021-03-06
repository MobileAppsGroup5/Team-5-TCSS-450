package tcss450.uw.edu.chapp;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import tcss450.uw.edu.chapp.connections.Connection;
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.PushReceiver;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass, Handles swapping out of connection information to keep information
 * fresh.
 */
public class ConnectionsContainerFragment extends Fragment implements PropertyChangeListener {
    public static final String REFRESH_CONNECTIONS = "refresh the connections please";
    private ArrayList<Connection> mConnections;
    private Credentials mCreds;
    private String mJwToken;
    private OnConnectionInformationFetchListener mListener;
    public boolean mCompactMode = false;

    public ConnectionsContainerFragment() {
        // Required empty public constructor
    }

    /**
     * Gets information from the arguments.
     * @param savedInstanceState The previous state of this fragment
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mCreds = (Credentials)getArguments().getSerializable(getString(R.string.key_credentials));
            mJwToken = (String)getArguments().getSerializable(getString(R.string.keys_intent_jwt));
            if (getArguments().getSerializable(getString(R.string.key_flag_compact_mode)) != null) {
                mCompactMode = true;
            } else {
                setHasOptionsMenu(true);
            }
        } else {
            mConnections = new ArrayList<>();
        }
    }

    /**
     * Add options menu item and assigns listener
     * @param menu The menu that will be shown
     * @param inflater The inflater to inflate the menu
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // connection menu item
        MenuItem newConnectionMenuItem = menu.add("New Connection");
        newConnectionMenuItem.setOnMenuItemClickListener(this::newConnectionMenuItemListener);

        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Listener for clicks on the new connection menu item
     * @param menuItem The menu item that was clicked on
     * @return true if the action was performed successfully, false otherwise
     */
    private boolean newConnectionMenuItemListener(MenuItem menuItem) {
        // fetch usernames from database here
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_connections_base))
                .appendPath(getString(R.string.ep_connections_get_all_member_info))
                .build();
        // Pass the credentials
        JSONObject msg = mCreds.asJSONObject();
        mListener.onWaitFragmentInteractionShow();
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleMemberInformationOnPostExecute)
                .onCancelled(error -> Log.e("ConnectionsContainerFragment", error))
                .addHeaderField("authorization", mJwToken) //add the JWT as a header
                .build().execute();

        return true;
    }

    /**
     * Handles member information and shows it to the user
     * @param result The JSON result of the async task.
     */
    private void handleMemberInformationOnPostExecute(String result) {
        // parse JSON
        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_users))) {

                JSONArray data = root.getJSONArray(
                        getString(R.string.keys_json_users));
                List<Credentials> users = new ArrayList<>();
                for(int i = 0; i < data.length(); i++) {
                    JSONObject jsonCred = data.getJSONObject(i);
                    users.add(new Credentials.Builder(
                            jsonCred.getString(getString(R.string.keys_json_email)),
                            "")
                            .addFirstName(jsonCred.getString(getString(R.string.keys_json_first_name)))
                            .addLastName(jsonCred.getString(getString(R.string.keys_json_last_name)))
                            .addUsername(jsonCred.getString(getString(R.string.keys_json_username)))
                            .build());
                }
                Bundle args = new Bundle();

                // Do this swapping so we can send in an array of Messages not Objects
                Credentials[] credsAsArray = new Credentials[users.size()];
                credsAsArray = users.toArray(credsAsArray);
                args.putSerializable(NewConnectionFragment.ARG_CRED_LIST, credsAsArray);
                args.putSerializable(getString(R.string.key_credentials), mCreds);
                args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
                args.putSerializable(getString(R.string.key_intent_connections), mConnections);
                NewConnectionFragment newConnectionFrag = new NewConnectionFragment();
                newConnectionFrag.setArguments(args);

                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.new_connection_container, newConnectionFrag)
                        .commit();

                newConnectionFrag.addPropertyChangeListener(this);
                mListener.onWaitFragmentInteractionHide();
            } else {
                Log.e("ERROR!", "No data array");
                mListener.onWaitFragmentInteractionHide();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            mListener.onWaitFragmentInteractionHide();
        }
    }

    /**
     * Begins the async task for grabbing the messages from the
     * Database given the specified chatid.
     */
    public void callWebServiceforConnections(){
        mListener.onWaitFragmentInteractionShow();
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_connections_base))
                .appendPath(getString(R.string.ep_connections_get_connections_and_requests))
                .build();
        // Pass the credentials
        JSONObject msg = mCreds.asJSONObject();
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleConnectionsOnPostExecute)
                .onCancelled(error -> Log.e("ConnectionsContainerFragment", error))
                .addHeaderField("authorization", mJwToken) //add the JWT as a header
                .build().execute();
    }

    /**
     * Parses the connections information and displays it
     * @param result The JSON result of the async task
     */
    private void handleConnectionsOnPostExecute(String result) {
        // parse JSON
        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_connections))) {

                JSONArray data = root.getJSONArray(
                        getString(R.string.keys_json_connections));
                List<Connection> connections = new ArrayList<>();
                for(int i = 0; i < data.length(); i++) {
                    JSONObject jsonChat = data.getJSONObject(i);
                    connections.add(new Connection.Builder(
                            jsonChat.getString(getString(R.string.keys_json_connections_from)),
                            jsonChat.getString(getString(R.string.keys_json_connections_to)),
                            Integer.parseInt(jsonChat.getString(getString(R.string.keys_json_connections_verified))))
                            .build());
                }
                mConnections = new ArrayList<>(connections);
                // update the reference in home activity
                mListener.updateConnections(mConnections);

                constructConnections();
                mListener.onWaitFragmentInteractionHide();
                Log.e("CONNECTIONSCONTAINER", "SUCCESSFUL");
            } else {
                Log.e("ERROR!", "No data array");
                mListener.onWaitFragmentInteractionHide();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            mListener.onWaitFragmentInteractionHide();
        }
    }

    /**
     * Constructs the connections fragment that will be shown on the screen, based on the result
     * of the async task.
     */
    private void constructConnections() {
        Bundle args = new Bundle();

        // Do this swapping so we can send in an array of Messages not Objects
        Connection[] connectionsAsArray = new Connection[mConnections.size()];
        connectionsAsArray = mConnections.toArray(connectionsAsArray);
        args.putSerializable(ConnectionsFragment.ARG_CONNECTIONS_LIST, connectionsAsArray);
        args.putSerializable(getString(R.string.key_credentials), mCreds);
        args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
        args.putSerializable(getString(R.string.key_flag_compact_mode), mCompactMode);
        ConnectionsFragment frag = new ConnectionsFragment();
        frag.setArguments(args);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.connections_container, frag)
                .commit();

        frag.addPropertyChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connections_container, container, false);
    }

    /**
     * Refresh our connections any time we resume.
     */
    @Override
    public void onResume() {
        super.onResume();

        callWebServiceforConnections();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof WaitFragment.OnFragmentInteractionListener) {
            mListener = (OnConnectionInformationFetchListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement WaitFragment.OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Called when we get a propertychangeevent, refreshes the connection list
     * @param evt the propertychangevent
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(REFRESH_CONNECTIONS)) {
            // refresh everything, something changed.
            callWebServiceforConnections();
        }
    }

    /**
     * The fragment interaction listener for connections information, also provides waitfragment functionality.
     */
    interface OnConnectionInformationFetchListener {
        /**
         * updates homeactivities connections reference
         * @param connections The new connections
         */
        void updateConnections(ArrayList<Connection> connections);

        /**
         * hide the waitfragment
         */
        void onWaitFragmentInteractionHide();

        /**
         * show the waitfragment
         */
        void onWaitFragmentInteractionShow();
    }


}
