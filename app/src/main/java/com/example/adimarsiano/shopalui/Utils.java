package com.example.adimarsiano.shopalui;

import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Utils {

    public static TextView createTextView(AppCompatActivity context, String text) {
        TextView textView = new TextView(context);
        textView.setText(text);

        return textView;
    }

    public static JSONObject toJson(String jsonString) {
        JSONParser parser = new JSONParser();
        JSONObject json = null;

        try {
            json = (JSONObject) parser.parse(jsonString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return json;
    }

}
