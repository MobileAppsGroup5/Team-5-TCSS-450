package tcss450.uw.edu.chapp.chat;

import java.io.Serializable;

public class Chat implements Serializable {

    private final String mId;
    private final String mName;

    public static class Builder {
        private final String mName;
        private final String mId;


        public Builder(String id, String name) {
            mName = name;
            mId = id;
        }

        public Chat build() {
            return new Chat(this);
        }

    }

    private Chat(final Builder builder) {
        mName = builder.mName;
        mId = builder.mId;
    }

    public String getName() {
        return mName;
    }

    public String getId() {
        return mId;
    }
}
