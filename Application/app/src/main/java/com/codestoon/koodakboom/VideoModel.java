package com.codestoon.koodakboom;

public class VideoModel {
    private int id;
    private String videoKey;
    private String title, views, duration, thumbnailName, username, likes, comments;

    public VideoModel(int id, String videoKey, String title, String views, String duration,
                      String thumbnailName, String username, String likes, String comments) {
        this.id = id;
        this.videoKey = videoKey;
        this.title = title;
        this.views = views;
        this.duration = duration;
        this.thumbnailName = thumbnailName;
        this.username = username;
        this.likes = likes;
        this.comments = comments;
    }

    public int getId() { return id; }
    public String getVideoKey() { return videoKey; }
    public String getTitle() { return title; }
    public String getViews() { return views; }
    public String getDuration() { return duration; }
    public String getThumbnailName() { return thumbnailName; }
    public String getUsername() { return username; }
    public String getLikes() { return likes; }
    public String getComments() { return comments; }
}