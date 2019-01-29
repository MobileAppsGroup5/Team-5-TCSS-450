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
 * {@link RegisterFragment.OnRegisterFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class RegisterFragment extends Fragment {

    public static final int MINIMUM_PASSWORD_SIZE = 6;

    private OnRegisterFragmentInteractionListener mListener;
    private Credentials mCredentials;

    public RegisterFragment() {
        // Required empty public constructor
    }

    public void validateRegisterCredentials(View v) {
        // Fetch Values
        EditText emailEditText = getActivity().findViewById(R.id.field_email_register);
        EditText passwordEditText = getActivity().findViewById(R.id.field_password_register);
        EditText passwordConfirmEditText = getActivity().findViewById(R.id.field_password_register_confirm);
        EditText firstNameEditText = getActivity().findViewById(R.id.field_first_name_register);
        EditText lastNameEditText = getActivity().findViewById(R.id.field_last_name_register);
        EditText userNameEditText = getActivity().findViewById(R.id.field_user_name_register);

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String passwordConfirm = passwordConfirmEditText.getText().toString();
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String userName = userNameEditText.getText().toString();

        // Validate
        boolean emailEmpty = "".equals(email);
        boolean passwordEmpty = "".equals(password);
        boolean passwordConfirmEmpty = "".equals(passwordConfirm);
        boolean firstNameEmpty = "".equals(firstName);
        boolean lastNameEmpty = "".equals(lastName);
        boolean userNameEmpty = "".equals(userName);
        boolean passwordTooShort = password.length() < MINIMUM_PASSWORD_SIZE;
        boolean passwordConfirmTooShort = passwordConfirm.length() < MINIMUM_PASSWORD_SIZE;
        boolean passwordsMatch = password.equals(passwordConfirm);
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

        if (!emailEmpty && !passwordEmpty && !passwordConfirmEmpty && !passwordTooShort
                && !passwordConfirmTooShort && !firstNameEmpty && !lastNameEmpty && !userNameEmpty) {
            // Successfully Verified (client side)!
            Credentials credentials = new Credentials.Builder(email, password)
                    .addFirstName(firstName)
                    .addLastName(lastName)
                    .addUsername(userName)
                    .build();

            // build the web service URL
            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_register))
                    .build();
            // build the JSONObject
            JSONObject msg = credentials.asJSONObject();
            mCredentials = credentials;
            // instantiate and execute the AsyncTask.
            new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::handleLoginOnPre)
                    .onPostExecute(this::handleLoginOnPost)
                    .onCancelled(this::handleErrorsInTask)
                    .build().execute();
        } else {
            // notify of all problems
            if (emailEmpty) {
                emailEditText.setError("Please enter an Email");
            } else if (moreThanOneAtInEmail) {
                emailEditText.setError("Too many @ symbols in Email");
            } else if (!atInEmail){
                emailEditText.setError("@ symbol must be in Email");
            }

            if (passwordEmpty) {
                passwordEditText.setError("Please enter a password");
            } else if (passwordTooShort) {
                passwordEditText.setError("Password must be at least " + MINIMUM_PASSWORD_SIZE
                        + " characters");
            } else if (!passwordsMatch) {
                passwordEditText.setError("Passwords do not match");
            }

            if (passwordConfirmEmpty) {
                passwordConfirmEditText.setError("Please enter a password");
            } else if (passwordConfirmTooShort) {
                passwordConfirmEditText.setError("Password must be at least " + MINIMUM_PASSWORD_SIZE
                        + " characters");
            }

            if (firstNameEmpty) {
                firstNameEditText.setError("Please enter a first name");
            }

            if (lastNameEmpty) {
                lastNameEditText.setError("Please enter a last name");
            }

            if (userNameEmpty) {
                userNameEditText.setError("Please enter a user name");
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_register, container, false);

        Button b = v.findViewById(R.id.button_register);
        b.setOnClickListener(this::validateRegisterCredentials);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRegisterFragmentInteractionListener) {
            mListener = (OnRegisterFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRegisterFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

            System.out.println(resultsJSON);
            if (success) {
                //Login was successful. Switch to the LoginFragment.
                mListener.onRegisterSuccess(mCredentials);
                mListener.onWaitFragmentInteractionHide();
                return;
            } else {
                //Login was unsuccessful. Don’t switch fragments and
                // inform the user
                ((TextView) getView().findViewById(R.id.field_email_register))
                        .setError("Register Unsuccessful");
            }
            mListener.onWaitFragmentInteractionHide();
        } catch (JSONException e) {
            //It appears that the web service did not return a JSON formatted
            //String or it did not have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
            mListener.onWaitFragmentInteractionHide();
            ((TextView) getView().findViewById(R.id.field_email_register))
                    .setError("Register Unsuccessful");
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
    public interface OnRegisterFragmentInteractionListener extends WaitFragment.OnFragmentInteractionListener {
        void onRegisterSuccess(Credentials theCredentials);
    }
}
