package tcss450.uw.edu.chapp;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tcss450.uw.edu.chapp.chat.Chat;
import tcss450.uw.edu.chapp.connections.Connection;
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;


/**
 * A fragment for creating a new chat.
 */
public class NewChatFragment extends Fragment implements AdapterView.OnItemClickListener {

    public static final String ARG_CONN_LIST = "connnnlist<>{}";

    private List<Connection> mConnectionList;
    private List<Chat> mChatList;
    private Credentials mCreds;
    private String mJwToken;
    private AutoCompleteTextView mAutoCompleteSearchBox;

    private List<String> userNameList;

    private PropertyChangeSupport myPcs = new PropertyChangeSupport(this);

//    private OnFragmentInteractionListener mListener;

    public NewChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mConnectionList = new ArrayList<>(Arrays.asList((Connection[])getArguments().getSerializable(ARG_CONN_LIST)));
            mChatList = new ArrayList<>(Arrays.asList((Chat[])getArguments().getSerializable(getString(R.string.keys_chats_arg))));
            mCreds = (Credentials) getArguments().getSerializable(getString(R.string.key_credentials));
            mJwToken = getArguments().getString(getString(R.string.keys_intent_jwt));

            setUpConnectionSearchArray();
        }
    }

    /**
     * This sets up the array that will be used for searching for people to add to the chatroom
     */
    private void setUpConnectionSearchArray() {
        userNameList = new ArrayList<>();

        // first, add all possible connection usernames
        //
        // this also adds our own username a lot because we are either A or B in all of these,
        // and we add both
        mConnectionList.forEach(connection -> {
            userNameList.add(connection.getUsernameA());
            userNameList.add(connection.getUsernameB());
        });

        // remove all occurances of our username
        while (mConnectionList.remove(mCreds.getUsername()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_chat, container, false);

        mAutoCompleteSearchBox = v.findViewById(R.id.auto_complete_new_chat_search);

        mAutoCompleteSearchBox.setOnItemClickListener(this);

        return v;
    }

    private void handleRoomCreationOnPostExecute(String result) {
        TextView tv = getActivity().findViewById(R.id.text_view_add_chat_result);
        tv.setVisibility(View.VISIBLE);
        try {
            //This is the result from the web service
            JSONObject res = new JSONObject(result);
            if(res.has("success") && res.getBoolean("success")) {

                //set the output text to show the sent message
                tv.setText("New chat room created!");

                // refresh chats
                myPcs.firePropertyChange(ChatsContainerFragment.PROPERTY_REFRESH_CHATS,
                        null, "refreshpls");

            } else {
                // error
                tv.setText("Error, that user does not exist.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        myPcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        myPcs.removePropertyChangeListener(listener);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String clickedUsername = ((TextView)view).getText().toString();
        submitChatRequest(clickedUsername);

        // Refresh chats view regardless (can't hurt)
    }

    private void submitChatRequest(String clickedUsername) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_chats_base))
                .appendPath(getString(R.string.ep_chats_submit_request))
                .build();

        JSONObject msg = new JSONObject();
        try {
            msg.put("username1", mCreds.getUsername());
            msg.put("username2", clickedUsername);
            msg.put("chatName", ((EditText)getActivity().findViewById(R.id.text_view_new_chat_room_name)).getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleRoomCreationOnPostExecute)
                .addHeaderField("authorization", mJwToken) // add the JWT as a header
                .build().execute();
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
