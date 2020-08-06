package com.neeri.wbis;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.neeri.wbis.database.DatabaseHelper;
import com.neeri.wbis.tools.RecordObject;
import com.neeri.wbis.tools.UploadRecordHelper;

import java.io.File;
import java.util.ArrayList;

import static com.neeri.wbis.database.DataBaseConstants.DESCRIPTION;
import static com.neeri.wbis.database.DataBaseConstants.LATITUDE;
import static com.neeri.wbis.database.DataBaseConstants.LONGITUDE;
import static com.neeri.wbis.database.DataBaseConstants.TITLE;
import static com.neeri.wbis.database.DataBaseConstants.U_CONTACT;
import static com.neeri.wbis.database.DataBaseConstants.U_EMAIL;
import static com.neeri.wbis.database.DataBaseConstants.U_NAME;

public class DraftActivity extends AppCompatActivity {

    private static final String TAG = "DRAFT";

    TextView title, description, name, email, contact;

    private ArrayList<String> imageNameList;
    private Button uploadButton, saveDraftButton, showImagesButton;
    private ProgressDialog progressDialog;

    private DatabaseReference mDatabaseFieldReports;
    private StorageReference mStorageRef;

    private int imageUploadTaskCounter;
    private String record_id;

    RecordObject recordObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft);

        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        contact = findViewById(R.id.contact);
        saveDraftButton = findViewById(R.id.save_draft_button);
        uploadButton = findViewById(R.id.upload_button);
        showImagesButton = findViewById(R.id.viewPhotosBtn);
        showImagesButton.setOnClickListener(v -> gotoImageAlbumActivity());
        
        record_id = getIntent().getStringExtra("record_id");

        showDetails();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Uploading");

        mDatabaseFieldReports = FirebaseDatabase.getInstance().getReference(BuildConfig.USER_RECORDS);
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    private void showDetails(){
        Cursor[] c_array = (new DatabaseHelper(this)).getRecordDetails(record_id);
        Cursor cr = c_array[0];
        Cursor imcr = c_array[1];
        imageNameList = new ArrayList<>();
        while (imcr.moveToNext()){
            imageNameList.add(imcr.getString(0));
        }
        cr.moveToNext();

        recordObject = new RecordObject(cr.getString(cr.getColumnIndex(TITLE)),
                        cr.getString(cr.getColumnIndex(DESCRIPTION)),
                        Double.parseDouble(cr.getString(cr.getColumnIndex(LATITUDE))),
                        Double.parseDouble(cr.getString(cr.getColumnIndex(LONGITUDE))),
                imageNameList,
                        cr.getString(cr.getColumnIndex(U_NAME)),
                        cr.getString(cr.getColumnIndex(U_EMAIL)),
                        cr.getString(cr.getColumnIndex(U_CONTACT))
        );

        title.setText(recordObject.getTitle());
        description.setText(recordObject.getDescription());
        email.setText(recordObject.getUEmail());
        contact.setText(recordObject.getUContact());
        name.setText(recordObject.getUName());
    }

    private void gotoImageAlbumActivity() {
        if (imageNameList != null && imageNameList.size() > 0) {
            Intent intent = new Intent(this, ImageAlbumActivity.class);
            intent.putExtra("image_name_list", imageNameList);
            startActivity(intent);
        } else {
            Toast.makeText(this, "No pictures", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteRecord(View view){
        new AlertDialog.Builder(this)
                .setTitle("Are you sure?")
                .setMessage("This action is not reversible.")
                .setPositiveButton(android.R.string.yes, (DialogInterface dialog, int which)->{
                    if((new DatabaseHelper(this)).deleteRecord(record_id)){
                        Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_LONG).show();
                        showInfoAlert("", "Information Deleted");
                    }
                    else
                        Toast.makeText(this, "Deleted FAILED", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void createRecordInFirebaseDatabase(RecordObject recordObject) {
        progressDialog.setMessage("Submitting report");
        progressDialog.show();

        String id = mDatabaseFieldReports.push().getKey();
        Log.d(TAG, "createFieldReportDocument: DocumentId: " + id);
        if (id != null) {

            mDatabaseFieldReports.child(id).setValue(recordObject).addOnSuccessListener(Void -> {
                progressDialog.cancel();
                Toast.makeText(this, "Field Report submitted", Toast.LENGTH_SHORT).show();

            }).addOnFailureListener(exc -> {
                progressDialog.cancel();
                Toast.makeText(this, "Error in submitting", Toast.LENGTH_SHORT).show();
            }).addOnCompleteListener(Void -> {
                (new DatabaseHelper(this)).deleteRecord(record_id);
                progressDialog.cancel();
                showInfoAlert("" ,"Information Uploaded Successfully");
            });
        } else {
            Toast.makeText(this, "Error generating database id", Toast.LENGTH_SHORT).show();
        }
    }

    public void uploadToFirebase(View view) {
        progressDialog.setMessage("Uploading images");
        progressDialog.show();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        for (String imageFileName : recordObject.getImages()) {
            File imageFile = new File(storageDir + "/" + imageFileName);
            if (imageFile.exists()) {
                Uri imageUri = Uri.fromFile(imageFile);
                StorageReference imageRef = mStorageRef.child(BuildConfig.IMAGE_DIR+ imageFileName);
                imageUploadTaskCounter++;
                imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                        {}
                ).addOnFailureListener(exception ->{
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();}
                ).addOnCompleteListener(command -> {
                    imageUploadTaskCounter--;
                    if (imageUploadTaskCounter == 0) {
                        Toast.makeText(this, "All upload tasks completed", Toast.LENGTH_SHORT).show();
                        progressDialog.cancel();
                        createRecordInFirebaseDatabase(recordObject);
                    }
                });
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void showInfoAlert(String title, String info){
        progressDialog = null;
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(info)
                .setCancelable(false)
                .setNeutralButton(android.R.string.ok, (DialogInterface dialog, int which)->{
                    startActivity(new Intent(this, RecordsListActivity.class));
                })
                .show();
    }

    public void back(View view){
        finish();
    }
}

