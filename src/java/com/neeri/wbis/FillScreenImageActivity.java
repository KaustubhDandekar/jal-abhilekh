package com.neeri.wbis;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;


public class FillScreenImageActivity extends AppCompatActivity {
    private String TAG = "FillScreenImageActivity";
    private ImageView imageView;
    private ImageButton deleteButton;
    private String imageName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_screen_image);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Bundle bundle = getIntent().getExtras();

        imageView = findViewById(R.id.fullscreen_image_view);

        deleteButton = findViewById(R.id.image_delete_button);
        deleteButton.setOnClickListener(view -> {
            showAlertDialog();
        });

        if (bundle != null) {
            String imagePath = bundle.getString("image_path");
            if (imagePath != null) {
                loadImage(imagePath);
            }
        }
    }

    private void loadImage(String imagePath) {
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            Uri imageUri = Uri.fromFile(imageFile);
            Glide.with(this)
                    .load(imageUri)
                    .into(imageView);
            imageName = imageFile.getName();
        } else {
            Log.e(TAG, "loadImage: File does not exists: " + imagePath);
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage("Delete this picture?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (dialog, which) -> {
            handleImageDelete();
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void handleImageDelete() {
        Intent resultIntent = getIntent();
        resultIntent.putExtra("image_deleted", true);
        resultIntent.putExtra("image_name", imageName);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    public void back(View view){
        finish();
    }
}
