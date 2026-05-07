package com.codestoon.koodakboom;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private int currentlyPlayingPosition = -1;

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

        if (holder instanceof InstagramViewHolder) {
            InstagramViewHolder instaHolder = (InstagramViewHolder) holder;
            bindInstagramView(instaHolder, video, isFavorite, position);
        } else if (holder instanceof ListViewHolder) {
            ListViewHolder listHolder = (ListViewHolder) holder;
            bindListView(listHolder, video, isFavorite);
        }
    }

    private void bindInstagramView(InstagramViewHolder holder, VideoModel video, boolean isFavorite, int position) {
        // تنظیم اطلاعات پایه
        holder.username.setText(video.getUsername());
        holder.tvTime.setText(" ");

       // String likeCount = formatNumber(video.getLikes());
        //String commentCount = formatNumber(video.getComments());
       // String viewCount = formatNumber(video.getViews());

        //holder.tvLikes.setText(likeCount);
        //holder.tvComments.setText(commentCount);
        holder.tvStats.setText(video.getDuration() + " ⏱️ " );
        holder.tvCaption.setText(video.getTitle());

        updateLikeButton(holder.btnLike, isFavorite);

        // لود تصویر thumbnail - پشتیبانی از URL و فایل محلی
        String thumbnailUrl = video.getThumbnailName();
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            if (thumbnailUrl.startsWith("http://") || thumbnailUrl.startsWith("https://")) {
                // بارگذاری از URL
                Glide.with(context)
                        .load(thumbnailUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(holder.thumbnail);
            } else {
                // بارگذاری از فایل asset
                Glide.with(context)
                        .load(Uri.parse("file:///android_asset/" + thumbnailUrl))
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(holder.thumbnail);
            }
        } else {
            holder.thumbnail.setImageResource(R.drawable.ic_placeholder);
        }

        // آواتار
        String firstChar = video.getUsername().length() > 0 ?
                video.getUsername().substring(0, 1).toUpperCase() : "U";
        holder.avatar.setText(firstChar);
        holder.tvDuration.setText(video.getDuration());

        // اگر این آیتم در حال پخش است، WebView را نشان بده
        if (currentlyPlayingPosition == position) {
            showWebView(holder, video);
        } else {
            hideWebView(holder);
        }

        // دکمه پخش امبد (پخش در همین صفحه)
        holder.btnPlay.setOnClickListener(v -> {
            if (currentlyPlayingPosition == position) {
                stopVideo(holder);
                currentlyPlayingPosition = -1;
            } else {
                if (currentlyPlayingPosition != -1) {
                    notifyItemChanged(currentlyPlayingPosition);
                }
                currentlyPlayingPosition = position;
                mainActivity.showVideoOpenedAdd();
                showWebView(holder, video);
            }
        });

        // دکمه تمام صفحه (باز کردن در VideoPlayerActivity)
        holder.btnOpenFullscreen.setOnClickListener(v -> {
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

        // کلیک روی لایک
        holder.btnLike.setOnClickListener(v -> {
            toggleFavorite(video, holder);
        });

        // کلیک روی آواتار (باز کردن صفحه کانال)
        holder.avatar.setOnClickListener(v -> {
            Toast.makeText(context, "👤 کانال: " + video.getUsername(), Toast.LENGTH_SHORT).show();
        });

        holder.username.setOnClickListener(v -> {
            Toast.makeText(context, "👤 کانال: " + video.getUsername(), Toast.LENGTH_SHORT).show();
        });



        // کلیک روی اشتراک
        holder.btnShare.setOnClickListener(v -> {
            shareVideo(video);
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

        holder.webView.setWebChromeClient(new WebChromeClient());

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
            holder.webView.loadUrl("about:blank");
        }
    }

    private void stopVideo(InstagramViewHolder holder) {
        if (holder.webView != null) {
            holder.webView.loadUrl("about:blank");
        }
        hideWebView(holder);
    }

    private void bindListView(ListViewHolder holder, VideoModel video, boolean isFavorite) {
        holder.title.setText(video.getTitle());
        holder.details.setText("👤 " + video.getUsername() + " · 👁️ " + formatNumber(video.getViews()) + " · ⏱️ " + video.getDuration());

        // لود تصویر thumbnail - پشتیبانی از URL و فایل محلی
        String thumbnailUrl = video.getThumbnailName();
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            if (thumbnailUrl.startsWith("http://") || thumbnailUrl.startsWith("https://")) {
                // بارگذاری از URL
                Glide.with(context)
                        .load(thumbnailUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(holder.thumbnail);
            } else {
                // بارگذاری از فایل asset
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

    private void shareVideo(VideoModel video) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "🎬 " + video.getTitle() + "\n" +
                        "📺 تماشا در آپارات:\n" +
                        "https://www.aparat.com/v/" + video.getVideoKey());
        context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری ویدیو"));
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
        TextView username, tvTime, tvStats, tvCaption, avatar, tvDuration;
        ImageView thumbnail, btnLike, btnComment, btnShare, btnPlay, btnOpenFullscreen;
        WebView webView;
        ProgressBar progressBar;
        FrameLayout videoContainer;

        public InstagramViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.tvUsername);
            tvTime = itemView.findViewById(R.id.tvTime);
            //tvLikes = itemView.findViewById(R.id.tvLikes);
            //tvComments = itemView.findViewById(R.id.tvComments);
            tvStats = itemView.findViewById(R.id.tvStats);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            avatar = itemView.findViewById(R.id.tvAvatar);
            thumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            btnLike = itemView.findViewById(R.id.ivLike);
             btnShare = itemView.findViewById(R.id.ivShare);
            btnPlay = itemView.findViewById(R.id.btnPlayVideo);
            btnOpenFullscreen = itemView.findViewById(R.id.btnOpenFullscreen);
            webView = itemView.findViewById(R.id.webViewPlayer);
            progressBar = itemView.findViewById(R.id.progressBarVideo);
            videoContainer = itemView.findViewById(R.id.videoContainer);
        }
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {
        TextView title, details, tvLikeCount;
        ImageView thumbnail, btnLike;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            details = itemView.findViewById(R.id.tvDetails);
            thumbnail = itemView.findViewById(R.id.ivThumb);
            btnLike = itemView.findViewById(R.id.ivLike);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
        }
    }
}