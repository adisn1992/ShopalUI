package com.example.adimarsiano.shopalui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;

import com.google.zxing.intergration.android.IntentIntegrator;
import com.google.zxing.intergration.android.IntentResult;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class BarcodeScannerActivity extends AppCompatActivity implements OnClickListener {
    // adi: get shoppingListId!
    private String stockId = "";
    private static final int BAD_REQUEST = 400;
    private static final int SUCCESS = 200;
    private static final int PRODUCT_NOT_EXIST = 300;
    private static final int PRODUCT_EXIST = 100;
    private final int VOID_SUCCESS = 204;
    private static final int NOT_ACCEPTABLE = 406;
    private static final int ERROR = -1;

    private Button scanBtn, addBtn, deleteBtn;
    private TextView formatTxt, contentTxt;

    private boolean isCurrProductExist = false;
    private boolean userSelectionAdd = false;
    private String currentBarcode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle b = getIntent().getExtras();
        if(b != null)
            stockId = b.getString("stockId");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        scanBtn = (Button) findViewById(R.id.scan_button);
        addBtn = (Button) findViewById(R.id.add_product_barcode_button);
        deleteBtn = (Button) findViewById(R.id.delete_product_barcode_button);

        scanBtn.setOnClickListener(this);
        addBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
    }

    //respond to clicks
    public void onClick(View v) {
        //scan
        if (v.getId() == R.id.scan_button) {
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
        //add new product
        if (v.getId() == R.id.add_product_barcode_button) {
            userSelectionAdd = true;
            scanBtn.setEnabled(true);
            addBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
        }
        //delete product
        if (v.getId() == R.id.delete_product_barcode_button) {
            userSelectionAdd = false;
            scanBtn.setEnabled(true);
            addBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
        }
    }

    //***********************************************************************************
    //**                                 GET PRODUCT FROM API                          **
    //***********************************************************************************
    private class GetProductTask extends AsyncTask<String, String, String> {
        private StringBuilder builder = new StringBuilder();
        private TextView contentTxt;

        @Override
        protected String doInBackground(String[] parameters) {
            try {
                System.out.println("running in new thread");
                URL shopalUrl = new URL("http://192.168.1.2:8080/rest/shopal/product/" + parameters[0].toString());

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

                while (jsonReader.hasNext()) { // Loop through all keys
                    String key = jsonReader.nextName(); // Fetch the next key
                    String value = jsonReader.nextString();// Fetch the value as a String

                    if (key.equals("product_name") || key.equals("product_description") || key.equals("product_barcode")) {
                        builder.append(key);
                        builder.append(" : ");
                        builder.append(value);
                        builder.append('\n');

                        //TODO: remove barcode from display
                        // save current barcode
                        if (key.equals("product_barcode"))
                            currentBarcode = value;
                    }
                }
                System.out.println(builder.toString());
            } catch (Exception e) {
                System.out.println("Exception with the scanner:");
                System.out.println(e);
            }

            return builder.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.isEmpty())
                createAndShowToast("Sorry, something went wrong...\nPlease try again.");
            // display scanner info alert
            scannerResultAlert(builder.toString());

            new ValidateProduct().execute(currentBarcode);
        }
    }

    private void scannerResultAlert(String scannerResult) {
        // wait to display scanner info
        if (scannerResult.isEmpty())
            scannerResult = "Barcode not found";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(scannerResult)
                .setTitle("Scanned Product");

        // Add the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //retrieve scan result
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        //we have a result
        if (scanningResult != null) {
            final String scanContent = scanningResult.getContents();
            GetProductTask getProductTask = new GetProductTask();
            getProductTask.execute(scanContent);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "No scan received!", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    //***********************************************************************************
    //**                                 THROW PRODUCT                                 **
    //***********************************************************************************
    private class Trash extends AsyncTask<Object, Void, JSONObject> {
        private JSONObject response = new JSONObject();
        private JSONObject data;

        @Override
        protected JSONObject doInBackground(Object[] parameters) {
            try {
                int statusCode;

                if (!isCurrProductExist) {
                    statusCode = PRODUCT_NOT_EXIST;
                } else {
                    // define postData
                    data = (JSONObject) parameters[0];
                    // Url
                    URL stockUrl = new URL("http://192.168.1.2:8080/rest/stock/productToTrash");
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
                    statusCode = urlConnection.getResponseCode();
                }

                switch (statusCode) {
                    case SUCCESS:
                    case VOID_SUCCESS:
                        response.put("status", SUCCESS);
                        break;
                    case BAD_REQUEST:
                        response.put("status", BAD_REQUEST);
                        break;
                    case PRODUCT_NOT_EXIST:
                        response.put("status", PRODUCT_NOT_EXIST);
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
                    // createAndShowToast : create toast (little alert to the user that showed up and disappeared after a few seconds)
                    createAndShowToast("Product was thrown into the trash");
                    break;
                case BAD_REQUEST:
                    createAndShowToast("Sorry, your stock or product are invalid");
                    break;
                case NOT_ACCEPTABLE:
                    createAndShowToast("Sorry, something went wrong");
                    break;
                case PRODUCT_NOT_EXIST:
                    createAndShowToast("Sorry, product does not exist in your stock.\nPlease add it first in order to throw it.");
                    break;
                default:
                    createAndShowToast("Sorry, something went wrong...\nPlease try again.");
                    break;
            }
            //scan done: update UI buttons
            setButtonsToScannerStartStatus();
        }
    }

    //***********************************************************************************
    //**                                  VALIDATE PRODUCT                             **
    //***********************************************************************************
    private class ValidateProduct extends AsyncTask<Object, String, JSONObject> {
        private JSONObject response = new JSONObject();

        @Override
        protected JSONObject doInBackground(Object[] parameters) {
            try {
                Long productId = Long.parseLong(parameters[0].toString());
                // Url
                URL stockUrl = new URL("http://192.168.1.2:8080/rest/stock/isProductExistInStock/" + stockId + "/" + productId);
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
                        String res = IOUtils.toString(responseInputStream, "UTF_8");
                        isCurrProductExist = Boolean.parseBoolean(res);
                        response.put("status", SUCCESS);
                        response.put("result", res);
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
                        String productExists = res.get("result").toString();
                        // user selected add: add the scanned product to server
                        if (userSelectionAdd) {
                            JSONObject data = new JSONObject();
                            data.put("stockId", stockId);
                            // get productId from currentBarcode
                            data.put("productId", currentBarcode);
                            data.put("limit", "1");
                            data.put("available", "1");

                            new AddProduct().execute(data);
                        }
                        //user selected delete: delete the scanned product from server
                        else {
                            JSONObject data = new JSONObject();
                            data.put("stockId", stockId);
                            // get productId from currentBarcode
                            data.put("productId", currentBarcode);

                            new Trash().execute(data);
                        }
                    } catch (Exception e) {
                        createAndShowToast("Sorry, something went wrong...\nPlease try again.");
                    }
                    break;
                case BAD_REQUEST:
                    createAndShowToast("Sorry, your stock or product are invalid");
                    break;
                default:
                    createAndShowToast("Sorry, something went wrong...\nPlease try again.");
                    break;
            }

            //scan done: update UI buttons
            setButtonsToScannerStartStatus();
        }
    }

    //***********************************************************************************
    //**                                  ADD PRODUCT                                  **
    //***********************************************************************************
    private class AddProduct extends AsyncTask<Object, Void, JSONObject> {
        private JSONObject response = new JSONObject();
        private JSONObject data;

        @Override
        protected JSONObject doInBackground(Object[] parameters) {
            try {
                int statusCode;
                if (isCurrProductExist) {
                    statusCode = PRODUCT_EXIST;
                } else {
                    // define postData
                    data = (JSONObject) parameters[0];

                    // Url
                    URL stockUrl = new URL("http://192.168.1.2:8080/rest/stock/addProduct/");
                    // connection
                    HttpURLConnection urlConnection = (HttpURLConnection) stockUrl.openConnection();
                    // request property
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    // request type
                    urlConnection.setRequestMethod("POST");
                    // send data
                    if (data != null) {
                        OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                        writer.write(data.toString());
                        writer.flush();
                    }

                    // status
                    statusCode = urlConnection.getResponseCode();
                }

                switch (statusCode) {
                    case SUCCESS:
                    case VOID_SUCCESS:
                        response.put("status", SUCCESS);
                        break;
                    case BAD_REQUEST:
                        response.put("status", BAD_REQUEST);
                        break;
                    case PRODUCT_EXIST:
                        response.put("status", PRODUCT_EXIST);
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
                    createAndShowToast("Product has been added");
                    break;
                case BAD_REQUEST:
                    createAndShowToast("Sorry, your stock or product are invalid");
                    break;
                case NOT_ACCEPTABLE:
                    createAndShowToast("Sorry, something went wrong");
                    break;
                case PRODUCT_EXIST:
                    createAndShowToast("Sorry, product already exist in your stock.\nPlease update it's values in your Shopping list/Stock.");
                    break;
                default:
                    createAndShowToast("Sorry, something went wrong...\nPlease try again.");
                    break;
            }

            //scan done: update UI buttons
            setButtonsToScannerStartStatus();
        }
    }

    private void setButtonsToScannerStartStatus(){
        scanBtn.setEnabled(false);
        deleteBtn.setEnabled(true);
        addBtn.setEnabled(true);
        isCurrProductExist = false;
    }

    private void createAndShowToast(String text) {
        final Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        toast.show();

        //show the msg for 3 seconds
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 5000);
    }
}
