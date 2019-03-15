package tcss450.uw.edu.chapp;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tcss450.uw.edu.chapp.model.Credentials;


/**
 * A Fragment that shows an introduction message and Chapp logo to the user.
 */
public class SuccessFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

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

    private void updateUserInfo(Credentials creds) {
        TextView centerText = getActivity().findViewById(R.id.text_success_email);
        String message = "Welcome to Chapp ";
        if (!creds.getUsername().isEmpty()) {
            centerText.setText(message + creds.getUsername() + "!");
        } else {
            centerText.setText(message + creds.getEmail() + "!");
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
