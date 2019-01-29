package tcss450.uw.edu.phishapp;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tcss450.uw.edu.phishapp.blog.BlogPost;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link tcss450.uw.edu.phishapp.blog.BlogPost} and makes a call to the
 * specified {@link BlogFragment.OnBlogListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyBlogRecyclerViewAdapter extends RecyclerView.Adapter<MyBlogRecyclerViewAdapter.ViewHolder> {

    private final List<BlogPost> mValues;
    private final BlogFragment.OnBlogListFragmentInteractionListener mListener;

    public MyBlogRecyclerViewAdapter(List<BlogPost> items, BlogFragment.OnBlogListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_blog, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mTitleView.setText(mValues.get(position).getTitle());
        holder.mPublishDateView.setText(mValues.get(position).getPubDate());
        // Cut down the teaser to about 100 characters
        String shortTeaser = mValues.get(position).getTeaser().substring(0, 101);
        while (shortTeaser.toCharArray()[shortTeaser.length() - 1] != ' ') {
            shortTeaser = shortTeaser.substring(0, shortTeaser.length() - 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.mSampleView.setText(Html.fromHtml(shortTeaser, Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.mSampleView.setText(Html.fromHtml(shortTeaser));
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final TextView mPublishDateView;
        public final TextView mSampleView;
        public BlogPost mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.text_view_blog_title);
            mPublishDateView = (TextView) view.findViewById(R.id.text_view_blog_publish_date);
            mSampleView = (TextView) view.findViewById(R.id.text_view_blog_sample);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }
}
