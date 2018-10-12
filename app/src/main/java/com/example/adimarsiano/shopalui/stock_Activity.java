package com.example.adimarsiano.shopalui;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;

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
    private String stockId = "";
    private final int BAD_REQUEST = 400;
    private final int SUCCESS = 200;
    private final int VOID_SUCCESS = 204;
    private final int NOT_ACCEPTABLE = 406;
    private final int ERROR = -1;

    //private String test = "{ \"_id\" : { \"$oid\" : \"5ba68a6df21c55ef12534b8a\"} , \"name\" : \"work\" , \"list\" : [ { \"productId\" : 1 , \"available\" : 2 , \"limit\" : 4} , { \"productId\" : 2 , \"available\" : 1 , \"limit\" : 1}]}";

    private TextView stockText;
    private TableLayout table;
    private TableRow head_row;
    private Button submitButton;
    private AppCompatActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle b = getIntent().getExtras();
        if(b != null)
            stockId = b.getString("stockId");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_);

        submitButton = findViewById(R.id.stock_submitButton);
        context = this;
        table = findViewById(R.id.stock_table);

        new ImportStock().execute();

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
            data.put("productId", getProductIdByDeleteButton(v));

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

                product.put("productId", getProductIdByTag(row));

                EditText available = (EditText) ((TableRow) row).getChildAt(1);
                product.put("available", Integer.parseInt(available.getText().toString()));

                EditText limit = (EditText) ((TableRow) row).getChildAt(2);
                product.put("limit", Integer.parseInt(limit.getText().toString()));

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
        private JSONObject response = new JSONObject();

        @Override
        protected JSONObject doInBackground(Object[] parameters) {
            try {
                // Url
                URL stockUrl = new URL("http://192.168.1.2:8080/rest/stock/get/" + stockId);
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
                        String stockStr = IOUtils.toString(responseInputStream, "UTF_8");

                        response.put("status", SUCCESS);
                        response.put("stock", Utils.toJson(stockStr));
                        break;
                    case BAD_REQUEST:
                        response.put("status", BAD_REQUEST);
                        break;
                    default:
                        response.put("status", ERROR);
                        break;
                }

                return response;
            } catch (Exception e){
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
                        JSONObject stock = (JSONObject) res.get("stock");

                        fillStockTable(stock, picturesAndNames);
                        // empty stock
                        if(!(stock.get("items").toString()).contains("productId")){
                            createAndShowAlert("Your stock is empty, please add products via the Scanner");
                        }
                    } catch (Exception e) {
                        createAndShowToast("Error: status code - unknown");
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

    private class SubmitTable extends AsyncTask<Object, Void, JSONObject> {
        private JSONObject response = new JSONObject();
        private  JSONObject data;

        @Override
        protected JSONObject doInBackground(Object[] parameters) {
            try {
                // define postData
                data = (JSONObject) parameters[0];

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

                int statusCode = urlConnection.getResponseCode();

                switch (statusCode) {
                    case SUCCESS:
                    case VOID_SUCCESS:
                        response.put("status", SUCCESS);
                        break;
                    case BAD_REQUEST:
                        response.put("status", BAD_REQUEST);
                        break;
                    case NOT_ACCEPTABLE:
                        response.put("status", NOT_ACCEPTABLE);
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
                    createAndShowToast("Submitted");
                    refreshTable();
                    break;
                case BAD_REQUEST:
                    createAndShowToast("sorry, your stock or sime product is invalid");
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

    private class RemoveProduct extends AsyncTask<Object, String, JSONObject> {
        private JSONObject response = new JSONObject();
        private Long productId = null;

        @Override
        protected JSONObject doInBackground(Object[] parameters) {
            try {
                JSONObject data = (JSONObject)parameters[0];
                productId = Long.parseLong(data.get("productId").toString());

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

                int statusCode = urlConnection.getResponseCode();

                switch (statusCode) {
                    case SUCCESS:
                    case VOID_SUCCESS:
                        response.put("status", SUCCESS);
                        break;
                    case BAD_REQUEST:
                        response.put("status", BAD_REQUEST);
                        break;
                    case NOT_ACCEPTABLE:
                        response.put("status", NOT_ACCEPTABLE);
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
                    createAndShowToast("product was removed");
                    removeRowByProductId(productId);
                    break;
                case BAD_REQUEST:
                    createAndShowToast("sorry, your stock or sime product is invalid");
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
                URL stockUrl = new URL("http://192.168.1.2:8080/rest/product/getImgs/" +  parameters[0].toString());
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

    private void removeRowByProductId(Long productId){
        for(int i = 1, rows = table.getChildCount(); i < rows; i++) {
            View row = table.getChildAt(i);
            if (row instanceof TableRow) {
                // find the row that contains the productId and remove it
                if (getProductIdByTag(row) == productId){
                    table.removeView(row);
                    return;
                }
            }
        }
    }

    private void fillStockTable(JSONObject stock, JSONArray picturesAndNames) {
        // getting iterators of products and images
        JSONArray products = null;
        JSONArray productsData = null;
        JSONParser parser = new JSONParser();

        try {
            products = (JSONArray) parser.parse(stock.get("items").toString());
            productsData = (JSONArray) parser.parse(picturesAndNames.toString());
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
            TableRow new_row =(TableRow)getLayoutInflater().inflate(R.layout.tablerow_stock_template, null);
            new_row.setTag(product.get("productId").toString());


            //img:
            ImageView imgView = (ImageView)new_row.getChildAt(0);
            String img = data.get("productImg").toString();
            // there is image for this product
            if(img.contains("https")){
                new LoadImage().execute(img, imgView);
            }

            //name:
            TextView nameView = (TextView)new_row.getChildAt(1);
            nameView.setText(data.get("productName").toString());

            // available:
            EditText available = (EditText)new_row.getChildAt(2);
            available.setText(product.get("available").toString());

            // limit:
            EditText limit = (EditText)new_row.getChildAt(3);
            limit.setText(product.get("limit").toString());

            // delete button
            ImageButton delete = (ImageButton)new_row.getChildAt(4);
            delete.setTag("deleteButton_" + product.get("productId").toString());
            delete.setOnClickListener((View.OnClickListener)context);

            table.addView(new_row);
        }
    }

    private Long getProductIdByDeleteButton(View v){
        String tag = v.getTag().toString();
        return Long.parseLong(tag.substring(tag.indexOf("_") + 1));
    }

    public static Long getProductIdByTag(View view){
        return Long.parseLong(view.getTag().toString());
    }

    private void createAndShowToast(String text){
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        toast.show();
    }

    private void createAndShowAlert(String text){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("Your stock is empty, please add products via the Scanner");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void refreshTable(){
        TableRow headRow = findViewById(R.id.head_row);
        table.removeAllViews();
        table.addView(headRow);
        new ImportStock().execute(context, stockId);
    }

}
