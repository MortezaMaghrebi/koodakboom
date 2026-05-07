package com.codestoon.koodakboom;

public class CommentModel {
    private String videoKey;
    private String comment;
    private String username;
    private long timestamp;

    public CommentModel(String videoKey, String comment, String username) {
        this.videoKey = videoKey;
        this.comment = comment;
        this.username = username;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getVideoKey() { return videoKey; }
    public String getComment() { return comment; }
    public String getUsername() { return username; }
    public long getTimestamp() { return timestamp; }
}