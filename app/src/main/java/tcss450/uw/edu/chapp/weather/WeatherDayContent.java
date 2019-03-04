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
public class WeatherDayContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<WeatherDayItem> ITEMS = new ArrayList<WeatherDayItem>();

    /**
     * A weather item representing a piece of content.
     */
    public static class WeatherDayItem {
        public final String id;
        public final String day;
        public final String icon;
        public final double highTemp;
        public final double lowTemp;

        public WeatherDayItem(String id, String day, String icon, double highTemp, double lowTemp) {
            this.id = id;
            this.day = day;
            this.icon = icon;
            this.highTemp = highTemp;
            this.lowTemp = lowTemp;


        }

        @Override
        public String toString() {
            return this.id;
        }
    }
}
