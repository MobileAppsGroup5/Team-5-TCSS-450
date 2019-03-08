package tcss450.uw.edu.chapp;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.Serializable;

import me.pushy.sdk.Pushy;
import tcss450.uw.edu.chapp.model.Credentials;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginFragmentInteractionListener,
        RegisterFragment.OnRegisterFragmentInteractionListener {

    private boolean mLoadFromChatNotification = false;
    private boolean mLoadFromConnectionRequest = false;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String mNotifChatId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pushy.listen(this);
        setContentView(R.layout.activity_main);

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("type")) {
                if (mLoadFromChatNotification = getIntent().getExtras().getString("type").equals("msg")){
                    mNotifChatId = getIntent().getExtras().getString("chatid");
                } else if (mLoadFromConnectionRequest = getIntent().getExtras().getString("type").equals("conn req")){
                    //get keys sender, message, to
                }


            }
        }

        if (savedInstanceState == null) {
            if (findViewById(R.id.frame_main_container) != null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.frame_main_container, new LoginFragment())
                        .commit();
            }
        }
    }


    @Override
    public void onLoginSuccess(Credentials theCredentials, String jwt) {
        // FOR DEBUG PURPOSES, REMOVE BEFORE PRODUCTION
        Log.d("JSON_WEB_TOKEN", jwt);

        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(getString(R.string.key_credentials), theCredentials);
        intent.putExtra(getString(R.string.keys_intent_notification_msg), mLoadFromChatNotification);
        intent.putExtra(getString(R.string.keys_intent_notification_connection), mLoadFromConnectionRequest);
        intent.putExtra(getString(R.string.keys_intent_jwt), jwt);
        intent.putExtra(getString(R.string.keys_intent_chatId), mNotifChatId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRegisterClicked() {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_main_container, new RegisterFragment())
                .addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onRegisterSuccess(Credentials theCredentials) {
        LoginFragment loginFragment;

        loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_login);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("A verification email has been sent, click on the link in that email to" +
                " verify your account")
                .setTitle("Verification")
                .setPositiveButton("OK", (i, d) -> {});
        AlertDialog dialog = builder.create();
        dialog.show();

        if (loginFragment != null) {
            loginFragment.fillFields(theCredentials);
        } else {
            loginFragment = new LoginFragment();
            Bundle args = new Bundle();
            args.putSerializable(getString(R.string.key_credentials), theCredentials);
            loginFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_main_container, loginFragment);

            // Clear back stack so back press exits app
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            transaction.commit();
        }
    }

    @Override
    public void onWaitFragmentInteractionShow() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_main_container, new WaitFragment(), "WAIT")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onWaitFragmentInteractionHide() {
        getSupportFragmentManager()
                .beginTransaction()
                .remove(getSupportFragmentManager().findFragmentByTag("WAIT"))
                .commit();
    }


}
