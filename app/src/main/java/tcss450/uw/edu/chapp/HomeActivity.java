package tcss450.uw.edu.chapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import me.pushy.sdk.Pushy;
import tcss450.uw.edu.chapp.blog.BlogPost;
import tcss450.uw.edu.chapp.chat.Chat;
import tcss450.uw.edu.chapp.chat.Message;
import tcss450.uw.edu.chapp.dummy.DummyContent;
import tcss450.uw.edu.chapp.chat.NewChatMember;
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.setlist.SetList;
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
        BlogFragment.OnBlogListFragmentInteractionListener,
        BlogPostFragment.OnFragmentInteractionListener,
        WaitFragment.OnFragmentInteractionListener,
        SetListFragment.OnListFragmentInteractionListener,
        AllChatsFragment.OnListFragmentInteractionListener,
        ChatFragment.OnChatMessageFragmentInteractionListener,
        ContactFragment.OnListFragmentInteractionListener,
        MessageFragment.OnListFragmentInteractionListener,
        NewChatMembersFragment.OnListFragmentInteractionListener {

    private Credentials mCreds;

    private String mJwToken;
    private PushMessageReceiver mPushMessageReciever;
    private static final String CHANNEL_ID = "1";
    //for easy access to which chat room user has open
    private String mCurrentChatId = "0";

    //currently open fragment
    private Fragment mChatfragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
                if (getIntent().getExtras().containsKey("type") ||
                        getIntent().getBooleanExtra(getString(R.string.keys_intent_notification_msg), false)) {

                    Bundle args = new Bundle();
                    // Get value from intent and put it in fragment args
                    mCurrentChatId = getIntent().getStringExtra(getString(R.string.keys_intent_chatId));
                    args.putSerializable(getString(R.string.key_credentials)
                            , mCreds);
                    args.putSerializable(getString(R.string.keys_intent_jwt)
                            , mJwToken);
                    args.putSerializable(getString(R.string.key_chatid),
                            mCurrentChatId);
                    mChatfragment = new ChatFragment();

                    mChatfragment.setArguments(args);

                    loadFragment(mChatfragment);

                } else {
                    loadHomeLandingPage();
                }
            }
        }
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

        loadFragment(successFragment);

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //Handle Navigation bar item press
        //TODO: FOR EACH NAVIGATION ITEM, CREATE URI TO BACKEND, THEN CREATE ASYNCTASK
        //TODO: AND CREATE HandleOnPostExecute FOR EACH ITEM TO LOAD NEW FRAGMENT.
        switch(id) {
            case R.id.nav_home:
                loadHomeLandingPage();
                break;

            case R.id.nav_connections:
                ContactFragment contactFrag = new ContactFragment();
                Bundle args = new Bundle();
                args.putSerializable(getString(R.string.key_credentials), mCreds);
                args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
                contactFrag.setArguments(args);
                loadFragment(contactFrag);
                break;

            case R.id.nav_chat:
                Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_chats_base))
                    .appendPath(getString(R.string.ep_chats_get_chats))
                    .build();
                // Create the JSON object with our username
                JSONObject msg = mCreds.asJSONObject();
                msg = new JSONObject();
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

    @Override
    public void onListFragmentInteraction(BlogPost blogPost) {
        BlogPostFragment blogPostFrag;

        blogPostFrag = (BlogPostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_blog_post);

        if (blogPostFrag != null) {
            blogPostFrag.updateContent(blogPost);
        } else {
            blogPostFrag = new BlogPostFragment();
            Bundle args = new Bundle();
            args.putSerializable(getString(R.string.key_blog_post), blogPost);
            blogPostFrag.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, blogPostFrag)
                    .addToBackStack(null);
            transaction.commit();
        }
    }

    @Override
    public void onListFragmentInteraction(SetList setList) {
        SetListViewFragment setListFrag;

        setListFrag = (SetListViewFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_set_list_view);

        if (setListFrag != null) {
            setListFrag.updateContent(setList);
        } else {
            setListFrag = new SetListViewFragment();
            Bundle args = new Bundle();
            args.putSerializable(getString(R.string.key_set_list), setList);
            setListFrag.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, setListFrag)
                    .addToBackStack(null);
            transaction.commit();
        }
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
     * A BroadcastReceiver that listens for messages sent from PushReceiver
     */
    private class PushMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("Notification Receiver", "Received broadcast in home activity");

            String typeOfMessage = intent.getStringExtra("type");
            String sender = intent.getStringExtra("sender");
            String messageText = intent.getStringExtra("message");
            String chatid = intent.getStringExtra("chatid");


            //if the chatFragment has not been loaded to view OR the
            //chat id currently being viewed is not the one from the received notification
            //then show the notification
            if(mChatfragment == null || mCurrentChatId != chatid) {

                //start up home activity again to route to correct fragment
                Intent i = new Intent(context, HomeActivity.class);

                //the data including type, message, sender and chatid
                i.putExtras(intent.getExtras());

                //must send these to HomeActivity since MainActivity usually sends them
                i.putExtra(getString(R.string.key_credentials),(Serializable) mCreds);
                i.putExtra(getString(R.string.keys_intent_jwt), mJwToken);
                i.putExtra(getString(R.string.keys_intent_chatId), chatid);

                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                        i, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setSmallIcon(R.mipmap.chapp_logo_trans_foreground)
                        .setContentIntent(pendingIntent);

                //Update look of the message notification before building
                if (typeOfMessage.equals("msg")) {
                    builder.setContentTitle("Message from: " + sender)
                            .setContentText(messageText);
                } else if (typeOfMessage.equals("topic_msg")) {
                    builder.setContentTitle("Topic Message from: " + sender)
                            .setContentText(messageText);
                }
                // Automatically configure a Notification Channel for devices running Android O+
                Pushy.setNotificationChannel(builder, context);

                // Get an instance of the NotificationManager service
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

                // Build the notification and display it
                notificationManager.notify(1, builder.build());
            }

        }
    }


}
