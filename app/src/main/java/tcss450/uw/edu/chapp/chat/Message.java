package tcss450.uw.edu.chapp.chat;

import java.io.Serializable;

/**
 * Message Object that holds Message information from the database, from
 * the endpoint chapp-server-5.herokuapp.com/messaging/getAll
 *
 * @version 02/20/19
 */
public class Message implements Serializable {
    private final String mMessage;
    private final String mUsername;
    private final String mTimestamp;

    public static class Builder {
        private final String mMessage;
        private final String mUsername;
        private final String mTimestamp;


        public Builder(String username, String message, String time) {
            mUsername = username;
            mMessage = message;
            mTimestamp = time;

        }

        public Message build() {
            return new Message(this);
        }

    }

    private Message(final Builder builder) {
        mMessage = builder.mMessage;
        mUsername = builder.mUsername;
        mTimestamp = builder.mTimestamp;
    }


    public String getMessage() {
        return mMessage;
    }

    public String getTimestamp() {
        return mTimestamp;
    }

    public String getUsername() {
        return mUsername;
    }


}
