package tcss450.uw.edu.chapp.connections;

import java.io.Serializable;

public class Connection implements Serializable {

    public final String usernameA;
    public final String usernameB;
    public final int verified;

    public static class Builder {
        private final String usernameA;
        private final String usernameB;
        private final int verified;


        public Builder(String theUsernameA, String theUsernameB, int theVerified) {
            usernameA = theUsernameA;
            usernameB = theUsernameB;
            verified = theVerified;

        }

        public Connection build() {
            return new Connection(this);
        }

    }

    private Connection(final Builder builder) {
        usernameA = builder.usernameA;
        usernameB = builder.usernameB;
        verified = builder.verified;
    }

    public String getUsernameA() {
        return usernameA;
    }

    public String getUsernameB() {
        return usernameB;
    }

    public int getVerified() {
        return verified;
    }
}