package tcss450.uw.edu.chapp;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import tcss450.uw.edu.chapp.AllChatsFragment.OnListFragmentInteractionListener;
import tcss450.uw.edu.chapp.chat.Chat;
import tcss450.uw.edu.chapp.connections.Connection;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Connection} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyAllChatsRecyclerViewAdapter extends RecyclerView.Adapter<MyAllChatsRecyclerViewAdapter.ViewHolder> {

    private final List<Chat> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final ArrayList<String> mUnreadChats;

    public MyAllChatsRecyclerViewAdapter(List<Chat> items, OnListFragmentInteractionListener listener, ArrayList<String> chatIds) {
        mValues = items;
        mListener = listener;
        mUnreadChats = chatIds;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_allchats, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mNameView.setText(mValues.get(position).getName());
        if(mUnreadChats != null){ //while there is a list
            if(mUnreadChats.contains(mValues.get(position).getId())){
                //if the list of unread chats contains the current chat
                //display the unread message
                Log.e("AllChatsRecyclerView", "Received unread chats list item: displaying");
                holder.mUnreadView.setText("UNREAD");
            }
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
        public final TextView mNameView;
        public final TextView mUnreadView;
        public Chat mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.list_item_chat_name);
            mUnreadView = (TextView) view.findViewById(R.id.allchats_text_unread);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
