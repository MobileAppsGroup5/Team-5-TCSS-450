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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tcss450.uw.edu.chapp.chat.Message;
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.PushReceiver;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private static final String TAG = "CHAT_FRAG";
    public static final String ARG_MESSAGE_LIST = "message list";

    private TextView mMessageOutputTextView;
    private EditText mMessageInputEditText;
    private List<Message> mMessages;
    private OnChatMessageFragmentInteractionListener mListener;


    private String mUsername;
    private String mJwToken;
    private String mSendUrl;
    private String mChatId;


    private PushMessageReceiver mPushMessageReciever;


    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getArguments() != null) {
            //get the email and JWT from the Activity. Make sure the Keys match what you used
            mUsername = ((Credentials) getArguments().get(getString(R.string.key_credentials))).getUsername();
            mJwToken = getArguments().getString(getString(R.string.keys_intent_jwt));
            mChatId = getArguments().getString(getString(R.string.key_chatid));

            //get the messages grabbed from the database
            callWebServiceforMessages();
        } else {
            mMessages = new ArrayList<Message>();
            Log.e("tag", "no messages received");
        }
        //We will use this url every time the user hits send. Let's only build it once, ya?
        mSendUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_messaging_base))
                .appendPath(getString(R.string.ep_messaging_send))
                .build()
                .toString();

    }

    private void callWebServiceforMessages(){
        //Create the url for getting all messages in chat
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_messaging_base))
                .appendPath(getString(R.string.ep_chats_get_messages))
                .build();

        // Create the JSON object with given chatID
        JSONObject msg = new JSONObject();
        try {
            msg.put("chatId", mChatId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(msg);
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleWaitFragmentShow)
                .onPostExecute(this::handleChatsMessagesOnPostExecute)
                .onCancelled(this::handleErrorsInTask)
                .addHeaderField("authorization", mJwToken) //add the JWT as a header
                .build().execute();
    }

    private void handleChatsMessagesOnPostExecute(final String result){
        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_chats_messages))) {

                JSONArray data = root.getJSONArray(
                        getString(R.string.keys_json_chats_messages));
                List<Message> messages = new ArrayList<>();
                for(int i = 0; i < data.length(); i++) {
                    JSONObject jsonMessage = data.getJSONObject(i);
                    messages.add(new Message.Builder(
                            jsonMessage.getString(getString(R.string.keys_json_chats_username)),
                            jsonMessage.getString(getString(R.string.keys_json_chats_message)),
                            jsonMessage.getString(getString(R.string.keys_json_chats_time)))
                            .build());
                    Log.e("TAG",jsonMessage.getString(getString(R.string.keys_json_chats_message)));

                }
                Message[] messagesAsArray = new Message[messages.size()];
                messagesAsArray = messages.toArray(messagesAsArray);
                mMessages = new ArrayList<Message>(Arrays.asList(messagesAsArray));

                //now update UI to show messages
                updateMessages();
                mListener.onWaitFragmentInteractionHide();
            } else {
                Log.e("ERROR!", "No data array");
                //notify user
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            //notify user
        }

    }

    private void handleWaitFragmentShow(){
        mListener.onWaitFragmentInteractionShow();
    }

    private void updateMessages(){
        StringBuilder s = new StringBuilder();
        for( Message m : mMessages){
            s.append(m.getMessage());
            s.append("\n");

        }
        mMessageOutputTextView.setText(s);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootLayout = inflater.inflate(R.layout.fragment_chat, container, false);

        mMessageOutputTextView = rootLayout.findViewById(R.id.text_chat_message_display);
        mMessageInputEditText = rootLayout.findViewById(R.id.edit_chat_message_input);
        rootLayout.findViewById(R.id.button_chat_send).setOnClickListener(this::handleSendClick);

        return rootLayout;
    }


    private void handleSendClick(final View theButton) {
        String msg = mMessageInputEditText.getText().toString();
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("chatId", mChatId);
            messageJson.put("username", mUsername);
            messageJson.put("message", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(mSendUrl, messageJson)
                .onPostExecute(this::endOfSendMsgTask)
                .onCancelled(error -> Log.e(TAG, error))
                .addHeaderField("authorization", mJwToken)
                .build().execute();
    }

    private void endOfSendMsgTask(final String result) {
        try {
            //This is the result from the web service
            JSONObject res = new JSONObject(result);
            if(res.has("success") && res.getBoolean("success")) {

                //set the output text to show the sent message
                mMessageOutputTextView.setText(mMessageInputEditText.getText().toString());

                //The web service got our message. Time to clear out the input EditText
                mMessageInputEditText.setText("");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * Handle errors that may occur during the AsyncTask.
     * @param result the error message provide from the AsyncTask
     */
    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPushMessageReciever == null) {
            mPushMessageReciever = new PushMessageReceiver();
        }
        IntentFilter iFilter = new IntentFilter(PushReceiver.RECEIVED_NEW_MESSAGE);
        getActivity().registerReceiver(mPushMessageReciever, iFilter);
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mPushMessageReciever != null){
            getActivity().unregisterReceiver(mPushMessageReciever);
        }
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChatMessageFragmentInteractionListener) {
            mListener = (OnChatMessageFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChatMessageFragmentInteractionListener");
        }
    }

    public interface OnChatMessageFragmentInteractionListener extends WaitFragment.OnFragmentInteractionListener {
        void onRetrieveMessage();
        void onReceivedMessage();
    }
    /**
     * A BroadcastReceiver that listens for messages sent from PushReceiver
     */
    private class PushMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("WORLD", intent.getStringExtra("MESSAGE"));
            if(intent.hasExtra("SENDER")
                    && intent.hasExtra("MESSAGE")
                    && intent.hasExtra("CHATID")) {
                String sender = intent.getStringExtra("SENDER");
                String messageText = intent.getStringExtra("MESSAGE");
                if (intent.getStringExtra("CHATID").equals(mChatId)) {
                    mMessageOutputTextView.append(sender + ":" + messageText);
                    mMessageOutputTextView.append(System.lineSeparator());
                    mMessageOutputTextView.append(System.lineSeparator());
                }
            }
        }
    }
}
