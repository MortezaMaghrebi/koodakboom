package com.codestoon.koodakboom;

import java.util.ArrayList;
import java.util.List;

public class PlaylistModel {
    private String name;
    private List<VideoItem> videos;
    private String thumbnailUrl;
    private int videoCount;

    public PlaylistModel(String name) {
        this.name = name;
        this.videos = new ArrayList<>();
        this.videoCount = 0;
    }

    public String getName() {
        return name;
    }

    public List<VideoItem> getVideos() {
        return videos;
    }

    public void addVideo(VideoItem video) {
        videos.add(video);
        videoCount = videos.size();
        if (thumbnailUrl == null && video.getThumbnailUrl() != null && !video.getThumbnailUrl().isEmpty()) {
            thumbnailUrl = video.getThumbnailUrl();
        }
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public static class VideoItem {
        private String playlistName;
        private String title;
        private String duration;
        private String thumbnailUrl;
        private String videoKey;

        public VideoItem(String playlistName, String title, String duration, String thumbnailUrl, String videoKey) {
            this.playlistName = playlistName;
            this.title = title;
            this.duration = duration;
            this.thumbnailUrl = thumbnailUrl;
            this.videoKey = videoKey;
        }

        public String getPlaylistName() { return playlistName; }
        public String getTitle() { return title; }
        public String getDuration() { return duration; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public String getVideoKey() { return videoKey; }
    }
}