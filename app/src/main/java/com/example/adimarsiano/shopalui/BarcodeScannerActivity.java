package com.example.adimarsiano.shopalui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.zxing.intergration.android.IntentIntegrator;
import com.google.zxing.intergration.android.IntentResult;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

    //retrieve scan result
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        //we have a result
        if(scanningResult != null){
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            formatTxt.setText("FORMAT: "+ scanFormat);
            contentTxt.setText("CONTENT: "+ scanContent);
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(),"No scan received!",Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
