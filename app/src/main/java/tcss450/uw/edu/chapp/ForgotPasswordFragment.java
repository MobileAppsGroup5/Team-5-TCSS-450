package tcss450.uw.edu.chapp;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class ForgotPasswordFragment extends Fragment implements View.OnClickListener {

    private EditText mUserEmail;
    private Button mResetPasswordButton;
    private Credentials mEmail;
    private String mJwToken;


    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        if (getArguments() != null) {
            mEmail = (Credentials) getArguments().getSerializable(getString(R.string.key_email));
            mJwToken = getArguments().getString(getString(R.string.keys_intent_jwt));

        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        mResetPasswordButton = (Button) view.findViewById(R.id.btn_reset_password);
        mResetPasswordButton.setOnClickListener(this::sendForgotEmail);


        // Inflate the layout for this fragment
        return view;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

        Toast.makeText(getContext(), "Password reset email sent! Check email!", Toast.LENGTH_LONG).show();
    }

    private void sendForgotEmail(View view) {
        Uri uri = new Uri.Builder()
                .scheme("https")
           //     .appendPath(getString(R.string.ep_base_url))
                .authority(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_forgot_base))
                .appendPath(getString(R.string.ep_forgot_email))
                .build();

        Log.d("URI", uri.toString());

        JSONObject msg = new JSONObject();
        try {
            msg.put("email", ((EditText) getActivity().findViewById(R.id.et_enter_email)).getText().toString());
            Log.d("EMAIL", "sent this email over " + msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleSentForgotEmailPostExecute)
         //       .addHeaderField("authorization", mJwToken) // add the JWT as a header
                .build().execute();
    }

        private void handleSentForgotEmailPostExecute(String result) {
            TextView tv = getActivity().findViewById(R.id.tv_sent_password_result);
            tv.setVisibility(View.VISIBLE);


            try {
                // This is the result from the web service
                JSONObject res = new JSONObject(result);
                if(res.has("success") && res.getBoolean("success")) {

                    // set the output text saying that email is sent
                    tv.setText("Email was sent for password reset! Check email " + res);
                } else {
                    // log the error
                    tv.setText("Error, something went wrong.");
                    Log.e("Oops", result);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

