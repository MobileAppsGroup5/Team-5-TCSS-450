package tcss450.uw.edu.chapp.setlist;

public class SetListGenerator {

    public static final SetList[] SETLISTS;
    public static final int COUNT = 20;


    static {
        SETLISTS = new SetList[COUNT];
        for (int i = 0; i < SETLISTS.length; i++) {
            SETLISTS[i] = new SetList
                    .Builder()
                    .addLongDate("error")
                    .addLocation("error")
                    .addVenue("error")
                    .build();
        }
    }

    private SetListGenerator() { }
}
