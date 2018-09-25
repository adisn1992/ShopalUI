package com.example.adimarsiano.shopalui;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;

public class stock_Activity extends AppCompatActivity {

    // adi: get stockId!
    private String stockId;

    private TextView stockText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_);
        stockText = (TextView)findViewById(R.id.stock_text);

        GetStock getStock = new GetStock();
        try {
            String stock_Str = getStock.execute("5ba68a6df21c55ef12534b8a").get();
            stockText.setText(stock_Str);
        }
        catch (Exception e){
            System.out.println(e);
        }
    }


    private class GetStock extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String[] parameters) {
            try {
                // Url
                URL stockUrl = new URL("http://192.168.1.2:8080/rest/stock/get/" + parameters[0].toString());
                // connection
                HttpURLConnection urlConnection = (HttpURLConnection) stockUrl.openConnection();
                // request type
                urlConnection.setRequestMethod("GET");
                // status
                int statusCode = urlConnection.getResponseCode();

                // success
                if (statusCode ==  200) {
                    InputStream responseInputStream = urlConnection.getInputStream();
                    String response = IOUtils.toString(responseInputStream, "UTF_8");
                    // adi: to json
                    return response;
                }
                // failure
                else{
                    // adi: handle error
                    return null;
                }
            } catch (Exception e){
                System.out.println(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //return result;
            //contentTxt = (TextView)findViewById(R.id.scan_content);
            //contentTxt.setText(builder.toString());
        }
    }
}
