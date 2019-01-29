package tcss450.uw.edu.phishapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tcss450.uw.edu.phishapp.blog.BlogGenerator;
import tcss450.uw.edu.phishapp.blog.BlogPost;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnBlogListFragmentInteractionListener}
 * interface.
 */
public class BlogFragment extends Fragment {

    public static final String ARG_BLOG_LIST = "blogs lists";
    private static final String ARG_COLUMN_COUNT = "column-count";
    private List<BlogPost> mBlogs;

    private int mColumnCount = 1;
    private OnBlogListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BlogFragment() {
    }

    @SuppressWarnings("unused")
    public static BlogFragment newInstance(int columnCount) {
        BlogFragment fragment = new BlogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBlogs = new ArrayList<BlogPost>(
                    Arrays.asList((BlogPost[]) getArguments().getSerializable(ARG_BLOG_LIST)));
        } else {
            mBlogs = Arrays.asList(BlogGenerator.BLOGS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blog_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MyBlogRecyclerViewAdapter(mBlogs, mListener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBlogListFragmentInteractionListener) {
            mListener = (OnBlogListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBlogListFragmentInteractionListener");
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnBlogListFragmentInteractionListener {
        void onListFragmentInteraction(BlogPost item);
    }
}
