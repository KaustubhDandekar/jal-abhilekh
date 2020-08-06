package com.neeri.wbis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Random;

public class SubmitGrievance extends AppCompatActivity {

    EditText waterbody_id, complainant_name, complainant_email, grievance_text;
    TextView waterbody_info;
    ProgressDialog progressDialog;

    private Handler handler;
    private ProgressBar circular_loading;
    boolean correct_wb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_grievance);

        waterbody_id = findViewById(R.id.waterbody_id);
        complainant_name = findViewById(R.id.complainant_name);
        complainant_email = findViewById(R.id.complainant_email);
        grievance_text = findViewById(R.id.grievance_text);

        waterbody_info = findViewById(R.id.waterbody_info);

        String[] communes = {"Ariyankuppam Commune", "Bahour Commune", "Mannadipet Commune",
                "Nettapakkam Commune", "Oulgaret Municipality", "Pondicherry Municipality",
                "Villianur Commune"};

        handler = new Handler();

        waterbody_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.removeCallbacksAndMessages(null);
                correct_wb = false;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                circular_loading.setVisibility(View.VISIBLE);

                String wid = editable.toString().toUpperCase().trim();
                handler.postDelayed(()->{
                    for(String commune : communes){
                        try{
                            FirebaseDatabase.getInstance().
                                    getReference(BuildConfig.APPROVED_RECORDS_DIR+commune+"/"+wid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    try{

                                        HashMap<String, Object> waterBodyData = (HashMap<String, Object>) dataSnapshot.getValue();
                                        if (waterBodyData == null) return;
                                        String[] description = ((String)waterBodyData.get("description"))
                                                .split("<br>");
                                        String details = "";
                                        details += "<b>Name :</b> "+ description[1] +"<br>";
                                        details += "<b>Commune : </b>"+ waterBodyData.get("commune") +"<br>";
                                        details += "<b>Description : </b>"+ description[4];
                                        waterbody_info.setText(Html.fromHtml(details));
                                        correct_wb = true;

                                    }catch (Exception e){
                                        Log.e("", e.getLocalizedMessage());
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    handler.removeCallbacksAndMessages(null);
                                    correct_wb = false;
                                }
                            });

                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(), "Not Found in "+commune, Toast.LENGTH_LONG).show();
                        }
                    }
                    handler.postDelayed(()->{
                        if (waterbody_info.getText().toString().equalsIgnoreCase(""))
                            waterbody_info.setText("Please enter correct Waterbody id");
                    }, 2500);
                    circular_loading.setVisibility(View.GONE);
                },2500);
                waterbody_info.setText("");
            }
        });

        circular_loading = findViewById(R.id.circular_loading);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Uploading");
        progressDialog.setMessage("Submitting Grievance");

        correct_wb = false;
    }

    public void submit(View view){
        String w_id = waterbody_id.getText().toString(),
                c_name = complainant_name.getText().toString(),
                c_email = complainant_email.getText().toString(),
                grievance = grievance_text.getText().toString();

        GrievanceObject grievanceObject = new GrievanceObject();

        if (!correct_wb){
            showNoFinishAlert("", "Please enter correct Waterbody Id.");
            return;
        }else grievanceObject.setWaterbody_id(w_id);
        if (c_name.isEmpty()){
            showNoFinishAlert("", "Please enter your Name.");
            return;
        }else grievanceObject.setComplainant_name(c_name);
        if (c_email.isEmpty()){
            showNoFinishAlert("", "Please enter your Email Address.");
            return;
        }else grievanceObject.setComplainant_email(c_email);
        if (grievance.isEmpty()){
            showNoFinishAlert("", "Please enter your Grievance.");
            return;
        }else grievanceObject.setGrievance(grievance);

        progressDialog.show();

        DatabaseReference database = FirebaseDatabase.getInstance().getReference(BuildConfig.GRIEVANCES_DIR);

        String id = database.push().getKey();
        if (id!=null){
            database.child(id).setValue(grievanceObject).addOnSuccessListener((Void aVoid)->{
                progressDialog.cancel();
                showInfoAlert("", "Grievance Submitted");
            }).addOnFailureListener((@NonNull Exception e)->{
                progressDialog.cancel();
                Log.e("Firebase database", e.getLocalizedMessage());
                showNoFinishAlert("", "Error occured while submitting your grievance. Please check internet connection and try again.");
            }).addOnCanceledListener(()->{
                progressDialog.cancel();
                showInfoAlert("", "Submission Cancelled by User");
            }).addOnCompleteListener((@NonNull Task<Void> task)->{
                progressDialog.cancel();
            });
        }else Log.d("Child id","Error while generating child id");
    }

    public void back(View view){
        finish();
    }

    public void showNoFinishAlert(String title, String info){
        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(info)
                .setCancelable(false)
                .setNeutralButton(android.R.string.ok, (DialogInterface dialog, int which)->{})
                .show();
    }

    public void showInfoAlert(String title, String info){
        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(info)
                .setCancelable(false)
                .setOnDismissListener(DialogInterface::cancel)
                .setNeutralButton(android.R.string.ok, (DialogInterface dialog, int which)->finish())
                .show();
    }
}
class GrievanceObject{
    private String waterbody_id, complainant_name, complainant_email, grievance;

    public String getWaterbody_id() {
        return waterbody_id;
    }

    public void setWaterbody_id(String waterbody_id) {
        this.waterbody_id = waterbody_id;
    }

    public String getComplainant_name() {
        return complainant_name;
    }

    public void setComplainant_name(String complainant_name) {
        this.complainant_name = complainant_name;
    }

    public String getComplainant_email() {
        return complainant_email;
    }

    public void setComplainant_email(String complainant_email) {
        this.complainant_email = complainant_email;
    }

    public String getGrievance() {
        return grievance;
    }

    public void setGrievance(String grievance) {
        this.grievance = grievance;
    }
}
