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
import java.util.Objects;

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

    // Reference type ArrayList instead of List to get Serialization
    private ArrayList<Chat> mChats;
    private Credentials mCreds;
    private String mJwToken;
    private OnChatInformationFetchListener mListener;
    private boolean mCompactMode = false;

    public ChatsContainerFragment() {
        // Required empty public constructor
    }

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
                ArrayList<Chat> chats = new ArrayList<>();
                for(int i = 0; i < data.length(); i++) {
                    JSONObject jsonChat = data.getJSONObject(i);

                    String chatid = jsonChat.getString(getString(R.string.keys_json_chats_chatid));

                    // First, build the list of users in the chat
                    ArrayList<String> usersInChat = new ArrayList<String>();
                    JSONArray jArray = (JSONArray) jsonChat.get(getString(R.string.keys_json_chats_users));
                    if (jArray != null) {
                        for (int j = 0; j < jArray.length(); j++) {
                            usersInChat.add(jArray.getString(j));
                        }
                    }

                    // Now inspect last senders to find out the last sender
                    String lastSender = "";
                    boolean hasMessages = false;
                    JSONArray lastSenders = jsonChat.getJSONArray(getString(R.string.keys_json_chats_last_senders));
                    // iterate to find the greatest primary key
                    int maxPrimaryKey = 0;
                    if (!lastSenders.getJSONObject(lastSenders.length()-1).isNull("f2")) {
                        hasMessages = true;
                        // iterate once to get the max
                        for (int j = 0; j < lastSenders.length(); j++) {
                            int tempKey = Integer.parseInt(lastSenders.getJSONObject(j).getString("f2"));
                            if (tempKey > maxPrimaryKey) {
                                maxPrimaryKey = tempKey;
                            }
                        }
                        // iterate again to find the associated username
                        for (int j = 0; j < lastSenders.length(); j++) {
                            int tempKey = Integer.parseInt(lastSenders.getJSONObject(j).getString("f2"));
                            if (tempKey == maxPrimaryKey) {
                                lastSender = lastSenders.getJSONObject(j).getString("f1");
                            }
                        }
                    }

                    Boolean hasBeenRead = null;
                    if (!hasMessages) {
                        // No messages have been sent in this chat, set last sender to null so we know
                        // this fact in other parts of the app
                        lastSender = null;
                    } else {
                        // if it's not null, capture if it's has been read or not.
                        if (!jsonChat.isNull(getString(R.string.keys_json_chats_has_been_read))) {
                            hasBeenRead = ((Integer) jsonChat.get(getString(R.string.keys_json_chats_has_been_read))) == 1;
                        }
                    }

                    // Retrieve accepted flags to know who has/hasn't accepted the chat room invite.
                    ArrayList<Boolean> acceptedFlags = new ArrayList<>();
                    JSONArray jsonFlags = jsonChat.getJSONArray(getString(R.string.keys_json_chats_accepted_list));
                    for (int j = 0; j < usersInChat.size(); j++) {
                        String currentUsername = usersInChat.get(j);
                        for (int x = 0; x < jsonFlags.length(); x++) {
                            if (currentUsername.equals(jsonFlags.getJSONObject(x).getString("f2"))) {
                                acceptedFlags.add("1".equals(jsonFlags.getJSONObject(x).getString("f1")));
                            }
                        }
                    }

//                    Log.e("users?", usersInChat.toString());
//                    Log.e("NAME", jsonChat.getString(getString(R.string.keys_json_chats_name)));
//                    Log.e("LAST SENDER", lastSender == null ? "NULL" : lastSender);
//                    Log.e("READ?", hasBeenRead == null ? "NULL" : Boolean.toString(hasBeenRead));
//                    Log.e("flags?", acceptedFlags.toString());


                    chats.add(new Chat.Builder(
                            chatid,
                            jsonChat.getString(getString(R.string.keys_json_chats_name)),
                            usersInChat,
                            acceptedFlags,
                            hasBeenRead,
                            lastSender)
                            .build());
                }
                mChats = chats;
                // update the reference in HomeActivity
                mListener.updateChats(mChats);
                Chat[] chatsAsArray = new Chat[chats.size()];
                chatsAsArray = chats.toArray(chatsAsArray);
                Bundle args = new Bundle();
                args.putSerializable(ChatsFragment.ARG_CHAT_LIST, chatsAsArray);
                args.putSerializable(getString(R.string.key_credentials), mCreds);
                args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
                args.putSerializable(getString(R.string.key_flag_compact_mode), mCompactMode);
                ChatsFragment frag = new ChatsFragment();
                frag.setArguments(args);

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.chats_container, frag)
                        .commit();

                mListener.onWaitFragmentInteractionHide();
                frag.addPropertyChangeListener(this);
            } else {
                Log.e("ERROR!", "No data array" + root.toString());
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
                args.putSerializable(getString(R.string.keys_chats_arg), mChats);
                NewChatFragment newChatFrag = new NewChatFragment();
                newChatFrag.setArguments(args);
                Log.e("CHaTSS", mChats.toString());

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
            mListener = (OnChatInformationFetchListener) context;
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

    interface OnChatInformationFetchListener {
        void updateChats(ArrayList<Chat> chats);
        void onWaitFragmentInteractionHide();
        void onWaitFragmentInteractionShow();
    }
}
