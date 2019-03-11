package tcss450.uw.edu.chapp;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tcss450.uw.edu.chapp.chat.Chat;
import tcss450.uw.edu.chapp.connections.Connection;
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddChatMemberFragment extends Fragment implements AdapterView.OnItemClickListener {
    private Credentials mCreds;
    private String mJwToken;
    private Chat mChat;
    private List<String> mUsernameList;
    private List<Connection> mConnections;

    private AutoCompleteTextView mAutoCompleteSearchBox;

//    private OnFragmentInteractionListener mListener;

    public AddChatMemberFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCreds = (Credentials) getArguments().getSerializable(getString(R.string.key_credentials));
            mJwToken = getArguments().getString(getString(R.string.keys_intent_jwt));
            mChat = (Chat) getArguments().get(getString(R.string.key_chat));
            mConnections = (ArrayList<Connection>) getArguments().get(getString(R.string.key_intent_connections));

        }
    }

    private void setUpSearchArray() {
        mUsernameList = new ArrayList<>();

        // first load with all possible connections
        mConnections.forEach(connection -> {
            if (connection.getVerified() == 1) {
                mUsernameList.add(connection.getUsernameA());
                mUsernameList.add(connection.getUsernameB());
            }
        });

        // remove us
        while (mUsernameList.remove(mCreds.getUsername()));
        Log.e("BEFORE TRIM", mUsernameList.toString());

        // remove people already in the chat
        mChat.getUsersInChat().forEach(username -> {
            while (mUsernameList.remove(username));
        });

        Log.e("AFTER TRIM", mUsernameList.toString());


        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                mUsernameList);

        mAutoCompleteSearchBox.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_chat_member, container, false);

        mAutoCompleteSearchBox = v.findViewById(R.id.autocomplete_text_add_new_chat_member_name);

        mAutoCompleteSearchBox.setOnItemClickListener(this);

        setUpSearchArray();

        return v;
    }

    private void addUserToChat(String username) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_chats_base))
                .appendPath(getString(R.string.ep_chats_add_member))
                .build();

        JSONObject msg = new JSONObject();
        try {
//            Log.e("STUFF", )
            msg.put("usernameTo", username);
            msg.put("usernameFrom", mCreds.getUsername());
            msg.put("chatid", mChat.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleAddMemberPostExecute)
                .addHeaderField("authorization", mJwToken) // add the JWT as a header
                .build().execute();
    }

    private void handleAddMemberPostExecute(String result) {
        TextView tv = getActivity().findViewById(R.id.text_view_add_chat_result);
        tv.setVisibility(View.VISIBLE);
        try {
            //This is the result from the web service
            JSONObject res = new JSONObject(result);
            if(res.has("success") && res.getBoolean("success")) {

                //set the output text to show the sent message
                tv.setText("Member add request sent!");

            } else {
                // error
                tv.setText("Error, that user does not exist.");
                Log.e("CRASH", result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String clickedUsername = ((TextView)view).getText().toString();
        addUserToChat(clickedUsername);
    }
}
