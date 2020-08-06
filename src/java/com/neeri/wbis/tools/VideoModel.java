package com.neeri.wbis.tools;

public class VideoModel {

    private String
            video_link,
            title,
            id;

    public String getVideoLink() {
        return video_link;
    }

    public void setVideoLink(String video_link) {
        this.video_link = video_link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getId() {
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    @Override
    public String toString() {
        return "YoutubeVideoModel{" +
                "videoLink='" + video_link + '\'' +
                ", id='" + id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
