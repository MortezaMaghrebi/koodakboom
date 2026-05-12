package com.codestoon.koodakboom;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_INSTAGRAM = 0;
    private static final int TYPE_LIST = 1;

    private Context context;
    private List<VideoModel> videoList;
    private String viewType;
    private OnVideoClickListener listener;
    private DataManager dataManager;
    private WatchHistoryManager historyManager;
    private int currentlyPlayingPosition = -1;

    // برای مدیریت تمام صفحه
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private FrameLayout fullscreenContainer;

    public interface OnVideoClickListener {
        void onVideoClick(String videoKey);
        void onVideoFullscreen(String videoKey, String title, String views, String likes, String username, String thumbnail, String duration);
    }

    MainActivity mainActivity;
    public VideoAdapter(MainActivity mainActivity, Context context, List<VideoModel> videoList, String viewType, OnVideoClickListener listener) {
        this.mainActivity = mainActivity;
        this.context = context;
        this.videoList = videoList;
        this.viewType = viewType;
        this.listener = listener;
        this.dataManager = new DataManager(context);
        this.historyManager = new WatchHistoryManager(context);
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return viewType.equals("instagram") ? TYPE_INSTAGRAM : TYPE_LIST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_INSTAGRAM) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_video_instagram, parent, false);
            return new InstagramViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_video_list, parent, false);
            return new ListViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        VideoModel video = videoList.get(position);
        boolean isFavorite = dataManager.isFavorite(video.getVideoKey());
        boolean isWatched = historyManager.isVideoWatched(video.getVideoKey());

        if (holder instanceof InstagramViewHolder) {
            InstagramViewHolder instaHolder = (InstagramViewHolder) holder;
            bindInstagramView(instaHolder, video, isFavorite, isWatched, position);
        } else if (holder instanceof ListViewHolder) {
            ListViewHolder listHolder = (ListViewHolder) holder;
            bindListView(listHolder, video, isFavorite, isWatched);
        }
    }

    private void bindInstagramView(InstagramViewHolder holder, VideoModel video, boolean isFavorite, boolean isWatched, int position) {
        holder.tvCaption.setText(video.getTitle());
        holder.tvStats.setText(video.getDuration() + " ⏱️");
        holder.tvDuration.setText(video.getDuration());

        updateLikeButton(holder.btnLike, isFavorite);

        if (isWatched) {
            holder.tvWatchedBadge.setVisibility(View.VISIBLE);
            holder.tvWatchedBadge.setText("✓ دیده شده");
        } else {
            holder.tvWatchedBadge.setVisibility(View.GONE);
        }

        String thumbnailUrl = video.getThumbnailName();
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            if (thumbnailUrl.startsWith("http://") || thumbnailUrl.startsWith("https://")) {
                Glide.with(context)
                        .load(thumbnailUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(holder.thumbnail);
            } else {
                Glide.with(context)
                        .load(Uri.parse("file:///android_asset/" + thumbnailUrl))
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(holder.thumbnail);
            }
        } else {
            holder.thumbnail.setImageResource(R.drawable.ic_placeholder);
        }

        if (currentlyPlayingPosition == position) {
            showWebView(holder, video);
        } else {
            hideWebView(holder);
        }

        holder.btnPlay.setOnClickListener(v -> {
            if (listener != null) {
                stopVideo(holder);
                currentlyPlayingPosition = -1;

                listener.onVideoFullscreen(
                        video.getVideoKey(),
                        video.getTitle(),
                        video.getViews(),
                        video.getLikes(),
                        video.getUsername(),
                        video.getThumbnailName(),
                        video.getDuration()
                );
            }
            //if (currentlyPlayingPosition == position) {
            //    stopVideo(holder);
            //    currentlyPlayingPosition = -1;
            //} else {
            //    if (currentlyPlayingPosition != -1) {
            //        notifyItemChanged(currentlyPlayingPosition);
            //    }
            //    currentlyPlayingPosition = position;
            //    mainActivity.showVideoOpenedAdd();
            //    showWebView(holder, video);
            //}
        });

        holder.btnLike.setOnClickListener(v -> {
            toggleFavorite(video, holder);
        });

        holder.thumbnail.setOnClickListener(v -> {
            if (listener != null) {
                stopVideo(holder);
                currentlyPlayingPosition = -1;
                listener.onVideoFullscreen(
                        video.getVideoKey(),
                        video.getTitle(),
                        video.getViews(),
                        video.getLikes(),
                        video.getUsername(),
                        video.getThumbnailName(),
                        video.getDuration()
                );
            }
        });
    }

    private void showWebView(InstagramViewHolder holder, VideoModel video) {
        holder.thumbnail.setVisibility(View.GONE);
        holder.btnPlay.setVisibility(View.GONE);
        holder.tvDuration.setVisibility(View.GONE);
        holder.webView.setVisibility(View.VISIBLE);
        holder.progressBar.setVisibility(View.VISIBLE);

        WebSettings webSettings = holder.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);

        holder.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                holder.progressBar.setVisibility(View.GONE);
            }
        });

        // WebChromeClient برای پشتیبانی از تمام صفحه
        holder.webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                // اگر قبلاً customView وجود دارد
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }

                customView = view;
                customViewCallback = callback;

                // پیدا کردن Activity و اضافه کردن View به fullscreen container
                if (mainActivity != null) {
                    FrameLayout fullscreenContainer = mainActivity.findViewById(R.id.fullscreenContainer);
                    if (fullscreenContainer != null) {
                        fullscreenContainer.setVisibility(View.VISIBLE);
                        fullscreenContainer.addView(customView, new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                        ));
                        mainActivity.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    } else {
                        // اگر fullscreenContainer وجود نداشت، از روش جایگزین استفاده کن
                        ViewParent parent = holder.webView.getParent();
                        if (parent instanceof FrameLayout) {
                            ((FrameLayout) parent).addView(customView, new FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                            ));
                        }
                    }
                }

                holder.webView.setVisibility(View.GONE);
            }

            @Override
            public void onHideCustomView() {
                if (customView == null) return;

                // پیدا کردن Activity و حذف View
                if (mainActivity != null) {
                    FrameLayout fullscreenContainer = mainActivity.findViewById(R.id.fullscreenContainer);
                    if (fullscreenContainer != null) {
                        fullscreenContainer.removeView(customView);
                        fullscreenContainer.setVisibility(View.GONE);
                        mainActivity.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    } else {
                        ViewParent parent = customView.getParent();
                        if (parent instanceof FrameLayout) {
                            ((FrameLayout) parent).removeView(customView);
                        }
                    }
                }

                if (customViewCallback != null) {
                    customViewCallback.onCustomViewHidden();
                }

                customView = null;
                customViewCallback = null;
                holder.webView.setVisibility(View.VISIBLE);
            }
        });

        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "        body { background: #000; display: flex; align-items: center; justify-content: center; height: 100vh; }\n" +
                "        iframe { width: 100%; height: 100%; border: none; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <iframe src=\"https://www.aparat.com/video/video/embed/videohash/" + video.getVideoKey() + "/vt/frame\"\n" +
                "            allowFullScreen=\"true\"\n" +
                "            webkitallowfullscreen=\"true\"\n" +
                "            mozallowfullscreen=\"true\">\n" +
                "    </iframe>\n" +
                "</body>\n" +
                "</html>";

        holder.webView.loadDataWithBaseURL("https://www.aparat.com/", html, "text/html", "UTF-8", null);
    }

    private void hideWebView(InstagramViewHolder holder) {
        holder.thumbnail.setVisibility(View.VISIBLE);
        holder.btnPlay.setVisibility(View.VISIBLE);
        holder.tvDuration.setVisibility(View.VISIBLE);
        holder.webView.setVisibility(View.GONE);
        holder.progressBar.setVisibility(View.GONE);

        if (holder.webView != null) {
            // بستن تمام صفحه اگر باز است
            if (customView != null && customViewCallback != null) {
                customViewCallback.onCustomViewHidden();
                customView = null;
                customViewCallback = null;
            }
            holder.webView.loadUrl("about:blank");
        }
    }

    private void stopVideo(InstagramViewHolder holder) {
        if (holder.webView != null) {
            // بستن تمام صفحه اگر باز است
            if (customView != null && customViewCallback != null) {
                customViewCallback.onCustomViewHidden();
                customView = null;
                customViewCallback = null;
            }
            holder.webView.loadUrl("about:blank");
        }
        hideWebView(holder);
    }

    private void bindListView(ListViewHolder holder, VideoModel video, boolean isFavorite, boolean isWatched) {
        holder.title.setText(video.getTitle());
        holder.details.setText("👤 " + video.getUsername() + " · ⏱️ " + video.getDuration());

        if (isWatched) {
            holder.tvWatchedBadge.setVisibility(View.VISIBLE);
            holder.tvWatchedBadge.setText("✓ دیده شده");
        } else {
            holder.tvWatchedBadge.setVisibility(View.GONE);
        }

        String thumbnailUrl = video.getThumbnailName();
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            if (thumbnailUrl.startsWith("http://") || thumbnailUrl.startsWith("https://")) {
                Glide.with(context)
                        .load(thumbnailUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(holder.thumbnail);
            } else {
                Glide.with(context)
                        .load(Uri.parse("file:///android_asset/" + thumbnailUrl))
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(holder.thumbnail);
            }
        } else {
            holder.thumbnail.setImageResource(R.drawable.ic_placeholder);
        }

        updateLikeButton(holder.btnLike, isFavorite);

        if (holder.tvLikeCount != null) {
            holder.tvLikeCount.setText(formatNumber(video.getLikes()));
        }

        holder.btnLike.setOnClickListener(v -> {
            toggleFavorite(video, holder);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVideoClick(video.getVideoKey());
            }
        });
    }

    private String formatNumber(String number) {
        if (number == null || number.isEmpty()) return "0";
        try {
            long num = Long.parseLong(number.replaceAll("[^0-9]", ""));
            if (num >= 1000000) {
                return (num / 1000000) + "M";
            } else if (num >= 1000) {
                return (num / 1000) + "K";
            }
            return String.valueOf(num);
        } catch (NumberFormatException e) {
            return number;
        }
    }

    private void updateLikeButton(ImageView likeButton, boolean isFavorite) {
        if (isFavorite) {
            likeButton.setImageResource(R.drawable.ic_like_filled);
        } else {
            likeButton.setImageResource(R.drawable.ic_like_outline);
        }
    }

    private void toggleFavorite(VideoModel video, Object holder) {
        boolean isFavorite = dataManager.isFavorite(video.getVideoKey());

        if (isFavorite) {
            dataManager.removeFromFavorites(video.getVideoKey());
        } else {
            FavoriteModel favorite = new FavoriteModel(
                    video.getVideoKey(),
                    video.getTitle(),
                    video.getThumbnailName(),
                    video.getUsername(),
                    video.getViews(),
                    video.getDuration()
            );
            dataManager.addToFavorites(favorite);
        }

        if (holder instanceof InstagramViewHolder) {
            InstagramViewHolder instaHolder = (InstagramViewHolder) holder;
            updateLikeButton(instaHolder.btnLike, !isFavorite);
            instaHolder.btnLike.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100)
                    .withEndAction(() -> instaHolder.btnLike.animate().scaleX(1f).scaleY(1f).setDuration(100).start());
        } else if (holder instanceof ListViewHolder) {
            ListViewHolder listHolder = (ListViewHolder) holder;
            updateLikeButton(listHolder.btnLike, !isFavorite);
            listHolder.btnLike.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100)
                    .withEndAction(() -> listHolder.btnLike.animate().scaleX(1f).scaleY(1f).setDuration(100).start());
        }
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public void updateVideos(List<VideoModel> newVideos) {
        this.videoList = newVideos;
        notifyDataSetChanged();
    }

    static class InstagramViewHolder extends RecyclerView.ViewHolder {
        TextView tvCaption, tvStats, tvDuration, tvWatchedBadge;
        ImageView thumbnail, btnLike, btnPlay;
        WebView webView;
        ProgressBar progressBar;
        FrameLayout videoContainer;

        public InstagramViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvStats = itemView.findViewById(R.id.tvStats);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvWatchedBadge = itemView.findViewById(R.id.tvWatchedBadge);
            thumbnail = itemView.findViewById(R.id.ivThumbnail);
            btnLike = itemView.findViewById(R.id.ivLike);
            btnPlay = itemView.findViewById(R.id.btnPlayVideo);
            webView = itemView.findViewById(R.id.webViewPlayer);
            progressBar = itemView.findViewById(R.id.progressBarVideo);
            videoContainer = itemView.findViewById(R.id.videoContainer);
        }
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {
        TextView title, details, tvLikeCount, tvWatchedBadge;
        ImageView thumbnail, btnLike;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            details = itemView.findViewById(R.id.tvDetails);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvWatchedBadge = itemView.findViewById(R.id.tvWatchedBadge);
            thumbnail = itemView.findViewById(R.id.ivThumb);
            btnLike = itemView.findViewById(R.id.ivLike);
        }
    }
}