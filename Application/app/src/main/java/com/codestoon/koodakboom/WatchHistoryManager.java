package com.codestoon.koodakboom;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WatchHistoryManager {
    private static final String PREF_NAME = "WatchHistory";
    private static final String KEY_WATCHED_VIDEOS = "watched_videos";
    private static final String KEY_WATCHED_EPISODES = "watched_episodes";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public WatchHistoryManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void markVideoAsWatched(String videoKey) {
        Set<String> watchedVideos = getWatchedVideos();
        watchedVideos.add(videoKey);
        saveWatchedVideos(watchedVideos);
    }

    public boolean isVideoWatched(String videoKey) {
        Set<String> watchedVideos = getWatchedVideos();
        return watchedVideos.contains(videoKey);
    }

    public Set<String> getWatchedVideos() {
        String json = sharedPreferences.getString(KEY_WATCHED_VIDEOS, "[]");
        Type type = new TypeToken<Set<String>>(){}.getType();
        Set<String> watchedVideos = gson.fromJson(json, type);
        if (watchedVideos == null) {
            watchedVideos = new HashSet<>();
        }
        return watchedVideos;
    }

    private void saveWatchedVideos(Set<String> watchedVideos) {
        String json = gson.toJson(watchedVideos);
        sharedPreferences.edit().putString(KEY_WATCHED_VIDEOS, json).apply();
    }

    public void markEpisodeAsWatched(String playlistName, String videoKey) {
        String key = playlistName + "_" + videoKey;
        Set<String> watchedEpisodes = getWatchedEpisodes();
        watchedEpisodes.add(key);
        saveWatchedEpisodes(watchedEpisodes);
    }

    public boolean isEpisodeWatched(String playlistName, String videoKey) {
        String key = playlistName + "_" + videoKey;
        Set<String> watchedEpisodes = getWatchedEpisodes();
        return watchedEpisodes.contains(key);
    }

    public int getWatchedEpisodeCount(String playlistName, List<PlaylistModel.VideoItem> videos) {
        int count = 0;
        for (PlaylistModel.VideoItem video : videos) {
            if (isEpisodeWatched(playlistName, video.getVideoKey())) {
                count++;
            }
        }
        return count;
    }

    public int getProgressPercent(String playlistName, List<PlaylistModel.VideoItem> videos) {
        if (videos == null || videos.isEmpty()) return 0;
        int watched = getWatchedEpisodeCount(playlistName, videos);
        return (watched * 100) / videos.size();
    }

    private Set<String> getWatchedEpisodes() {
        String json = sharedPreferences.getString(KEY_WATCHED_EPISODES, "[]");
        Type type = new TypeToken<Set<String>>(){}.getType();
        Set<String> watchedEpisodes = gson.fromJson(json, type);
        if (watchedEpisodes == null) {
            watchedEpisodes = new HashSet<>();
        }
        return watchedEpisodes;
    }

    private void saveWatchedEpisodes(Set<String> watchedEpisodes) {
        String json = gson.toJson(watchedEpisodes);
        sharedPreferences.edit().putString(KEY_WATCHED_EPISODES, json).apply();
    }

    public void clearAllHistory() {
        sharedPreferences.edit()
                .remove(KEY_WATCHED_VIDEOS)
                .remove(KEY_WATCHED_EPISODES)
                .apply();
    }
}