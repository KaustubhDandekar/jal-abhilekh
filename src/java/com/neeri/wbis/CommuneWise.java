package com.neeri.wbis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class CommuneWise extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private String COMMUNE;
    private TableLayout ll;
    private ProgressBar loading;

    private int numberOfWaterBodies, alreadyfetched;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commune_wise);

        COMMUNE = getIntent().getStringExtra("commune");

        ll = findViewById(R.id.waterbody_table);
        loading = findViewById(R.id.circular_loading);

        TextView commune_heading = findViewById(R.id.commune_heading);
        commune_heading.setText(COMMUNE);

        numberOfWaterBodies = 0;
        getCommuneData();
    }

    private void getCommuneData(){

        mDatabase = FirebaseDatabase.getInstance().getReference(BuildConfig.APPROVED_RECORDS_DIR+COMMUNE);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                System.out.println(dataSnapshot);

                int count = (int) dataSnapshot.getChildrenCount();
                if(count == 0)
                    conveyEmpty();
                numberOfWaterBodies = count;
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                HashMap<String, Object> waterBodyData = (HashMap<String, Object>) dataSnapshot.getValue();
                if (waterBodyData == null) return;

                populateWaterBodies(waterBodyData);
                if (alreadyfetched == numberOfWaterBodies){
                    new Handler().postDelayed(()->{
                        loading.setVisibility(View.GONE);
                    }, 2000);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                System.out.println("CHANGED");

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                System.out.println("REMOVED");
                numberOfWaterBodies--;
                conveyEmpty();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                System.out.println("MOVED");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("ERROR");
            }
        });


    }

    private void clearEmpty(){
        onRestart();
    }

    private void conveyEmpty(){
        if (numberOfWaterBodies == 0){
            Button card = (Button) Button.inflate(this, R.layout.records_row, null);
            card.setText("No Data Available around this Commune");
            ll.addView(card);
        }
    }

    private void populateWaterBodies(HashMap<String, Object> data) {

        LinearLayout card = (LinearLayout) LinearLayout.inflate(this, R.layout.waterbody_records, null);

//        System.out.println(data.keySet());
        String s = ((String)data.get("description")).split("<br>")[1];
        ((TextView)card.findViewWithTag("name")).setText(s);
        ((TextView)card.findViewWithTag("id")).setText((String)data.get("id"));

        if(data.get("video") != null){
            card.findViewById(R.id.video_icon).setVisibility(View.VISIBLE);
        }

        card.setOnClickListener((View v)->showWaterBodyOnMap(data));

        ll.addView(card);
//        alreadyfetched++;
    }

    private void showWaterBodyOnMap(HashMap data){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("lat", (String)data.get("latitude"));
        intent.putExtra("lon", (String)data.get("longitude"));
        intent.putExtra("id", (String)data.get("id"));
        startActivity(intent);
    }

    public void back(View view){
        finish();
    }

}
