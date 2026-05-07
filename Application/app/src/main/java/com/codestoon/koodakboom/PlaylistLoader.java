package com.codestoon.koodakboom;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaylistLoader {

    public static List<PlaylistModel> loadPlaylistsFromAssets(Context context) {
        Map<String, PlaylistModel> playlistMap = new HashMap<>();

        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("playlists.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("#");
                if (parts.length >= 5) {
                    String playlistName = parts[0].trim();
                    String title = parts[1].trim();
                    String duration = parts[2].trim();
                    String thumbnailUrl = parts[3].trim();
                    String videoKey = parts[4].trim();

                    PlaylistModel playlist = playlistMap.get(playlistName);
                    if (playlist == null) {
                        playlist = new PlaylistModel(playlistName);
                        playlistMap.put(playlistName, playlist);
                    }

                    PlaylistModel.VideoItem videoItem = new PlaylistModel.VideoItem(
                            playlistName, title, duration, thumbnailUrl, videoKey
                    );
                    playlist.addVideo(videoItem);
                }
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(playlistMap.values());
    }
}