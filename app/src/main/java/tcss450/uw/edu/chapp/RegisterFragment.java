package tcss450.uw.edu.chapp;

import android.content.Context;
import android.content.res.Resources;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RegisterFragment.OnRegisterFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class RegisterFragment extends Fragment {

    private OnRegisterFragmentInteractionListener mListener;
    private Credentials mCredentials;

    public RegisterFragment() {
        // Required empty public constructor
    }

    public void validateRegisterCredentials(View v) {
        // Fetch Values
        Resources res = getContext().getResources();
        int minPasswordSize = res.getInteger(R.integer.reg_password_min_chars);
        int maxPasswordSize = res.getInteger(R.integer.reg_password_max_chars);
        int minUsernameSize = res.getInteger(R.integer.reg_username_min_chars);
        int maxUsernameSize = res.getInteger(R.integer.reg_username_max_chars);

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

        boolean firstNameEmpty = "".equals(firstName);
        boolean lastNameEmpty = "".equals(lastName);

        boolean userNameEmpty = "".equals(userName);
        boolean userNameTooShort = userName.length() < minUsernameSize;
        boolean userNameTooLong = userName.length() > maxUsernameSize;

        boolean passwordEmpty = "".equals(password);
        boolean passwordTooShort = password.length() < minPasswordSize;
        boolean passwordTooLong = password.length() > maxPasswordSize;
        boolean passwordsMatch = password.equals(passwordConfirm);

        boolean passwordHasSpecialChar = checkForSpecialChar(password);
        boolean passwordHasNumber = false;
        boolean passwordHasUpperCase = false;
        boolean passwordHasLowerCase = false;

        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);
            if (Character.isDigit(ch)) {
                passwordHasNumber = true;
            } else if (Character.isLowerCase(ch)) {
                passwordHasLowerCase = true;
            } else if (Character.isUpperCase(ch)) {
                passwordHasUpperCase = true;
            }
        }



        boolean passwordConfirmEmpty = "".equals(passwordConfirm);

        // Check if the email has exactly a single @
        boolean moreThanOneAtInEmail = false;
        boolean atInEmail = false;
        for (char c : email.toCharArray()) {
            if (c == '@') {
                if (atInEmail) {
                    moreThanOneAtInEmail = true;
                }
                atInEmail = true;
            }
        }


        boolean registerValid = !emailEmpty && atInEmail && !moreThanOneAtInEmail
                && !firstNameEmpty && !lastNameEmpty
                && !userNameEmpty && !userNameTooShort && !userNameTooLong
                && !passwordEmpty && !passwordTooShort && !passwordTooLong
                && passwordsMatch && passwordHasSpecialChar && passwordHasNumber
                && passwordHasUpperCase && passwordHasLowerCase
                && !passwordConfirmEmpty;

        if (registerValid) {
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
                    .onPreExecute(this::handleRegisterOnPre)
                    .onPostExecute(this::handleRegisterOnPost)
                    .onCancelled(this::handleErrorsInTask)
                    .build().execute();
        } else {
            // notify of all problems
            //email error messages
            if (emailEmpty) {
                emailEditText.setError(getString(R.string.reg_error_email_empty));
            } else if (moreThanOneAtInEmail) {
                emailEditText.setError(getString(R.string.reg_error_email_too_many_at));
            } else if (!atInEmail){
                emailEditText.setError(getString(R.string.reg_error_email_no_at));
            }

            //password error messages
            if (passwordEmpty) {
                passwordEditText.setError(getString(R.string.reg_error_password_empty));
            } else if (passwordTooShort || passwordTooLong) {
                passwordEditText.setError(getString(R.string.reg_error_password_outofbounds));
            } else if (!passwordHasSpecialChar) {
                passwordEditText.setError(getString(R.string.reg_error_password_nospecialchar));
            } else if (!passwordHasNumber) {
                passwordEditText.setError(getString(R.string.reg_error_password_no_number));
            } else if (!passwordHasUpperCase) {
                passwordEditText.setError(getString(R.string.reg_error_password_no_upper_case));
            } else if (!passwordHasLowerCase) {
                passwordEditText.setError(getString(R.string.reg_error_password_no_lower_case));
            } else if (!passwordsMatch) {
                passwordEditText.setError(getString(R.string.reg_error_password_doesnotmatch));
            }

            //password confirm error messages
            if (passwordConfirmEmpty) {
                passwordConfirmEditText.setError(getString(R.string.reg_error_password_confirm_empty));
            }

            //first name error messages
            if (firstNameEmpty) {
                firstNameEditText.setError(getString(R.string.reg_error_firstname_empty));
            }

            //last name error messages
            if (lastNameEmpty) {
                lastNameEditText.setError(getString(R.string.reg_error_lastname_empty));
            }

            //user name error messages
            if (userNameEmpty) {
                userNameEditText.setError(getString(R.string.reg_error_username_empty));
            } else if (userNameTooShort || userNameTooLong) {
                userNameEditText.setError(getString(R.string.reg_error_username_outofbounds));
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
    private void handleRegisterOnPre() {
        mListener.onWaitFragmentInteractionShow();
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleRegisterOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success =
                    resultsJSON.getBoolean(
                            getString(R.string.keys_json_register_success));

            System.out.println(resultsJSON);
            if (success) {
                //Login was successful. Switch to the LoginFragment.
                mListener.onRegisterSuccess(mCredentials);
                mListener.onWaitFragmentInteractionHide();
                return;

            } else {
                //register was unsuccessful. Don’t switch fragments and
                // inform the user

                //if we get here, then either the user name or the email already exists.
                JSONObject errorJSON = resultsJSON.getJSONObject(getString(R.string.keys_json_register_error));
                String errorDetail = errorJSON.getString(getString(R.string.keys_json_register_error_message));
                String errorCode = errorJSON.getString(getString(R.string.keys_json_register_code));

                //if the error code is the error code from the database that the key already exists
                if(errorCode.equals(getString(R.string.reg_error_code_key_already_exists))) {
                    //use regex to get the key and value from the errorDetail
                    Pattern p = Pattern.compile(getString(R.string.reg_error_regex_detail), Pattern.CASE_INSENSITIVE);
                    //Key (email)=(test@test) already exists.
                    //Pattern p = Pattern.compile("\\((.*?)\\)=\\((.*?)\\)");
                    Matcher m = p.matcher(errorDetail);
                    if (m.find()) {
                        //get regex groups
                        String key = m.group(1);
                        String value = m.group(2);
                        String errorMessage = value + " " + key + " already exists";
                        //if the key is the email, then set the error on the email
                        if(key.equals("email")) {
                            ((TextView) getView().findViewById(R.id.field_email_register))
                                    .setError(errorMessage);
                        }
                        //if the key is the username, then set the error on the email
                        else if(key.equals("username")) {
                            ((TextView) getView().findViewById(R.id.field_user_name_register))
                                    .setError(errorMessage);
                        }
                    }

                } else { //if we get here, then the error is not the user name or email already existing.
                    ((TextView) getView().findViewById(R.id.field_email_register))
                            .setError("Register Unsuccessful");
                }



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
     * private helper method that will check the string for the existance of a
     * special character defined in register_values.xml
     * @param string is the string to check for special characters
     * @return boolean if the string has a special character
     */
    private boolean checkForSpecialChar(String string) {
        String specialCharacters = getString(R.string.reg_password_special_chars);
        boolean result = false;
        for (int i = 0; i < specialCharacters.length(); i++) {
            char specChar = specialCharacters.charAt(i);
            if (string.contains(Character.toString(specChar))) {
                result = true;
            }
        }
        return result;
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


