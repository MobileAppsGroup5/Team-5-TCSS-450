package tcss450.uw.edu.chapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import tcss450.uw.edu.chapp.setlist.SetList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SetListViewFragment extends Fragment {

//    private OnFragmentInteractionListener mListener;

    private SetList mSetList;

    public SetListViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_set_list_view, container, false);

        Button b = v.findViewById(R.id.button_view_set_list_url);
        b.setOnClickListener(this::viewSetUrl);

        return v;
    }

    private void viewSetUrl(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mSetList.getUrl()));
        startActivity(browserIntent);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getArguments() != null) {
            SetList setList = (SetList) getArguments().get(getString(R.string.key_set_list));

            updateContent(setList);
        }
    }

    public void updateContent(SetList theSetList) {
        mSetList = theSetList;
        FragmentActivity fragActivity = getActivity();
        ((TextView) fragActivity.findViewById(R.id.text_set_list_view_long_date)).setText(theSetList.getLongDate());
        ((TextView) fragActivity.findViewById(R.id.text_set_list_view_location)).setText(theSetList.getLocation());
        TextView sampleText = fragActivity.findViewById(R.id.text_set_list_view_data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sampleText.setText(Html.fromHtml(mSetList.getSetListData(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            sampleText.setText(Html.fromHtml(mSetList.getSetListData()));
        }
        sampleText = fragActivity.findViewById(R.id.text_set_list_view_notes);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sampleText.setText(Html.fromHtml(mSetList.getSetListNotes(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            sampleText.setText(Html.fromHtml(mSetList.getSetListNotes()));
        }

//        TextView venueText = fragActivity.findViewById(R.id.text_view_venue);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            venueText.setText(Html.fromHtml(theSetList.getVenue(), Html.FROM_HTML_MODE_COMPACT));
//        } else {
//            venueText.setText(Html.fromHtml(theSetList.getVenue()));
//        }
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

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
//    public interface OnFragmentInteractionListener {
//        void onFragmentInteraction(Uri uri);
//    }
}
