package tcss450.uw.edu.chapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import tcss450.uw.edu.chapp.chat.Chat;
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class AllChatsFragment extends Fragment {

    public static final String ARG_CHAT_LIST = "chats lists";
    private static final String ARG_COLUMN_COUNT = "column-count";
    private List<Chat> mChats;
    private Credentials mCreds;
    private String mJwToken;

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AllChatsFragment() {
    }

    public static AllChatsFragment newInstance(int columnCount) {
        AllChatsFragment fragment = new AllChatsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mChats = new ArrayList<>(Arrays.asList((Chat[])getArguments().getSerializable(ARG_CHAT_LIST)));
            mCreds = (Credentials)getArguments().getSerializable(getString(R.string.key_credentials));
            mJwToken = (String)getArguments().getSerializable(getString(R.string.keys_intent_jwt));

//            callWebServiceforChats();
        } else {
            mChats = new ArrayList<>();
        }
    }

//    /**
//     * Begins the async task for grabbing the chats from the database that the user is in
//     */
//    private void callWebServiceforChats(){
//        //Create the url for getting all chats
//        Uri uri = new Uri.Builder()
//                .scheme("https")
//                .appendPath(getString(R.string.ep_base_url))
//                .appendPath(getString(R.string.ep_chats_base))
//                .appendPath(getString(R.string.ep_chats_get_chats))
//                .build();
//
//        // Create the JSON object with given chatID
//        JSONObject msg = new JSONObject();
//        try {
//            msg.put("username", mCreds.getUsername());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        new SendPostAsyncTask.Builder(uri.toString(), msg)
////                .onPreExecute(this::handleWaitFragmentShow)
//                .onPostExecute(this::handleChatsPostOnPostExecute)
////                .onCancelled(this::handleErrorsInTask)
//                .addHeaderField("authorization", mJwToken) //add the JWT as a header
//                .build().execute();
//    }
//
//    private void handleChatsPostOnPostExecute(String result) {
//        // parse JSON
//        try {
//            JSONObject root = new JSONObject(result);
//            if (root.has(getString(R.string.keys_json_chats_chatlist))) {
//
//                JSONArray data = root.getJSONArray(
//                        getString(R.string.keys_json_chats_chatlist));
//                List<Chat> chats = new ArrayList<>();
//                for(int i = 0; i < data.length(); i++) {
//                    JSONObject jsonChat = data.getJSONObject(i);
//                    chats.add(new Chat.Builder(
//                            jsonChat.getString(getString(R.string.keys_json_chats_chatid)),
//                            jsonChat.getString(getString(R.string.keys_json_chats_name)))
//                            .build());
//                }
//
//                mChats = chats;
//                inflateView();
//            } else {
//                Log.e("ERROR!", "No data array");
//                // notify user in the future
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Log.e("ERROR!", e.getMessage());
//            // notify user in the future
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        callWebServiceforChats();
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_allchats_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MyAllChatsRecyclerViewAdapter(mChats, mListener));
        }
        return view;
    }

    /**
     * Override onCreateOptionsMenu to populate the options menu
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Add menu entries
        MenuItem newChatMenuItem = menu.add("Create new chat");
        newChatMenuItem.setOnMenuItemClickListener(this::newChatMenuItemListener);

        // NOTE: this super call adds the logout button so we don't have to worry about that
        super.onCreateOptionsMenu(menu, inflater);
    }

    private boolean newChatMenuItemListener(MenuItem menuItem) {
        // do a sendAsyncTask to getallcontacts
        // which in the postExecute call the addNewContact fragment

        // for now just call the fragment
        Fragment frag = new NewChatFragment();
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.key_credentials), mCreds);
        args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
        frag.setArguments(args);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, frag)
                .addToBackStack(null)
                .commit();

        return true;
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
        void onListFragmentInteraction(Chat item);
    }
}
