package tcss450.uw.edu.chapp;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.weather.CurrentWeatherFragment;


/**
 * The fragment that should display the dynamic content for the
 * app's home page.
 *
 *
 * A simple {@link Fragment} subclass.
 */
public class LandingPage extends Fragment {

    private Credentials mCreds;
    private String mJwToken;
    private OnLandingPageReturnListener mListener;
    private boolean mAlreadyLoaded;

    public LandingPage() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mCreds = (Credentials)getArguments().getSerializable(getString(R.string.key_credentials));
            mJwToken = (String)getArguments().getSerializable(getString(R.string.keys_intent_jwt));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_landing_page, container, false);
        if (mAlreadyLoaded) {
            mListener.reloadLandingPage();
        }

        if (savedInstanceState == null && !mAlreadyLoaded) {
            mAlreadyLoaded = true;
        }


        TextView centerText = v.findViewById(R.id.text_success_email2);
        String message = "Welcome to Chapp ";
        if (!mCreds.getUsername().isEmpty()) {
            centerText.setText(message + mCreds.getUsername() + "!");
        } else {
            centerText.setText(message + mCreds.getEmail() + "!");
        }

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof WaitFragment.OnFragmentInteractionListener) {
            mListener = (OnLandingPageReturnListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLandingPageReturnListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnLandingPageReturnListener {
        void reloadLandingPage();
    }

}
