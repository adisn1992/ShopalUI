package com.example.adimarsiano.shopalui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class details_Account_Activity  extends AppCompatActivity implements View.OnClickListener {

    private TextView firstName, displayName, lastName,email;
    private ImageView imagePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_account);
        firstName = (TextView) findViewById(R.id.responseFirstName);
        displayName = (TextView)findViewById(R.id.responseDisplay);
        lastName = (TextView)findViewById(R.id.responseLastName);
        email = (TextView)findViewById(R.id.responseEmail);

        firstName.setText(signIn_Activity.userDtails.getFirstName());
        displayName.setText(signIn_Activity.userDtails.getDisplayName());
        lastName.setText(signIn_Activity.userDtails.getLastName());
        email.setText(signIn_Activity.userDtails.getEmail());



      /*  try {
            imagePic.setImageDrawable(Drawable.createFromStream(
                    getContentResolver().openInputStream(signIn_Activity.userDtails.getImageUrl()),
                    null));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
    }


    @Override
    public void onClick(View v) {


    }
}
