package tcss450.uw.edu.phishapp;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.phishapp.model.Credentials;
import tcss450.uw.edu.phishapp.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnLoginFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class LoginFragment extends Fragment {

    private OnLoginFragmentInteractionListener mListener;
    private Credentials mCredentials;

    public LoginFragment() {
        // Required empty public constructor
    }

    public void validateCredentials(View v) {
        // Fetch Values
        EditText emailEditText = getActivity().findViewById(R.id.field_email_login);
        EditText passwordEditText = getActivity().findViewById(R.id.field_password);

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // Validate
        boolean emailEmpty = "".equals(email);
        boolean passwordEmpty = "".equals(password);
        // Check if the email has exactly a single @
        boolean moreThanOneAtInEmail = false;
        boolean atInEmail = false;
        for (char c : email.toCharArray()) {
            if (c == '@') {
                if (atInEmail == true) {
                    moreThanOneAtInEmail = true;
                }
                atInEmail = true;
            }
        }

        if (!emailEmpty && !passwordEmpty && atInEmail && !moreThanOneAtInEmail) {
            // Successfully verified (on the client side)!
            Credentials credentials = new Credentials.Builder(email, password).build();

            //build the web service URL
            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_login))
                    .build();
            //build the JSONObject
            JSONObject msg = credentials.asJSONObject();
            mCredentials = credentials;
            //instantiate and execute the AsyncTask.
            new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::handleLoginOnPre)
                    .onPostExecute(this::handleLoginOnPost)
                    .onCancelled(this::handleErrorsInTask)
                    .build().execute();

        } else {
            // Notify of problems
            if (emailEmpty) {
                emailEditText.setError("Please Enter an Email");
            } else if (moreThanOneAtInEmail) {
                emailEditText.setError("Too many @ symbols in Email");
            } else if (!atInEmail){
                emailEditText.setError("@ symbol must be in Email");
            }

            if (passwordEmpty) {
                passwordEditText.setError("Please Enter a Password");
            }
        }
    }

    public void registerClicked(View v) {
        mListener.onRegisterClicked();
    }

    public void fillFields(Credentials theCredentials) {
        EditText emailEditText = getActivity().findViewById(R.id.field_email_login);
        EditText passwordEditText = getActivity().findViewById(R.id.field_password);

        emailEditText.setText(theCredentials.getEmail());
        passwordEditText.setText(theCredentials.getPassword());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getArguments() != null) {
            Credentials creds = (Credentials) getArguments().get(getString(R.string.key_credentials));
            fillFields(creds);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginFragmentInteractionListener) {
            mListener = (OnLoginFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoginFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        Button b = v.findViewById(R.id.button_signin);
        b.setOnClickListener(this::validateCredentials);

        b = v.findViewById(R.id.button_register);
        b.setOnClickListener(this::registerClicked);

        return v;
    }

    /**
     * Handle errors that may occur during the AsyncTask.
     * @param result the error message provide from the AsyncTask
     */
    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
    }

    /**
     * Handle the setup of the UI before the HTTP call to the webservice.
     */
    private void handleLoginOnPre() {
        mListener.onWaitFragmentInteractionShow();
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleLoginOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success =
                    resultsJSON.getBoolean(
                            getString(R.string.keys_json_login_success));
            if (success) {
                //Login was successful. Switch to the loadSuccessFragment.
                mListener.onLoginSuccess(mCredentials,
                        resultsJSON.getString(
                                getString(R.string.keys_json_login_jwt)));
                return;
            } else {
                //Login was unsuccessful. Don’t switch fragments and
                // inform the user
                ((TextView) getView().findViewById(R.id.field_email_login))
                        .setError("Login Unsuccessful");
            }
            mListener.onWaitFragmentInteractionHide();
        } catch (JSONException e) {
            //It appears that the web service did not return a JSON formatted
            //String or it did not have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
            mListener.onWaitFragmentInteractionHide();
            ((TextView) getView().findViewById(R.id.field_email_login))
                    .setError("Login Unsuccessful");
        }
    }


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
    public interface OnLoginFragmentInteractionListener extends WaitFragment.OnFragmentInteractionListener {
        void onLoginSuccess(Credentials theCredentials, String jwt);
        void onRegisterClicked();
    }


}