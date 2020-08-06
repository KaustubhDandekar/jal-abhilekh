package com.neeri.wbis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.neeri.wbis.tools.RecyclerOnClickListener;
import com.neeri.wbis.tools.VideoAdapter;
import com.neeri.wbis.tools.VideoModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class VideoGalleryActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private String COMMUNE;
    private RecyclerView recyclerView;
    private TableLayout v_table;
    private ProgressBar loading;

    private ArrayList<VideoModel> videoList;

    private int numberOfVideos, alreadyfetched;

    private class ThumbnailItem{
        YouTubeThumbnailLoader loader;
        String videoLink;

        public ThumbnailItem(YouTubeThumbnailLoader loader, String videoLink){
            this.loader = loader;
            this.videoLink = videoLink;
        }

        public YouTubeThumbnailLoader getLoader() {
            return loader;
        }

        public void setLoader(YouTubeThumbnailLoader loader) {
            this.loader = loader;
        }

        public String getVideoLink() {
            return videoLink;
        }

        public void setVideoLink(String videoLink) {
            this.videoLink = videoLink;
        }
    }
    private Queue<ThumbnailItem> thumbnailQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_gallery);

        COMMUNE = "Mannadipet Commune";

        videoList = new ArrayList<>();
        v_table = findViewById(R.id.v_table);
        loading = findViewById(R.id.circular_loading);

        TextView commune_heading = findViewById(R.id.commune_heading);
        commune_heading.setText("Videos");

        numberOfVideos = 0;
        alreadyfetched = 0;
        getCommuneData();
    }

//    private void populateVideos() {
//        recyclerView = findViewById(R.id.video_recycler_view);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(new VideoAdapter(this, videoList));
//
//        recyclerView.addOnItemTouchListener(new RecyclerOnClickListener(this,
//                (View view, int position)->viewVideo(videoList.get(position))));
//
//    }

    private void populateVideos(){
        thumbnailQueue = new ConcurrentLinkedDeque<>();
        System.out.println("videoList.size() = " + videoList.size());
        for(VideoModel model : videoList){
            LinearLayout card = (LinearLayout) LinearLayout.inflate(this, R.layout.video_item, null);
            ((TextView)card.findViewById(R.id.id_space)).setText(model.getId());
            ((TextView)card.findViewById(R.id.name_space)).setText(model.getTitle());
            card.setOnClickListener((View v)->viewVideo(model));

            v_table.addView(card);

            ((YouTubeThumbnailView)card.findViewById(R.id.thumbnail)).initialize(BuildConfig.YTK, new YouTubeThumbnailView.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {

                    thumbnailQueue.add(new ThumbnailItem(youTubeThumbnailLoader ,model.getVideoLink()));
                    loadThumbnail();

                }

                @Override
                public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

                }
            });

        }

        loading.setVisibility(View.GONE);

    }

    private void getCommuneData(){

        mDatabase = FirebaseDatabase.getInstance().getReference(BuildConfig.VIDEOS_DIR+COMMUNE);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                System.out.println(dataSnapshot);

                int count = (int) dataSnapshot.getChildrenCount();
                if(count == 0)
                    conveyEmpty();
                numberOfVideos = count;

                mDatabase.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        HashMap<String, Object> videoData = (HashMap<String, Object>) dataSnapshot.getValue();
                        if (videoData == null) return;

                        VideoModel video = new VideoModel();
                        video.setVideoLink(((String) videoData.get("link")).substring(17).replaceAll("(\\r|\\n)", ""));
                        video.setTitle(((String) videoData.get("name")));
                        video.setId(((String) videoData.get("id")));

                        videoList.add(video);

                        if (numberOfVideos == ++alreadyfetched){
                            populateVideos();
                        }

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        System.out.println("CHANGED");

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        System.out.println("REMOVED");
                        numberOfVideos--;
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

                if (alreadyfetched == numberOfVideos){
                    new Handler().postDelayed(()-> loading.setVisibility(View.GONE), 2000);
//                    populateVideos();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    private boolean busyLoading = false;
    private void loadThumbnail(){
        if (!thumbnailQueue.isEmpty() && !busyLoading){
            busyLoading = true;
            loading.setVisibility(View.VISIBLE);
            ThumbnailItem item = thumbnailQueue.poll();
            item.getLoader().setVideo(item.getVideoLink());
            item.getLoader().setOnThumbnailLoadedListener(new YouTubeThumbnailLoader.OnThumbnailLoadedListener() {
                @Override
                public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
                    busyLoading = false;
                    loading.setVisibility(View.GONE);
                    item.getLoader().release();
                    loadThumbnail();
                }

                @Override
                public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {
                    busyLoading = false;
                    loading.setVisibility(View.GONE);
                    item.getLoader().release();
                    Log.e(VideoGalleryActivity.class.getSimpleName(), "Youtube Thumbnail Error : "+errorReason);
                }
            });
        }
    }

    private void clearEmpty(){
        onRestart();
    }

    private void conveyEmpty(){
        if (numberOfVideos == 0){
            Button card = (Button) Button.inflate(this, R.layout.records_row, null);
            card.setText("No Data Available around this Commune");
//            ll.addView(card);
        }
    }

    private void viewVideo(VideoModel data){
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra("id", data.getId());
        intent.putExtra("title", data.getTitle());
        intent.putExtra("link", data.getVideoLink());
        startActivity(intent);
    }

    public void back(View view){
        super.onPause();
        super.onBackPressed();
//        finish();
    }

    @Override
    public void onBackPressed() {
        super.onPause();
        super.onBackPressed();
    }
}
