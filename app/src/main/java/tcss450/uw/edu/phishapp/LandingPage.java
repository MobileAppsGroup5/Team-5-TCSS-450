package tcss450.uw.edu.phishapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * The fragment that should display the dynamic content for the
 * app's home page.
 *
 *
 * A simple {@link Fragment} subclass.
 */
public class LandingPage extends Fragment {


    public LandingPage() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_landing_page, container, false);


        return v;
    }

}
