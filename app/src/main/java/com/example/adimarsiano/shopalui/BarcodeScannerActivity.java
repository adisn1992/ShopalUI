package com.example.adimarsiano.shopalui;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.zxing.intergration.android.IntentIntegrator;
import com.google.zxing.intergration.android.IntentResult;
import android.app.Activity;
import android.content.Intent;
import android.util.JsonReader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class BarcodeScannerActivity extends AppCompatActivity implements OnClickListener{

    private Button scanBtn;
    private TextView formatTxt, contentTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        scanBtn = (Button)findViewById(R.id.scan_button);
        formatTxt = (TextView)findViewById(R.id.scan_format);
        contentTxt = (TextView)findViewById(R.id.scan_content);

        scanBtn.setOnClickListener(this);
    }

    //respond to clicks
    public void onClick(View v){
        //scan
        if(v.getId()==R.id.scan_button){
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
    }

    //retrieve scan result
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        //we have a result
        if(scanningResult != null){
            final String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            formatTxt.setText("FORMAT: "+ scanFormat);
            contentTxt.setText("CONTENT: "+ scanContent);


            System.out.println("going to send rest");
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    // Send barcode to server in order to get product
                    // Create URL
                    try {
                        System.out.println("running in new thread");
                        URL shopalUrl = new URL("http://192.168.1.13:8080/rest/shopal/product/" + scanContent);

                        System.out.println("url: " + shopalUrl);
                        // Create connection
                        HttpURLConnection myConnection = (HttpURLConnection) shopalUrl.openConnection();

//                        System.out.println("response input stream");
//                        InputStream responseInputStream = myConnection.getInputStream();
//                        System.out.println("response body reader");
//                        InputStreamReader responseBodyReader =
//                                new InputStreamReader(responseInputStream, "UTF-8");
//
//                        System.out.println("json reader");
//                        JsonReader jsonReader = new JsonReader(responseBodyReader);
//
//                        System.out.println("begin object");
//                        jsonReader.beginObject(); // Start processing the JSON object
//                        while (jsonReader.hasNext()) { // Loop through all keys
//                            System.out.println("next");
//                            String key = jsonReader.nextName(); // Fetch the next key
//                            // Fetch the value as a String
//                            String value = jsonReader.nextString();
//
//                            System.out.println(key + ":" + value);
//
//                            // Do something with the value
//                            // ...
//                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            });



        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(),"No scan received!",Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
