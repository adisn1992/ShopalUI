package com.example.adimarsiano.shopalui;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class shoppingList_Activity extends AppCompatActivity  implements View.OnClickListener{
    // shoppingList
    // adi: get shoppingListId!
    private String stockId = "5bb0909031e93b5b3b3c21ec";

    private TextView shoppingListText;
    private TableLayout table;
    private TableRow head_row;
    private Button purchaseButton;
    private AppCompatActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_);

        shoppingListText = (TextView)findViewById(R.id.shoppingList_text);

        purchaseButton = (Button)findViewById(R.id.purchaseButton);
        purchaseButton.setOnClickListener((android.view.View.OnClickListener)context);
        purchaseButton.setTag("purchaseButton");

        context = this;

        createShoppingListTable();
        new ImportShoppingList().execute();
    }

    @Override
    public void onClick(View button) {

        String buttonTag = button.getTag().toString();

        if(button.getTag().toString().contains("purchaseButton")){
            clickPurchaseButton();
        }
        else{

        }


        // add code to both buttons
        // all the logic! also back!
    }

    private JSONObject getQuantitiesFromTable(){
        JSONArray productsArray = new JSONArray();

        // iterate over the stock table (from the second row) and build products json
        for(int i = 1, rows = table.getChildCount(); i < rows; i++) {
            View row = table.getChildAt(i);
            if (row instanceof TableRow) {
                JSONObject product = new JSONObject();

                // adi: get product id by index (i-1) from stocks , need to change to take from imgs the productId
                NumberPicker productId = (NumberPicker) ((TableRow) row).getChildAt(0);
                product.put("productId", productId.getValue());

                NumberPicker quantity = (NumberPicker) ((TableRow) row).getChildAt(1);
                product.put("quantity", quantity.getValue());


                productsArray.add(product);
            }
        }

        JSONObject data = new JSONObject();
        data.put("shoppingListId", stockId);
        data.put("products", productsArray);

        return data;
    }

    // create table structure
    private void clickPurchaseButton(){
        new Purchase().execute(getQuantitiesFromTable());
    }

    private void createShoppingListTable(){
        table = findViewById(R.id.shoppingList_table);
        head_row = findViewById(R.id.shoppingList_head_row);

        head_row.addView(Utils.createTextView(this,"img"));
        head_row.addView(Utils.createTextView(this,"quantity"));
    }

    private void fillShoppingListTable(JSONObject shoppingList, JSONObject pictures) throws IOException {
        JSONArray productArray = null;
        JSONArray productsImgArray = null;
        JSONParser parser = new JSONParser();
        try {
            productArray = (JSONArray) parser.parse(shoppingList.get("list").toString());
            productsImgArray = (JSONArray) parser.parse(pictures.get("productsImg").toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Iterator<JSONObject> itr_productArray = productArray.iterator();
        Iterator<JSONObject> itr_productsImgArray = productsImgArray.iterator();


        while (itr_productArray.hasNext() && itr_productsImgArray.hasNext()) {

            JSONObject product = itr_productArray.next();
            JSONObject img = itr_productsImgArray.next();

            TableRow new_row = new TableRow(this);

            //IMG: set tag by productId!
            //String imgStr = img.get("productImg").toString();
            //ImageView imgView = new ImageView(context);
            //new stock_Activity.AsyncTaskLoadImage().execute(imgStr, imgView);
            //new_row.setWeightSum(3);
            //new_row.addView(imgView);

            // adi!!!!: need to delete the first (productId) (img tag instead)
            new_row.addView(Utils.createNumberPicker(context, Integer.parseInt(product.get("productId").toString())));
            new_row.addView(Utils.createNumberPicker(context, Integer.parseInt(product.get("quantity").toString())));

            table.addView(new_row);

        }
    }

    private class Purchase extends AsyncTask<Object, Void, String> {

        private  JSONObject data;

        @Override
        protected String doInBackground(Object[] parameters) {
            try {
                // define postData
                data = (JSONObject)parameters[0];

                // Url
                URL stockUrl = new URL("http://192.168.1.2:8080/rest/shoppingList/purchase");
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
            new ImportShoppingList().execute(stockId);
            //adi : Toast toast = Toast.makeText(getApplicationContext(), "Submitted", Toast.LENGTH_LONG);
            //toast.show();
        }
    }

    private class ImportShoppingList extends AsyncTask<Object, String, JSONObject> {
        @Override
        protected JSONObject doInBackground(Object[] parameters) {
            try {
                // Url
                URL stockUrl = new URL("http://192.168.1.2:8080/rest/shoppingList/get/" + stockId);
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
        protected void onPostExecute(JSONObject shoppingList) {
            JSONObject pictures = null;
            try {
                pictures = new GetProductsImg().execute().get();
                fillShoppingListTable(shoppingList, pictures);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private class GetProductsImg extends AsyncTask<Object, String, JSONObject> {
        @Override
        protected JSONObject doInBackground(Object[] parameters) {

            try {
                URL stockUrl = new URL("http://192.168.1.2:8080/rest/product/getImgsByShoppingList/" + stockId);
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

}
