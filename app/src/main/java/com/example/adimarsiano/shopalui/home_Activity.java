package com.example.adimarsiano.shopalui;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class home_Activity extends Activity implements View.OnClickListener {

    //TextView tv1;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView mStatusTextView;


    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {

            //findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
           // mStatusTextView.setText(R.string.signed_out);

           // findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    // [START signOut]
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        updateUI(null);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_out_button:
                signOut();

                break;
            case R.id.disconnect_button:
                revokeAccess();

                break;
        }
    }

    // [START revokeAccess]
    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        updateUI(null);

                        // [END_EXCLUDE]
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Views
        mStatusTextView = findViewById(R.id.status);
        findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);

        // Button listeners
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);
        //SignInButton signInButton = findViewById(R.id.sign_in_button);

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}

