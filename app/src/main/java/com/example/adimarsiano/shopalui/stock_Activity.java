package com.example.adimarsiano.shopalui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

// adi TODO: getStockId, , *add product, handle EXCEPTION, design!
// adi: update quantity, delete product (picture of garbage), get pictures of products


// pay attention: the deleteButton contains the productId in it's name (for example: deleteButton_4573) - that is the way we are locating the productId
public class stock_Activity extends AppCompatActivity implements View.OnClickListener{

    // adi: get stockId!
    private String stockId = "5bb0909031e93b5b3b3c21ec";
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

        stockText = findViewById(R.id.stock_text);
        submitButton = findViewById(R.id.stock_submitButton);
        context = this;
        table = findViewById(R.id.stock_table);

        new ImportStock().execute(context, stockId);

        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clickSubmitButton();
            }
        });
    }

    @Override
    public void onClick(View v) {
        String buttonTag = v.getTag().toString();

        // if delete button
        if (buttonTag.contains("deleteButton_")){

            JSONObject data = new JSONObject();
            data.put("stockId", stockId);

            // get productId from button tag
            data.put("productId", getProductIdByTag(v));

            new RemoveProduct().execute(data);
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

                product.put("productId", getProductIdByRow(row));

                NumberPicker available = (NumberPicker) ((TableRow) row).getChildAt(1);
                product.put("available", available.getValue());

                NumberPicker limit = (NumberPicker) ((TableRow) row).getChildAt(2);
                product.put("limit", limit.getValue());

                productsArray.add(product);
            }
        }

        data.put("stockId", stockId);
        data.put("products", productsArray);

        new SubmitTable().execute(data);
    }

    public static class LoadImage extends AsyncTask<Object, String, Bitmap> {
        private ImageView imageView;

        @Override
        protected Bitmap doInBackground(Object[] parameters) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(parameters[0].toString());
                imageView = (ImageView)parameters[1];
                bitmap = BitmapFactory.decodeStream((InputStream)url.getContent());
            } catch (IOException e) {
                Log.e("AsyncTaskLoadImage", e.getMessage());
            }
            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
            return;
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
        protected void onPostExecute(JSONObject stock) {
            try {
                JSONObject pictures = new GetImagesUrls().execute(stockId).get();
                fillStockTable(stock, pictures, context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
            new ImportStock().execute(context, stockId);
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
            removeRowByProductId(Integer.parseInt(productId));
        }

    }

    private class GetImagesUrls extends AsyncTask<Object, String, JSONObject> {
        @Override
        protected JSONObject doInBackground(Object[] parameters) {

            try {

               // a = "{\"s\":2}";
                // Url - to product and not to stock
                URL stockUrl = new URL("http://192.168.1.2:8080/rest/product/getImgs_stock/" +  parameters[0].toString());
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
    }

    private void removeRowByProductId(Integer productId){
        for(int i = 1, rows = table.getChildCount(); i < rows; i++) {
            View row = table.getChildAt(i);
            if (row instanceof TableRow) {
                // find the row that contains the productId and remove it
                if (getProductIdByRow(row) == productId){
                    table.removeView(row);
                    return;
                }
            }
        }
    }

    // create table structure
    private void createStockTable(){
        table = findViewById(R.id.stock_table);
        head_row = findViewById(R.id.head_row);

        head_row.addView(Utils.createTextView(this,"img"));
        head_row.addView(Utils.createTextView(this,"available"));
        head_row.addView(Utils.createTextView(this,"limit"));
    }

    // TODO
    private void fillStockTable(JSONObject stock, JSONObject pictures, AppCompatActivity context) {
        JSONArray productArray = null;
        JSONArray productsImgArray = null;
        JSONParser parser = new JSONParser();
        try {
            productArray = (JSONArray) parser.parse(stock.get("items").toString());
            productsImgArray = (JSONArray) parser.parse(pictures.get("productsImg").toString());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        Iterator<JSONObject> itr_productArray = productArray.iterator();
        Iterator<JSONObject> itr_productsImgArray = productsImgArray.iterator();


        while (itr_productArray.hasNext() && itr_productsImgArray.hasNext()) {
            JSONObject product = itr_productArray.next();
            JSONObject img = itr_productsImgArray.next();

            TableRow new_row =(TableRow)getLayoutInflater().inflate(R.layout.tablerow_template, null);

            // adi: set min max

            // available:
            EditText available = (EditText)new_row.getChildAt(1);
            available.setText(product.get("available").toString());
            // limit:
            EditText limit = (EditText)new_row.getChildAt(2);
            limit.setText(product.get("limit").toString());

            //img:
            ImageView imgView = (ImageView)new_row.getChildAt(0);
            new LoadImage().execute(img.get("productImg").toString(), imgView);

            // delete button
            ImageButton delete = (ImageButton)new_row.getChildAt(3);
            delete.setTag("deleteButton_" + product.get("productId").toString());
            delete.setOnClickListener((View.OnClickListener)context);

            table.addView(new_row);
        }
    }

    private View createNumberPickerByField(JSONObject product, String field){
        View numberPicker = Utils.createNumberPicker(context, Integer.parseInt(product.get(field).toString()));

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        );
        numberPicker.setLayoutParams(param);

        return numberPicker;
    }

    private Integer getProductIdByTag(View v){
        String tag = v.getTag().toString();
        return Integer.parseInt(tag.substring(tag.indexOf("_") + 1));
    }

    private Integer getProductIdByRow(View row){
        return getProductIdByTag(((TableRow) row).getChildAt(0));
    }
}
