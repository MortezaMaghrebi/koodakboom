package com.codestoon.koodakboom;

public class FavoriteModel {
    private String videoKey;
    private String title;
    private String thumbnailName;
    private String username;
    private String views;
    private String duration;
    private long timestamp;

    public FavoriteModel(String videoKey, String title, String thumbnailName,
                         String username, String views, String duration) {
        this.videoKey = videoKey;
        this.title = title;
        this.thumbnailName = thumbnailName;
        this.username = username;
        this.views = views;
        this.duration = duration;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getVideoKey() { return videoKey; }
    public String getTitle() { return title; }
    public String getThumbnailName() { return thumbnailName; }
    public String getUsername() { return username; }
    public String getViews() { return views; }
    public String getDuration() { return duration; }
    public long getTimestamp() { return timestamp; }
}