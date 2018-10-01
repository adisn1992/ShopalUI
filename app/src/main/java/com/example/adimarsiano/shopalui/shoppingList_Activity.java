package com.example.adimarsiano.shopalui;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import static com.example.adimarsiano.shopalui.stock_Activity.getProductIdByTag;

public class shoppingList_Activity extends AppCompatActivity  implements View.OnClickListener{
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
        context = this;

        shoppingListText = findViewById(R.id.shoppingList_text);
        table = findViewById(R.id.shoppingList_table);

        purchaseButton = findViewById(R.id.purchaseButton);
        purchaseButton.setOnClickListener((android.view.View.OnClickListener)context);
        purchaseButton.setTag("purchaseButton");



        new ImportShoppingList().execute();
    }

    @Override
    public void onClick(View button) {
        if(button.getTag().toString().contains("purchaseButton")){
            clickPurchaseButton();
        }
        else{

        }
    }

    private void clickPurchaseButton() {
        JSONObject data = new JSONObject();
        JSONArray productsArray = new JSONArray();

        // iterate over the stock table (from the second row) and build products json
        for (int i = 1, rows = table.getChildCount(); i < rows; i++) {

            View row = table.getChildAt(i);

            if (row instanceof TableRow) {
                JSONObject product = new JSONObject();
                EditText toPurchase = (EditText) ((TableRow) row).getChildAt(2);

                product.put("productId", getProductIdByTag(row));
                product.put("purchased", Integer.parseInt(toPurchase.getText().toString()));
                productsArray.add(product);
            }
        }

        data.put("stockId", stockId);
        data.put("products", productsArray);

        new Purchase().execute(data);
    }

    private void fillShoppingListTable(JSONObject shoppingList, JSONObject pictures) {
        // getting iterators of products and images
        JSONArray products = null;
        JSONArray images = null;
        JSONParser parser = new JSONParser();

        try {
            images = (JSONArray) parser.parse(pictures.get("productsImg").toString());
            products = (JSONArray) parser.parse(shoppingList.get("items").toString());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        Iterator<JSONObject> itr_images = images.iterator();
        Iterator<JSONObject> itr_products = products.iterator();

        // iterate over products and images and builds rows
        while (itr_products.hasNext() && itr_images.hasNext()) {
            JSONObject product = itr_products.next();
            JSONObject img = itr_images.next();

            // row:
            TableRow new_row =(TableRow)getLayoutInflater().inflate(R.layout.tablerow_shoppinglist_template, null);
            new_row.setTag(product.get("productId").toString());

            // img:
            ImageView imgView = (ImageView)new_row.getChildAt(1);
            new stock_Activity.LoadImage().execute(img.get("productImg").toString(), imgView);

            // to buy:
            EditText limit = (EditText)new_row.getChildAt(2);
            limit.setText(product.get("toPurchase").toString());

            table.addView(new_row);
        }
    }

    private class ImportShoppingList extends AsyncTask<Object, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(Object[] parameters) {
            try {
                // Url
                URL stockUrl = new URL("http://192.168.1.2:8080/rest/stock/getShopList/" + stockId);
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
            try {
                JSONObject pictures = new stock_Activity.GetImagesUrls().execute(stockId).get();
                fillShoppingListTable(shoppingList, pictures);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                URL stockUrl = new URL("http://192.168.1.2:8080/rest/stock/purchase");
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
            TableRow headRow = findViewById(R.id.head_row_shoppingList);
            table.removeAllViews();
            table.addView(headRow);
            new ImportShoppingList().execute();

            //Toast toast = Toast.makeText(getApplicationContext(), "Submitted", Toast.LENGTH_LONG);
            //toast.show();
            //adi : Toast toast = Toast.makeText(getApplicationContext(), "Submitted", Toast.LENGTH_LONG);
            //toast.show();
        }
    }


}
