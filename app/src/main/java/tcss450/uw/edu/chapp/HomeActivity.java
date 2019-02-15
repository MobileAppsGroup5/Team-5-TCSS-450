package tcss450.uw.edu.chapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.setlist.SetList;

/**
 *
 * The main Activity for the app that includes the home page, and navigation bar.
 *
 */
public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        BlogFragment.OnBlogListFragmentInteractionListener,
        BlogPostFragment.OnFragmentInteractionListener,
        WaitFragment.OnFragmentInteractionListener,
        SetListFragment.OnListFragmentInteractionListener {

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

        // Set the logout listener for the navigation drawer
        TextView logoutText = (TextView) findViewById(R.id.nav_logout);
        logoutText.setOnClickListener(this::onLogoutClick);

        // Get values from the intent
        mCreds = (Credentials) getIntent().getSerializableExtra(getString(R.string.key_credentials));
        mJwToken = getIntent().getStringExtra(getString(R.string.keys_intent_jwt));

        // Load SuccessFragment into content_home (aka fragment_container)
        if (savedInstanceState == null) {
            if (findViewById(R.id.fragment_container) != null) {
                Fragment fragment;
                Bundle args = new Bundle();
                // Get value from intent and put it in fragment args
                args.putSerializable(getString(R.string.key_credentials)
                        , mCreds);
                if (getIntent().getBooleanExtra(getString(R.string.keys_intent_notification_msg), false)) {
                    fragment = new ChatFragment();
                } else {
                    fragment = new LandingPage();
                    fragment.setArguments(args);
                }

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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

//        if (id == R.id.nav_home) {
//            SuccessFragment successFrag = new SuccessFragment();
//            Bundle args = new Bundle();
//            args.putSerializable(getString(R.string.key_credentials), mCreds);
//            successFrag.setArguments(args);
//            loadFragment(successFrag);
//        } else if (id == R.id.nav_blog_posts) {
//            Uri uri = new Uri.Builder()
//                    .scheme("https")
//                    .appendPath(getString(R.string.ep_base_url))
//                    .appendPath(getString(R.string.ep_phish))
//                    .appendPath(getString(R.string.ep_blog))
//                    .appendPath(getString(R.string.ep_get))
//                    .build();
//            new GetAsyncTask.Builder(uri.toString())
//                    .onPreExecute(this::onWaitFragmentInteractionShow)
//                    .onPostExecute(this::handleBlogGetOnPostExecute)
//                    .addHeaderField("authorization", mJwToken) //add the JWT as a header
//                    .build().execute();
//
//        } else if (id == R.id.nav_set_lists) {
//            Uri uri = new Uri.Builder()
//                    .scheme("https")
//                    .appendPath(getString(R.string.ep_base_url))
//                    .appendPath(getString(R.string.ep_phish))
//                    .appendPath(getString(R.string.ep_setlists))
//                    .appendPath(getString(R.string.ep_recent))
//                    .build();
//            new GetAsyncTask.Builder(uri.toString())
//                    .onPreExecute(this::onWaitFragmentInteractionShow)
//                    .onPostExecute(this::handleSetListGetOnPostExecute)
//                    .addHeaderField("authorization", mJwToken)
//                    .build().execute();
//        }
        //Handle Navigation bar item press
        //TODO: FOR EACH NAVIGATION ITEM, CREATE URI TO BACKEND, THEN CREATE ASYNCTASK
        //TODO: AND CREATE HandleOnPostExecute FOR EACH ITEM TO LOAD NEW FRAGMENT.
        switch(id) {
            case R.id.nav_home:
                break;

            case R.id.nav_connections:
                break;

            case R.id.nav_chat:
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
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, frag)
                .addToBackStack(null);
        // Commit the transaction (obviously)
        transaction.commit();
    }

    private void handleSetListGetOnPostExecute(final String result) {
        // parse JSON
        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_response))) {
                JSONObject response = root.getJSONObject(
                        getString(R.string.keys_json_response));
                if (response.has(getString(R.string.keys_json_data))) {
                    JSONArray data = response.getJSONArray(
                            getString(R.string.keys_json_data));
                    List<SetList> setLists = new ArrayList<>();
                    for(int i = 0; i < data.length(); i++) {
                        JSONObject jsonSetList = data.getJSONObject(i);
                        setLists.add(new SetList.Builder()
                                .addLongDate(jsonSetList.getString(getString(R.string.keys_json_setlists_long_date)))
                                .addLocation(jsonSetList.getString(getString(R.string.keys_json_setlists_location)))
                                .addVenue(jsonSetList.getString(getString(R.string.keys_json_setlists_venue)))
                                .addSetListData(jsonSetList.getString(getString(R.string.keys_json_setlists_set_list_data)))
                                .addSetListNotes(jsonSetList.getString(getString(R.string.keys_json_setlists_set_list_notes)))
                                .addUrl(jsonSetList.getString(getString(R.string.keys_json_setlists_url)))
                                .build());
                    }
                    SetList[] setListsAsArray = new SetList[setLists.size()];
                    setListsAsArray = setLists.toArray(setListsAsArray);
                    Bundle args = new Bundle();
                    args.putSerializable(SetListFragment.ARG_SET_LISTS, setListsAsArray);
                    Fragment frag = new SetListFragment();
                    frag.setArguments(args);
                    onWaitFragmentInteractionHide();
                    loadFragment(frag);
                } else {
                    Log.e("ERROR!", "No data array");
                    //notify user
                    onWaitFragmentInteractionHide();
                }
            } else {
                Log.e("ERROR!", "No response");
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

    private void handleBlogGetOnPostExecute(final String result) {
        //parse JSON
        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_response))) {
                JSONObject response = root.getJSONObject(
                        getString(R.string.keys_json_response));
                if (response.has(getString(R.string.keys_json_data))) {
                    JSONArray data = response.getJSONArray(
                            getString(R.string.keys_json_data));
                    List<BlogPost> blogs = new ArrayList<>();
                    for(int i = 0; i < data.length(); i++) {
                        JSONObject jsonBlog = data.getJSONObject(i);
                        blogs.add(new BlogPost.Builder(
                                jsonBlog.getString(
                                        getString(R.string.keys_json_blogs_pubdate)),
                                jsonBlog.getString(
                                        getString(R.string.keys_json_blogs_title)))
                                .addTeaser(jsonBlog.getString(
                                        getString(R.string.keys_json_blogs_teaser)))
                                .addUrl(jsonBlog.getString(
                                        getString(R.string.keys_json_blogs_url)))
                                .build());
                    }
                    BlogPost[] blogsAsArray = new BlogPost[blogs.size()];
                    blogsAsArray = blogs.toArray(blogsAsArray);
                    Bundle args = new Bundle();
                    args.putSerializable(BlogFragment.ARG_BLOG_LIST, blogsAsArray);
                    Fragment frag = new BlogFragment();
                    frag.setArguments(args);
                    onWaitFragmentInteractionHide();
                    loadFragment(frag);
                } else {
                    Log.e("ERROR!", "No data array");
                    //notify user
                    onWaitFragmentInteractionHide();
                }
            } else {
                Log.e("ERROR!", "No response");
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
        getSupportFragmentManager().popBackStack();
    }

    /**
     * This is the logout method
     * @author Trung Thai
     */
    private void logout() {
        new DeleteTokenAsyncTask().execute();
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
            finishAndRemoveTask();
            //or close this activity and bring back the Login
            // Intent i = new Intent(this, MainActivity.class);
            // startActivity(i);
            // //Ends this Activity and removes it from the Activity back stack.
            // finish();
        }
    }



}
