package com.example.adimarsiano.shopalui;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

// adi TODO: getStockId, get pictures of products, *add product, handle EXCEPTION
// adi: update quantity, delete product (picture of garbage)

public class stock_Activity extends AppCompatActivity implements View.OnClickListener{

    // adi: get stockId!
    private String stockId = "5ba68a6df21c55ef12534b8a";
    //private String test = "{ \"_id\" : { \"$oid\" : \"5ba68a6df21c55ef12534b8a\"} , \"name\" : \"work\" , \"list\" : [ { \"productId\" : 1 , \"available\" : 2 , \"limit\" : 4} , { \"productId\" : 2 , \"available\" : 1 , \"limit\" : 1}]}";

    private TextView stockText;
    private TableLayout table;
    private TableRow head_row;
    private Button submitButton;
    private AppCompatActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_);

        stockText = (TextView)findViewById(R.id.stock_text);
        submitButton = (Button)findViewById(R.id.stock_submitButton);
        context = this;

        createStockTable();
        ImportStock importStock = new ImportStock();
        importStock.execute(this, stockId);

        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clickSubmitButton();
            }
        });
    }

    @Override
    public void onClick(View v) {
        String buttonTag = v.getTag().toString();

        if (buttonTag.contains("deleteButton_")){
            JSONObject data = new JSONObject();
            data.put("stockId", stockId);
            String productId_str =buttonTag.substring(13);
            data.put("productId", Integer.parseInt(productId_str));
            RemoveProduct remove = new RemoveProduct();
            remove.execute(data);
        }
        else {
            // default method for handling onClick Events..
        }
    }

    private void clickSubmitButton(){
        JSONObject data = new JSONObject();
        JSONArray productsArray = new JSONArray();

        // iterate over the stock table (from the second row) and build products json
        for(int i = 1, rows = table.getChildCount(); i < rows; i++) {
            View row = table.getChildAt(i);
            if (row instanceof TableRow) {
                JSONObject product = new JSONObject();

                // adi: get product id by index (i-1) from stocks
                NumberPicker productId = (NumberPicker) ((TableRow) row).getChildAt(1);
                product.put("productId", productId.getValue());

                NumberPicker available = (NumberPicker) ((TableRow) row).getChildAt(2);
                product.put("available", available.getValue());


                NumberPicker limit = (NumberPicker) ((TableRow) row).getChildAt(3);
                product.put("limit", limit.getValue());

                productsArray.add(product);
            }
        }

        data.put("stockId", stockId);
        data.put("products", productsArray);


        SubmitTable submit = new SubmitTable();
        submit.execute(data);
    }

    private class SubmitTable extends AsyncTask<Object, Void, String> {

        private  JSONObject data;

        @Override
        protected String doInBackground(Object[] parameters) {
            try {
                // define postData
                data = (JSONObject)parameters[0];

                // Url
                URL stockUrl = new URL("http://192.168.1.2:8080/rest/stock/update/");
                // connection
                HttpURLConnection urlConnection = (HttpURLConnection) stockUrl.openConnection();
                // request property
                urlConnection.setRequestProperty("Content-Type", "application/json");
                // request type
                urlConnection.setRequestMethod("PUT");
                // send data
                if (data != null) {
                    OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                    writer.write(data.toString());
                    writer.flush();
                }

                // status
                int statusCode = urlConnection.getResponseCode();

                // success
                if (statusCode ==  200) {
                    InputStream responseInputStream = urlConnection.getInputStream();
                    String response = IOUtils.toString(responseInputStream, "UTF_8");
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
        protected void onPostExecute(String res) {
            Toast toast = Toast.makeText(getApplicationContext(), "Submitted", Toast.LENGTH_LONG);
            toast.show();
            // adi: load ?
        }
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

    private class RemoveProduct extends AsyncTask<Object, String, String> {

        @Override
        protected String doInBackground(Object[] parameters) {
            try {
                JSONObject data = (JSONObject)parameters[0];

                // Url
                URL stockUrl = new URL("http://192.168.1.2:8080/rest/stock/remove/");
                // connection
                HttpURLConnection urlConnection = (HttpURLConnection) stockUrl.openConnection();
                // request type
                urlConnection.setRequestMethod("PUT");
                // send data
                if (stockId != null) {
                    OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                    writer.write(data.toJSONString());
                    writer.flush();
                }
                // status
                int statusCode = urlConnection.getResponseCode();

                // success
                if (statusCode ==  200) {
                    InputStream responseInputStream = urlConnection.getInputStream();
                    String response = IOUtils.toString(responseInputStream, "UTF_8");
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
        protected void onPostExecute(String productId) {
            removeRowByProductId(productId);
        }

    }

    private void removeRowByProductId(String productId){
        for(int i = 1, rows = table.getChildCount(); i < rows; i++) {
            View row = table.getChildAt(i);
            if (row instanceof TableRow) {
                Button delete = (Button) ((TableRow) row).getChildAt(4);
                String deleteTag = (String)delete.getTag();

                if (deleteTag.contains(productId)){
                    table.removeView(row);
                    return;
                }
            }
        }

        int x =5;
    }

    // create table structure
    private void createStockTable(){
        table = findViewById(R.id.stock_table);
        head_row = findViewById(R.id.head_row);

        head_row.addView(Utils.createTextView(this,"img"));
        head_row.addView(Utils.createTextView(this,"available"));
        head_row.addView(Utils.createTextView(this,"limit"));
    }

    private void fillStockTable(JSONObject stock, AppCompatActivity context){
        JSONArray productArray = null;
        JSONParser parser = new JSONParser();
        try {
            String a = stock.get("list").toString();
            productArray = (JSONArray) parser.parse(a);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for(Object item: productArray){
            if ( item instanceof JSONObject ) {
                JSONObject product =  (JSONObject)item;

                TableRow new_row = new TableRow(this);


                new_row.addView(Utils.createTextView(context, product.get("productId").toString() ));
                new_row.addView(Utils.createNumberPicker(context, Integer.parseInt(product.get("productId").toString())));
                new_row.addView(Utils.createNumberPicker(context, Integer.parseInt(product.get("available").toString())));
                new_row.addView(Utils.createNumberPicker(context, Integer.parseInt(product.get("limit").toString())));

                Button delete = new Button(context);
                String buttonId = "deleteButton_" + product.get("productId").toString();
                delete.setOnClickListener((android.view.View.OnClickListener)context);
                delete.setTag(buttonId);
                delete.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_trash_icon_round, 0, 0, 0);

                new_row.addView(delete);

                table.addView(new_row);
            }
        }
    }
}
