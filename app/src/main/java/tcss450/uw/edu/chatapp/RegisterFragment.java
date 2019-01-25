package tcss450.uw.edu.chatapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import tcss450.uw.edu.chatapp.model.Credentials;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RegisterFragment.OnRegisterFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class RegisterFragment extends Fragment {

    public static final int MINIMUM_PASSWORD_SIZE = 6;

    private OnRegisterFragmentInteractionListener mListener;
    private View rootView;

    public RegisterFragment() {
        // Required empty public constructor
    }

    public void validateRegisterCredentials(View v) {
        // Fetch Values
        EditText emailEditText = rootView.findViewById(R.id.field_email_register);
        EditText passwordEditText = rootView.findViewById(R.id.field_password_register);
        EditText passwordConfirmEditText = rootView.findViewById(R.id.field_password_register_confirm);

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String passwordConfirm = passwordConfirmEditText.getText().toString();

        // Validate
        boolean emailEmpty = "".equals(email);
        boolean passwordEmpty = "".equals(password);
        boolean passwordConfirmEmpty = "".equals(passwordConfirm);
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
                && !passwordConfirmTooShort) {
            // Successfully Verified!
            Credentials.Builder credBuilder = new Credentials.Builder(email, password);
            mListener.onRegisterSuccess(credBuilder.build());
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
                passwordConfirmEditText.setError("Please Enter a Password");
            } else if (passwordConfirmTooShort) {
                passwordConfirmEditText.setError("Password must be at least " + MINIMUM_PASSWORD_SIZE
                        + " characters");
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_register, container, false);
        rootView = v;

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
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnRegisterFragmentInteractionListener {
        void onRegisterSuccess(Credentials theCredentials);
    }
}
