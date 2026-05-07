package com.codestoon.koodakboom;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class DataManager {
    private static final String PREF_NAME = "ClipChinPrefs";
    private static final String KEY_FAVORITES = "favorites";
    private static final String KEY_COMMENTS = "comments";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public DataManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // ========== مدیریت Favorites ==========
    public void addToFavorites(FavoriteModel favorite) {
        List<FavoriteModel> favorites = getFavorites();
        // بررسی تکراری نبودن
        for (FavoriteModel f : favorites) {
            if (f.getVideoKey().equals(favorite.getVideoKey())) {
                return;
            }
        }
        favorites.add(0, favorite); // اضافه کردن به ابتدای لیست
        saveFavorites(favorites);
    }

    public void removeFromFavorites(String videoKey) {
        List<FavoriteModel> favorites = getFavorites();
        favorites.removeIf(f -> f.getVideoKey().equals(videoKey));
        saveFavorites(favorites);
    }

    public boolean isFavorite(String videoKey) {
        List<FavoriteModel> favorites = getFavorites();
        for (FavoriteModel f : favorites) {
            if (f.getVideoKey().equals(videoKey)) {
                return true;
            }
        }
        return false;
    }

    public List<FavoriteModel> getFavorites() {
        String json = sharedPreferences.getString(KEY_FAVORITES, "[]");
        Type type = new TypeToken<List<FavoriteModel>>(){}.getType();
        return gson.fromJson(json, type);
    }

    private void saveFavorites(List<FavoriteModel> favorites) {
        String json = gson.toJson(favorites);
        sharedPreferences.edit().putString(KEY_FAVORITES, json).apply();
    }

    // ========== مدیریت Comments ==========
    public void addComment(String videoKey, String comment, String username) {
        List<CommentModel> comments = getCommentsForVideo(videoKey);
        comments.add(0, new CommentModel(videoKey, comment, username)); // نظر جدید در ابتدا
        saveComments(videoKey, comments);
    }

    public List<CommentModel> getCommentsForVideo(String videoKey) {
        String json = sharedPreferences.getString(KEY_COMMENTS + "_" + videoKey, "[]");
        Type type = new TypeToken<List<CommentModel>>(){}.getType();
        return gson.fromJson(json, type);
    }

    private void saveComments(String videoKey, List<CommentModel> comments) {
        String json = gson.toJson(comments);
        sharedPreferences.edit().putString(KEY_COMMENTS + "_" + videoKey, json).apply();
    }
}