package com.neeri.wbis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import android.animation.ObjectAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.neeri.wbis.database.DatabaseHelper;

import java.io.File;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;

import static android.os.Environment.getExternalStorageDirectory;
import static com.neeri.wbis.database.DataBaseConstants.R_ID;
import static com.neeri.wbis.database.DataBaseConstants.TITLE;

public class CircularsActivity extends AppCompatActivity {

    private ArrayList<String> stored_list;
    private File storageDir;
    private TableLayout ll;
    private final String MIMETYPE = "application/pdf";
    private int numberOfCirculars;

    private TextView progress_percent;
    private ProgressBar progressBar;
    private ProgressBar loading;
    private ConstraintLayout downloadBar;

    private Queue<String> downloadQueue;
    private boolean isDownloadingError, isDownloading;

    private TaskStackBuilder stackBuilder;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circulars);

        progress_percent = findViewById(R.id.progress_percent);
        progressBar = findViewById(R.id.progressBar);
        loading = findViewById(R.id.circular_loading);
        downloadBar = findViewById(R.id.downloadBar);

        downloadQueue = new PriorityQueue<>();
        isDownloadingError = false;
        isDownloading = false;
        isReDownload = false;

        Intent relauchFromNotifications = new Intent(this, CircularsActivity.class);
        relauchFromNotifications.setAction(Intent.ACTION_MAIN);
        relauchFromNotifications.addCategory(Intent.CATEGORY_LAUNCHER);
        stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(relauchFromNotifications);

        storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        ll = findViewById(R.id.circular_table);

        getLocalCirculars();

        if(!isNetworkConnected()){
            for (String name : stored_list){
                populateCircular(name);
            }
            loading.setVisibility(View.GONE);
        }else{
            FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(this,  task->{
                if (task.isSuccessful()) {
                    Log.d("CIRCULARS", "signInAnonymously:success");

                    getCircularsFromDataBase();

                    runOnUiThread(()->new Handler().postDelayed(()->{
                        if(!isDownloading) loading.setVisibility(View.GONE);
                    },3000));

                } else {
                    Log.e("CIRCULARS", "signInAnonymously:failure", task.getException());
                    Toast.makeText(getApplicationContext(), "Please Check your Internet Connection and try again", Toast.LENGTH_SHORT).show();
                }
            });
        }



    }

    private void getCircularsFromDataBase(){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(BuildConfig.CIRCULARS);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if((int) dataSnapshot.getChildrenCount() == 1)
                    conveyEmpty();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
         mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String filename = dataSnapshot.getValue(String.class);
                if (filename == null) return;
                if(filename.equals("zero")) return;

                if(!stored_list.contains(filename)){
                    downloadQueue.add(filename);
                    startDownload();
                }else{
                    populateCircular(filename);
                }
                clearEmpty();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                System.out.println("CHANGED");

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                System.out.println("REMOVED");
                deleteFile(storageDir+"\\"+dataSnapshot.getValue(String.class));
                numberOfCirculars--;
                getLocalCirculars();
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

    private void startDownload(){
        if (isDownloadingError) return;
        if (!downloadQueue.isEmpty() && !isDownloading){
            String filename = downloadQueue.poll();
            File file = new File(storageDir+"/"+filename);
            showDownloadProgress();
            StorageReference mStorage = FirebaseStorage.getInstance().getReference();

            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(this, "Jal Abhilekh");
            mBuilder.setContentTitle("Circular Download")
                    .setContentText("Download in progress")
                    .setSmallIcon(R.drawable.triangle_btn)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOngoing(true)
                    .setContentIntent(stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));

            mStorage.child(BuildConfig.CIRCULARS_DIR+filename).getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {

                    mStorage.child(BuildConfig.CIRCULARS_DIR+filename).getFile(file).addOnSuccessListener((FileDownloadTask.TaskSnapshot taskSnapshot)->{
                        populateCircular(filename);
                        hideDownloadProgress();
                    }).addOnFailureListener((Exception exception)->{
                        Log.e("CIRCULARS", exception.getLocalizedMessage());
//                Toast.makeText(getApplicationContext(),"New Circular(s) Pending, Make sure you have active Internet Connection", Toast.LENGTH_SHORT).show();
                        isDownloadingError = true;
                        hideDownloadProgress();
                    }).addOnProgressListener((@NonNull FileDownloadTask.TaskSnapshot taskSnapshot)-> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressBar.setProgress((int)progress);
                        progress_percent.setText(((int)progress) + "%");
                        mBuilder.setProgress(100, (int)progress, false);
                        mNotifyManager.notify(0, mBuilder.build());
                    });

                    new DatabaseHelper(getApplicationContext()).saveCircularSize(filename, String.valueOf(storageMetadata.getSizeBytes()));
                }
            }).addOnFailureListener((@NonNull Exception e)->{
//                    Toast.makeText(getApplicationContext(), "Check Your Internet Connection and Try Again", Toast.LENGTH_LONG).show();
            });

        }
    }

    private void showDownloadProgress(){
        isDownloading = true;
        loading.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        progress_percent.setText("0%");

        Animation alpha = new AlphaAnimation(0.0f, 1.0f);
        alpha.setDuration(500);
        ObjectAnimator animation = ObjectAnimator.ofFloat(downloadBar, "translationY", -50f);
        animation.setDuration(500);

        downloadBar.setVisibility(View.VISIBLE);
        animation.start();
        downloadBar.startAnimation(alpha);
    }

    private void hideDownloadProgress(){
        isDownloading = false;
        loading.setVisibility(View.INVISIBLE);
        Animation alpha = new AlphaAnimation(1.0f, 0.0f);
        alpha.setDuration(500);
        ObjectAnimator animation = ObjectAnimator.ofFloat(downloadBar, "translationY", 50f);
        animation.setDuration(500);
        animation.start();
        downloadBar.startAnimation(alpha);

        runOnUiThread(()->
            new Handler().postDelayed(() ->{
                progressBar.setProgress(0);
                progress_percent.setText("0%");
                downloadBar.setVisibility(View.GONE);

                mBuilder.setContentText("Download complete").setProgress(0, 0, false)
                        .setAutoCancel(true);
                mNotifyManager.notify(0, mBuilder.build());

                startDownload();
            },500)
        );
    }

    private void clearEmpty(){
        onRestart();
    }

    private void getLocalCirculars(){
        loading.setVisibility(View.VISIBLE);
        numberOfCirculars = 0;
        stored_list = new ArrayList<>();
        Log.d("Files", "Path: " + storageDir);
        if(storageDir != null){
            String[] files = storageDir.list();
            if (files != null)
                stored_list.addAll(Arrays.asList(files));
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void conveyEmpty(){
        if (numberOfCirculars == 0){
            Button card = (Button) Button.inflate(this, R.layout.records_row, null);
            card.setText("No New Circular");
            ll.addView(card);
        }
    }

    private void populateCircular(String filename) {
        if (isReDownload){
            isReDownload = false;
            recreate();
            return;
        }
        Button card = (Button) Button.inflate(this, R.layout.records_row, null);

        String s = filename.split("\\.")[0];
        card.setText(s);

        card.setOnClickListener((View v)->open_file(filename));

        ll.addView(card);
        numberOfCirculars++;
    }

    public void open_file(String filename) {
        File file = new File(storageDir, filename);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Uri apkURI = FileProvider.getUriForFile(this, this.getPackageName() + ".provider", file);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(apkURI, MIMETYPE);
        try {
            if((long)(file.length()) == new DatabaseHelper(this).getCircularSize(filename))
                this.startActivity(intent);
            else
                reDownload(filename);   /*RE-DOWNLOAD CORRUPT FILE */
        }
        catch (Exception e){
            Log.e("CORRUPT FILE", filename);
            reDownload(filename);
        }
    }

    private boolean isReDownload = false;
    private void reDownload(String filename){
        Toast.makeText(getApplicationContext(), "File is not readable. Trying to download again.", Toast.LENGTH_LONG).show();
        downloadQueue.add(filename);
        isReDownload = true;
        startDownload();
    }

    public void back(View view){
        super.onBackPressed();
//        finish();
    }
}
