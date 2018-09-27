package com.example.adimarsiano.shopalui;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

// adi TODO: getStockId, get pictures of products, delete product, update quantity, add product, handle EXCEPTION

public class stock_Activity extends AppCompatActivity {

    // adi: get stockId!
    private String stockId;

    private TextView stockText;
    private TableLayout table;
    private TableRow head_row;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_);

        createStockTable();
        ImportStock importStock = new ImportStock();
        importStock.execute(this, "5ba68a6df21c55ef12534b8a");
    }



    private class ImportStock extends AsyncTask<Object, String, JSONObject> {

        private  AppCompatActivity context;

        @Override
        protected JSONObject doInBackground(Object[] parameters) {
            try {
                // define context
                context = (AppCompatActivity)parameters[0];

                // Url
                URL stockUrl = new URL("http://192.168.1.2:8080/rest/stock/get/" + parameters[1].toString());
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
                    return Utils.toJson(response);
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
        protected void onPostExecute(JSONObject result) {
            fillStockTable(result, context);
        }

    }

    // create table structure
    private void createStockTable(){
        stockText = (TextView)findViewById(R.id.stock_text);
        table = (TableLayout)findViewById(R.id.stock_table);
        head_row = (TableRow)findViewById(R.id.head_row);

        head_row.addView(Utils.createTextView(this,"img"));
        head_row.addView(Utils.createTextView(this,"available"));
        head_row.addView(Utils.createTextView(this,"limit"));
    }

    private void fillStockTable(JSONObject stock, AppCompatActivity context){
        JSONArray productArray = (JSONArray)stock.get("list");

        for(Object item: productArray){
            if ( item instanceof JSONObject ) {
                JSONObject product =  (JSONObject)item;

                TableRow new_row = new TableRow(this);

                new_row.addView(Utils.createTextView(context, product.get("productId").toString()));
                new_row.addView(Utils.createTextView(context, product.get("available").toString()));
                new_row.addView(Utils.createTextView(context, product.get("limit").toString()));

                table.addView(new_row);
            }
        }
    }
}
