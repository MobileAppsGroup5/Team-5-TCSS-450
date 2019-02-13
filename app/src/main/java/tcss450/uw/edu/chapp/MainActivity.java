package tcss450.uw.edu.chapp;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.Serializable;

import tcss450.uw.edu.chapp.model.Credentials;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginFragmentInteractionListener, RegisterFragment.OnRegisterFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(getString(R.string.key_credentials), theCredentials);
        intent.putExtra(getString(R.string.keys_intent_jwt), jwt);
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

    /**
     *
     */
    private void login(final Credentials credentials, String jwt) {
        Intent i = new Intent(this, HomeActivity.class);
        i.putExtra(getString(R.string.key_email), (Serializable) credentials);

        i.putExtra(getString(R.string.keys_intent_jwt), jwt);
        startActivity(i);
        // End this Activity and remove it from the Activity back stack.
        finish();
    }
}