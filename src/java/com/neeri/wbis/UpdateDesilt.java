package com.neeri.wbis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static com.neeri.wbis.CreateRecord.REQUEST_CHECK_SETTINGS;

public class UpdateDesilt extends AppCompatActivity {

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

    public static final int PICK_IMAGE = 1;
    private boolean choosingbeforepic;

    ImageView before_imgview, after_imgview;
    EditText waterbody_id, desilting_date;
    TextView waterbody_info;

    private DatabaseReference firedatabase;
    private StorageReference fireStorage;

    private ProgressBar circular_loading;
    private ProgressDialog progressDialog;

    private Handler handler;
    private Random rng;

    private InfoObject waterbody_object;
    private int imageUploadTaskCounter = 0;
    private String[] imageList;
    private boolean correct_wb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_desilt);

        requestExternalStoragePermission();

        before_imgview = findViewById(R.id.before_view);
        after_imgview = findViewById(R.id.after_view);

        waterbody_id = findViewById(R.id.waterbody_id);
        desilting_date = findViewById(R.id.desiltdate);

        waterbody_info = findViewById(R.id.waterbody_info);

        String[] communes = {"Ariyankuppam Commune", "Bahour Commune", "Mannadipet Commune",
                "Nettapakkam Commune", "Oulgaret Municipality", "Pondicherry Municipality",
                "Villianur Commune"};

        handler = new Handler();
        rng = new Random();

        waterbody_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

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
                            firedatabase = FirebaseDatabase.getInstance().
                                    getReference(BuildConfig.APPROVED_RECORDS_DIR+commune+"/"+wid);

                            firedatabase.addListenerForSingleValueEvent(new ValueEventListener() {
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
        progressDialog.setMessage("Submitting Information");

        waterbody_object = new InfoObject();
        imageList = new String[2];

        correct_wb = false;
    }

    private void requestExternalStoragePermission(){
        if(!allPermissionsGranted())
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
    }

    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE_PERMISSIONS)
            if(!allPermissionsGranted()){
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE))
                    showRationale();
                else
                    showPermissionRejectedAlert(this,
                            "Storage Permission is required for Selecting Image");
            }
    }

    public void showPermissionRejectedAlert(Context context, String permission){
        new AlertDialog.Builder(context)
                .setTitle(permission)
                .setMessage("Do you want to cancel this upload?")
                .setPositiveButton(R.string.yes, (DialogInterface dialog, int which)->{
                    finish();
                })
                .setNegativeButton(R.string.no, (DialogInterface dialog, int which)->{
                    requestExternalStoragePermission();
                })
                .show();
    }

    public void submit(View view){
        String w_id = waterbody_id.getText().toString(),
                date = desilting_date.getText().toString();

        if (w_id.equalsIgnoreCase("") || !correct_wb){
            showInfoAlert("", "Please Enter Waterbody Id.", false);
            return;
        }else waterbody_object.setWaterbody_id(w_id);
        if (date.equalsIgnoreCase("")){
            showInfoAlert("", "Please Enter Date of Desilting Waterbody.", false);
            return;
        }else waterbody_object.setDesilt_date(date);
        if (waterbody_object.getBefore_pic() == null){
            showInfoAlert("", "Please Select Image Before Desilting Waterbody.", false);
            return;
        }
        if (waterbody_object.getAfter_pic() == null){
            showInfoAlert("", "Please Select Image After Desilting Waterbody.", false);
            return;
        }

        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(this, task->{
            if (task.isSuccessful()) {
                Log.d("Firebase Auth", "signInAnonymously:success");
                uploadFileToFirebase();
            } else {
                Log.e("Firebase Auth", "signInAnonymously:failure", task.getException());
                Toast.makeText(getApplicationContext(), "Please Check your Internet Connection and try again", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void uploadFileToFirebase() {

        progressDialog.setMessage("Uploading images");
        progressDialog.show();

        int beforePic = 0;
        for (String imageFileName : imageList) {
            File imageFile = new File(imageFileName);
            if (imageFile.exists()) {

                Uri imageUri = Uri.fromFile(imageFile);
                imageFileName = imageFileName.substring(imageFileName.lastIndexOf("/")+1);
                imageFileName = rng.nextInt()+"_"+imageFileName;
                if (beforePic++ == 0) waterbody_object.setBefore_pic(imageFileName);
                else waterbody_object.setAfter_pic(imageFileName);

                StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(BuildConfig.UPDATED_IMAGE_DIR + imageFileName);
                imageUploadTaskCounter++;
                imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {})
                .addOnFailureListener(exception -> {
                    Toast.makeText(getApplicationContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                    progressDialog.cancel();
                })
                .addOnCompleteListener(command -> {
                    imageUploadTaskCounter--;
                    if (imageUploadTaskCounter == 0) {
                        progressDialog.cancel();
                        createRecordInFirebaseDatabase();
                    }
                });

            }else{
                progressDialog.cancel();
                Toast.makeText(this, "Unable to locate image file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createRecordInFirebaseDatabase(){
        progressDialog.setMessage("Saving Information");
        progressDialog.show();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(BuildConfig.UPDATED_RECORDS);

        String id = database.push().getKey();
        if (id!=null){
            database.child(id).setValue(waterbody_object).addOnSuccessListener((Void aVoid)->{
                progressDialog.cancel();
                showInfoAlert("", "Information Submitted for Verification", true);
            }).addOnFailureListener((@NonNull Exception e)->{
                progressDialog.cancel();
                Log.e("Firebase database", e.getLocalizedMessage());
                showInfoAlert("", "Error occured while submitting. Please check internet connection and try again.", false);
            }).addOnCanceledListener(()->{
                progressDialog.cancel();
                showInfoAlert("", "Submission Cancelled by User", true);
            }).addOnCompleteListener((@NonNull Task<Void> task)->{
                progressDialog.cancel();
            });
        }else Log.d("Child id","Error while generating child id");
    }

    public void beforeDesiltPic(View view){
        choosingbeforepic = true;
        openImagePickIntent();
    }

    public void afterDesiltPic(View view){
        choosingbeforepic = false;
        openImagePickIntent();
    }

    private void showRationale(){
        new android.app.AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Storage Permission is required for selecting and Uploading images from" +
                        " the gallery.\n You can turn on this permission" +
                        " by going into app settings -> app permissions.\n\n" +
                        " Do you wish to goto App Settings?")
                .setCancelable(false)
                .setOnDismissListener(DialogInterface::cancel)
                .setPositiveButton(R.string.yes, (DialogInterface dialig, int which)->{
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.no, (DialogInterface dialog, int which)-> finish())
                .show();
    }

    public void showInfoAlert(String title, String info, boolean finish){
        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(info)
                .setCancelable(false)
                .setOnDismissListener(DialogInterface::cancel)
                .setNeutralButton(android.R.string.ok, (DialogInterface dialog, int which)-> {
                    if(finish) finish();
                }).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data.getData() != null) {

            Bitmap imageBitmap;
            try{
                InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                imageBitmap = BitmapFactory.decodeStream(inputStream);
                int width = (imageBitmap.getWidth()*200)/ imageBitmap.getHeight();
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, width, 200, false);

                inputStream.close();
            }catch (IOException e){
                Toast.makeText(this, "Error while fetching the file", Toast.LENGTH_LONG).show();
                return;
            }

            String fileName = data.getData().getPath().substring(5);
            if(choosingbeforepic){
                before_imgview.setImageBitmap(imageBitmap);
                before_imgview.setVisibility(View.VISIBLE);
                imageList[0] = fileName;
                waterbody_object.setBefore_pic(fileName);
            }
            else{
                after_imgview.setImageBitmap(imageBitmap);
                after_imgview.setVisibility(View.VISIBLE);
                imageList[1] = fileName;
                waterbody_object.setAfter_pic(fileName);
            }
        }

    }

    private void openImagePickIntent(){

        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");

        startActivityForResult(Intent.createChooser(pickIntent, "Select Image"), PICK_IMAGE);
    }

    public void back(View view){
        onBackPressed();
    }

    public void onBackPressed(){
        handler.removeCallbacksAndMessages(null);
        super.onBackPressed();
    }

    public void onStop(){
        handler.removeCallbacksAndMessages(null);
        super.onStop();
    }

    public void onPause(){
        handler.removeCallbacksAndMessages(null);
        super.onPause();
    }

}

class InfoObject{
    private String desilt_date, waterbody_id, before_pic, after_pic;

    public String getDesilt_date() {
        return desilt_date;
    }

    public void setDesilt_date(String desilt_date) {
        this.desilt_date = desilt_date;
    }

    public String getWaterbody_id() {
        return waterbody_id;
    }

    public void setWaterbody_id(String waterbody_id) {
        this.waterbody_id = waterbody_id;
    }

    public void setBefore_pic(String before_pic) {
        this.before_pic = before_pic;
    }

    public void setAfter_pic(String after_pic) {
        this.after_pic = after_pic;
    }

    public String getBefore_pic() {
        return before_pic;
    }

    public String getAfter_pic() {
        return after_pic;
    }
}
