package tcss450.uw.edu.phishapp;

import android.content.Context;
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

import tcss450.uw.edu.phishapp.blog.BlogPost;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BlogPostFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class BlogPostFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private BlogPost mPost;

    public BlogPostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_blog_post, container, false);

        Button b = v.findViewById(R.id.button_view_full_post);
        b.setOnClickListener(this::viewFullPost);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getArguments() != null) {
            BlogPost blogPost = (BlogPost) getArguments().get(getString(R.string.key_blog_post));

            updateContent(blogPost);
        }
    }

    public void updateContent(BlogPost thePost) {
        mPost = thePost;
        FragmentActivity fragActivity = getActivity();
        ((TextView) fragActivity.findViewById(R.id.text_blog_post_title)).setText(thePost.getTitle());
        ((TextView) fragActivity.findViewById(R.id.text_blog_post_pub_date)).setText(thePost.getPubDate());
        TextView sampleText = fragActivity.findViewById(R.id.text_blog_post_teaser);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sampleText.setText(Html.fromHtml(thePost.getTeaser(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            sampleText.setText(Html.fromHtml(thePost.getTeaser()));
        }
    }

    public void viewFullPost(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mPost.getUrl()));
        startActivity(browserIntent);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
    public interface OnFragmentInteractionListener {

    }
}
