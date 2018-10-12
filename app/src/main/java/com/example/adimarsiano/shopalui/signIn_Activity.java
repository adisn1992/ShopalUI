package com.example.adimarsiano.shopalui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

        import android.content.Intent;
        import android.os.Bundle;
        import android.support.annotation.NonNull;
        import android.support.annotation.Nullable;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;
        import android.view.View;
        import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
        import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
        import com.google.android.gms.auth.api.signin.GoogleSignInClient;
        import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
        import com.google.android.gms.common.SignInButton;
        import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity to demonstrate basic retrieval of the Google user's ID, email address, and basic
 * profile.
 */
public class signIn_Activity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private String idToken;
    public static GoogleSignInClient mGoogleSignInClient;
    public static userAccount userDtails;
    private TextView mStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views
        //mStatusTextView = findViewById(R.id.status);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // [END configure_signin]


        // [START build_client]
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // [END build_client]

        // [START customize_button]
        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setColorScheme(SignInButton.COLOR_LIGHT);
        // [END customize_button]
    }

    @Override
    public void onStart() {
        super.onStart();

        // [START on_start_sign_in]
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);


        updateUI(account);
        // [END on_start_sign_in]
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //move to next page
        Intent k = new Intent(getApplicationContext(),home_Activity.class);
        startActivity(k);
        super.onActivityResult(requestCode, resultCode, data);


        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]



 private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            idToken = account.getIdToken();
            userDtails = new userAccount();
            userDtails.setDisplayName(account.getDisplayName());
            userDtails.setFirstName(account.getGivenName());
            userDtails.setLastName(account.getFamilyName());
            userDtails.setEmail(account.getEmail());
            userDtails.setImageUrl(account.getPhotoUrl());

            // findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);

       class GetUser extends AsyncTask<String, String, String>
       {
            private StringBuilder builder = new StringBuilder();
            private TextView contentTxt;

            @Override
            protected String doInBackground(String[] parameters)
            {
                try
                {
                   String firstName  = parameters[0].toString();
                   String lastName = parameters[1].toString();
                   String email = parameters[3].toString();
                    System.out.println("running in new thread");
                     URL tokenUrl = new URL("http://192.168.1.11:8080/rest/shopal/user/addUser/" + firstName + "/" + lastName + "/" + email);
                            System.out.println("id token: " + tokenUrl);
                            // Create connection
                            HttpURLConnection myConnection = (HttpURLConnection) tokenUrl.openConnection();

                            System.out.println("response input stream");
                            InputStream responseInputStream = myConnection.getInputStream();
                    System.out.println("response body reader");
                    java.io.InputStreamReader responseBodyReader =
                            new java.io.InputStreamReader(responseInputStream, "UTF-8");

                    System.out.println("json reader");
                    android.util.JsonReader jsonReader = new android.util.JsonReader(responseBodyReader);

                    System.out.println("begin object");
                    jsonReader.beginArray(); // Start processing the JSON object
                    jsonReader.beginObject();

                    while (jsonReader.hasNext()) { // Loop through all keys
                        String key = jsonReader.nextName(); // Fetch the next key
                        String value = jsonReader.nextString();// Fetch the value as a String

                        if (key.equals("product_name") || key.equals("product_description") || key.equals("product_barcode")) {
                            builder.append(key);
                            builder.append(" : ");
                            builder.append(value);
                            builder.append('\n');

                            // save current barcode
                          // if (key.equals("product_barcode"))
                                /*currentBarcode = value;*/
                        }
                    }
                    System.out.println(builder.toString());
                }
                catch (Exception e)
                {
                    System.out.println("Exception idStock");
                    System.out.println(e);
                }

                return builder.toString();
            }

            @Override
            protected void onPostExecute(String result)
            {
               /*   if (result.)


                if (result.isEmpty())
                    createAndShowToast("Sorry, something went wrong...\nPlease try again.");
                // display scanner info alert
                scannerResultAlert(builder.toString());

                new ValidateProduct().execute(currentBarcode);*/
            }
         }

    }
    catch (ApiException e)
    {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
        }






    // [END handleSignInResult]

    // [START signIn]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                     //   updateUI(null);
                        finish();
                        System.exit(0);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]


    // [START revokeAccess]
    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]

                        updateUI(null);
                       // Intent j = new Intent(getApplicationContext(),home_Activity.class);
                       // startActivity(j);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {

        //    mStatusTextView.setText(getString(R.string.signed_in_fmt, account.getDisplayName()));

            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            //Intent k = new Intent(getApplicationContext(),home_Activity.class);
           // startActivity(k);
           // findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);

        } else {

            //mStatusTextView.setText(R.string.signed_out);

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            //findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
               break;
        }
    }
}