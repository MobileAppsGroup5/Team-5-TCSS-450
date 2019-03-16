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

import tcss450.uw.edu.chapp.chat.Chat;
import tcss450.uw.edu.chapp.model.Credentials;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fragment representing a list of Chat items.
 *
 * @author Mike Osborne, Trung Thai, Michael Josten, Jessica Medrzycki
 * @version 03/15/19
 */
public class ChatsFragment extends Fragment implements PropertyChangeListener {

    public static final String ARG_CHAT_LIST = "chats lists";
    private static final String ARG_COLUMN_COUNT = "column-count";
    private List<Chat> mChats;
    private Credentials mCreds;
    private String mJwToken;
    private boolean mCompactMode;

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private PropertyChangeSupport myPcs = new PropertyChangeSupport(this);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChatsFragment() {
    }

    /**
     * method that is called to create a new instance of the chat fragment.
     */
    public static ChatsFragment newInstance(int columnCount) {
        ChatsFragment fragment = new ChatsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * method that will get and display a list of chats.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mChats = new ArrayList<>(Arrays.asList((Chat[])getArguments().getSerializable(ARG_CHAT_LIST)));
            mCreds = (Credentials)getArguments().getSerializable(getString(R.string.key_credentials));
            mJwToken = (String)getArguments().getSerializable(getString(R.string.keys_intent_jwt));
            mCompactMode = getArguments().getBoolean(getString(R.string.key_flag_compact_mode));

        } else {
            mChats = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            MyChatsRecyclerViewAdapter adapter = new MyChatsRecyclerViewAdapter(mChats, mListener, mCreds, getContext(), mJwToken, mCompactMode);
            adapter.addPropertyChangeListener(this);
            recyclerView.setAdapter(adapter);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Adds a propertychangelistener to this objects propertychangesupport
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        myPcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes a propertychangelistener to this objects propertychangesupport
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        myPcs.removePropertyChangeListener(listener);
    }

    /**
     * Listens for events from the recycleradapter and fires them to listeners of this class,
     * just passes it along
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ChatsContainerFragment.PROPERTY_REFRESH_CHATS.equals(evt.getPropertyName())) {
            // pass the refresh request along
            myPcs.firePropertyChange(ChatsContainerFragment.PROPERTY_REFRESH_CHATS, null, "thanku");
        }
    }

    /**
     * defines an interface when the list item is interacted with
     */
    public interface OnListFragmentInteractionListener {
        /**
         * Perform an action when a chat list item is interacted with
         * @param item The item that was interacted with
         */
        void onListFragmentInteraction(Chat item);
    }
}
