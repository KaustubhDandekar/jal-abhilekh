package com.neeri.wbis.tools;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.neeri.wbis.BuildConfig;
import com.neeri.wbis.R;

import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoHandler> {
    private static final String TAG = VideoAdapter.class.getSimpleName();
    private Context context;
    private ArrayList<VideoModel> VideoModelArrayList;


    public VideoAdapter(Context context, ArrayList<VideoModel> VideoModelArrayList) {
        this.context = context;
        this.VideoModelArrayList = VideoModelArrayList;
    }

    @Override
    public VideoHandler onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.video_item, parent, false);
        return new VideoHandler(view);
    }

    @Override
    public void onBindViewHolder(VideoHandler handler, final int position) {

        final VideoModel youtubeVideoModel = VideoModelArrayList.get(position);

        handler.videoTitle.setText(youtubeVideoModel.getTitle());
        handler.videoId.setText(youtubeVideoModel.getId());

        handler.videoThumbnail.initialize(BuildConfig.YTK, new YouTubeThumbnailView.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, final YouTubeThumbnailLoader youTubeThumbnailLoader) {

                youTubeThumbnailLoader.setVideo(youtubeVideoModel.getVideoLink());

                youTubeThumbnailLoader.setOnThumbnailLoadedListener(new YouTubeThumbnailLoader.OnThumbnailLoadedListener() {
                    @Override
                    public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
                        youTubeThumbnailView.refreshDrawableState();
                        youTubeThumbnailLoader.release();
                    }
                    @Override
                    public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {
                        youTubeThumbnailLoader.release();
                        Log.e(TAG, "Youtube Thumbnail Error : "+errorReason);
                    }
                });
            }
            @Override
            public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {
                Log.e(TAG, "Youtube Initialization Failure");
            }
        });

    }

    @Override
    public int getItemCount() {
        return VideoModelArrayList != null ? VideoModelArrayList.size() : 0;
    }
}
