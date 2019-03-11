package tcss450.uw.edu.chapp;

import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.chapp.ChatsFragment.OnListFragmentInteractionListener;
import tcss450.uw.edu.chapp.chat.Chat;
import tcss450.uw.edu.chapp.connections.Connection;
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Connection} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 *
 * NOTE: This class is heavily based off of {@link MyConnectionsRecyclerViewAdapter}, the code is better
 * documented there
 */
public class MyChatsRecyclerViewAdapter extends RecyclerView.Adapter<MyChatsRecyclerViewAdapter.ViewHolder> {

    public static final int NOT_ACCEPTED = 0;

    public static final int ACCEPTED = 1;

    private final List<Chat> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final Credentials mCreds;
    private final Context mContext;
    private final String mJwToken;
    private boolean mCompactMode;

    private PropertyChangeSupport myPcs = new PropertyChangeSupport(this);

    public MyChatsRecyclerViewAdapter(List<Chat> items, OnListFragmentInteractionListener listener,
                                      Credentials credentials, Context context, String jwToken, boolean compactMode) {
        Collections.reverse(items);
        // if in compact mode, limit to first 5
        if (compactMode) {
            if (items.size() < 5) {
                mValues = items;
            } else {
                mValues = items.subList(0, 5);
            }
        } else {
            mValues = items;
        }
        mListener = listener;
        mCreds = credentials;
        mContext = context;
        mJwToken = jwToken;
        mCompactMode = compactMode;
    }

    @Override
    public int getItemViewType(int position) {
        // find our position in the usernames
        int ourIndex = 0;
        for (int i = 0; i < mValues.get(position).getUsersInChat().size(); i++) {
            if (mCreds.getUsername().equals(mValues.get(position).getUsersInChat().get(i))) {
                ourIndex = i;
            }
        }

        if (mValues.get(position).getAcceptedFlags().get(ourIndex)) {
            return ACCEPTED;
        } else {
            return NOT_ACCEPTED;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_chats_accepted, parent, false);
        if (viewType == ACCEPTED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_chats_accepted, parent, false);
        } else if (viewType == NOT_ACCEPTED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_chats_not_accepted, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mNameView.setText(mValues.get(position).getName());

        // populate chatroom member text view
        StringJoiner joiner = new StringJoiner(", ");
        holder.mItem.getUsersInChat().forEach(username -> {
            // don't add our own username
            if (!username.equals(mCreds.getUsername())) {
                joiner.add(username);
            }
        });

        if (joiner.length() == 0) {
            holder.mMembersView.setText("Members: None :(");
        } else {
            holder.mMembersView.setText("Members: " + joiner.toString());
        }

        if (holder.getItemViewType() == NOT_ACCEPTED) {
            // Accept only shows up if this is not accepted
            holder.mView.findViewById(R.id.image_accept_chat).setOnClickListener(this::handleAcceptChat);
        }

        // All adapter items will have a cancel button, handle it with one listener
        holder.mView.findViewById(R.id.image_cancel_chat).setOnClickListener(this::handleDeclineCancelChat);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // notify the activity to switch
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    private void handleDeclineCancelChat(View view) {
        View parent = (View) view.getParent();
        String chatName = ((TextView)parent.findViewById(R.id.list_item_chat_name)).getText().toString();
        String tempid = "";
        for (Chat chat : mValues) {
            if (chat.getName().equals(chatName)) {
                tempid = chat.getId();
            }
        }
        // final variable to use in lambdas
        final String chatid = tempid;

        // confirm with the user
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Are you sure you want to leave " + chatName + "?")
                .setTitle("Delete/Cancel?")
                .setPositiveButton("YES", (dialog, which) -> {
                    leaveChat(chatid);
                })
                .setNegativeButton("CANCEL", (dialog, which) -> {});
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void leaveChat(String chatid) {
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("chatid", chatid);
            messageJson.put("username", mCreds.getUsername());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(mContext.getString(R.string.ep_base_url))
                .appendPath(mContext.getString(R.string.ep_chats_base))
                .appendPath(mContext.getString(R.string.ep_chats_leave))
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                .onPostExecute(this::handleChatsChangePostExecute)
                .onCancelled(error -> Log.e("MyChatsRecyclerViewAdapter", error))
                .addHeaderField("authorization", mJwToken)
                .build().execute();
    }

    private void handleAcceptChat(View view) {
        View parent = (View) view.getParent();
        String chatName = ((TextView)parent.findViewById(R.id.list_item_chat_name)).getText().toString();
        String chatid = "";
        for (Chat chat : mValues) {
            if (chat.getName().equals(chatName)) {
                chatid = chat.getId();
            }
        }

        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put(mContext.getString(R.string.keys_json_username), mCreds.getUsername());
            messageJson.put(mContext.getString(R.string.keys_json_chats_chatid), chatid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(mContext.getString(R.string.ep_base_url))
                .appendPath(mContext.getString(R.string.ep_chats_base))
                .appendPath(mContext.getString(R.string.ep_chats_accept_request))
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                .onPostExecute(this::handleChatsChangePostExecute)
                .onCancelled(error -> Log.e("MyConnectionsRecyclerViewAdapter", error))
                .addHeaderField("authorization", mJwToken)
                .build().execute();
    }

    private void handleChatsChangePostExecute(String result) {
        // notify listeners that we need to refresh
        Log.e("RESULT", result);
        myPcs.firePropertyChange(ChatsContainerFragment.PROPERTY_REFRESH_CHATS, null, "thx");
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        myPcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        myPcs.removePropertyChangeListener(listener);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mMembersView;
        public Chat mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.list_item_chat_name);
            mMembersView = (TextView) view.findViewById(R.id.list_item_chat_members);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
