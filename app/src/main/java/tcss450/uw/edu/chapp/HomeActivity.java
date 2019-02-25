package tcss450.uw.edu.chapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
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

import java.util.ArrayList;
import java.util.List;

import me.pushy.sdk.Pushy;
import tcss450.uw.edu.chapp.blog.BlogPost;
import tcss450.uw.edu.chapp.chat.Chat;
import tcss450.uw.edu.chapp.chat.Message;
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.setlist.SetList;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;

/**
 *
 * The main Activity for the app that includes the home page, and navigation bar.
 * Navigates to the HomeLanding Fragment, Chat Fragment, Contacts Fragment, and Weather
 * Fragment.
 *
 * @author Mike Osborne, Trung Thai, Michael Josten, Jessica Medrzycki
 * @version 02/21/19
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
        MessageFragment.OnListFragmentInteractionListener {

    private Credentials mCreds;

    private String mJwToken;

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


//        // Set the logout listener for the navigation drawer
//        TextView logoutText = (TextView) findViewById(R.id.nav_logout);
//        logoutText.setOnClickListener(this::onLogoutClick);

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

                if (getIntent().getBooleanExtra(getString(R.string.keys_intent_notification_msg), false)) {
                    Fragment fragment;
                    Bundle args = new Bundle();
                    // Get value from intent and put it in fragment args
                    args.putSerializable(getString(R.string.key_credentials)
                            , mCreds);
                    args.putSerializable(getString(R.string.keys_intent_jwt)
                            , mJwToken);
                    fragment = new ChatFragment();

                    //GET CHAT ID FROM INTENT SOMEHOW?
                    //We need to bundle the chatId up in order to be able to show the messages
                    //in the chat fragment
                    fragment.setArguments(args);

                    loadFragment(fragment);

                } else {
                    loadHomeLandingPage();
                }
                fragment.setArguments(args);

                loadFragment(fragment);
            }
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

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, frag)
                .addToBackStack(null);
        // Commit the transaction (obviously)
        transaction.replace(R.id.framelayout_homelanding_email, successFragment);
        //transaction.add(R.id.framelayout_homelanding_chatlist, chats);

        transaction.commit();
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

    /**
     * Logout click listener for the logout button in the navigation drawer
     * TODO: logout the user
     * @param theText the TextView that was pressed
     */
    public void onLogoutClick(View theText) {
        Log.i("NAVIGATION_INFORMATION", "Pressed: " + ((TextView)theText).getText().toString());
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



}
