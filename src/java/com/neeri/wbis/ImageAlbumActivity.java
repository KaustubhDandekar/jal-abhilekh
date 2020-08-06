package com.neeri.wbis;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.GridView;
import android.widget.Toast;

import com.neeri.wbis.tools.ImageAdaptor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageAlbumActivity extends AppCompatActivity {
    private String TAG = "ImageAlbumActivity";
    private static final int KEEP_OR_DELETE_IMAGE = 1;
    private GridView gridView;
    private ArrayList<String> imageNameList;
    private List<Uri> imageUris;
    private boolean isImageListModified = false;
    ImageAdaptor imageAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_album);

        gridView = findViewById(R.id.image_grid_view);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            imageNameList = bundle.getStringArrayList("image_name_list");
        }

        if (imageNameList != null) {
            imageUris = getImageUris(imageNameList);
            imageAdaptor = new ImageAdaptor(imageUris, this);
            gridView.setAdapter(imageAdaptor);
            gridView.setOnItemClickListener((parent, view, position, id) -> {
                gotoFullscreenImageActivity(imageUris.get(position).getPath());
            });
        }
    }

    private void gotoFullscreenImageActivity(String imagePath) {
        Intent intent = new Intent(getApplicationContext(), FillScreenImageActivity.class);
        intent.putExtra("image_path", imagePath);
        startActivityForResult(intent, KEEP_OR_DELETE_IMAGE);
    }

    private List<Bitmap> getImageBitmaps(List<String> imageNameList) {
        List<Bitmap> imageBitmapList = new ArrayList<>();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        for (String imageFileName : imageNameList) {
            String imageFilePath = storageDir + "/" + imageFileName;
            Log.d(TAG, "getImageBitmaps: " + imageFilePath);
            File imageFile = new File(imageFilePath);
            if (imageFile.exists()) {
                Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                imageBitmapList.add(imageBitmap);
            }
        }
        return imageBitmapList;
    }

    private List<Uri> getImageUris(List<String> imageNameList) {
        List<Uri> imageUriList = new ArrayList<>();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (storageDir != null) {
            for (String imageFileName : imageNameList) {
                String imageFilePath = storageDir.getAbsolutePath() + "/" + imageFileName;
                Log.d(TAG, "getImageBitmaps: " + imageFilePath);
                File imageFile = new File(imageFilePath);
                if (imageFile.exists()) {
                    Log.d(TAG, "getImageUris: IMAGE EXISTS: " + imageFile.getAbsolutePath());
                    Uri imageFileUri = Uri.fromFile(imageFile);
                    imageUriList.add(imageFileUri);
                }
            }
        }

        Log.d(TAG, "getImageUris: Uri array size: " + imageUriList.size());
        return imageUriList;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case KEEP_OR_DELETE_IMAGE:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        if (data.getBooleanExtra("image_deleted", false)) {
                            String deletedImageName = data.getStringExtra("image_name");
                            if (deletedImageName != null && !deletedImageName.isEmpty()) {
                                HandleImageDelete(deletedImageName);
                            }
                        }
                    }
                }
                break;
        }
    }

    private void HandleImageDelete(String deletedImageName) {
        Toast.makeText(this, TAG + ": HandleImageDelete: " + deletedImageName, Toast.LENGTH_SHORT).show();
        if (imageNameList.remove(deletedImageName)) {
            for (int i = 0; i < imageUris.size(); i++) {
                String text = imageUris.get(i).getLastPathSegment();
                if (deletedImageName.equals(imageUris.get(i).getLastPathSegment())) {
                    imageUris.remove(i);
                    imageAdaptor.notifyDataSetChanged();
                    isImageListModified = true;
                    Log.d(TAG, "HandleImageDelete: image removed from imageUris list");
                    break;
                }
            }
        }
    }

    private static ArrayList<String> getImageNameList(List<Uri> imageUris) {
        ArrayList<String> imageNameList = new ArrayList<>();
        for (Uri imageUri : imageUris) {
            imageNameList.add(imageUri.getLastPathSegment());
        }
        return imageNameList;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, TAG + ": onBackPressed: ");
        if (isImageListModified) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("image_list_modified", true);
            resultIntent.putExtra("image_name_list", getImageNameList(imageUris));
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
}
