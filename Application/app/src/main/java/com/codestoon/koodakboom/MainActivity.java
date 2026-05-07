package com.codestoon.koodakboom;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adivery.sdk.Adivery;
import com.adivery.sdk.AdiveryBannerAdView;
import com.adivery.sdk.AdiveryListener;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    final String ADDIVERY_APP_ID = "779dbd87-6ba4-4cdd-9868-a3f0018af0f6";
    final String ADDIVERY_REWARD_ID = "32f45500-4ffe-4c60-afdc-f6255ea451e7";
    final String ADDIVERY_APPOPEN_ID = "0ea304f9-6d55-4971-92b0-fb246f28927a";
    final String ADDIVERY_BANNER_ID = "e354955a-a82c-418f-80cb-735ef2ecea85";

    private ImageView playHeroBtn, ivHeroImage, homeSearchBtn;
    private EditText searchInput;
    private TextView toggleGrid, toggleList, tvPlaylistCount, tvHeroTitle, tvHeroDescription, tvListTitle, tvListDescription;
    private RecyclerView rvPlaylists, recyclerView;

    private VideoAdapter videoAdapter;
    private PlaylistAdapter playlistAdapter;
    private LinearLayoutManager layoutManager;

    private List<PlaylistModel> allPlaylists;
    private List<PlaylistModel.VideoItem> currentVideos;
    private PlaylistModel currentPlaylist;
    private String currentView = "instagram";
    private int rewardedCount = 0;
    private boolean isOnHomePage = true;
    private long lastPauseTime = 0L;

    @Override
    protected void onPause() {
        lastPauseTime = System.currentTimeMillis();
        super.onPause();
    }

    @Override
    protected void onResume() {
        long pauseTime = System.currentTimeMillis() - lastPauseTime;
        if (pauseTime > TimeUnit.SECONDS.toMillis(10)) {
            // showOpenAdd();
        }
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        loadPlaylistsFromAssets();
        setupHomePage();
        setupListPage();
        setupAdivery();
        setupBackPressed();

        showHomePage();
    }

    private void initViews() {
        // تصاویر
        playHeroBtn = findViewById(R.id.playHeroBtn);
        ivHeroImage = findViewById(R.id.ivTitlePicture);
        homeSearchBtn = findViewById(R.id.homeSearchBtn);

        // ادیت‌تکست
        searchInput = findViewById(R.id.searchInput);

        // TextViewها
        toggleGrid = findViewById(R.id.toggleGrid);
        toggleList = findViewById(R.id.toggleList);
        tvPlaylistCount = findViewById(R.id.tvPlaylistCount);

        // TextViewهای صفحه اصلی (Hero)
        tvHeroTitle = findViewById(R.id.tvHeroTitle);
        tvHeroDescription = findViewById(R.id.tvHeroDescription);

        // TextViewهای صفحه لیست
        tvListTitle = findViewById(R.id.tvListTitle);
        tvListDescription = findViewById(R.id.tvListDescription);

        // RecyclerViewها
        rvPlaylists = findViewById(R.id.rvPlaylists);
        recyclerView = findViewById(R.id.recyclerView);
    }

    private void loadPlaylistsFromAssets() {
        allPlaylists = PlaylistLoader.loadPlaylistsFromAssets(this);

        if (allPlaylists.isEmpty()) {
            Toast.makeText(this, "هیچ سریالی یافت نشد", Toast.LENGTH_SHORT).show();
            addDemoPlaylists();
        }

        if (tvPlaylistCount != null) {
            tvPlaylistCount.setText(allPlaylists.size() + " مجموعه");
        }
    }

    private void addDemoPlaylists() {
        if (allPlaylists.isEmpty()) {
            PlaylistModel demoPlaylist = new PlaylistModel("بلوپی");
            demoPlaylist.addVideo(new PlaylistModel.VideoItem("بلوپی", "بلوپی - ماجراجویی در پارک", "12:30", "", "j5r68"));
            demoPlaylist.addVideo(new PlaylistModel.VideoItem("بلوپی", "بلوپی - قایم‌موشک", "11:45", "", "j5r69"));
            allPlaylists.add(demoPlaylist);

            PlaylistModel demoPlaylist2 = new PlaylistModel("پپا پیگ");
            demoPlaylist2.addVideo(new PlaylistModel.VideoItem("پپا پیگ", "پپا پیگ - روز بارانی", "10:15", "", "n3qLt"));
            allPlaylists.add(demoPlaylist2);
        }
    }

    private void setupHomePage() {
        if (!allPlaylists.isEmpty()) {
            String thumbUrl = allPlaylists.get(0).getThumbnailUrl();
            if (thumbUrl != null && !thumbUrl.isEmpty()) {
                Glide.with(this)
                        .load(thumbUrl)
                        .placeholder(R.drawable.titleimage)
                        .error(R.drawable.titleimage)
                        .into(ivHeroImage);
            }

            // تنظیم اطلاعات Hero
            if (tvHeroTitle != null) {
                tvHeroTitle.setText(allPlaylists.get(0).getName());
            }
            if (tvHeroDescription != null) {
                tvHeroDescription.setText(allPlaylists.get(0).getVideoCount() + " قسمت");
            }
        }

        playHeroBtn.setOnClickListener(v -> {
            if (!allPlaylists.isEmpty()) {
                openPlaylistVideos(allPlaylists.get(0));
            }
        });

        homeSearchBtn.setOnClickListener(v -> {
            if (!allPlaylists.isEmpty()) {
                openPlaylistVideos(allPlaylists.get(0));
            } else {
                showListPage();
            }
        });

        playlistAdapter = new PlaylistAdapter(this, allPlaylists, playlist -> {
            openPlaylistVideos(playlist);
        });
        rvPlaylists.setLayoutManager(new LinearLayoutManager(this));
        rvPlaylists.setAdapter(playlistAdapter);

        findViewById(R.id.showVideosBtn).setOnClickListener(v -> {
            if (!allPlaylists.isEmpty()) {
                openPlaylistVideos(allPlaylists.get(0));
            }
        });

        findViewById(R.id.commentMainBtn).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CommentActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.otherAppsBtn).setOnClickListener(v -> com.codestoon.koodakboom.StoreIntents.openDeveloperPage(MainActivity.this));
    }

    private void openPlaylistVideos(PlaylistModel playlist) {
        currentPlaylist = playlist;
        currentVideos = new ArrayList<>(playlist.getVideos());

        // تنظیم TextViewهای صفحه لیست
        if (tvListTitle != null) tvListTitle.setText(playlist.getName());
        if (tvListDescription != null) tvListDescription.setText(playlist.getVideoCount() + " قسمت");

        setupVideoRecyclerView();
        showListPage();
    }

    private void setupVideoRecyclerView() {
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        videoAdapter = new VideoAdapter(this, this, convertToVideoModelList(currentVideos), currentView,
                new VideoAdapter.OnVideoClickListener() {
                    @Override
                    public void onVideoClick(String videoKey) {
                        showRewardAdd();
                        openVideoInAparat(videoKey);
                    }

                    @Override
                    public void onVideoFullscreen(String videoKey, String title, String views, String likes,
                                                  String username, String thumbnail, String duration) {
                        showRewardAdd();
                        Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                        intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_KEY, videoKey);
                        intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_TITLE, title);
                        intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_VIEWS, views);
                        intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_LIKES, likes);
                        intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_USERNAME, username);
                        intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_THUMBNAIL, thumbnail);
                        intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_DURATION, duration);
                        startActivity(intent);
                    }
                });

        recyclerView.setAdapter(videoAdapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterVideos(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        toggleGrid.setOnClickListener(v -> {
            currentView = "instagram";
            toggleGrid.setBackgroundResource(R.drawable.toggle_active_bg);
            toggleGrid.setTextColor(Color.WHITE);
            toggleList.setBackgroundResource(R.drawable.toggle_inactive_bg);
            toggleList.setTextColor(Color.BLACK);
            if (videoAdapter != null) videoAdapter.setViewType(currentView);
        });

        toggleList.setOnClickListener(v -> {
            currentView = "list";
            toggleList.setBackgroundResource(R.drawable.toggle_active_bg);
            toggleList.setTextColor(Color.WHITE);
            toggleGrid.setBackgroundResource(R.drawable.toggle_inactive_bg);
            toggleGrid.setTextColor(Color.BLACK);
            if (videoAdapter != null) videoAdapter.setViewType(currentView);
        });
    }

    private List<VideoModel> convertToVideoModelList(List<PlaylistModel.VideoItem> videoItems) {
        List<VideoModel> videoModels = new ArrayList<>();
        for (int i = 0; i < videoItems.size(); i++) {
            PlaylistModel.VideoItem item = videoItems.get(i);
            VideoModel model = new VideoModel(
                    i + 1,
                    item.getVideoKey(),
                    item.getTitle(),
                    "0",
                    item.getDuration(),
                    item.getThumbnailUrl(),  // تغییر: آدرس تصویر از همان آدرس URL استفاده می‌شود
                    currentPlaylist != null ? currentPlaylist.getName() : "کانال کودک",
                    "0",
                    "0"
            );
            videoModels.add(model);
        }
        return videoModels;
    }

    private void filterVideos(String query) {
        if (currentPlaylist == null) return;

        List<PlaylistModel.VideoItem> filtered = new ArrayList<>();
        if (query.isEmpty()) {
            filtered.addAll(currentPlaylist.getVideos());
        } else {
            for (PlaylistModel.VideoItem video : currentPlaylist.getVideos()) {
                if (video.getTitle().contains(query)) {
                    filtered.add(video);
                }
            }
        }

        if (videoAdapter != null) {
            videoAdapter.updateVideos(convertToVideoModelList(filtered));
        }
    }

    private void openVideoInAparat(String videoKey) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_KEY, videoKey);

        if (currentPlaylist != null) {
            for (PlaylistModel.VideoItem video : currentPlaylist.getVideos()) {
                if (video.getVideoKey().equals(videoKey)) {
                    intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_TITLE, video.getTitle());
                    intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_DURATION, video.getDuration());
                    break;
                }
            }
        }
        startActivity(intent);
    }

    private void setupListPage() {
        findViewById(R.id.backToHomeBtn).setOnClickListener(v -> showHomePage());
    }

    private void showHomePage() {
        findViewById(R.id.scrollHome).setVisibility(View.VISIBLE);
        findViewById(R.id.scrollList).setVisibility(View.GONE);
        isOnHomePage = true;
    }

    private void showListPage() {
        findViewById(R.id.scrollHome).setVisibility(View.GONE);
        findViewById(R.id.scrollList).setVisibility(View.VISIBLE);
        isOnHomePage = false;
    }

    private void setupAdivery() {
        Adivery.configure(getApplication(), ADDIVERY_APP_ID);
        loadBannerAdd();
        Adivery.prepareRewardedAd(MainActivity.this, ADDIVERY_REWARD_ID);
        addAdiveryGlobalListener();
    }

    private void loadBannerAdd() {
        AdiveryBannerAdView bannerAd = findViewById(R.id.banner_ad);
        if (bannerAd != null) {
            bannerAd.loadAd(ADDIVERY_BANNER_ID);
        }
    }

    private void addAdiveryGlobalListener() {
        Adivery.addGlobalListener(new AdiveryListener() {
            @Override
            public void onAppOpenAdLoaded(String placementId) {}
            @Override
            public void onInterstitialAdLoaded(String placementId) {}
            @Override
            public void onRewardedAdLoaded(String placementId) {}
            @Override
            public void onRewardedAdClosed(String placementId, boolean isRewarded) {
                if (!isRewarded) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("🎁 پیشنهاد ویژه")
                            .setMessage("اگر سه بار تبلیغ رو تا آخر ببینی، دیگه تبلیغی بهت نشون داده نمیشه! 🙌\n\nلطفاً تبلیغ رو نبند و تا انتها تماشا کن.")
                            .setCancelable(false)
                            .setPositiveButton("👀 باشه، کامل می‌بینم", (dialog, which) -> {})
                            .setNegativeButton("❌ می‌بندمش", null)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();
                } else {
                    rewardedCount++;
                }
            }
            @Override
            public void log(String placementId, String log) {}
        });
    }

    public void showVideoOpenedAdd() {
        if (rewardedCount < 3) {
            showRewardAdd();
        }
    }

    public boolean showRewardAdd() {
       // if (Adivery.isLoaded(ADDIVERY_REWARD_ID)) {
       //     Adivery.showAd(ADDIVERY_REWARD_ID);
       //     return true;
       // } else {
       //     Adivery.prepareRewardedAd(MainActivity.this, ADDIVERY_REWARD_ID);
       //     return false;
       // }
        return true;
    }

    public boolean showOpenAdd() {
        if (Adivery.isLoaded(ADDIVERY_APPOPEN_ID)) {
            Adivery.showAd(ADDIVERY_APPOPEN_ID);
            return true;
        } else {
            Adivery.prepareAppOpenAd(MainActivity.this, ADDIVERY_APPOPEN_ID);
            return false;
        }
    }

    private void setupBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!isOnHomePage) {
                    showHomePage();
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}