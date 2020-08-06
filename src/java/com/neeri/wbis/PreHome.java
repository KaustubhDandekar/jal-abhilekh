package com.neeri.wbis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class PreHome extends AppCompatActivity {

    private TextView title_switcher, slogan_switcher;
    private String[] titleList, sloganList;

    private static final int LONG_DELAY = 3500; // 3.5 seconds
    private static final int SHORT_DELAY = 2000; // 2 seconds

    private int ti = 0, si = 0, backPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_home);

        backPressed = 0;

        titleList = new String[]{
                getString(R.string.app_name_tl),
                getString(R.string.app_name_en),
                getString(R.string.app_name_hn)
        };
        sloganList = new String[]{
                getString(R.string.slogan_tl),
                getString(R.string.slogan_fr),
                getString(R.string.slogan_hn),
                getString(R.string.slogan_en)
        };

        title_switcher = findViewById(R.id.title_switcher);
        slogan_switcher = findViewById(R.id.slogan_switcher);

        new Thread(()->{
            animateText();
        }).start();

    }

    public void gotoHome(View view){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        backPressed = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        backPressed = 0;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        backPressed = 0;
    }

    private void animateText(){
        final Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(2000);

        final Animation out = new AlphaAnimation(1.0f, 0.0f);
        out.setDuration(2000);

        title_switcher.startAnimation(in);
        slogan_switcher.startAnimation(in);

        out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                title_switcher.startAnimation(in);
                slogan_switcher.startAnimation(in);
                out.reset();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                title_switcher.setText(titleList[(ti++)%3]);
                slogan_switcher.setText(sloganList[(si++)%4]);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                title_switcher.startAnimation(out);
                slogan_switcher.startAnimation(out);
                in.reset();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public void onBackPressed() {

        if (++backPressed == 1){
            Toast.makeText(this, "Press Back Again to Exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(()-> backPressed = 0, SHORT_DELAY);
            return;
        }

        super.onBackPressed();
    }



}
