package com.neeri.wbis;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class WaterBodiesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_bodies);
    }

    public void showARMCommune(View view){
        gotoCommuneWise("Ariyankuppam Commune");
    }

    public void showBARCommune(View view){
        gotoCommuneWise("Bahour Commune");
    }

    public void showVIRCommune(View view){
        gotoCommuneWise("Villianur Commune");
    }

    public void showMATCommune(View view){
        gotoCommuneWise("Mannadipet Commune");
    }

    public void showNEMCommune(View view) {
        gotoCommuneWise("Nettapakkam Commune");
    }

    public void showOUTCommune(View view) {
        gotoCommuneWise("Oulgaret Municipality");
    }

    public void showPOYCommune(View view) {
        gotoCommuneWise("Pondicherry Municipality");
    }

    public void gotoCommuneWise(String commune){
        Intent intent = new Intent(this, CommuneWise.class);
        intent.putExtra("commune", commune);
        startActivity(intent);
    }

    public void back(View view){
        finish();
    }

}
