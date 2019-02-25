package tcss450.uw.edu.chapp.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Contact> ITEMS = new ArrayList<Contact>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Contact> CONTACTS_MAP = new HashMap<String, Contact>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createContacts(i));
        }
    }

    private static void addItem(Contact contactName) {
        ITEMS.add(contactName);
        CONTACTS_MAP.put(contactName.id, contactName);
    }

    private static Contact createContacts(int position) {
        return new Contact(String.valueOf(position), "Contact " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Contact: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class Contact {
        public final String id;
        public final String username;
        public final String details;

        public Contact(String id, String content, String details) {
            this.id = id;
            this.username = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return username;
        }
    }
}
