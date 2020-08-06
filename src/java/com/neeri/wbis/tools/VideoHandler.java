package com.neeri.wbis.tools;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.youtube.player.YouTubeThumbnailView;
import com.neeri.wbis.R;

public class VideoHandler extends RecyclerView.ViewHolder {
    public YouTubeThumbnailView videoThumbnail;
    public TextView videoTitle, videoId;

    public VideoHandler(View itemView) {
        super(itemView);
        videoThumbnail = itemView.findViewById(R.id.thumbnail);
        videoTitle = itemView.findViewById(R.id.name_space);
        videoId = itemView.findViewById(R.id.id_space);
    }
}
