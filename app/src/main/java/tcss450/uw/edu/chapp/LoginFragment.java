package tcss450.uw.edu.chapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import me.pushy.sdk.Pushy;
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;


/**
 * Login fragment will display the login screen for the user to enter their credentials,
 * Will verify the creadentials entered so the user doesn't enter in a password that is
 * invalid.
 * @author Mike Osborne, Trung Thai, Michael Josten, Jessica Medrzycki
 */
public class LoginFragment extends Fragment {

    private OnLoginFragmentInteractionListener mListener;
    private Credentials mCredentials;
    private String mJwt;

    public LoginFragment() {
        // Required empty public constructor
    }

    public void validateCredentials(View v) {
        // Fetch Values
        EditText emailEditText = getActivity().findViewById(R.id.edit_login_email);
        EditText passwordEditText = getActivity().findViewById(R.id.edit_login_password);

        String email = emailEditText.getText().toString().toLowerCase();
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
      //      Credentials credentials = new Credentials.Builder(email, password).build();

            doLogin(new Credentials.Builder(
                    emailEditText.getText().toString().toLowerCase(),
                    passwordEditText.getText().toString())
                    .build());

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

    /**
     * method is called when the register button is clicked, call the main activity
     * that the register is clicked
     * @param v the register button
     */
    public void registerClicked(View v) {
        mListener.onRegisterClicked();
    }

    /**
     * Method is called when the forgot password button is pressed,
     * Will tell the main activity that the forgot password button is pressed.
     * @param v the forgot password button
     */
    public void forgotPasswordClicked(View v) {
        mListener.onForgotPasswordClicked();
    }

    /**
     * Method that will set the email and the password of the login page to the
     * credentials passed
     * @param theCredentials of the user.
     */
    public void fillFields(Credentials theCredentials) {
        EditText emailEditText = getActivity().findViewById(R.id.edit_login_email);
        EditText passwordEditText = getActivity().findViewById(R.id.edit_login_password);

        emailEditText.setText(theCredentials.getEmail());
        passwordEditText.setText(theCredentials.getPassword());
    }

    /**
     * Lifecycle method of the login fragment, if the credentials exist in the shared preferences.
     * then login the user automatically.
     */
    @Override
    public void onStart() {
        super.onStart();
     /*   if (getArguments() != null) {
            Credentials creds = (Credentials) getArguments().get(getString(R.string.key_credentials));
            fillFields(creds);
        }   */

        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);

        // Retrieve the stored credentials from SharedPrefs
        if (prefs.contains(getString(R.string.keys_prefs_email)) &&
                prefs.contains(getString(R.string.keys_prefs_password))) {

            final String email = prefs.getString(getString(R.string.keys_prefs_email), "");
            final String password = prefs.getString(getString(R.string.keys_prefs_password), "");

            // Load the two login EditTexts with the credentials found in SharedPrefs
            EditText emailEdit = getActivity().findViewById(R.id.edit_login_email);
            emailEdit.setText(email);

            EditText passwordEdit = getActivity().findViewById(R.id.edit_login_password);
            passwordEdit.setText(password);

            doLogin(new Credentials.Builder(
                    emailEdit.getText().toString(),
                    passwordEdit.getText().toString())
                    .build());
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


    /**
     * Lifecycle method, set the listeners for the buttons.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        Button b = v.findViewById(R.id.button_signin);
        b.setOnClickListener(this::validateCredentials);

        b = v.findViewById(R.id.button_register);
        b.setOnClickListener(this::registerClicked);

        b = v.findViewById(R.id.tv_forgot_password);
        b.setOnClickListener(this::forgotPasswordClicked);

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

                // Store our token, also augment our credentials with information from the server.
                mJwt = resultsJSON.getString(
                        getString(R.string.keys_json_login_jwt));
                mCredentials = new Credentials.Builder(mCredentials.getEmail(), mCredentials.getPassword())
                        .addFirstName(resultsJSON.getString("firstname"))
                        .addLastName(resultsJSON.getString("lastname"))
                        .addUsername(resultsJSON.getString("username"))
                        .build();
                System.out.println(mCredentials);

                new RegisterForPushNotificationsAsync().execute();

                return;
            } else {
                //Login was unsuccessful. Don’t switch fragments and
                // inform the user
                ((TextView) getView().findViewById(R.id.edit_login_email))
                        .setError(resultsJSON.getString("message"));
            }
            mListener.onWaitFragmentInteractionHide();
        } catch (JSONException e) {
            //It appears that the web service did not return a JSON formatted
            //String or it did not have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
            mListener.onWaitFragmentInteractionHide();
            ((TextView) getView().findViewById(R.id.edit_login_email))
                    .setError("Login Unsuccessful");
        }
    }


    /**
     * Interface for the login fragment to interact with the activity.
     */
    public interface OnLoginFragmentInteractionListener extends WaitFragment.OnFragmentInteractionListener {
        void onLoginSuccess(Credentials theCredentials, String jwt);
        void onRegisterClicked();
        void onForgotPasswordClicked();
    }
    /**
     * Added this functionality in Lab 5
     * saveCredentials method is aimed to store the credentials in the SharedPrefs.
     */
    private void saveCredentials(final Credentials credentials) {
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        // Store the credentials in SharedPrefs
        prefs.edit().putString(getString(R.string.keys_prefs_email), credentials.getEmail()).apply();
        prefs.edit().putString(getString(R.string.keys_prefs_password), credentials.getPassword()).apply();
    }

    /**
     * Helper method from lab5 that does the login process automatically.
     * @author Trung Thai
     */
    private void doLogin(Credentials credentials) {
        // build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_login))
                .build();

        // build the JSONObject
        JSONObject msg = credentials.asJSONObject();

        mCredentials = credentials;

        Log.d("JSON Credentials", msg.toString());

        // instantiate and execute the AsyncTask.
        // Feel free to add a handler for onPreExecution so that a progress bar
        // is displayed or maybe disable buttons.

        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleLoginOnPre)
                .onPostExecute(this::handleLoginOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();


    }

    /**
     * method that will register the device for push notifications.
     */
    private class RegisterForPushNotificationsAsync extends AsyncTask<Void, String, String>
    {
        protected String doInBackground(Void... params) {
            String deviceToken = "";
            try {
                // Assign a unique token to this device
                deviceToken = Pushy.register(getActivity().getApplicationContext());
                //subscribe to a topic (this is a Blocking call)
                Pushy.subscribe("all", getActivity().getApplicationContext());
            }
            catch (Exception exc) {
                cancel(true);
                // Return exc to onCancelled
                return exc.getMessage();
            }
            // Success
            return deviceToken;
        }
        @Override
        protected void onCancelled(String errorMsg) {
            super.onCancelled(errorMsg);
            Log.d("CHAPP", "Error getting Pushy Token: " + errorMsg);
        }
        @Override
        protected void onPostExecute(String deviceToken) {
            // Log it for debugging purposes
            Log.d("CHAPP", "Pushy device token: " + deviceToken);

            //build the web service URL
            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_pushy))
                    .appendPath(getString(R.string.ep_token))
                    .build();
            //build the JSONObject
            JSONObject msg = mCredentials.asJSONObject();

            try {
                msg.put("token", deviceToken);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //instantiate and execute the AsyncTask.
            new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPostExecute(LoginFragment.this::handlePushyTokenOnPost)
                    .onCancelled(LoginFragment.this::handleErrorsInTask)
                    .addHeaderField("authorization", mJwt)
                    .build().execute();
        }
    }

    /**
     * method that will check if the user has a pushy token
     * @param result
     */
    private void handlePushyTokenOnPost(String result) {
        try {
            Log.d("JSON result",result);
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            if (success) {
                if (((Switch)getActivity().findViewById(R.id.switch_stay_logged_in)).isChecked()) {
                    saveCredentials(mCredentials);
                }
                mListener.onLoginSuccess(mCredentials, mJwt);
                return;
            } else {
                //Saving the token wrong. Don’t switch fragments and inform the user
                ((TextView) getView().findViewById(R.id.edit_login_email))
                        .setError("Login Unsuccessful");
            }
            mListener.onWaitFragmentInteractionHide();
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
            mListener.onWaitFragmentInteractionHide();
            ((TextView) getView().findViewById(R.id.edit_login_email))
                    .setError("Login Unsuccessful");
        }
    }
}
