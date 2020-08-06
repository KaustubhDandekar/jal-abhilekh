package com.neeri.wbis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AdoptLake extends AppCompatActivity {

    TextView collector_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adopt_lake);

        collector_email = findViewById(R.id.collector_email);

        collector_email.setOnLongClickListener(view ->{
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("District Collectorate Email", "dcrev.pon@nic.in");
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Email Copied", Toast.LENGTH_LONG).show();
            return true;
        });
    }

    public void openEmail(View view){
        String mailto = "mailto:dcrev.pon@nic.in";
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse(mailto));
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException e) {
            //TODO: Handle case where no email app is available
        }
    }

    public void openDialer(View view){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:04132299502"));
        startActivity(intent);
    }

    public void back(View view){
        finish();
    }
}
