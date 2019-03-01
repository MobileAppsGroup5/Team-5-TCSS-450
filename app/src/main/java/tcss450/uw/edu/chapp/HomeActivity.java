package tcss450.uw.edu.chapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.pushy.sdk.Pushy;
import tcss450.uw.edu.chapp.chat.Chat;
import tcss450.uw.edu.chapp.chat.Message;
import tcss450.uw.edu.chapp.dummy.DummyContent;
import tcss450.uw.edu.chapp.chat.NewChatMember;
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.BadgeDrawerIconDrawable;
import tcss450.uw.edu.chapp.utils.BadgeDrawerToggle;
import tcss450.uw.edu.chapp.utils.PushReceiver;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;

/**
 *
 * The main Activity for the app that includes the home page, and navigation bar.
 * Navigates to the HomeLanding Fragment, Chat Fragment, Contacts Fragment, and Weather
 * Fragment.
 *
 * @author Mike Osborne, Trung Thai, Michael Josten, Jessica Medrzycki
 * @version 02/25/19
 *
 */
public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        WaitFragment.OnFragmentInteractionListener,
        AllChatsFragment.OnListFragmentInteractionListener,
        ChatFragment.OnChatMessageFragmentInteractionListener,
        ContactFragment.OnListFragmentInteractionListener,
        MessageFragment.OnListFragmentInteractionListener,
        NewChatMembersFragment.OnListFragmentInteractionListener {

    private Credentials mCreds;
    private String mJwToken;

    private PushMessageReceiver mPushMessageReciever;

    //NavDrawer Icon constants
    private BadgeDrawerToggle toggleBadgeIcon;
    private BadgeDrawerIconDrawable badgeDrawable;
    private TextView mChatCounterView;
    private TextView mContactCounterView;
    private boolean mHasNotifications = false;
    private int mChatCounter;
    private int mContactCounter;

    private static final String CHANNEL_ID = "1";

    //currently open fragment
    private Fragment mChatfragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Initialize drawer icon and it's ActionBarDrawerToggle object to
        // manage the on and off red dot for notifications.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggleBadgeIcon = new BadgeDrawerToggle(this, drawer,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        badgeDrawable = new BadgeDrawerIconDrawable(getSupportActionBar().getThemedContext());
        toggleBadgeIcon.setDrawerArrowDrawable(badgeDrawable);
        drawer.addDrawerListener(toggleBadgeIcon);
        toggleBadgeIcon.syncState();
        badgeDrawable.setEnabled(false);

        //initialize the navigation drawer counter badges
        mChatCounterView = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.nav_chat));
        mContactCounterView = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.nav_connections));
        initializeCountDrawer(mChatCounterView);
        initializeCountDrawer(mContactCounterView);


        // Set the logout listener for the navigation drawer
        TextView logoutText = (TextView) findViewById(R.id.nav_logout);
        logoutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("CHAPP_LOGOUT", "Logout clicked in navigation bar");
                logout();
            }
        });

        // Get values from the intent
        mCreds = (Credentials) getIntent().getSerializableExtra(getString(R.string.key_credentials));
        mJwToken = getIntent().getStringExtra(getString(R.string.keys_intent_jwt));


        // Load SuccessFragment into content_home (aka fragment_container)
        if (savedInstanceState == null) {
            if (findViewById(R.id.fragment_container) != null) {
                //getIntent().getBooleanExtra(getString(R.string.keys_intent_notification_msg), false) OLD IF STATEMENT

                //getIntent().getExtras().getString("type").equals("msg") ||
                //                        getIntent().getExtras().getString("type").equals("topic_msg")

                //getIntent().getExtras().containsKey("type")
                if (getIntent().getBooleanExtra(getString(R.string.keys_intent_notification_msg), false)) {
                    Bundle args = new Bundle();
                    // Get value from intent and put it in fragment args
                    String chatid = getIntent().getStringExtra(getString(R.string.keys_intent_chatId));
                    args.putSerializable(getString(R.string.key_credentials)
                            , mCreds);
                    args.putSerializable(getString(R.string.keys_intent_jwt)
                            , mJwToken);
                    args.putSerializable(getString(R.string.key_chatid),
                            chatid);
                    mChatfragment = new ChatFragment();

                    mChatfragment.setArguments(args);

                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, mChatfragment)
                            .commit();

                } else {
                    loadHomeLandingPage();
                }
            }
        }
    }

    /**
     * Initializes the textviews to display on the Navigation Drawer
     * Action Items.
     * @param viewCounter   the text view counter to be initialized
     */
    private void initializeCountDrawer(TextView viewCounter){
        //Gravity property aligns the text
        viewCounter.setGravity(Gravity.CENTER_VERTICAL);
        viewCounter.setTypeface(null, Typeface.BOLD);
        viewCounter.setTextColor(getResources().getColor(R.color.colorLogoText)); //Color.RED
        //set the viewcounter text to a number or "" to change the counter!
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPushMessageReciever == null) {
            mPushMessageReciever = new PushMessageReceiver();
        }
        IntentFilter iFilter = new IntentFilter(PushReceiver.RECEIVED_NEW_MESSAGE);
        registerReceiver(mPushMessageReciever, iFilter);
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mPushMessageReciever != null){
            unregisterReceiver(mPushMessageReciever);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Helper method that loads the home landing page to include the success fragment
     * and *later* load the contacts fragment.
     *
     */
    private void loadHomeLandingPage(){
        Fragment frag = new LandingPage();
        SuccessFragment successFragment = new SuccessFragment();
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.key_credentials)
                , mCreds);
        args.putSerializable(getString(R.string.keys_intent_jwt)
                , mJwToken);

        //set the email to show in the top fragment
        successFragment.setArguments(args);
        frag.setArguments(args);

//        loadFragment(successFragment);
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, successFragment)
                .commit();


        //ORIGINAL SCROLL VIEW WITH BLOG POST SCROLLING
//        FragmentTransaction transaction = getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.fragment_container, frag)
//                .addToBackStack(null);
//         //Commit the transaction (obviously)
//        transaction.replace(R.id.framelayout_homelanding_email, successFragment);
//        //transaction.add(R.id.framelayout_homelanding_chatlist, chats);
//
//        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_logout) {
//            logout();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Navigation drawer item listener.
     * When action item has been clicked, the counter for notifications gets set to not show.
     * Then loads the correct fragment.
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch(id) {
            case R.id.nav_home:
                loadHomeLandingPage();
                break;

            case R.id.nav_connections:
                mContactCounterView.setText("");
                badgeDrawable.setEnabled(false);
//                if (!mHasNotifications) {
//                    badgeDrawable.setEnabled(false);
//                }
                ContactFragment contactFrag = new ContactFragment();
                Bundle args = new Bundle();
                args.putSerializable(getString(R.string.key_credentials), mCreds);
                args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
                contactFrag.setArguments(args);
                loadFragment(contactFrag);
                break;

            case R.id.nav_chat:
                mChatCounterView.setText("");
                badgeDrawable.setEnabled(false);
//                if (!mHasNotifications) {
//                    badgeDrawable.setEnabled(false);
//                }
                Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_chats_base))
                    .appendPath(getString(R.string.ep_chats_get_chats))
                    .build();
                // Create the JSON object with our username
                JSONObject msg = new JSONObject();
                try {
                    msg.put("username", mCreds.getUsername());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(msg);
                new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::onWaitFragmentInteractionShow)
                    .onPostExecute(this::handleChatsPostOnPostExecute)
                    .onCancelled(this::handleErrorsInTask)
                    .addHeaderField("authorization", mJwToken) //add the JWT as a header
                    .build().execute();
                break;

            case R.id.nav_weather:
                break;

            case R.id.nav_logout:
                break;
        }
        Log.i("NAVIGATION_INFORMATION", "Pressed: " + item.getTitle().toString());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    private void loadFragment(Fragment frag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, frag)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Post call for retrieving the list of Chats from the Database.
     * Calls chat fragment to load and sends in the chatId to load into.
     * @param result the chatId and name of the chats from the result to display in UI
     */
    private void handleChatsPostOnPostExecute(final String result) {
        // parse JSON
        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_chats_chatlist))) {

                    JSONArray data = root.getJSONArray(
                            getString(R.string.keys_json_chats_chatlist));
                    List<Chat> chats = new ArrayList<>();
                    for(int i = 0; i < data.length(); i++) {
                        JSONObject jsonChat = data.getJSONObject(i);
                        chats.add(new Chat.Builder(
                                jsonChat.getString(getString(R.string.keys_json_chats_chatid)),
                                jsonChat.getString(getString(R.string.keys_json_chats_name)))
                                .build());
                    }
                    Chat[] chatsAsArray = new Chat[chats.size()];
                    chatsAsArray = chats.toArray(chatsAsArray);
                    Bundle args = new Bundle();
                    args.putSerializable(AllChatsFragment.ARG_CHAT_LIST, chatsAsArray);
                    args.putSerializable(getString(R.string.key_credentials), mCreds);
                    args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
                    Fragment frag = new AllChatsFragment();
                    frag.setArguments(args);
                    onWaitFragmentInteractionHide();
                    loadFragment(frag);
                } else {
                    Log.e("ERROR!", "No data array");
                    //notify user
                    onWaitFragmentInteractionHide();
                }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            //notify user
            onWaitFragmentInteractionHide();
        }
    }

    /**
     * Post call for Async Task when returning from getting all messages from
     * Database with the given chatID.
     * @param result
     */


    /**
     * Handle errors that may occur during the AsyncTask.
     * @param result the error message provide from the AsyncTask
     */
    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
    }

    /**
     * Handles a click on a Chatroom item.
     * Opens the chat fragment and sends in the JWToken, Credentials, and the
     * clicked chatId.
     * @param item  the chat room to be opened.
     */
    @Override
    public void onListFragmentInteraction(Chat item) {
        ChatFragment chatFrag = new ChatFragment();
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
        args.putSerializable(getString(R.string.key_credentials), mCreds);
        args.putSerializable(getString(R.string.key_chatid), item.getId());
        chatFrag.setArguments(args);
        loadFragment(chatFrag);
    }

    @Override
    public void onWaitFragmentInteractionShow() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new WaitFragment(), "WAIT")
                .commit();
    }

    @Override
    public void onWaitFragmentInteractionHide() {
        getSupportFragmentManager()
                .beginTransaction()
                .remove(getSupportFragmentManager().findFragmentByTag("WAIT"))
                .commit();
        getSupportFragmentManager(); //.popBackStack();
    }

    /**
     * This is the logout method
     * @author Trung Thai
     */
    private void logout() {
        new DeleteTokenAsyncTask().execute();
    }

    @Override
    public void onListFragmentInteraction(Message item) {
        // don't do anything for now, messages aren't able to be interacted with
    }

    @Override
    public void onListFragmentInteraction(DummyContent.Contact contact) {
        ChatFragment chatFrag = new ChatFragment();
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
        args.putSerializable(getString(R.string.key_credentials), mCreds);
        args.putSerializable(getString(R.string.key_chatid), contact.username);
        chatFrag.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, chatFrag)
                .addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onListFragmentInteraction(NewChatMember item) {

    }


    // Deleting the Pushy device token must be done asynchronously. Good thing
    // we have something that allows us to do that.
    class DeleteTokenAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            onWaitFragmentInteractionShow();
        }
        @Override
        protected Void doInBackground(Void... voids) {
            //since we are already doing stuff in the background, go ahead
            //and remove the credentials from shared prefs here.
            SharedPreferences prefs =
                    getSharedPreferences(
                            getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);
            prefs.edit().remove(getString(R.string.keys_prefs_password)).apply();
            prefs.edit().remove(getString(R.string.keys_prefs_email)).apply();
            //unregister the device from the Pushy servers
            Pushy.unregister(HomeActivity.this);
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //close the app
            //finishAndRemoveTask();
            //or close this activity and bring back the Login
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            // //Ends this Activity and removes it from the Activity back stack.
            finish();
        }
    }

     /**
     * A BroadcastReceiver that listens for messages sent from PushReceiver while
      * the Home Activity is open (i.e. in App Notifications)
     */
    private class PushMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("Notification Receiver", "Received broadcast in home activity");

            String typeOfMessage = intent.getStringExtra("type");
            String sender = intent.getStringExtra("sender");
            String messageText = intent.getStringExtra("message");
            String chatid = intent.getStringExtra("chatid");

            if(typeOfMessage.equals("msg")){ //if received broadcast from message notification.
                //update global boolean for badge icon to show
                mHasNotifications = true;
                badgeDrawable.setEnabled(true);
                mChatCounterView.setText("1");
                //could add global counter to increment the amount of chat messages
                //how to add in red dot alongside specific chat room?
                Log.e("Notification Receiver", "Received message type: msg");
                Log.e("Notification Receiver", "toggle: " + toggleBadgeIcon.isBadgeEnabled());
            } else {
                //add in logic for new connection request, new chat room request
            }


        }
    }


}
