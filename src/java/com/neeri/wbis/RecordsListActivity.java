package com.neeri.wbis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;

import com.neeri.wbis.database.DatabaseHelper;

import static com.neeri.wbis.database.DataBaseConstants.*;

public class RecordsListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records_list);

        showLocalRecords();
    }

    private void showLocalRecords() {
        Cursor cr = (new DatabaseHelper(this)).getDraftInfo();

        TableLayout ll = findViewById(R.id.tableLayoutList);

        Button card;
        int i = 0;
        while(cr.moveToNext()){
            i++;
            card = (Button) Button.inflate(this, R.layout.records_row, null);

            String s = "Title : "+cr.getString(cr.getColumnIndex(TITLE));
            String record_id = cr.getString(cr.getColumnIndex(R_ID));
            card.setText(s);

            card.setOnClickListener((View v)->{
                    Intent intent = new Intent(RecordsListActivity.this, DraftActivity.class);
                    intent.putExtra("record_id", record_id);
                    startActivity(intent);
            });

            ll.addView(card);

        }
        if(i == 0){
            card = (Button) Button.inflate(this, R.layout.records_row, null);
            card.setText(getString(R.string.no_records));
            ll.addView(card);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        recreate();
    }

    public void back(View view){
        finish();
    }
}

