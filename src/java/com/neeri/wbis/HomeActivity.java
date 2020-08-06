package com.neeri.wbis;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.neeri.wbis.tools.ContextHelpers;

public class HomeActivity extends AppCompatActivity {

    private TextView title_switcher, slogan_switcher;
    private Button about_btn, profile_btn, drafts_btn, photos_btn, waterbodies_btn;
    private Button map_btn, adopt_btn, contact_btn, grievance_btn;

    String[] titleList, sloganList, about, profile, photos, drafts, map, waterbodies, grievance;

    private int si = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        titleList = new String[]{
                getString(R.string.app_name_tl),
                getString(R.string.app_name_en),
                getString(R.string.app_name_hn),
                getString(R.string.app_name_en)
        };
        sloganList = new String[]{
                getString(R.string.slogan_tl),
                getString(R.string.slogan_fr),
                getString(R.string.slogan_hn),
                getString(R.string.slogan_en)
        };
        map = new String[]{
                getString(R.string.map_tl),
                getString(R.string.map_fr),
                getString(R.string.map_hn),
                getString(R.string.map_en),
        };
        drafts = new String[]{
                getString(R.string.draft_tl),
                getString(R.string.draft_fr),
                getString(R.string.draft_hn),
                getString(R.string.draft_en),
        };
        profile = new String[]{
                getString(R.string.profile_tl),
                getString(R.string.profile_fr),
                getString(R.string.profile_hn),
                getString(R.string.profile_en),
        };
        photos = new String[]{
                getString(R.string.photo_tl),
                getString(R.string.photo_fr),
                getString(R.string.photo_hn),
                getString(R.string.photo_en),
        };
        about = new String[]{
                getString(R.string.about_tl),
                getString(R.string.about_fr),
                getString(R.string.about_hn),
                getString(R.string.about_en),
        };
        waterbodies = new String[]{
                getString(R.string.waterbodies_tl),
                getString(R.string.waterbodies_fr),
                getString(R.string.waterbodies_hn),
                getString(R.string.waterbodies_en),
        };
        grievance = new String[]{
                getString(R.string.grievance_tl),
                getString(R.string.grievance_fr),
                getString(R.string.grievance_hn),
                getString(R.string.grievance_en)
        };

        title_switcher = findViewById(R.id.title_switcher);
        slogan_switcher = findViewById(R.id.slogan_switcher);
        about_btn = findViewById(R.id.about_btn);
        photos_btn = findViewById(R.id.takephotos_btn);
        drafts_btn = findViewById(R.id.drafts_btn);
        profile_btn = findViewById(R.id.saveprofile_btn);
        profile_btn = findViewById(R.id.saveprofile_btn);
        waterbodies_btn = findViewById(R.id.waterbodies_btn);

        map_btn = findViewById(R.id.map_btn);
        adopt_btn = findViewById(R.id.adopt_btn);
        contact_btn = findViewById(R.id.contact_btn);
        grievance_btn = findViewById(R.id.grievance_btn);

        animateText();


    }

    public void gotoDrafts(View view){
        startActivity(new Intent(this, RecordsListActivity.class));
    }

    public void gotoAdopt(View view){
        startActivity(new Intent(this, AdoptLake.class));
    }

    public void gotoContact(View view){
        startActivity(new Intent(this, Contact.class));
    }

    public void gotoAbout(View view){
        startActivity(new Intent(this, About.class));
    }

    public void gotoCapturePhotos(View view){
        startActivity(new Intent(this, CapturePhotos.class));
    }

    public void gotoSaveProfile(View view){
        startActivity(new Intent(this, SaveProfile.class));
    }

    public void gotoGrievance(View view){
        startActivity(new Intent(this, SubmitGrievance.class));
    }

    public void gotoDesiltUpdate(View view){
        startActivity(new Intent(this, UpdateDesilt.class));
    }

    public void gotoMap(View view){

        if(!isNetworkConnected()){
            showInfoAlert("", "Please check your Internet Connection");
            return;
        }

        startActivity(new Intent(this, MapsActivity.class));
    }

    private void animateText(){
        final Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(2000);

        final Animation out = new AlphaAnimation(1.0f, 0.0f);
        out.setDuration(2000);

        title_switcher.startAnimation(in);
        slogan_switcher.startAnimation(in);
//        about_btn.startAnimation(in);
//        profile_btn.startAnimation(in);
//        photos_btn.startAnimation(in);
//        drafts_btn.startAnimation(in);

        out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                title_switcher.startAnimation(in);
                slogan_switcher.startAnimation(in);
//                about_btn.startAnimation(in);
//                profile_btn.startAnimation(in);
//                photos_btn.startAnimation(in);
//                drafts_btn.startAnimation(in);
                out.reset();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (si == 4) si = 0;
                title_switcher.setText(titleList[(si)]);
                slogan_switcher.setText(sloganList[(si)]);
                about_btn.setText(about[(si)]);
                profile_btn.setText(profile[(si)]);
                photos_btn.setText(photos[(si)]);
                drafts_btn.setText(drafts[(si)]);
                grievance_btn.setText(grievance[(si)]);
                waterbodies_btn.setText(waterbodies[(si++)]);
//                map_btn.(map[(si++)%4]);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                title_switcher.startAnimation(out);
                slogan_switcher.startAnimation(out);
//                about_btn.startAnimation(out);
//                profile_btn.startAnimation(out);
//                photos_btn.startAnimation(out);
//                drafts_btn.startAnimation(out);
                in.reset();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void gotoCirculars(View view){
        Intent intent = new Intent(this, CircularsActivity.class);
        startActivity(intent);
    }

    public void gotoWaterBodies(View view){
        Intent intent = new Intent(this, WaterBodiesActivity.class);
        startActivity(intent);
    }

    public void gotoVideoGallery(View view){
        Intent intent = new Intent(this, VideoGalleryActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
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
    protected void onStart() {
        super.onStart();
    }

    private void showInfoAlert(String title, String info){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(info)
                .setCancelable(false)
                .setNeutralButton(android.R.string.ok, (DialogInterface dialog, int which)-> {})
                .show();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

}
