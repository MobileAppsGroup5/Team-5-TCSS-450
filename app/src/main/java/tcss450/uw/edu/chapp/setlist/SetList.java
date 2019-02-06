package tcss450.uw.edu.chapp.setlist;

import java.io.Serializable;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class SetList implements Serializable {
    private static final long serialVersionUID = -1634677417576883013L;

    private final String mLongDate;
    private final String mLocation;
    private final String mVenue;
    private final String mSetListData;
    private final String mSetListNotes;
    private final String mUrl;

    /**
     * Helper class for building Credentials.
     *
     * @author Charles Bryan
     */
    public static class Builder {

        private String mLongDate = "";
        private String mLocation = "";
        private String mVenue = "";
        private String mSetListData = "";
        private String mSetListNotes = "";
        private String mUrl = "";

        public SetList.Builder addLongDate(final String val) {
            mLongDate = val;
            return this;
        }

        public SetList.Builder addLocation(final String val) {
            mLocation = val;
            return this;
        }

        public SetList.Builder addVenue(final String val) {
            mVenue = val;
            return this;
        }

        public SetList.Builder addSetListData(final String val) {
            mSetListData = val;
            return this;
        }

        public SetList.Builder addSetListNotes(final String val) {
            mSetListNotes = val;
            return this;
        }

        public SetList.Builder addUrl(final String val) {
            mUrl = val;
            return this;
        }

        public SetList build() {
            return new SetList(this);
        }
    }

    /**
     * Construct a Credentials internally from a builder.
     *
     * @param builder the builder used to construct this object
     */
    private SetList(final SetList.Builder builder) {
        mLongDate = builder.mLongDate;
        mLocation = builder.mLocation;
        mVenue = builder.mVenue;
        mSetListData = builder.mSetListData;
        mSetListNotes = builder.mSetListNotes;
        mUrl = builder.mUrl;

    }

    public String getLongDate() {
        return mLongDate;
    }

    public String getLocation() {
        return mLocation;
    }

    public String getVenue() {
        return mVenue;
    }

    public String getSetListData() {
        return mSetListData;
    }

    public String getSetListNotes() {
        return mSetListNotes;
    }

    public String getUrl() { return mUrl; }
}
