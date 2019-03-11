package tcss450.uw.edu.chapp.weather;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 */
public class WeatherLocationContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<WeatherLocationItem> ITEMS = new ArrayList<WeatherLocationItem>();
    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(WeatherLocationItem item) {
        ITEMS.add(item);
    }

    private static WeatherLocationItem createDummyItem(int position) {
        return new WeatherLocationItem(String.valueOf(position), "petersburg, ru", -100, 120, "someData");
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class WeatherLocationItem {
        public final String id;
        public final String city;
        public final double lat;
        public final double lon;
        public final String prefString;


        public WeatherLocationItem(String id, String city, double lat, double lon, String prefString) {
            this.id = id;
            this.city = city;
            this.lat = lat;
            this.lon = lon;
            this.prefString = prefString;
        }

        @Override
        public String toString() {
            return prefString;
        }
    }
}
