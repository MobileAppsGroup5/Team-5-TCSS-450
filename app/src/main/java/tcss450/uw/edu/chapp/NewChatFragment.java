package tcss450.uw.edu.chapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
 * A fragment for creating a new chat.
 */
public class NewChatFragment extends Fragment {
    private Credentials mCreds;
    private String mJwToken;

//    private OnFragmentInteractionListener mListener;

    public NewChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCreds = (Credentials) getArguments().getSerializable(getString(R.string.key_credentials));
            mJwToken = getArguments().getString(getString(R.string.keys_intent_jwt));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_chat, container, false);

        // Add action listener to button
        v.findViewById(R.id.button_add_new_chat_member).setOnClickListener(this::createNewChat);
        return inflater.inflate(R.layout.fragment_new_chat, container, false);
    }

    private void createNewChat(View view) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_chats_base))
                .appendPath(getString(R.string.ep_chats_new))
                .build();

        JSONObject msg = new JSONObject();
        try {
            msg.put("username1", mCreds.getUsername());
            msg.put("username2", ((EditText)getActivity().findViewById(R.id.text_view_new_chat_member_name)).getText().toString());
            msg.put("chatName", ((EditText)getActivity().findViewById(R.id.text_view_new_chat_room_name)).getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleRoomCreationOnPostExecute)
                .addHeaderField("authorization", mJwToken) // add the JWT as a header
                .build().execute();
    }

    private void handleRoomCreationOnPostExecute(String result) {
        TextView tv = getActivity().findViewById(R.id.text_view_new_chat_result);
        tv.setVisibility(View.VISIBLE);
        try {
            //This is the result from the web service
            JSONObject res = new JSONObject(result);
            if(res.has("success") && res.getBoolean("success")) {

                //set the output text to show the sent message
                tv.setText("New chat room created!");

            } else {
                // error
                tv.setText("Error, that user does not exist.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

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
//    public interface OnFragmentInteractionListener {
//        void onFragmentInteraction(Uri uri);
//    }
}
