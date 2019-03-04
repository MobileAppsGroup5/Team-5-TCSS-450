package tcss450.uw.edu.chapp.chat;

import java.io.Serializable;

public class User implements Serializable {

    private final String mName;

    public static class Builder {
        private final String mName;


        public Builder(String name) {
            mName = name;
        }

        public User build() {
            return new User(this);
        }

    }

    private User(final Builder builder) {
        mName = builder.mName;
    }

    public String getName() {
        return mName;
    }

}
