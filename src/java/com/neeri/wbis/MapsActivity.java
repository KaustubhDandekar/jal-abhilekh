package com.neeri.wbis;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MapsActivity extends AppCompatActivity {

    private WebView map;
    private static final String TAG = "MAPS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        String latitude, longitude, id;

        if(!isNetworkConnected()){
            showInfoAlert("", "Please check your Internet Connection");
        }

        Intent intent = getIntent();
        String params = null;
        if (intent.hasExtra("lat")){
            latitude = intent.getStringExtra("lat");
            params = "?lat="+latitude;
        }
        if (intent.hasExtra("lon")){
            longitude = intent.getStringExtra("lon");
            params += "&lon="+longitude;
        }
        if (intent.hasExtra("id")){
            id = intent.getStringExtra("id");
            params += "&id="+id;
        }

        System.out.println(params);

        map = findViewById(R.id.map);

        map.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.startsWith("https://youtu.be/")){
                    Intent intent = new Intent(getApplicationContext(), VideoPlayerActivity.class);
                    intent.putExtra("id", url.substring(17).replaceAll("(\\r|\\n)", ""));
                    intent.putExtra("title", "");
                    intent.putExtra("link", "");
                    startActivity(intent);
                    view.reload();
                    return true;
                }

                else{
//                    view.loadUrl(url);
                }
                return true;
            }});
        WebSettings settings = map.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        if (params == null)
            map.loadUrl("https://jal-abhilekh.web.app");
        else
            map.loadUrl("https://jal-abhilekh.web.app/index.html"+params);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void showInfoAlert(String title, String info){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(info)
                .setCancelable(false)
                .setNeutralButton(android.R.string.ok, (DialogInterface dialog, int which)-> finish())
                .show();
    }

    public void closeMap(View view){
        finish();
    }
}
