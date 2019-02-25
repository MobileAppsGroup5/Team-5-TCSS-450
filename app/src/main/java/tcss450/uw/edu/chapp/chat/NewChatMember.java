package tcss450.uw.edu.chapp.chat;

import java.io.Serializable;

public class NewChatMember implements Serializable {

    private final String mName;

    public static class Builder {
        private final String mName;


        public Builder(String name) {
            mName = name;
        }

        public NewChatMember build() {
            return new NewChatMember(this);
        }

    }

    private NewChatMember(final Builder builder) {
        mName = builder.mName;
    }

    public String getName() {
        return mName;
    }

}
