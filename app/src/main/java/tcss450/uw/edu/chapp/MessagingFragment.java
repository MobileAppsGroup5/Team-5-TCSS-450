package tcss450.uw.edu.chapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tcss450.uw.edu.chapp.chat.Message;
import tcss450.uw.edu.chapp.model.Credentials;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fragment representing a list of messages for the user to view
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class MessagingFragment extends Fragment {

    public static final String ARG_MESSAGE_LIST = "messages list";
    private static final String ARG_COLUMN_COUNT = "column-count";
    private List<Message> mMessages;
    private Credentials mCredentials;

    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private MyMessagingRecyclerViewAdapter mAdapter;

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MessagingFragment() {
    }

    public static MessagingFragment newInstance(int columnCount) {
        MessagingFragment fragment = new MessagingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mMessages = new ArrayList<>(Arrays.asList((Message[])getArguments().getSerializable(ARG_MESSAGE_LIST)));
            mCredentials = (Credentials) getArguments().getSerializable(getString(R.string.key_credentials));
        } else {
            mMessages = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                LinearLayoutManager linLay = new LinearLayoutManager(context);
                linLay.setReverseLayout(true);
                mRecyclerView.setLayoutManager(linLay);
            } else {
                mLayoutManager = new GridLayoutManager(context, mColumnCount);
                mRecyclerView.setLayoutManager(mLayoutManager);
            }
            mAdapter = new MyMessagingRecyclerViewAdapter(mMessages, mListener, mCredentials.getUsername());
            mRecyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    public void append(Message message) {
        mMessages.add(message);
        Log.e("MESSAGES", mMessages.toString());
        // Force the view to update
//        mRecyclerView.setAdapter(null);
//        mRecyclerView.setLayoutManager(null);
//        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter.notifyDataSetChanged();
        mAdapter.notifyItemInserted(mMessages.size() - 1);
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
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // We might need the onListFragmentInteractionListener in the future, but for now
        // theres no reason why you would want to interact with a message
        void onListFragmentInteraction(Message item);
    }
}
