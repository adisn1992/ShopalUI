package com.example.adimarsiano.shopalui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.zxing.intergration.android.IntentIntegrator;

public class home_Activity extends AppCompatActivity implements View.OnClickListener {

    //TextView tv1;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView mStatusTextView;
    private String stockId = "";
    private ImageButton infoBtn;

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
           // mStatusTextView.setText(getString(R.string.signed_in_fmt, account.getDisplayName()));

          //  findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            //mStatusTextView.setText(R.string.signed_out);

          //  findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }


    // [START signOut]
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                       finish();
                       System.exit(0);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle b = getIntent().getExtras();
        if(b != null)
            stockId = b.getString("stockId");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Views
       // mStatusTextView = findViewById(R.id.status);
        findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);

        // Button listeners
        findViewById(R.id.disconnect_button).setOnClickListener(this);
        //SignInButton signInButton = findViewById(R.id.sign_in_button);


        mGoogleSignInClient = signIn_Activity.mGoogleSignInClient;
        infoBtn = (ImageButton) findViewById(R.id.infoButton);
        infoBtn.setOnClickListener(this);
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build();
        // [END configure_signin]


        // [START build_client]
        // Build a GoogleSignInClient with the options specified by gso.
       // mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // [END build_client]

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.disconnect_button:
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Sign Out")
                        .setMessage("Are you sure you want to sign out your google account and return to login menu?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                             signOut();
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
                break;
                //info button
            case  R.id.infoButton :
                    infoAlert();
                    break;
        }
    }

    public void barcodeScanner(View view){
        Intent startBarcodeScannerActivity = new Intent(this, BarcodeScannerActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("stockId", stockId);
        startBarcodeScannerActivity.putExtras(bundle);

        startActivity(startBarcodeScannerActivity);
    }

    public void detailsAccount(View view){
        Intent startDetailsAccountActivity = new Intent(this, details_Account_Activity.class);
        startActivity(startDetailsAccountActivity);
    }

    public void shoppingList(View view){
        Intent startShoppingListActivity = new Intent(this, shoppingList_Activity.class);

        Bundle bundle = new Bundle();
        bundle.putString("stockId", stockId);
        startShoppingListActivity.putExtras(bundle);

        startActivity(startShoppingListActivity);
    }

    public void aboutUs(View view){
        Intent startAboutUsActivity = new Intent(this, about_Activity.class);
        startActivity(startAboutUsActivity);
    }

    public void stock(View view){
        Intent startStockActivity = new Intent(this, stock_Activity.class);

        Bundle bundle = new Bundle();
        bundle.putString("stockId", stockId);
        startStockActivity.putExtras(bundle);

        startActivity(startStockActivity);
    }

    private void infoAlert() {
        String msgToShow = "Welcome to Shopal. You can :\n\n-Add products to your stock via Barcode Scanner\n\n-Update how many available products and how many you wish to have of each of them in your Stock\n\n-View and update your Shopping List\n\n-Throw an used product into the trash via Barcode Scanner\n\nEnjoy :)  ";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msgToShow)
                .setTitle("Hello");

        // Add the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}

