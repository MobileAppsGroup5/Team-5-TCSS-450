package tcss450.uw.edu.chapp;


import android.content.Context;
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

import tcss450.uw.edu.chapp.chat.Chat;
import tcss450.uw.edu.chapp.connections.Connection;
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;


/**
 * A container for all things chats/messaging. Handles switching out messaging/chats and auto updates
 * chats/messages each time it does so.
 */
public class ChatsContainerFragment extends Fragment implements PropertyChangeListener {

    public static final String PROPERTY_REFRESH_CHATS = "refresh chats pls";

    private List<Chat> mChats;
    private Credentials mCreds;
    private String mJwToken;
    private WaitFragment.OnFragmentInteractionListener mListener;

    public ChatsContainerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mCreds = (Credentials)getArguments().getSerializable(getString(R.string.key_credentials));
            mJwToken = (String)getArguments().getSerializable(getString(R.string.keys_intent_jwt));
        } else {
            mChats = new ArrayList<>();
        }
    }

    private void callWebServiceforChats() {
        Uri uri = new Uri.Builder()
            .scheme("https")
            .appendPath(getString(R.string.ep_base_url))
            .appendPath(getString(R.string.ep_chats_base))
            .appendPath(getString(R.string.ep_chats_get_chats))
            .build();
        // Pass the credentials
        JSONObject msg = mCreds.asJSONObject();
        mListener.onWaitFragmentInteractionShow();
        new SendPostAsyncTask.Builder(uri.toString(), msg)
            .onPostExecute(this::handleChatsPostOnPostExecute)
            .onCancelled(error -> Log.e("ConnectionsContainerFragment", error))
            .addHeaderField("authorization", mJwToken) //add the JWT as a header
            .build().execute();
    }

    /**
     * Post call for retrieving the list of Chats from the Database.
     * Calls chat fragment to load and sends in the chatId to load into.
     * @param result the chatId and name of the chats from the result to display in UI
     */
    private void handleChatsPostOnPostExecute(final String result) {
        // parse JSON
        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_chats_chatlist))) {

                JSONArray data = root.getJSONArray(
                        getString(R.string.keys_json_chats_chatlist));
                List<Chat> chats = new ArrayList<>();
                for(int i = 0; i < data.length(); i++) {
                    JSONObject jsonChat = data.getJSONObject(i);
                    chats.add(new Chat.Builder(
                            jsonChat.getString(getString(R.string.keys_json_chats_chatid)),
                            jsonChat.getString(getString(R.string.keys_json_chats_name)))
                            .build());
                }
                Chat[] chatsAsArray = new Chat[chats.size()];
                chatsAsArray = chats.toArray(chatsAsArray);
                Bundle args = new Bundle();
                args.putSerializable(ChatsFragment.ARG_CHAT_LIST, chatsAsArray);
                args.putSerializable(getString(R.string.key_credentials), mCreds);
                args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
                android.support.v4.app.Fragment frag = new ChatsFragment();
                frag.setArguments(args);

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.chats_container, frag)
                        .commit();

                mListener.onWaitFragmentInteractionHide();
            } else {
                Log.e("ERROR!", "No data array");
                //notify user
                mListener.onWaitFragmentInteractionHide();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            //notify user
            mListener.onWaitFragmentInteractionHide();
        }
    }

    /**
     * Override onCreateOptionsMenu to populate the options menu
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Add menu entries
        MenuItem newChatMenuItem = menu.add("New chat");
        newChatMenuItem.setOnMenuItemClickListener(this::newChatMenuItemListener);

        // NOTE: this super call adds any previous buttons so we don't have to worry about that
        super.onCreateOptionsMenu(menu, inflater);
    }

    private boolean newChatMenuItemListener(MenuItem menuItem) {
        // do a sendAsyncTask to getallcontacts
        // which in the postExecute call the addNewContact fragment

        // fetch usernames from database here
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_connections_base))
                .appendPath(getString(R.string.ep_connections_get_connections_and_requests))
                .build();
        // Pass the credentials
        JSONObject msg = mCreds.asJSONObject();
        mListener.onWaitFragmentInteractionShow();
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleConnectionInformationOnPostExecute)
                .onCancelled(error -> Log.e("ChatsContainerFragment", error))
                .addHeaderField("authorization", mJwToken) //add the JWT as a header
                .build().execute();

        return true;

//        // for now just call the fragment
//        NewChatFragment frag = new NewChatFragment();
//
//        Bundle args = new Bundle();
//        args.putSerializable(getString(R.string.key_credentials), mCreds);
//        args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
//        frag.setArguments(args);
//        getActivity().getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.new_chat_container, frag)
//                .commit();
//
//        // listen for when it needs to be refreshed
//        frag.addPropertyChangeListener(this);
//        return true;
    }

    private void handleConnectionInformationOnPostExecute(String result) {
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

                // Do this swapping so we can send in an array of Messages not Objects
                Bundle args = new Bundle();
                Connection[] connsAsArray = new Connection[connections.size()];
                connsAsArray = connections.toArray(connsAsArray);
                args.putSerializable(NewChatFragment.ARG_CONN_LIST, connsAsArray);
                args.putSerializable(getString(R.string.key_credentials), mCreds);
                args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
                NewChatFragment newChatFrag = new NewChatFragment();
                newChatFrag.setArguments(args);

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.new_chat_container, newChatFrag)
                        .commit();

                newChatFrag.addPropertyChangeListener(this);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats_container, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof WaitFragment.OnFragmentInteractionListener) {
            mListener = (WaitFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement WaitFragment.OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        callWebServiceforChats();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PROPERTY_REFRESH_CHATS.equals(evt.getPropertyName())) {
            callWebServiceforChats();
        }
    }
}
