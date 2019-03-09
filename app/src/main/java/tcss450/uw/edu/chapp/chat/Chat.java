package tcss450.uw.edu.chapp.chat;

import java.io.Serializable;
import java.util.List;

public class Chat implements Serializable {

    private final String mId;
    private final String mName;
    private final List<String> mUsersInChat;
    private final boolean mHasBeenRead;
    private final String mLastMessageUsername;

    /**
     * This builder is kind of pointless because there is no building, its basically a stand in for
     * the constructor, but we might need builder-y stuff in the future so keep it.
     */
    public static class Builder {
        private final String mId;
        private final String mName;
        private final List<String> mUsersInChat;

        private final boolean mHasBeenRead;
        private final String mLastMessageUsername;


        public Builder(String id, String name, List<String> usersInChat, boolean hasBeenRead, String lastMessageUsername) {
            mName = name;
            mId = id;
            mUsersInChat = usersInChat;
            mHasBeenRead = hasBeenRead;
            mLastMessageUsername = lastMessageUsername;
        }

        public Chat build() {
            return new Chat(this);
        }

    }

    private Chat(final Builder builder) {
        mName = builder.mName;
        mId = builder.mId;
        mUsersInChat = builder.mUsersInChat;
        mHasBeenRead = builder.mHasBeenRead;
        mLastMessageUsername = builder.mLastMessageUsername;
    }

    public String getName() {
        return mName;
    }

    public String getId() {
        return mId;
    }

    public List<String> getUsersInChat() {
        return mUsersInChat;
    }

    public boolean isHasBeenRead() {
        return mHasBeenRead;
    }

    public String getLastMessageUsername() {
        return mLastMessageUsername;
    }
}
