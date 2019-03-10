package tcss450.uw.edu.chapp.weather;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * class of weather hour items which holds information for each item.
 */
public class WeatherHourContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<WeatherHourItem> ITEMS = new ArrayList<WeatherHourItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, WeatherHourItem> ITEM_MAP = new HashMap<String, WeatherHourItem>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createWeatherItem(i));
        }
    }

    private static void addItem(WeatherHourItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static WeatherHourItem createWeatherItem(int position) {
        return new WeatherHourItem(String.valueOf(position), "2pm", 50, "c03n");
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class WeatherHourItem {
        public final String id;
        public final String time;
        public final double temp;
        public final String icon;

        public WeatherHourItem(String id, String time, double temp, String icon) {
            this.id = id;
            this.time = time;
            this.temp = temp;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return this.time;
        }
    }
}
