package com.neeri.wbis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

public class SplashScreen extends AppCompatActivity {

    AnimationDrawable anim;
    ImageView anim_view,name_view;
    Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);
        Intent intent = new Intent(this, PreHome.class);
        startActivity(intent);
        finish();

//        handler = new Handler();
//        anim_view = (ImageView)findViewById(R.id.name_anim);
//        anim_view.setBackgroundResource(R.drawable.logo_animation);
//        anim = (AnimationDrawable)anim_view.getBackground();
//        playAnim();
    }

    @Override
    public void onResume(){
        super.onResume();
        setImage47();
    }

    private void playAnim(){
        onWindowFocusChanged(hasWindowFocus());
        anim.start();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, RecordsListActivity.class);
                startActivity(intent);
                finish();
            }
        },2200);
    }
    private void setImage47(){
        name_view.setVisibility(View.VISIBLE);
    }




}
