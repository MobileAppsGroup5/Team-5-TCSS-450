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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
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
import tcss450.uw.edu.chapp.weather.CurrentWeatherFragment;
import tcss450.uw.edu.chapp.chat.Chat;
import tcss450.uw.edu.chapp.chat.Message;
import tcss450.uw.edu.chapp.connections.Connection;
import tcss450.uw.edu.chapp.chat.User;
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.BadgeDrawerIconDrawable;
import tcss450.uw.edu.chapp.utils.PushReceiver;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;
import tcss450.uw.edu.chapp.weather.WeatherFragment;

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
        AllConnectionsFragment.OnListFragmentInteractionListener,
        MessageFragment.OnListFragmentInteractionListener,
        NewChatMembersFragment.OnListFragmentInteractionListener,
        ConnectionsContainerFragment.OnListFragmentInteractionListener,
        WeatherFragment.OnFragmentInteractionListener {

    private Credentials mCreds;
    private String mJwToken;

    private PushMessageReceiver mPushMessageReciever;

    //NavDrawer Icon constants
    private BadgeDrawerIconDrawable badgeDrawable;
    private TextView mChatCounterView;
    private TextView mContactCounterView;
    //tells if the user unread notifications
    private boolean mHasNotifications = false;

    private ArrayList<String> unreadChatList; //holds the chatIDs received from notifications
    private int mChatCounter;
    private int mContactCounter;

    private static final String CHANNEL_ID = "1";

    //currently open fragment
    private Fragment mChatfragment;

    // The list of connections for the current user.
    private List<Connection> mConnections;

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
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        badgeDrawable = new BadgeDrawerIconDrawable(getSupportActionBar().getThemedContext());
        toggle.setDrawerArrowDrawable(badgeDrawable);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        badgeDrawable.setEnabled(false);

        //initialize the navigation drawer counter badges
        mChatCounterView = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.nav_chat));
        mContactCounterView = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.nav_connections));
        initializeCountDrawer(mChatCounterView);
        initializeCountDrawer(mContactCounterView);
        unreadChatList = new ArrayList<String>();


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
        Uri uri;
        JSONObject msg;

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
                Fragment frag = new ConnectionsContainerFragment();
                Bundle args = new Bundle();
                args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
                args.putSerializable(getString(R.string.key_credentials), mCreds);
                frag.setArguments(args);
                loadFragment(frag);
                break;

            case R.id.nav_chat:

                  mChatCounterView.setText("");
                badgeDrawable.setEnabled(false);
//                if (!mHasNotifications) {
//                    badgeDrawable.setEnabled(false);
//                }
                uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_chats_base))
                    .appendPath(getString(R.string.ep_chats_get_chats))
                    .build();

                // Pass the credentials
                msg = mCreds.asJSONObject();
                new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::onWaitFragmentInteractionShow)
                    .onPostExecute(this::handleChatsPostOnPostExecute)
                    .onCancelled(this::handleErrorsInTask)
                    .addHeaderField("authorization", mJwToken) //add the JWT as a header
                    .build().execute();
                break;

            case R.id.nav_weather:
//                CurrentWeatherFragment cwf = new CurrentWeatherFragment();
//                loadFragment(cwf);
                WeatherFragment wf = new WeatherFragment();
                loadFragment(wf);
                break;

            case R.id.nav_logout:
                break;
        }
        Log.i("NAVIGATION_INFORMATION", "Pressed: " + item.getTitle().toString());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Begins the async task for grabbing the messages from the
     * Database given the specified chatid.
     */
    public void callWebServiceforConnections(){
        onWaitFragmentInteractionShow();
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_connections_base))
                .appendPath(getString(R.string.ep_connections_get_contacts))
                .build();
        // Pass the credentials
        JSONObject msg = mCreds.asJSONObject();
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleConnectionsOnPostExecute)
                .onCancelled(error -> Log.e("AllConnectionsFragment", error))
                .addHeaderField("authorization", mJwToken) //add the JWT as a header
                .build().execute();
    }

    private void handleConnectionsOnPostExecute(String result) {
        // parse JSON
        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_connections))) {

                JSONArray data = root.getJSONArray(
                        getString(R.string.keys_json_connections));
                List<Connection> connections = new ArrayList<>();
                for(int i = 0; i < data.length(); i++) {
                    JSONObject jsonChat = data.getJSONObject(i);
                    connections.add(new Connection.Builder(
                            jsonChat.getString(getString(R.string.keys_json_connections_from)),
                            jsonChat.getString(getString(R.string.keys_json_connections_to)),
                            Integer.parseInt(jsonChat.getString(getString(R.string.keys_json_connections_verified))))
                            .build());
                }
                mConnections = new ArrayList<>(connections);

                constructConnections();
                onWaitFragmentInteractionHide();
            } else {
                Log.e("ERROR!", "No data array");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
        }
    }

    private void constructConnections() {
        Bundle args = new Bundle();

        // Do this swapping so we can send in an array of Messages not Objects
        Connection[] connectionsAsArray = new Connection[mConnections.size()];
        connectionsAsArray = mConnections.toArray(connectionsAsArray);
        args.putSerializable(AllConnectionsFragment.ARG_CONNECTIONS_LIST, connectionsAsArray);
        args.putSerializable(getString(R.string.key_credentials), mCreds);
        args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
        AllConnectionsFragment frag = new AllConnectionsFragment();
        frag.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, frag)
                .commit();

    }

    /**
     * Post call for retrieving the all of the contacts the current user is a part of from the database
     * and sending them into contactfragment
     * @param result the chatId and name of the chats from the result to display in UI
     */
    private void handleContactsPostOnPostExecute(String result) {
        // parse JSON
        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_connections))) {

                JSONArray data = root.getJSONArray(
                        getString(R.string.keys_json_connections));
                List<Connection> connections = new ArrayList<>();
                for(int i = 0; i < data.length(); i++) {
                    JSONObject jsonChat = data.getJSONObject(i);
                    connections.add(new Connection.Builder(
                            jsonChat.getString(getString(R.string.keys_json_connections_from)),
                            jsonChat.getString(getString(R.string.keys_json_connections_to)),
                            Integer.parseInt(jsonChat.getString(getString(R.string.keys_json_connections_verified))))
                            .build());
                }
                Connection[] connectionsAsArray = new Connection[connections.size()];
                connectionsAsArray = connections.toArray(connectionsAsArray);
                Bundle args = new Bundle();
                args.putSerializable(AllConnectionsFragment.ARG_CONNECTIONS_LIST, connectionsAsArray);
                args.putSerializable(getString(R.string.key_credentials), mCreds);
                args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
                Fragment frag = new AllConnectionsFragment();
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
        mChatfragment = new ChatFragment();             //ChatFragment chatfrag
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
        args.putSerializable(getString(R.string.key_credentials), mCreds);
        args.putSerializable(getString(R.string.key_chatid), item.getId());
        mChatfragment.setArguments(args);
        loadFragment(mChatfragment);
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

//    @Override
//    public void onListFragmentInteraction(DummyContent.Contact contact) {
//        ChatFragment chatFrag = new ChatFragment();
//        Bundle args = new Bundle();
//        args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
//        args.putSerializable(getString(R.string.key_credentials), mCreds);
//        args.putSerializable(getString(R.string.key_chatid), contact.username);
//        chatFrag.setArguments(args);
//        FragmentTransaction transaction = getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.fragment_container, chatFrag)
//                .addToBackStack(null);
//        transaction.commit();
//    }

    @Override
    public void onListFragmentInteraction(User item) {

    }

    /**
     * Called when you click x on a contact in {@link AllConnectionsFragment}
     * Prompts the user if they want to delete the contact, then deletes it on confirm
     * @param c The connection that was clicked on
     */
    @Override
    public void onXClicked(Connection c) {
        String otherUsername = ((TextView)findViewById(R.id.list_item_connection_name)).getText().toString();

        // confirm with the user
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to cancel/delete " + otherUsername + "?")
                .setTitle("Delete/Cancel?")
                .setPositiveButton("YES", (dialog, which) -> {
                    deleteContact(otherUsername);
                })
                .setNegativeButton("CANCEL", (dialog, which) -> {});
        AlertDialog dialog = builder.create();
        dialog.show();
        Log.e("CONTACTSBUTTONCLICKED", "DECLINE CLICKED ON "
                + ((TextView)findViewById(R.id.list_item_connection_name)).getText().toString());
    }

    /**
     * Helper method for onXClicked, called when the user confirms they want to delete/cancel contact
     * @param otherUsername The username of the other person in the contact.
     */
    private void deleteContact(String otherUsername) {
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("decliningUsername", mCreds.getUsername());
            messageJson.put("requestUsername", otherUsername);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_connections_base))
                .appendPath(getString(R.string.ep_connections_decline_request))
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                .onPostExecute(this::handleConnectionsChangePostExecute)
                .onCancelled(error -> Log.e("MyAllConnectionsRecyclerViewAdapter", error))
                .addHeaderField("authorization", mJwToken)
                .build().execute();
    }

    /**
     * Called when you click check on a contact in {@link AllConnectionsFragment}
     * @param c The connection that was clicked on
     */
    @Override
    public void onCheckClicked(Connection c) {
        String otherUsername = ((TextView)findViewById(R.id.list_item_connection_name)).getText().toString();
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("acceptingUsername", mCreds.getUsername());
            messageJson.put("requestUsername", otherUsername);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_connections_base))
                .appendPath(getString(R.string.ep_connections_accept_request))
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                .onPostExecute(this::handleConnectionsChangePostExecute)
                .onCancelled(error -> Log.e("HomeActivity", error))
                .addHeaderField("authorization", mJwToken)
                .build().execute();

        Log.e("CONTACTSBUTTONCLICKED", "ACCEPT CLICKED ON "
                + ((TextView)findViewById(R.id.list_item_connection_name)).getText().toString());
    }

    private void handleConnectionsChangePostExecute(String result) {
        // for now, reload the fragment regardless
        // TODO: in the future handle error catching and displaying
        callWebServiceforConnections();
    }

    /**
     * From the ChatFragment interface that updates the count of unread
     * chat Ids from notifications.
     * @param chatId    chatId to add to the list
     */
    @Override
    public void incrementUnreadChatNotifications(String chatId) {

        //adds the chatId to list of unread chats
        //show badge on home button.
        //show number on nav menu chat button

        mHasNotifications = true;
        unreadChatList.add(chatId);
        badgeDrawable.setEnabled(true);
        mChatCounterView.setText(unreadChatList.size());
        //show badge on recycler view item in all chats.
    }

//    /**
//     * From the ChatFragment interface that removes a viewed chatid from the counter
//     * of unread chat ids
//     * @param chatId    the chatId already viewed.
//     */
//    @Override
//    public void updateViewedChatroom(String chatId) {
//        unreadChatList.remove(chatId);
//    }


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

            /*
             * CASES: For viewing notifications in app
             *
             * MSG NOTIFICATIONS
             * 1. User is viewing OTHER Fragment and msg notification is received.
             * 2. User is viewing chat room fragment and msg inside of chat is received.
             * 3. User is viewing chat room fragment and msg from another chat is received.
             *
             *
             *
            */



            if(typeOfMessage.equals("msg")){ //if received broadcast from message notification.
                if (findViewById(R.id.fragment_chat) == null){ //case where user is NOT in chat fragment
                    //update global boolean for badge icon to show
                    mHasNotifications = true;
                    unreadChatList.add(chatid);
                    //update home icon and the counter
                    badgeDrawable.setEnabled(true);
                    mChatCounterView.setText(unreadChatList.size());

                    //how to add in red dot alongside specific chat room?
                    //call backend to get all chatIds with unread messages
                    //then add to global list, increment counts and count views.
                    Log.e("Notification Receiver", "Received message type: msg");
                }


            } else {
                //add in logic for new connection request, new chat room request
            }


        }
    }


}
