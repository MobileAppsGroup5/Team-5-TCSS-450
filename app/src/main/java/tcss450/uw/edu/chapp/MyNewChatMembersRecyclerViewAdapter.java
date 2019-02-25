package tcss450.uw.edu.chapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tcss450.uw.edu.chapp.NewChatMembersFragment.OnListFragmentInteractionListener;
import tcss450.uw.edu.chapp.chat.NewChatMember;

import java.util.List;

/**
 * An adaptor for NewChatMembers
 *
 * @author Michael Osborne
 */
public class MyNewChatMembersRecyclerViewAdapter extends RecyclerView.Adapter<MyNewChatMembersRecyclerViewAdapter.ViewHolder> {

    private final List<NewChatMember> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyNewChatMembersRecyclerViewAdapter(List<NewChatMember> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_newchatmembers, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mNameView.setText(mValues.get(position).getName());

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
        public final TextView mNameView;
        public NewChatMember mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.text_view_new_chat_member_name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
