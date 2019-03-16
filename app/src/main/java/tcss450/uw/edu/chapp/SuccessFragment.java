package tcss450.uw.edu.chapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tcss450.uw.edu.chapp.model.Credentials;


/**
 * A Fragment that shows an introduction message and Chapp logo to the user.
 *
 * @author Mike Osborne, Trung Thai, Michael Josten, Jessica Medrzycki
 */
public class SuccessFragment extends Fragment {
    public SuccessFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_success, container, false);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getArguments() != null) {
            Credentials creds = (Credentials) getArguments().getSerializable(getString(R.string.key_credentials));

            updateUserInfo(creds);
        }
    }

    /**
     * method that will set the username that is displayed on the success screen.
     * @param creds
     */
    private void updateUserInfo(Credentials creds) {
        TextView centerText = getActivity().findViewById(R.id.text_success_email);
        String message = "Welcome to Chapp ";
        if (!creds.getUsername().isEmpty()) {
            centerText.setText(message + creds.getUsername() + "!");
        } else {
            centerText.setText(message + creds.getEmail() + "!");
        }

    }
}
