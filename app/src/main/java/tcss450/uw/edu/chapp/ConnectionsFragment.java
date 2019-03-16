package tcss450.uw.edu.chapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tcss450.uw.edu.chapp.connections.Connection;
import tcss450.uw.edu.chapp.model.Credentials;

/**
 * A fragment representing a list of Connections to display for the user to view and modify.
 */
public class ConnectionsFragment extends Fragment implements PropertyChangeListener {

    // Misspell this to lower the change of a tag conflict
    public static final String ARG_CONNECTIONS_LIST = "connections lists";
    private static final String ARG_COLUMN_COUNT = "column-count";
    private List<Connection> mConnections;
    private Credentials mCreds;
    private String mJwToken;
    private MyConnectionsRecyclerViewAdapter mAdapter;
    private boolean mCompactMode;
    private int mColumnCount = 1;

    private PropertyChangeSupport myPcs = new PropertyChangeSupport(this);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ConnectionsFragment() {
    }

    /**
     * Default list view new instance method
     * @param columnCount the number of columns in the recycler
     * @return The constructed fragment
     */
    public static ConnectionsFragment newInstance(int columnCount) {
        ConnectionsFragment fragment = new ConnectionsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Get information from the arguments
     * @param savedInstanceState The saved instance state of the fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mConnections = new ArrayList<>(Arrays.asList((Connection[])getArguments().getSerializable(ARG_CONNECTIONS_LIST)));
            mCreds = (Credentials)getArguments().getSerializable(getString(R.string.key_credentials));
            mJwToken = (String)getArguments().getSerializable(getString(R.string.keys_intent_jwt));
            mCompactMode = getArguments().getBoolean(getString(R.string.key_flag_compact_mode));
        } else {
            mConnections = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connections_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mAdapter = new MyConnectionsRecyclerViewAdapter(mConnections, mCreds, mJwToken, getContext(), mCompactMode);
            mAdapter.addPropertyChangeListener(this);
            recyclerView.setAdapter(mAdapter);
        }

        return view;
    }

    /**
     * adds a propertychangelistener
     * @param listener The propertychangelistener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        myPcs.addPropertyChangeListener(listener);
    }

    /**
     * removes a propertychangelistener
     * @param listener The propertychangelistener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        myPcs.removePropertyChangeListener(listener);
    }

    /**
     * called when connecton information needs to be refreshed
     * @param evt the {@link PropertyChangeEvent}
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Wrap the adapter's connections changed adapter and put that up.
        if (evt.getPropertyName() == ConnectionsContainerFragment.REFRESH_CONNECTIONS) {
            myPcs.firePropertyChange(ConnectionsContainerFragment.REFRESH_CONNECTIONS,
                    null, evt.getNewValue());
        }
    }
}
