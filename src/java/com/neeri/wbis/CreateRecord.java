package com.neeri.wbis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.neeri.wbis.database.DatabaseHelper;
import com.neeri.wbis.tools.RecordObject;
import java.io.File;
import java.util.ArrayList;

import static com.neeri.wbis.database.DataBaseConstants.U_CONTACT;
import static com.neeri.wbis.database.DataBaseConstants.U_EMAIL;
import static com.neeri.wbis.database.DataBaseConstants.U_NAME;

public class CreateRecord extends AppCompatActivity {

    private static final String TAG = "CreateRecord";

    protected static final int REQUEST_CHECK_SETTINGS = 0x1, EDIT_IMAGE_LIST = 0x2;

    private ArrayList<String> imageNameList;
    private Location location;

    private EditText title, description, name, email, contact;
    private ProgressDialog progressDialog;

    private DatabaseReference mDatabaseFieldReports;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;

    private Button uploadButton, saveDraftButton, viewPicturesButton;
    private int imageUploadTaskCounter;
    boolean isViewingImages = false;

    private boolean isUploading;

    FusedLocationProviderClient mFusedLocationClient;
    LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_record);

        isUploading = false;

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Uploading");

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            imageNameList = bundle.getStringArrayList("image_name_list");
        }

        mDatabaseFieldReports = FirebaseDatabase.getInstance().getReference(BuildConfig.USER_RECORDS);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        initializeLocationObjects();
        mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> this.location = location);

        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        contact = findViewById(R.id.contact);
        saveDraftButton = findViewById(R.id.save_draft_button);
        uploadButton = findViewById(R.id.upload_button);
        viewPicturesButton = findViewById(R.id.viewPhotosBtn);

        setUserInfo();

        saveDraftButton.setOnClickListener(v -> {
            if (location == null) {
                Toast.makeText(this, "current device location not available, please try again", Toast.LENGTH_SHORT).show();
                return;
            }
            saveDraftLocally();
        });

        uploadButton.setOnClickListener(v -> {
            if (location == null) {
                Toast.makeText(this, "Device location not available, please try again", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth = FirebaseAuth.getInstance();
            mAuth.signInAnonymously().addOnCompleteListener(this,  task->{
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInAnonymously:success");
//                    FirebaseUser user = mAuth.getCurrentUser();
                    uploadFileToFirebase();
                } else {
                    Log.e(TAG, "signInAnonymously:failure", task.getException());
                    Toast.makeText(getApplicationContext(), "Please Check your Internet Connection and try again", Toast.LENGTH_SHORT).show();
                }
            });
        });

        viewPicturesButton.setOnClickListener(v -> gotoImageAlbumActivity());

    }

    public static void setLocation(Location loc){
//        location = loc;
//        Log.d("LOCATION : ", "LAT : "+location.getLatitude()+" LON : "+location.getLongitude());
    }

    public void saveDraftLocally() {
        ContentLoadingProgressBar progressBar = new ContentLoadingProgressBar(this);

        progressBar.show();
        RecordObject recordObject = getRecordObject();
        (new DatabaseHelper(this)).saveRecord(recordObject);
        progressBar.hide();

        showInfoAlert("","Draft Saved Successfully");
    }

    private RecordObject getRecordObject() {
        return new RecordObject(title.getText().toString(), description.getText().toString(), location.getLatitude(),
                location.getLongitude(), imageNameList, name.getText().toString(), email.getText().toString(), contact.getText().toString());
    }

    private void createRecordInFirebaseDatabase() {
        progressDialog.setMessage("Submitting report");
        progressDialog.show();

        String id = mDatabaseFieldReports.push().getKey();
        Log.d(TAG, "createFieldReportDocument: DocumentId: " + id);
        if (id != null) {
            RecordObject recordObject = getRecordObject();
            mDatabaseFieldReports.child(id).setValue(recordObject).addOnSuccessListener(Void -> {
                progressDialog.cancel();
                isUploading = false;
                Toast.makeText(CreateRecord.this, "Field Report submitted", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(exc -> {
                Toast.makeText(CreateRecord.this, "Error in submitting", Toast.LENGTH_SHORT).show();
                progressDialog.cancel();
            }).addOnCompleteListener(Void -> {
                progressDialog.cancel();
                showInfoAlert("", "Information Uploaded Successfully");
            });
        } else {
            Toast.makeText(this, "Error generating database id", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFileToFirebase() {

//        if(!validateLocation(location)) return;

        isUploading = true;
        progressDialog.setMessage("Uploading images");
        progressDialog.show();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        for (String imageFileName : imageNameList) {
            File imageFile = new File(storageDir + "/" + imageFileName);
            if (imageFile.exists()) {
                Uri imageUri = Uri.fromFile(imageFile);
                StorageReference imageRef = mStorageRef.child(BuildConfig.IMAGE_DIR + imageFileName);
                imageUploadTaskCounter++;
                imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {})
                .addOnFailureListener(exception -> {
                    Toast.makeText(CreateRecord.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    progressDialog.cancel();
                })
                .addOnCompleteListener(command -> {
                    imageUploadTaskCounter--;
                    if (imageUploadTaskCounter == 0) {
                        Toast.makeText(CreateRecord.this, "All upload tasks completed", Toast.LENGTH_SHORT).show();
                        progressDialog.cancel();
                        createRecordInFirebaseDatabase();
                    }
                });
            }
        }
    }

    private void setUserInfo(){
        Cursor cr = new DatabaseHelper(this).getUserInfo();
        cr.moveToNext();
        name.setText(cr.getString(cr.getColumnIndex(U_NAME)));
        email.setText(cr.getString(cr.getColumnIndex(U_EMAIL)));
        contact.setText(cr.getString(cr.getColumnIndex(U_CONTACT)));
    }

    private void gotoImageAlbumActivity() {
        if (imageNameList != null && imageNameList.size() > 0) {
            Intent intent = new Intent(this, ImageAlbumActivity.class);
            intent.putExtra("image_name_list", imageNameList);
            isViewingImages = true;
            startActivityForResult(intent, EDIT_IMAGE_LIST);
        } else {
            isViewingImages = false;
            Toast.makeText(this, "No pictures taken", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        if (isUploading) saveDraftLocally();
        super.onStop();
        if (!isViewingImages) finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isViewingImages = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isViewingImages = false;
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates: called");
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates: called");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void initializeLocationObjects(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //initialize location request
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //Check and correct device location settings
        checkAndRequestDeviceLocationSettingChanges();

        //initialize location callback
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            Log.d(TAG, "onLocationResult: LAT: " + location.getLatitude() + " LON: " + location.getLongitude());
//                            validateLocation(location);
                            CreateRecord.this.location = location;
                        }
                    }
                } else {
                    Log.e(TAG, "LocationCallback: location object is null");
                }
            }
        };
    }

    private void checkAndRequestDeviceLocationSettingChanges() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(locationSettingsResponse -> {
            Log.d(TAG, "findLocation: All device location settings are satisfied");
        });

        task.addOnFailureListener((exc) -> {
            Log.d(TAG, "findLocation: device location settings not are satisfied");
            if (exc instanceof ResolvableApiException) {
                Log.d(TAG, "findLocation: prompting user to change device location settings");
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) exc;
                    resolvable.startResolutionForResult(this,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    Log.e(TAG, "LocationSettingsResponse: " + sendEx.getMessage());
                    // Ignore the error.
                }
            }
        });
    }

    private boolean validateLocation(Location location){
        if(location.getLatitude() > 12.0632 || location.getLatitude() < 11.76548237 ||
                location.getLongitude() > 79.8794 || location.getLongitude() < 79.576){
            stopLocationUpdates();
            showInfoAlert("", "Your Location is out of our Study Area. Please get inside 20km (approx) of the pondicherry Boundary to continue");
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Toast.makeText(this, "Device location enabled successfully", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onActivityResult: User allowed to change device location settings");
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "Cannot proceed without device location, please try again", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onActivityResult: User denied device location setting changes");
                        showPermissionRejectedAlert(this, "Latitude & Longitude is required for geo-tagging Image");
                        break;
                    default:
                        Log.e(TAG, "onActivityResult: REQUEST_CHECK_SETTINGS: unknown resultCode: " + resultCode);
                        break;
                }
                break;
            case EDIT_IMAGE_LIST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                    case Activity.RESULT_CANCELED:
                        if (data != null && data.getBooleanExtra("image_list_modified", false)) {
                            ArrayList<String> newImageNameList = data.getStringArrayListExtra("image_name_list");
                            if (newImageNameList != null) {
                                Toast.makeText(this, "onActivityResult: newImageNameList size: " + newImageNameList.size(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                }
                break;
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
                    checkAndRequestDeviceLocationSettingChanges();
                })
                .show();
    }

    public void showInfoAlert(String title, String info){
          new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(info)
                .setCancelable(false)
                .setNeutralButton(android.R.string.ok, (DialogInterface dialog, int which)->finish())
                .show();
    }
}
