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
 * {@link LoginFragment.OnLoginFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class LoginFragment extends Fragment {

    private OnLoginFragmentInteractionListener mListener;
    private View rootView;

    public LoginFragment() {
        // Required empty public constructor
    }

    public void validateCredentials(View v) {
        // Fetch Values
        EditText emailEditText = rootView.findViewById(R.id.field_email_login);
        EditText passwordEditText = rootView.findViewById(R.id.field_password);

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
            // Successfully verified!
            Credentials.Builder credBuilder = new Credentials.Builder(email, password);
            mListener.onLoginSuccess(credBuilder.build(), null);
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
        EditText emailEditText = rootView.findViewById(R.id.field_email_login);
        EditText passwordEditText = rootView.findViewById(R.id.field_password);

        emailEditText.setText(theCredentials.getEmail());
        passwordEditText.setText(theCredentials.getPassword());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getArguments() != null) {
            Credentials creds = (Credentials) getArguments().get(getString(R.string.credentials_key));
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
        rootView = v;

        Button b = v.findViewById(R.id.button_signin);
        b.setOnClickListener(this::validateCredentials);

        b = v.findViewById(R.id.button_register);
        b.setOnClickListener(this::registerClicked);

        return v;
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
    public interface OnLoginFragmentInteractionListener {
        void onLoginSuccess(Credentials theCredentials, String jwt);
        void onRegisterClicked();
    }


}
