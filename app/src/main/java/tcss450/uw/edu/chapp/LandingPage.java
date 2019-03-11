package tcss450.uw.edu.chapp;


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

        TextView centerText = v.findViewById(R.id.text_success_email2);
        String message = "Welcome to Chapp ";
        if (!mCreds.getUsername().isEmpty()) {
            centerText.setText(message + mCreds.getUsername() + "!");
        } else {
            centerText.setText(message + mCreds.getEmail() + "!");
        }

        // populate framelayouts
        ChatsContainerFragment cchatf = new ChatsContainerFragment();
        Bundle args2 = new Bundle();
        args2.putSerializable(getString(R.string.key_credentials)
                , mCreds);
        args2.putSerializable(getString(R.string.keys_intent_jwt)
                , mJwToken);
        args2.putSerializable(getString(R.string.key_flag_compact_mode), true);
        cchatf.setArguments(args2);

        ConnectionsContainerFragment ccontactf = new ConnectionsContainerFragment();
        Bundle args3 = new Bundle();
        args3.putSerializable(getString(R.string.key_credentials)
                , mCreds);
        args3.putSerializable(getString(R.string.keys_intent_jwt)
                , mJwToken);
        args3.putSerializable(getString(R.string.key_flag_compact_mode), true);
        ccontactf.setArguments(args3);

        CurrentWeatherFragment cwf = new CurrentWeatherFragment();

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.framelayout_homelanding_weather, cwf)
                .replace(R.id.framelayout_homelanding_contactlist, ccontactf)
                .replace(R.id.framelayout_homelanding_chatlist, cchatf)
                .commit();

        return v;
    }

}
