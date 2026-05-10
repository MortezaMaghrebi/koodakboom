package com.codestoon.koodakboom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import android.view.ViewGroup;

public class VideoPlayerActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEO_KEY = "video_key";
    public static final String EXTRA_VIDEO_TITLE = "video_title";
    public static final String EXTRA_VIDEO_VIEWS = "video_views";
    public static final String EXTRA_VIDEO_LIKES = "video_likes";
    public static final String EXTRA_VIDEO_USERNAME = "video_username";
    public static final String EXTRA_VIDEO_THUMBNAIL = "video_thumbnail";
    public static final String EXTRA_VIDEO_DURATION = "video_duration";
    public static final String EXTRA_PLAYLIST_NAME = "playlist_name";

    private WebView webView;
    private TextView tvTitle, tvUsername, tvViews, tvLikes, tvLoadingText;
    private ImageView btnBack, btnRefresh, ivAvatar;
    private TextView btnShare, btnLike, btnDownload;
    private ProgressBar progressBar;
    private FrameLayout fullscreenContainer;
    private CardView headerCard, infoCard, commentsCard;
    private RecyclerView commentsRecyclerView;
    private EditText etNewComment;
    private Button btnSendComment;
    private TextView tvNoComments;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    private DataManager dataManager;
    private CommentsAdapter commentsAdapter;
    private WatchHistoryManager historyManager;
    private String videoKey, videoTitle, videoViews, videoLikes, videoUsername, videoThumbnail, videoDuration;
    private String playlistName;
    private boolean isFavorite = false;
    private boolean isFullscreen = false;
    private int originalOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        dataManager = new DataManager(this);
        historyManager = new WatchHistoryManager(this);
        initViews();
        getIntentData();
        setupListeners();
        setupWebView();
        loadVideo();
        showVideoInfo();
        checkFavoriteStatus();
        loadComments();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isFullscreen && customView != null) {
                    exitFullscreen();
                } else if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    VideoPlayerActivity.this.finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void initViews() {
        webView = findViewById(R.id.webView);
        tvTitle = findViewById(R.id.tvVideoTitle);
        tvUsername = findViewById(R.id.tvUsername);
        tvViews = findViewById(R.id.tvViews);
        tvLikes = findViewById(R.id.tvLikes);
        tvLoadingText = findViewById(R.id.tvLoadingText);
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnRefresh = findViewById(R.id.btnRefresh);
        ivAvatar = findViewById(R.id.ivAvatar);
        progressBar = findViewById(R.id.progressBar);
        fullscreenContainer = findViewById(R.id.fullscreenContainer);
        headerCard = findViewById(R.id.headerCard);
        infoCard = findViewById(R.id.infoCard);
        commentsCard = findViewById(R.id.commentsCard);

        LinearLayout actionButtonsLayout = findViewById(R.id.actionButtonsLayout);
        if (actionButtonsLayout != null) {
            for (int i = 0; i < actionButtonsLayout.getChildCount(); i++) {
                View child = actionButtonsLayout.getChildAt(i);
                if (child instanceof TextView) {
                    TextView textView = (TextView) child;
                    String buttonText = textView.getText().toString();
                    if (buttonText.equals("اشتراک")) {
                        btnShare = textView;
                    } else if (buttonText.equals("پسندیدن")) {
                        btnLike = textView;
                    } else if (buttonText.equals("دانلود")) {
                        btnDownload = textView;
                    }
                }
            }
        }

        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        etNewComment = findViewById(R.id.etNewComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        tvNoComments = findViewById(R.id.tvNoComments);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getIntentData() {
        videoKey = getIntent().getStringExtra(EXTRA_VIDEO_KEY);
        videoTitle = getIntent().getStringExtra(EXTRA_VIDEO_TITLE);
        videoViews = getIntent().getStringExtra(EXTRA_VIDEO_VIEWS);
        videoLikes = getIntent().getStringExtra(EXTRA_VIDEO_LIKES);
        videoUsername = getIntent().getStringExtra(EXTRA_VIDEO_USERNAME);
        videoThumbnail = getIntent().getStringExtra(EXTRA_VIDEO_THUMBNAIL);
        videoDuration = getIntent().getStringExtra(EXTRA_VIDEO_DURATION);
        playlistName = getIntent().getStringExtra(EXTRA_PLAYLIST_NAME);

        if (videoUsername == null) videoUsername = "کانال کودک";
        if (videoViews == null) videoViews = "0";
        if (videoLikes == null) videoLikes = "0";
        if (playlistName == null) playlistName = "";
    }

    private void showVideoInfo() {
        if (videoTitle != null) tvTitle.setText(videoTitle);
        if (videoUsername != null) tvUsername.setText(videoUsername);
        if (videoViews != null) tvViews.setText("👁️ " + videoViews);
        if (videoLikes != null) tvLikes.setText("❤️ " + videoLikes);

        if (videoUsername != null && !videoUsername.isEmpty()) {
            ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }
    }

    private void checkFavoriteStatus() {
        isFavorite = dataManager.isFavorite(videoKey);
        updateLikeButton();
    }

    private void updateLikeButton() {
        if (btnLike != null) {
            if (isFavorite) {
                btnLike.setText("پسندیده");
                btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_filled, 0, 0, 0);
                btnLike.setTextColor(getColor(R.color.liked_color));
            } else {
                btnLike.setText("پسندیدن");
                btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_outline_white, 0, 0, 0);
                btnLike.setTextColor(getColor(android.R.color.white));
            }
        }
    }

    private void loadComments() {
        List<CommentModel> comments = dataManager.getCommentsForVideo(videoKey);
        commentsAdapter = new CommentsAdapter(comments);
        commentsRecyclerView.setAdapter(commentsAdapter);

        if (comments.isEmpty()) {
            tvNoComments.setVisibility(View.VISIBLE);
            commentsRecyclerView.setVisibility(View.GONE);
        } else {
            tvNoComments.setVisibility(View.GONE);
            commentsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnShare.setOnClickListener(v -> shareVideo());
        btnRefresh.setOnClickListener(v -> {
            webView.reload();
            progressBar.setVisibility(View.VISIBLE);
            if (tvLoadingText != null) tvLoadingText.setVisibility(View.VISIBLE);
        });

        if (btnLike != null) {
            btnLike.setOnClickListener(v -> toggleFavorite());
        }

        if (btnDownload != null) {
            btnDownload.setOnClickListener(v -> downloadVideo());
        }

        if (btnSendComment != null) {
            btnSendComment.setOnClickListener(v -> addComment());
        }
    }

    private void shareVideo() {
        String shareText = "🎬 " + videoTitle + "\n" +
                "📺 تماشا در آپارات:\n" +
                "https://www.aparat.com/v/" + videoKey;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری ویدیو"));
    }

    private void toggleFavorite() {
        if (isFavorite) {
            dataManager.removeFromFavorites(videoKey);
            Toast.makeText(this, "❌ از علاقه‌مندی‌ها حذف شد", Toast.LENGTH_SHORT).show();
        } else {
            FavoriteModel favorite = new FavoriteModel(
                    videoKey, videoTitle, videoThumbnail,
                    videoUsername, videoViews, videoDuration
            );
            dataManager.addToFavorites(favorite);
            Toast.makeText(this, "❤️ به علاقه‌مندی‌ها اضافه شد", Toast.LENGTH_SHORT).show();
        }
        isFavorite = !isFavorite;
        updateLikeButton();
    }

    private void addComment() {
        if (etNewComment == null) return;
        String comment = etNewComment.getText().toString().trim();
        if (comment.isEmpty()) {
            Toast.makeText(this, "لطفاً نظر خود را بنویسید", Toast.LENGTH_SHORT).show();
            return;
        }
        String username ="شما";// videoUsername != null ? videoUsername : "کاربر";
        dataManager.addComment(videoKey, comment, username);
        etNewComment.setText("");
        loadComments();
        Toast.makeText(this, "💬 نظر شما ثبت شد", Toast.LENGTH_SHORT).show();
        if (commentsRecyclerView != null) {
            commentsRecyclerView.smoothScrollToPosition(0);
        }
    }

    private void downloadVideo() {
        showDownloadConfirmDialog();
    }

    private void showDownloadConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("دانلود ویدیو")
                .setMessage("آیا می‌خواهید این ویدیو را در آپارات مشاهده و دانلود کنید؟\n" + videoTitle)
                .setPositiveButton("باز کردن آپارات", (dialog, which) -> openInBrowser())
                .setNegativeButton("انصراف", null)
                .show();
    }

    private void openInBrowser() {
        String url = "https://www.aparat.com/v/" + videoKey;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void markVideoAsWatched() {
        historyManager.markVideoAsWatched(videoKey);
        if (playlistName != null && !playlistName.isEmpty()) {
            historyManager.markEpisodeAsWatched(playlistName, videoKey);

            // ذخیره آخرین سریال تماشا شده در SharedPreferences
            SharedPreferences prefs = getSharedPreferences("KoodakBoomPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("last_watched_playlist", playlistName);
            editor.putString("last_watched_video_key", videoKey);
            editor.apply();
        }
    }

    private void enterFullscreen() {
        isFullscreen = true;
        // مخفی کردن هدر و اطلاعات
        if (headerCard != null) headerCard.setVisibility(View.GONE);
        if (infoCard != null) infoCard.setVisibility(View.GONE);
        if (commentsCard != null) commentsCard.setVisibility(View.GONE);
        // مخفی کردن نوار وضعیت
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // تغییر جهت صفحه به Landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    private void exitFullscreen() {
        isFullscreen = false;
        // نمایش مجدد هدر و اطلاعات
        if (headerCard != null) headerCard.setVisibility(View.VISIBLE);
        if (infoCard != null) infoCard.setVisibility(View.VISIBLE);
        if (commentsCard != null) commentsCard.setVisibility(View.VISIBLE);
        // نمایش مجدد نوار وضعیت
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // برگرداندن جهت صفحه به Portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // حذف customView از fullscreenContainer
        if (fullscreenContainer != null && customView != null) {
            fullscreenContainer.removeView(customView);
            fullscreenContainer.setVisibility(View.GONE);
        }

        if (customViewCallback != null) {
            customViewCallback.onCustomViewHidden();
        }
        customView = null;
        customViewCallback = null;
        webView.setVisibility(View.VISIBLE);
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (tvLoadingText != null) tvLoadingText.setVisibility(View.GONE);
                markVideoAsWatched();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (tvLoadingText != null) {
                    tvLoadingText.setText("❌ خطا در بارگذاری\n" + description);
                    tvLoadingText.setVisibility(View.VISIBLE);
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }

                customView = view;
                customViewCallback = callback;

                // اضافه کردن view به fullscreenContainer
                if (fullscreenContainer != null) {
                    fullscreenContainer.setVisibility(View.VISIBLE);
                    fullscreenContainer.addView(customView, new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    ));
                }

                webView.setVisibility(View.GONE);

                // ورود به حالت تمام صفحه
                enterFullscreen();
            }

            @Override
            public void onHideCustomView() {
                if (customView == null) return;

                // خروج از حالت تمام صفحه
                exitFullscreen();

                webView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadVideo() {
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover\">\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "        body { background-color: #000000; overflow: hidden; }\n" +
                "        .video-container { position: relative; width: 100%; height: 100vh; display: flex; align-items: center; justify-content: center; background: #000; }\n" +
                "        .video-wrapper { position: relative; width: 100%; height: 100%; }\n" +
                "        iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: none; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"video-container\">\n" +
                "        <div class=\"video-wrapper\">\n" +
                "            <iframe src=\"https://www.aparat.com/video/video/embed/videohash/" + videoKey + "/vt/frame\"\n" +
                "                    allowFullScreen=\"true\"\n" +
                "                    webkitallowfullscreen=\"true\"\n" +
                "                    mozallowfullscreen=\"true\"\n" +
                "                    allow=\"autoplay; fullscreen\">\n" +
                "            </iframe>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";

        webView.loadDataWithBaseURL("https://www.aparat.com/", html, "text/html", "UTF-8", null);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.destroy();
        }
        super.onDestroy();
    }
}