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
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tcss450.uw.edu.chapp.connections.Connection;
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;

/**
 * A fragment for creating a new connection
 */
public class NewConnectionFragment extends Fragment implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {

    public static String ARG_CRED_LIST = "credentials list}{{}";

    private Credentials mCreds;
    private String mJwToken;
    private List<Credentials> mMemberList;
    private List<Connection> mConnectionList;
    private String[] searchBySelections;
    private AutoCompleteTextView mAutoCompleteSearchBox;
    private Spinner mMemberSearchBySpinner;

    private List<String> userNameList;
    private List<String> emailList;
    private List<String> firstNameList;
    private List<String> lastNameList;

    private List<List<String>> searchOptionLists;

    private PropertyChangeSupport myPcs = new PropertyChangeSupport(this);

//    private OnFragmentInteractionListener mListener;

    public NewConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMemberList = new ArrayList<>(Arrays.asList((Credentials[])getArguments().getSerializable(ARG_CRED_LIST)));
            mConnectionList = (ArrayList<Connection>) getArguments().getSerializable(getString(R.string.key_intent_connections));
            mJwToken = (String) getArguments().getSerializable(getString(R.string.keys_intent_jwt));
            mCreds = (Credentials) getArguments().getSerializable(getString(R.string.key_credentials));

            setUpSearchArrays();
        }

        searchBySelections = getResources().getStringArray(R.array.member_search_values);
    }

    private void setUpSearchArrays() {
        userNameList = new ArrayList<>();
        emailList = new ArrayList<>();
        firstNameList = new ArrayList<>();
        lastNameList = new ArrayList<>();

        mMemberList.forEach(credentials -> {
            boolean shouldAdd = true;
            // don't show connections that have already been made
            for (int i = 0; i < mConnectionList.size(); i++) {
                if ((credentials.getUsername().equals(mConnectionList.get(i).getUsernameA())
                        && mCreds.getUsername().equals(mConnectionList.get(i).getUsernameB()))
                    || (credentials.getUsername().equals(mConnectionList.get(i).getUsernameB())
                        && mCreds.getUsername().equals(mConnectionList.get(i).getUsernameA()))) {
                    shouldAdd = false;
                }
            }
            if (shouldAdd) {
                Log.e("adding", credentials.getUsername());
                userNameList.add(credentials.getUsername());
                emailList.add(credentials.getEmail());
                firstNameList.add(credentials.getFirstName());
                lastNameList.add(credentials.getLastName());
            }
        });

        // Handle collisions in non-unique fields.
        // This is SLOW but it WORKS.
        // CURRENTLY WE ARE NOT CHECKING FOR COLLISIONS, EVERYTHING IS CONSIDERED A COLLISION
        List<String> noCollisionFirstNames = new ArrayList<>();
        List<String> noCollisionLastNames = new ArrayList<>();

        for (int i = 0; i < firstNameList.size(); i++) {
            boolean firstNameCollision = false;
            boolean lastNameCollision = false;
            for (int j = 0; j < firstNameList.size(); j++) {
                // Uncomment this if to actually collision handle, without this username gets added
                // to every first and last name (which might be better
//                if (i != j) {
                    if (firstNameList.get(i).equals(firstNameList.get(j))) {
                        firstNameCollision = true;
                    }
                    if (lastNameList.get(i).equals(lastNameList.get(j))) {
                        lastNameCollision = true;
                    }
//                }
            }
            if (!userNameList.get(i).equals(mCreds.getUsername())) {
                if (firstNameCollision) {
                    noCollisionFirstNames.add(firstNameList.get(i) + " (user: " + userNameList.get(i) + ")");
                } else {
                    noCollisionFirstNames.add(firstNameList.get(i));
                }
                if (lastNameCollision) {
                    noCollisionLastNames.add(lastNameList.get(i) + " (user: " + userNameList.get(i) + ")");
                } else {
                    noCollisionLastNames.add(lastNameList.get(i));
                }
            }
        }

        // order matters, same order as the spinner simplifies code
        searchOptionLists = new ArrayList<>();
        searchOptionLists.add(userNameList);
        searchOptionLists.add(emailList);
        searchOptionLists.add(noCollisionFirstNames);
        searchOptionLists.add(noCollisionLastNames);

        // Remove unique entries for the current user
        userNameList.remove(mCreds.getUsername());
        emailList.remove(mCreds.getEmail());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_connection, container, false);


        // Initialize spinner with values
        mMemberSearchBySpinner = v.findViewById(R.id.spinner_member_search_by);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.member_search_values, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mMemberSearchBySpinner.setAdapter(adapter);

        mMemberSearchBySpinner.setOnItemSelectedListener(this);

        mAutoCompleteSearchBox = v.findViewById(R.id.auto_complete_new_connection_search);

        mAutoCompleteSearchBox.setOnItemClickListener(this);

        return v;
    }

    private void submitConnectionRequest(String otherUsername) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_connections_base))
                .appendPath(getString(R.string.ep_connections_submit_request))
                .build();

        JSONObject msg = new JSONObject();
        try {
            msg.put("to", otherUsername);
            msg.put("from", mCreds.getUsername());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleAddMemberPostExecute)
                .addHeaderField("authorization", mJwToken) // add the JWT as a header
                .build().execute();
    }

    private void handleAddMemberPostExecute(String result) {
        TextView tv = getActivity().findViewById(R.id.text_view_add_connection_result);
        tv.setVisibility(View.VISIBLE);
        try {
            //This is the result from the web service
            JSONObject res = new JSONObject(result);
            if(res.has("success") && res.getBoolean("success")) {

                //set the output text to show the sent message
                tv.setText("Member added!");
                mAutoCompleteSearchBox.setText("");

            } else {
                // error
                tv.setText("Error, did you already send a request?");
                Log.e("CRASH", result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void swapSearchBoxArrayAdapter(List<String> newList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                newList);


        mAutoCompleteSearchBox.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.equals(mMemberSearchBySpinner)) {
            swapSearchBoxArrayAdapter(searchOptionLists.get(position));
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        myPcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        myPcs.removePropertyChangeListener(listener);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // rip the username out
        String[] splitField = ((TextView)view).getText().toString().split("user: ");
        String otherUsername = splitField[splitField.length-1];
        String currentSpinnerSelection = mMemberSearchBySpinner.getSelectedItem().toString();

        if ("First Name".equals(currentSpinnerSelection) || "Last Name".equals(currentSpinnerSelection)) {
            otherUsername = otherUsername.substring(0, otherUsername.length()-1);
            submitConnectionRequest(otherUsername);
        } else if ("Email".equals(currentSpinnerSelection)) {
            submitConnectionRequest(userNameList.get(emailList.indexOf(otherUsername)));
        } else if ("Username".equals(currentSpinnerSelection)) {
            submitConnectionRequest(otherUsername);
        }

        // Refresh contacts view regardless (can't hurt)
        myPcs.firePropertyChange(ConnectionsContainerFragment.REFRESH_CONNECTIONS, null, "please");
    }
}
