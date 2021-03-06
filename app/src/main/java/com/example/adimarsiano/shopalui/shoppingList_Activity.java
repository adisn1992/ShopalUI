package com.example.adimarsiano.shopalui;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import static com.example.adimarsiano.shopalui.stock_Activity.getProductIdByTag;

public class shoppingList_Activity extends AppCompatActivity  implements View.OnClickListener {
    // private vars
    private String stockId = "";
    private AppCompatActivity context;
    private Button purchaseButton;
    private TableLayout table;

    // status code:
    private static final int BAD_REQUEST = 400;
    private static final int SUCCESS = 200;
    private final int VOID_SUCCESS = 204;
    private static final int NOT_ACCEPTABLE = 406;
    private static final int ERROR = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // get stockId from prev activity
        Bundle b = getIntent().getExtras();
        if(b != null)
            stockId = b.getString("stockId");

        // configurations
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_);

        // configure private vars from view
        context = this;
        table = findViewById(R.id.shoppingList_table);

        // configure purchase button
        purchaseButton = findViewById(R.id.purchaseButton);
        purchaseButton.setOnClickListener((android.view.View.OnClickListener) context);
        purchaseButton.setTag("purchaseButton");

        // import shopping list
        new ImportShoppingList().execute();
    }

    @Override
    public void onClick(View button) {
        if (button.getTag().toString().contains("purchaseButton")) {
            clickPurchaseButton();
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

    private void fillShoppingListTable(JSONObject shoppingList, JSONArray picturesAndNames) {
        // getting iterators of products and images
        JSONArray products = null;
        JSONArray productsData = null;
        JSONParser parser = new JSONParser();

        try {
            productsData = (JSONArray) parser.parse(picturesAndNames.toString());
            products = (JSONArray) parser.parse(shoppingList.get("items").toString());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        Iterator<JSONObject> itr_products = products.iterator();
        Iterator<JSONObject> itr_productsData = productsData.iterator();

        // iterate over products and images and builds rows
        while (itr_products.hasNext() && itr_productsData.hasNext()) {
            JSONObject product = itr_products.next();
            JSONObject data = itr_productsData.next();

            // row:
            TableRow new_row = (TableRow) getLayoutInflater().inflate(R.layout.tablerow_shoppinglist_template, null);
            new_row.setTag(product.get("productId").toString());

            // img:
            ImageView imgView = (ImageView) new_row.getChildAt(0);
            String img = data.get("productImg").toString();
            if(img.contains("https")){
                new stock_Activity.LoadImage().execute(img, imgView);
            }

            // name:
            TextView nameView = (TextView)new_row.getChildAt(1);
            nameView.setText(data.get("productName").toString());

            // to buy:
            EditText toPurchase = (EditText) new_row.getChildAt(2);
            toPurchase.setText(product.get("toPurchase").toString());

            table.addView(new_row);
        }
    }

    private void createAndShowToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        toast.show();
    }

    private void refreshTable() {
        TableRow headRow = findViewById(R.id.head_row_shoppingList);
        table.removeAllViews();
        table.addView(headRow);
        new ImportShoppingList().execute();
    }

    private void createAndShowAlert(String text){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(text);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private class ImportShoppingList extends AsyncTask<Object, String, JSONObject> {
        private JSONObject response = new JSONObject();

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

                switch (statusCode) {
                    case SUCCESS:
                    case VOID_SUCCESS:
                        InputStream responseInputStream = urlConnection.getInputStream();
                        String shoppingListStr = IOUtils.toString(responseInputStream, "UTF_8");

                        response.put("status", SUCCESS);
                        response.put("shoppingList", Utils.toJson(shoppingListStr));
                        break;
                    case BAD_REQUEST:
                        response.put("status", BAD_REQUEST);
                        break;
                    default:
                        response.put("status", ERROR);
                        break;
                }

                return response;
            } catch (Exception e) {
                response.put("status", ERROR);
                return response;
            }
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            int statusCode = Integer.parseInt(res.get("status").toString());

            switch (statusCode) {
                case SUCCESS:
                    try {
                        JSONArray picturesAndNames = new GetImagesUrlsAndNames().execute(stockId).get();
                        JSONObject shoppingList = (JSONObject) res.get("shoppingList");

                        // empty shoppingList
                        if(!(shoppingList.get("items").toString()).contains("toPurchase")){
                            // gone table and purchase button
                            findViewById(R.id.head_row_shoppingList).setVisibility(View.GONE);
                            findViewById(R.id.purchaseButton).setVisibility(View.GONE);

                            createAndShowAlert("You have no items to purchase :) \nyour stock is full");
                        }
                        else{
                            // visible table and purchase button
                            findViewById(R.id.head_row_shoppingList).setVisibility(View.VISIBLE);
                            findViewById(R.id.purchaseButton).setVisibility(View.VISIBLE);

                            fillShoppingListTable(shoppingList, picturesAndNames);
                        }
                    } catch (Exception e) {
                        createAndShowToast("Error: something went wrong");
                    }
                    break;
                case BAD_REQUEST:
                    createAndShowToast("sorry, your stock or some product is invalid");
                    break;
                default:
                    createAndShowToast("Error: status code - unknown");
                    break;
            }
        }
    }

    private class Purchase extends AsyncTask<Object, Void, JSONObject> {
        private JSONObject response = new JSONObject();
        private JSONObject data;

        @Override
        protected JSONObject doInBackground(Object[] parameters) {
            try {
                // define postData
                data = (JSONObject) parameters[0];

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

                switch (statusCode) {
                    case SUCCESS:
                    case VOID_SUCCESS:
                        response.put("status", SUCCESS);
                        break;
                    case BAD_REQUEST:
                        response.put("status", BAD_REQUEST);
                        break;
                    default:
                        response.put("status", ERROR);
                        break;
                }

                return response;

            } catch (Exception e) {
                response.put("status", ERROR);
                return response;
            }
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            int statusCode = Integer.parseInt(res.get("status").toString());

            switch (statusCode) {
                case SUCCESS:
                    createAndShowToast("Product was purchased");
                    refreshTable();
                    break;
                case BAD_REQUEST:
                    createAndShowToast("sorry, your stock or some product is invalid");
                    break;
                case NOT_ACCEPTABLE:
                    createAndShowToast("sorry, something went wrong");
                    break;
                default:
                    createAndShowToast("Error: status code - unknown");
                    break;
            }
        }
    }

    public static class GetImagesUrlsAndNames extends AsyncTask<Object, String, JSONArray> {
        @Override
        protected JSONArray doInBackground(Object[] parameters) {

            try {
                // Url - to product and not to stock
                URL stockUrl = new URL("http://192.168.1.2:8080/rest/product/getImgsToShoppingList/" +  parameters[0].toString());
                // connection
                HttpURLConnection urlConnection = (HttpURLConnection) stockUrl.openConnection();
                // request type
                urlConnection.setRequestMethod("GET");
                // status
                int statusCode = urlConnection.getResponseCode();

                // success
                if (statusCode == 200 || statusCode ==  204 ) {
                    InputStream responseInputStream = urlConnection.getInputStream();
                    String response = IOUtils.toString(responseInputStream, "UTF_8");
                    return Utils.toJsonArray(response);
                }
                // failure
                else{
                    return null;
                }
            } catch (Exception e){
                System.out.println(e);
                return null;
            }
        }
    }
}

