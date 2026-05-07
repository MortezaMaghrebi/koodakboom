package com.codestoon.koodakboom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private Context context;
    private List<PlaylistModel> playlistList;
    private OnPlaylistClickListener listener;
    private WatchHistoryManager historyManager;

    public interface OnPlaylistClickListener {
        void onPlaylistClick(PlaylistModel playlist);
    }

    public PlaylistAdapter(Context context, List<PlaylistModel> playlistList, OnPlaylistClickListener listener) {
        this.context = context;
        this.playlistList = playlistList;
        this.listener = listener;
        this.historyManager = new WatchHistoryManager(context);
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        PlaylistModel playlist = playlistList.get(position);
        holder.tvTitle.setText(playlist.getName());

        int watchedCount = playlist.getWatchedCount(historyManager);
        int totalCount = playlist.getVideoCount();

        if (watchedCount == totalCount && totalCount > 0) {
            holder.tvEpisodeCount.setText("✅ " + watchedCount + "/" + totalCount + " قسمت (تکمیل شد)");
            holder.tvEpisodeCount.setTextColor(0xFF10B981);
        } else if (watchedCount > 0) {
            holder.tvEpisodeCount.setText("📺 " + watchedCount + "/" + totalCount + " قسمت دیده شده");
            holder.tvEpisodeCount.setTextColor(0xFFE67E22);
        } else {
            holder.tvEpisodeCount.setText(totalCount + " قسمت");
            holder.tvEpisodeCount.setTextColor(0xFFE67E22);
        }

        int progress = playlist.getProgressPercent(historyManager);
        if (holder.progressBar != null) {
            holder.progressBar.setProgress(progress);
            holder.progressBar.setVisibility(progress > 0 ? View.VISIBLE : View.GONE);
        }

        String thumbUrl = playlist.getThumbnailUrl();
        if (thumbUrl != null && !thumbUrl.isEmpty()) {
            Glide.with(context)
                    .load(thumbUrl)
                    .placeholder(R.drawable.titleimage)
                    .error(R.drawable.titleimage)
                    .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.titleimage);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlaylistClick(playlist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle;
        TextView tvEpisodeCount;
        ProgressBar progressBar;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivPlaylistThumbnail);
            tvTitle = itemView.findViewById(R.id.tvPlaylistTitle);
            tvEpisodeCount = itemView.findViewById(R.id.tvEpisodeCount);
            progressBar = itemView.findViewById(R.id.progressPlaylist);
        }
    }
}