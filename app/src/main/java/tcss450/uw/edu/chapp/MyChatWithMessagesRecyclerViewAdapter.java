package tcss450.uw.edu.chapp;

import android.icu.util.Calendar;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import tcss450.uw.edu.chapp.ChatWithMessagesFragment.OnListFragmentInteractionListener;
import tcss450.uw.edu.chapp.chat.Message;

import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;

/**
 * The recyclerView adapter for diplaying messages and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 *
 * @author Jessica Medrzycki
 * @version 02/19/19
 */
public class MyChatWithMessagesRecyclerViewAdapter extends RecyclerView.Adapter<MyChatWithMessagesRecyclerViewAdapter.ViewHolder> {

    private final List<Message> mValues;
    private final OnListFragmentInteractionListener mListener;
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    public MyChatWithMessagesRecyclerViewAdapter(List<Message> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_chatwithmessages, parent, false);
        return new ViewHolder(view);
    }

//    // Inflates the appropriate layout according to the ViewType.
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view;
//
//        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
//            view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_message_sent, parent, false);
//            return new SentMessageHolder(view);
//        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
//            view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_message_received, parent, false);
//            return new ReceivedMessageHolder(view);
//        }
//
//        return null;
//    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        Message message = (Message) mValues.get(position);

//        if (message.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
//            // If the current user is the sender of the message
//            return VIEW_TYPE_MESSAGE_SENT;
//        } else {
//            // If some other user sent the message
//            return VIEW_TYPE_MESSAGE_RECEIVED;
//        }
        return VIEW_TYPE_MESSAGE_RECEIVED;
    }

//    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
//    @Override
//    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
//        Message message = (Message) mValues.get(position);
//
//        switch (holder.getItemViewType()) {
//            case VIEW_TYPE_MESSAGE_SENT:
//                ((SentMessageHolder) holder).bind(message);
//                break;
//            case VIEW_TYPE_MESSAGE_RECEIVED:
//                ((ReceivedMessageHolder) holder).bind(message);
//        }
//    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
//        holder.mMessage.setText(mValues.get(position).getMessage());
//        holder.mTimestamp.setText(mValues.get(position).getTimestamp());

    }

//    private String formateDateTime(Timestamp time){
//        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
//        cal.setTimeInMillis(time);
//        return DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();
//    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            //timeText = (TextView) itemView.findViewById(R.id.text_message_time);
        }
        void bind(Message message) {
            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            //timeText.setText(Utils.formatDateTime(message.getTimestamp()));
        }
    }
    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            //timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            nameText = (TextView) itemView.findViewById(R.id.text_message_name);
        }

        void bind(Message message) {
            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            //timeText.setText(Utils.formatDateTime(message.getTimestamp()));

            nameText.setText(message.getUsername());

        }
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
//        public final TextView mMessage;
//        public final TextView mTimestamp;
        public Message mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
//            mIdView = (TextView) view.findViewById(R.id.item_number);
//            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mItem.getMessage() + "'";
        }
    }
}
