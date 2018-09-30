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
import java.net.URL;

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

    private class GetProductTask extends AsyncTask<String, String, String>  {
        private  StringBuilder builder = new StringBuilder();
        private TextView  contentTxt;
        @Override
        protected String doInBackground(String[] parameters) {
            try {
                System.out.println("running in new thread");
                URL shopalUrl = new URL("http://192.168.1.13:8080/rest/shopal/product/" + parameters[0].toString());

                System.out.println("url: " + shopalUrl);
                // Create connection
                HttpURLConnection myConnection = (HttpURLConnection) shopalUrl.openConnection();

                System.out.println("response input stream");
                InputStream responseInputStream = myConnection.getInputStream();

                System.out.println("response body reader");
                InputStreamReader responseBodyReader =
                        new InputStreamReader(responseInputStream, "UTF-8");

                System.out.println("json reader");
                JsonReader jsonReader = new JsonReader(responseBodyReader);

                System.out.println("begin object");
                jsonReader.beginArray(); // Start processing the JSON object
                jsonReader.beginObject();
                //StringBuilder builder = new StringBuilder();
                while (jsonReader.hasNext()) { // Loop through all keys
                    String key = jsonReader.nextName(); // Fetch the next key
                    // Fetch the value as a String
                    String value = jsonReader.nextString();

                    builder.append(key);
                    builder.append(" : ");
                    builder.append(value);
                    builder.append('\n');

                }
                System.out.println(builder.toString());
//            contentTxt = (TextView) objects[1];
//            contentTxt.setText("Product Details: " + builder.toString());
            } catch (Exception e)
            {
                System.out.println("I have an exception");
                System.out.println(e);
            }


            return builder.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            contentTxt = (TextView)findViewById(R.id.scan_content);
            contentTxt.setText(builder.toString());
        }
    }


    //retrieve scan result
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        //we have a result
        if(scanningResult != null) {
            final String scanContent = scanningResult.getContents();
            GetProductTask getProductTask = new GetProductTask();
            getProductTask.execute(scanContent);

        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(),"No scan received!",Toast.LENGTH_SHORT);
            toast.show();
        }

    }
}
