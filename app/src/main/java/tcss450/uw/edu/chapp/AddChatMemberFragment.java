package tcss450.uw.edu.chapp;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddChatMemberFragment extends Fragment {
    private Credentials mCreds;
    private String mJwToken;
    private String mChatId;

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
            mChatId = getArguments().getString(getString(R.string.key_chatid));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_chat_member, container, false);

        // Add action listener to button
        v.findViewById(R.id.button_add_new_chat_member).setOnClickListener(this::addNewMemberToChat);

        return v;
    }

    private void addNewMemberToChat(View view) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_chats_base))
                .appendPath(getString(R.string.ep_chats_add_member))
                .build();

        JSONObject msg = new JSONObject();
        try {
//            Log.e("STUFF", )
            msg.put("username", ((EditText) getActivity().findViewById(R.id.text_view_add_new_chat_member_name)).getText().toString());
            msg.put("chatId", mChatId);
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
                tv.setText("Member added!");

            } else {
                // error
                tv.setText("Error, that user does not exist.");
                Log.e("CRASH", result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
