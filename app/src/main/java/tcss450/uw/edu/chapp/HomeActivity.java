package tcss450.uw.edu.chapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
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
import tcss450.uw.edu.chapp.connections.Connection;
import tcss450.uw.edu.chapp.chat.User;
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.BadgeDrawerIconDrawable;
import tcss450.uw.edu.chapp.utils.PushReceiver;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;
import tcss450.uw.edu.chapp.weather.CurrentWeatherFragment;
import tcss450.uw.edu.chapp.weather.WeatherFragment;
import tcss450.uw.edu.chapp.weather.WeatherLocationContent;
import tcss450.uw.edu.chapp.weather.WeatherLocationFragment;

/**
 *
 * The main Activity for the app that includes the home page, and navigation bar.
 * Navigates to the HomeLanding Fragment, Chat Fragment, Contacts Fragment, and Weather
 * Fragment.
 *
 * @author Mike Osborne, Trung Thai, Michael Josten, Jessica Medrzycki
 * @version 03/08/19
 *
 */
public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        WaitFragment.OnFragmentInteractionListener,
        ChatsFragment.OnListFragmentInteractionListener,
        MessagingContainerFragment.OnChatMessageFragmentInteractionListener,
        MessagingFragment.OnListFragmentInteractionListener,
        ConnectionsContainerFragment.OnConnectionInformationFetchListener,
        ChatsContainerFragment.OnChatInformationFetchListener,
        WeatherFragment.OnFragmentInteractionListener,
        WeatherLocationFragment.OnListFragmentInteractionListener,
        CurrentWeatherFragment.OnCurrentWeatherFragmentInteractionListener,
        LandingPage.OnLandingPageReturnListener {

    /** CREDENTIAL CONSTANTS */
    private Credentials mCreds;
    private String mJwToken;
    private PushMessageReceiver mPushMessageReciever;

    /** NAVIGATION CONSTANTS */
    private BadgeDrawerIconDrawable badgeDrawable;
    private TextView mChatCounterView;
    private TextView mContactCounterView;
    private boolean mHasConnectionNotifications = false;
    private boolean mHasMessageNotifications = false;
    private ArrayList<Connection> mConnections;
    private ChatsContainerFragment mCurrentChatsContainerInstance;
    private ConnectionsContainerFragment mCurrentConnectionsContainerInstance;



    // The list of chats for the current user
    private ArrayList<Chat> mChats;

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
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        badgeDrawable = new BadgeDrawerIconDrawable(getSupportActionBar().getThemedContext());
        toggle.setDrawerArrowDrawable(badgeDrawable);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        //initialize the navigation drawer counter badges
        mChatCounterView = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_chat));
        mContactCounterView = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_connections));
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

        checkForNotifications();
        if (savedInstanceState == null) {
            if (findViewById(R.id.fragment_container) != null) {

                if (getIntent().getBooleanExtra(getString(R.string.keys_intent_notification_msg), false)) {
                    //Was the Bundle received from Main Activity spurred by a message notification?
                    Bundle args = new Bundle();
                    // Get value from intent and put it in fragment args
                    Fragment chatfragment = new MessagingContainerFragment();
                    String chatid = getIntent().getStringExtra(getString(R.string.keys_intent_chatId));
                    args.putSerializable(getString(R.string.key_credentials), mCreds);
                    args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
                    args.putSerializable(getString(R.string.key_chatid), chatid);
                    chatfragment.setArguments(args);

                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, chatfragment)
                            .commit();


                } else if (getIntent().getBooleanExtra(getString(R.string.keys_weather_from_map_activity), false)) {
                    //If the HomeActivity was loaded from the WeatherMapActivity
                    WeatherFragment wf = new WeatherFragment();
                    //add location from MapActivity as fragment argument
                    Bundle args = new Bundle();
                    args.putParcelable(getString(R.string.keys_weather_location_from_map),
                            getIntent().getParcelableExtra(getString(R.string.keys_weather_location_from_map)));
                    args.putSerializable(getString(R.string.key_credentials), mCreds);
                    args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
                    wf.setArguments(args);
                    //load the weather fragment
                    loadFragment(wf);


                } else if (getIntent().getBooleanExtra(getString(R.string.keys_intent_notification_connection), false)){
                    //Was the Bundle received from Main Activity spurred by a connection notification?
                    //load the connections fragment
                    mCurrentConnectionsContainerInstance = new ConnectionsContainerFragment();
                    Bundle args = new Bundle();
                    args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
                    args.putSerializable(getString(R.string.key_credentials), mCreds);
                    mCurrentConnectionsContainerInstance.setArguments(args);
                    loadFragment(mCurrentConnectionsContainerInstance);

                } else if (getIntent().getBooleanExtra(getString(R.string.keys_intent_notification_conversation), false)){
                    //Was the Bundle received from Main Activity spurred by a conversation request notification?
                    //load the chats container fragment
                    mCurrentChatsContainerInstance = new ChatsContainerFragment();
                    Bundle args = new Bundle();
                    args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
                    args.putSerializable(getString(R.string.key_credentials), mCreds);
                    mCurrentChatsContainerInstance.setArguments(args);
                    loadFragment(mCurrentChatsContainerInstance);

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
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.key_credentials)
                , mCreds);
        args.putSerializable(getString(R.string.keys_intent_jwt)
                , mJwToken);
        frag.setArguments(args);

        // update homeactivity information with the latest
        callWebServiceforConnections();
        callWebServiceforChats();

//        loadFragment(successFragment);
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // populate framelayouts
        mCurrentChatsContainerInstance = new ChatsContainerFragment();
        Bundle args2 = new Bundle();
        args2.putSerializable(getString(R.string.key_credentials)
                , mCreds);
        args2.putSerializable(getString(R.string.keys_intent_jwt)
                , mJwToken);
        args2.putSerializable(getString(R.string.key_flag_compact_mode), true);
        mCurrentChatsContainerInstance.setArguments(args2);

        mCurrentConnectionsContainerInstance = new ConnectionsContainerFragment();
        Bundle args3 = new Bundle();
        args3.putSerializable(getString(R.string.key_credentials)
                , mCreds);
        args3.putSerializable(getString(R.string.keys_intent_jwt)
                , mJwToken);
        args3.putSerializable(getString(R.string.key_flag_compact_mode), true);
        mCurrentConnectionsContainerInstance.setArguments(args3);

        CurrentWeatherFragment cwf = new CurrentWeatherFragment();


        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, frag, "1")
                .replace(R.id.framelayout_homelanding_weather, cwf, "2")
                .replace(R.id.framelayout_homelanding_contactlist, mCurrentConnectionsContainerInstance, "3")
                .replace(R.id.framelayout_homelanding_chatlist, mCurrentChatsContainerInstance, "4")
                .commit();


        // make sure wait fragment is removed
        if (getSupportFragmentManager().findFragmentByTag("WAIT") != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentByTag("WAIT"))
                    .commit();
        }
        //transaction.replace(R.id.framelayout_homelanding_email, successFragment);
        //transaction.add(R.id.framelayout_homelanding_chatlist, chats);


    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.home, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
////        int id = item.getItemId();
////
////        //noinspection SimplifiableIfStatement
////        if (id == R.id.action_logout) {
////            logout();
////            return true;
////        }
//
//        return super.onOptionsItemSelected(item);
//    }

    private void checkForNotifications(){
        callWebServiceforChats(); //updates mChats
        callWebServiceforConnections(); //update mConnections

        if (mConnections!= null) {
            for (Connection contact : mConnections) {
                //usernameB is the recipient of the request
                if (contact.getVerified() == 0 && contact.getUsernameB().equals(mCreds.getUsername())) {
                    //if the user is the recipient of the request and it hasn't been accepted yet
                    //show notification icons
                    mHasConnectionNotifications = true;
                    mContactCounterView.setText("NEW");
                }
            }
        }
        if (mChats!= null) {
            for (Chat chat : mChats) {
                //check if last message in chat was sent by another user
                //and if that last message has been not been read
                if (!chat.getLastMessageUsername().equals(mCreds.getUsername()) && !chat.isHasBeenRead()) {
                    //unread message
                    mHasMessageNotifications = true;
                    mContactCounterView.setText("NEW");
                    return;
                }
            }
        }

        //TODO maybe add in item for checking if chat has been verified. It is same icon on chats not adding for now

        //update badge icon
        if (mHasConnectionNotifications || mHasMessageNotifications){
            badgeDrawable.setEnabled(true);
        } else {
            badgeDrawable.setEnabled(false);
        }

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

                if (!mHasMessageNotifications){ //make sure there are no other pending notifications
                    badgeDrawable.setEnabled(false);
                }
                mContactCounterView.setText("");
                mCurrentConnectionsContainerInstance = new ConnectionsContainerFragment();
                Bundle args = new Bundle();
                args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
                args.putSerializable(getString(R.string.key_credentials), mCreds);
//                args.putStringArrayList(getString(R.string.keys_intent_chatId), unreadChatList);
                mCurrentConnectionsContainerInstance.setArguments(args);
                loadFragment(mCurrentConnectionsContainerInstance);
                break;

            case R.id.nav_chat:
                mChatCounterView.setText("");
                mCurrentChatsContainerInstance = new ChatsContainerFragment();
                if (!mHasConnectionNotifications){ //make sure there are no other pending notifications
                    badgeDrawable.setEnabled(false);
                }
                args = new Bundle();
                args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
                args.putSerializable(getString(R.string.key_credentials), mCreds);
                mCurrentChatsContainerInstance.setArguments(args);
                loadFragment(mCurrentChatsContainerInstance);
                break;

            case R.id.nav_weather:
//                CurrentWeatherFragment cwf = new CurrentWeatherFragment();
//                loadFragment(cwf);
                WeatherFragment wf = new WeatherFragment();
                Bundle wArgs = new Bundle();
                wArgs.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
                wArgs.putSerializable(getString(R.string.key_credentials), mCreds);
                wf.setArguments(wArgs);
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
                args.putSerializable(ConnectionsFragment.ARG_CONNECTIONS_LIST, connectionsAsArray);
                args.putSerializable(getString(R.string.key_credentials), mCreds);
                args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
                Fragment frag = new ConnectionsFragment();
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
     * Begins the async task for grabbing connections from the db
     */
    public void callWebServiceforConnections(){
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_connections_base))
                .appendPath(getString(R.string.ep_connections_get_connections_and_requests))
                .build();
        // Pass the credentials
        JSONObject msg = mCreds.asJSONObject();
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleConnectionsOnPostExecute)
                .onCancelled(error -> Log.e("ConnectionsContainerFragment", error))
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
                // update the reference in home activity
                updateConnections(mConnections);
            } else {
                Log.e("ERROR!", "No data array");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
        }
    }

    private void callWebServiceforChats() {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_chats_base))
                .appendPath(getString(R.string.ep_chats_get_chats))
                .build();
        // Pass the credentials
        JSONObject msg = mCreds.asJSONObject();
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleChatsPostOnPostExecute)
                .onCancelled(error -> Log.e("ConnectionsContainerFragment", error))
                .addHeaderField("authorization", mJwToken) //add the JWT as a header
                .build().execute();
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
                ArrayList<Chat> chats = new ArrayList<>();
                for(int i = 0; i < data.length(); i++) {
                    JSONObject jsonChat = data.getJSONObject(i);

                    String chatid = jsonChat.getString(getString(R.string.keys_json_chats_chatid));

                    // First, build the list of users in the chat
                    ArrayList<String> usersInChat = new ArrayList<String>();
                    JSONArray jArray = (JSONArray) jsonChat.get(getString(R.string.keys_json_chats_users));
                    if (jArray != null) {
                        for (int j = 0; j < jArray.length(); j++) {
                            usersInChat.add(jArray.getString(j));
                        }
                    }

                    // Now inspect last senders to find out the last sender
                    String lastSender = "";
                    boolean hasMessages = false;
                    JSONArray lastSenders = jsonChat.getJSONArray(getString(R.string.keys_json_chats_last_senders));
                    // iterate to find the greatest primary key
                    int maxPrimaryKey = 0;
                    if (!lastSenders.getJSONObject(lastSenders.length()-1).isNull("f2")) {
                        hasMessages = true;
                        // iterate once to get the max
                        for (int j = 0; j < lastSenders.length(); j++) {
                            int tempKey = Integer.parseInt(lastSenders.getJSONObject(j).getString("f2"));
                            if (tempKey > maxPrimaryKey) {
                                maxPrimaryKey = tempKey;
                            }
                        }
                        // iterate again to find the associated username
                        for (int j = 0; j < lastSenders.length(); j++) {
                            int tempKey = Integer.parseInt(lastSenders.getJSONObject(j).getString("f2"));
                            if (tempKey == maxPrimaryKey) {
                                lastSender = lastSenders.getJSONObject(j).getString("f1");
                            }
                        }
                    }

                    Boolean hasBeenRead = null;
                    if (!hasMessages) {
                        // No messages have been sent in this chat, set last sender to null so we know
                        // this fact in other parts of the app
                        lastSender = null;
                    } else {
                        // if it's not null, capture if it's has been read or not.
                        if (!jsonChat.isNull(getString(R.string.keys_json_chats_has_been_read))) {
                            hasBeenRead = ((Integer) jsonChat.get(getString(R.string.keys_json_chats_has_been_read))) == 1;
                        }
                    }

                    // Retrieve accepted flags to know who has/hasn't accepted the chat room invite.
                    ArrayList<Boolean> acceptedFlags = new ArrayList<>();
                    JSONArray jsonFlags = jsonChat.getJSONArray(getString(R.string.keys_json_chats_accepted_list));
                    for (int j = 0; j < usersInChat.size(); j++) {
                        String currentUsername = usersInChat.get(j);
                        for (int x = 0; x < jsonFlags.length(); x++) {
                            if (currentUsername.equals(jsonFlags.getJSONObject(x).getString("f2"))) {
                                acceptedFlags.add("1".equals(jsonFlags.getJSONObject(x).getString("f1")));
                            }
                        }
                    }

//                    Log.e("users?", usersInChat.toString());
//                    Log.e("NAME", jsonChat.getString(getString(R.string.keys_json_chats_name)));
//                    Log.e("LAST SENDER", lastSender == null ? "NULL" : lastSender);
//                    Log.e("READ?", hasBeenRead == null ? "NULL" : Boolean.toString(hasBeenRead));
//                    Log.e("flags?", acceptedFlags.toString());


                    chats.add(new Chat.Builder(
                            chatid,
                            jsonChat.getString(getString(R.string.keys_json_chats_name)),
                            usersInChat,
                            acceptedFlags,
                            hasBeenRead,
                            lastSender)
                            .build());
                }
                // update the reference in HomeActivity
                updateChats(chats);



            } else {
                Log.e("ERROR!", "No data array" + root.toString());

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());

        }
    }


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
        MessagingContainerFragment chatfragment = new MessagingContainerFragment();
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
        args.putSerializable(getString(R.string.key_credentials), mCreds);
        args.putSerializable(getString(R.string.key_chat), item);
        args.putSerializable(getString(R.string.key_intent_connections), mConnections);
        chatfragment.setArguments(args);
        loadFragment(chatfragment);
    }



    @Override
    public void onWaitFragmentInteractionShow() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new WaitFragment(), "WAIT")
                .commit();
    }

    // Whenever a web service call to fetch connections is made, update the reference in homeactivity
    @Override
    public void updateConnections(ArrayList<Connection> connections) {
        mConnections = connections;
    }

    /**
     * Part of the interface from ChatsContainer Fragment that sends in the updated list
     * of all chats for the user on create of the chatscontainer.
     * @param chats     list of chats user is in
     */
    @Override
    public void updateChats(ArrayList<Chat> chats) {
        mChats = chats;
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

//    /**
//     * From the MessagingContainerFragment interface that updates the count of unread
//     * chat Ids from notifications.
//     * @param chatId    chatId to add to the list
//     */
//    @Override
//    public void incrementUnreadChatNotifications(String chatId) {
//
//        //adds the chatId to list of unread chats
//        //show badge on home button.
//        //show number on nav menu chat button
//        mHasNotifications = true;
//        unreadChatList.add(chatId);
//        badgeDrawable.setEnabled(true);
//        mChatCounterView.setText("NEW");
//        //show badge on recycler view item in all chats.
//    }

    @Override
    public void unreadMessageReceivedinOtherChatNotifications(String chatId) {

    }

    @Override
    public void reloadLandingPage() {
        loadHomeLandingPage();
    }

    /**
     * This method will be called when the user clicks on a weather location to load from
     * a saved weather location list.
     * @param item
     */
    @Override
    public void onListFragmentInteraction(WeatherLocationContent.WeatherLocationItem item) {
        WeatherFragment wf = new WeatherFragment();
        Location location = new Location("");
        location.setLatitude(item.lat);
        location.setLongitude(item.lon);
        //add location from MapActivity as fragment argument
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.keys_weather_location_load), location);

        args.putSerializable(getString(R.string.key_credentials), mCreds);
        args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
        wf.setArguments(args);
        //load the weather fragment
        loadFragment(wf);
    }

    /**
     * method that will load the WeatherLocationFragment.
     * called from WeatherFragment load button.
     */
    @Override
    public void onLoadWeatherClicked() {
        loadFragment(new WeatherLocationFragment());
    }

//    /**
//     * From the MessagingContainerFragment interface that removes a viewed chatid from the counter
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


            if(typeOfMessage.equals("msg")){ //if received broadcast from message notification.
                Log.e("Notification Receiver", "Received message type: msg");

                if (mCurrentChatsContainerInstance.mCompactMode && findViewById(R.id.framelayout_homelanding_chatlist) == null
                        && findViewById(R.id.chats_container) == null && findViewById(R.id.chat_messages_container) == null){
                    //case where user is NOT in chat list fragment
                    Log.e("Notification Receiver", "chat fragment not open");
                    String id = intent.getStringExtra("chatid");
                    callWebServiceforChats(); //update chat list

                    for (Chat chats : mChats){
                        if (id.equals(chats.getId()) && !chats.getLastMessageUsername().equals(mCreds.getUsername())){
                            //check if the message is in a chat room that the user is in
                            List<String> users = chats.getUsersInChat();
                            List<Boolean> flags = chats.getAcceptedFlags();
                            Log.e("Notification Receiver", "Last message sent from: " + chats.getLastMessageUsername() );
                            //only show notification if user has accepted the chat room invite already
                            //flag should be true if accepted chatroom request
                            if (flags.get(users.indexOf(mCreds.getUsername()))) {
                                Log.e("Notification Receiver", "Updating badge" );

                                mHasMessageNotifications = true;
                                badgeDrawable.setEnabled(true);
                                mChatCounterView.setText("NEW");
                            }
                            return;
                        }
                    }

                }
                if (findViewById(R.id.chats_container) != null || findViewById(R.id.framelayout_homelanding_chatlist) != null){
                    //case where user is viewing chat fragment
                    Log.e("Home Activity Notification Receiver", "not viewing chats container, calling webservice for chats");
                    mCurrentChatsContainerInstance.callWebServiceforChats();
                }


            } else if(typeOfMessage.equals("conn req")){ //if received broadcast from connection request.
                if (mCurrentConnectionsContainerInstance.mCompactMode || (findViewById(R.id.framelayout_homelanding_contactlist) == null)
                        && findViewById(R.id.connections_container) == null){
                    //case where user is in HomeFragment
                    //or if contact fragment == null
                    badgeDrawable.setEnabled(true);
                    mContactCounterView.setText("NEW");
                    mHasConnectionNotifications = true;

                }
                if (findViewById(R.id.connections_container) != null || findViewById(R.id.framelayout_homelanding_contactlist) != null){
                    //case where user is viewing contact fragment
                    mCurrentConnectionsContainerInstance.callWebServiceforConnections();
                }
                Log.e("Notification Receiver", "Received message type: conn req");
            } else if(typeOfMessage.equals("convo req")){ //if received broadcast from conversation request.
                if (mCurrentChatsContainerInstance.mCompactMode || (findViewById(R.id.framelayout_homelanding_chatlist) == null)
                        && findViewById(R.id.chats_container) == null){
                    //case where user is in HomeFragment
                    badgeDrawable.setEnabled(true);
                    mChatCounterView.setText("NEW");

                }
                if (findViewById(R.id.chats_container) != null || findViewById(R.id.framelayout_homelanding_chatlist) != null){
                    //case where user is viewing chat fragment
                    mCurrentChatsContainerInstance.callWebServiceforChats();
                }
                Log.e("RELOAD", "RELOADING CHATS FROM IN APP NOTIFICATION");
                Log.e("Notification Receiver", "Received message type: convo req");
            }



         }
     }

}
