package tcss450.uw.edu.chatapp;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import tcss450.uw.edu.chatapp.model.Credentials;

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
        SuccessFragment successFragment = new SuccessFragment();
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.credentials_key), theCredentials);
        successFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_main_container, successFragment);

        // Clear back stack so back press exits app
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        transaction.commit();
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

        if (loginFragment != null) {
            loginFragment.fillFields(theCredentials);
        } else {
            loginFragment = new LoginFragment();
            Bundle args = new Bundle();
            args.putSerializable(getString(R.string.credentials_key), theCredentials);
            loginFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_main_container, loginFragment);

            // Clear back stack so back press exits app
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            transaction.commit();
        }
    }
}
