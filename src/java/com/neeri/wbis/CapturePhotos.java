package com.neeri.wbis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraX;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.SensorOrientedMeteringPointFactory;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executor;

public class CapturePhotos extends AppCompatActivity {

    private final static String TAG = "CapturePhotos";

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION};
    View flash_screen;
    TextureView textureView;
    ImageButton imagePreviewBtn;
    ImageCapture imgCap;

    ArrayList<String> imageList;
    private MediaActionSound shutterSound;
    private ContentLoadingProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_photos);

        textureView = findViewById(R.id.view_finder);
        imagePreviewBtn = findViewById(R.id.image_preview_btn);
        imageList = new ArrayList<>();
        shutterSound = new MediaActionSound();
        flash_screen = findViewById(R.id.flash_screen);

        loadingProgressBar = new ContentLoadingProgressBar(this);

        requestCameraAndGpsPermissions();

    }

    private void startCamera() {

        CameraX.unbindAll();

//        Rational aspectRatio = (new Rational(textureView.getWidth(), textureView.getHeight()));
        Size screen = new Size(textureView.getWidth(), textureView.getHeight()); //size of the screen


        PreviewConfig pConfig = new PreviewConfig.Builder()
                .setLensFacing(CameraX.LensFacing.BACK)
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
//                .setTargetResolution(screen)
//                .setDefaultResolution()
                .build();

        Preview preview = new Preview(pConfig);

        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            //to update the surface texture we  have to destroy it first then re-add it
            @Override
            public void onUpdated(@NonNull Preview.PreviewOutput output){
                ViewGroup parent = (ViewGroup) textureView.getParent();
                parent.removeView(textureView);
                parent.addView(textureView, 0);

                textureView.setSurfaceTexture(output.getSurfaceTexture());
                updateTransform();
            }
        });


        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()
                .setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();

        imgCap = new ImageCapture(imageCaptureConfig);

        findViewById(R.id.view_finder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        CameraX.bindToLifecycle(this, preview, imgCap);
    }

    private File generateImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = UUID.randomUUID() + "_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = null;
        try {
            imageFile = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
        } catch (IOException ioe) {
            ioe.printStackTrace();
            Toast.makeText(this, "Error while accessing Storage!", Toast.LENGTH_LONG).show();
        }
        return imageFile;
    }

    private Drawable generateImageThumbnail(String fileName){
        Drawable image = null;

        try
        {
            final int THUMBNAIL_SIZE = 64;

            FileInputStream fis = new FileInputStream(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsoluteFile()+"/"+fileName);


            Bitmap imageBitmap = BitmapFactory.decodeStream(fis);

            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, THUMBNAIL_SIZE, THUMBNAIL_SIZE, false);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] thumb = baos.toByteArray();

            image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(thumb, 0, thumb.length));

        }
        catch(Exception ex) {
            Toast.makeText(this, "Could not update Image Preview", Toast.LENGTH_SHORT).show();
            Log.d("Picture Dir",getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        }
        return image;
    }

    private void conveyImageCaptured(){
        shutterSound.play(MediaActionSound.SHUTTER_CLICK);
        Animation out = new AlphaAnimation(1.0f, 0.0f);
        out.setDuration(500);
        runOnUiThread(()-> {
            flash_screen.setVisibility(View.VISIBLE);
            flash_screen.startAnimation(out);
            (new Handler()).postDelayed(() -> flash_screen.setVisibility(View.GONE), 500);
        });
    }

    public void captureImage(View view){

        loadingProgressBar.show();
        File imageFile = generateImageFile();

        if (imageFile == null) {
            Toast.makeText(this, "Error in accessing storage", Toast.LENGTH_SHORT).show();
            return;
        }

        imgCap.takePicture(imageFile, new Executor() {
            @Override
            public void execute(Runnable command) {
                conveyImageCaptured();
                imageList.add(imageFile.getName());
            }
        }, new ImageCapture.OnImageSavedListener() {
            @Override
            public void onImageSaved(@NonNull File file) {
                /*Drawable thumbnail = generateImageThumbnail(file.getAbsolutePath());
                if (thumbnail != null)
                    imagePreviewBtn.setBackground(thumbnail);
//                loadingProgressBar.hide();

                String msg = "Pic captured at " + file.getAbsolutePath();
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                imageList.add(file.getName());*/

                Toast.makeText(CapturePhotos.this, "onImageSaved: " + file.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull ImageCapture.ImageCaptureError imageCaptureError, @NonNull String message, @Nullable Throwable cause) {
//                loadingProgressBar.hide();
                Toast.makeText(getBaseContext(), "onError: Error while saving image", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateTransform(){
        Matrix mx = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int)textureView.getRotation();

        switch(rotation){
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float)rotationDgr, cX, cY);
        textureView.setTransform(mx);
    }

    private void requestCameraAndGpsPermissions(){
        if(allPermissionsGranted()){
            startCamera();
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    public void showPermissionRejectedAlert(Context context, String permission){
        new AlertDialog.Builder(context)
                .setTitle(permission)
                .setMessage("Do you want to cancel this upload?")
                .setPositiveButton(R.string.yes, (DialogInterface dialog, int which)->{
                    finish();
                })
                .setNegativeButton(R.string.no, (DialogInterface dialog, int which)->{
                    requestCameraAndGpsPermissions();
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE_PERMISSIONS)
            if(allPermissionsGranted()){
                startCamera();
            } else{
                if (!(ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) && ActivityCompat.
                        shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)))
                    showRationale();
                else
                    showPermissionRejectedAlert(this,
                            "Permission for accessing GPS and Camera is required");
            }
    }

    private void showRationale(){
        new android.app.AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("1. GPS Location is Required for geo-tagging (pinning) the images of waterbodies on Map.\n" +
                        "2. The Take Picture and Video permission is required for accessing Camera of device.\n" +
                        "Without these permissions this feature of App will not work. You can turn on these permissions" +
                        " by going into app settings -> app permissions.\n\n" +
                        "Do you wish to goto App Settings?")
                .setCancelable(false)
                .setOnDismissListener(DialogInterface::cancel)
                .setPositiveButton(R.string.yes, (DialogInterface dialig, int which)->{
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.no, (DialogInterface dialog, int which)-> finish())
                .show();
    }

    public void gotoCreateRecordActivity(View view){

        Log.d(TAG, "Number of image files: " + imageList.size());
        if (imageList.isEmpty()){
            Toast.makeText(this, "Capture at least one photo", Toast.LENGTH_LONG).show();
            return;
        }

        CameraX.unbindAll();
        Intent intent = new Intent(this, CreateRecord.class);
        intent.putExtra("image_name_list", imageList);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    public void back(View view){
        finish();
    }
}
