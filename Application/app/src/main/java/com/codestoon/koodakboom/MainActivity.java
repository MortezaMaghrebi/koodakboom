package com.codestoon.koodakboom;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.adivery.sdk.*;

public class MainActivity extends AppCompatActivity {
    //final String ADDIVERY_APP_ID="f2c3e217-e4a2-4767-9c80-bfe582eec9a9";
    //final String ADDIVERY_REWARD_ID="78ca6832-abfd-4bd0-b6e3-a41bbc5f41ef";
    //final String ADDIVERY_APPOPEN_ID="52311cee-fb86-4f76-9382-8a22c63f7fc5";
    //final String ADDIVERY_BANNER_ID="a176f5bd-61c2-438f-8c42-735791c8395a";

    final String ADDIVERY_APP_ID="779dbd87-6ba4-4cdd-9868-a3f0018af0f6";
    final String ADDIVERY_REWARD_ID="32f45500-4ffe-4c60-afdc-f6255ea451e7";
    final String ADDIVERY_APPOPEN_ID="0ea304f9-6d55-4971-92b0-fb246f28927a";
    final String ADDIVERY_BANNER_ID="e354955a-a82c-418f-80cb-735ef2ecea85";

    // UI Elements
    private LinearLayout pageHome, pageList, showVideosBtn, commentMainBtn, otherAppsBtn, backToHomeBtn;
    private ImageView homeSearchBtn, playHeroBtn,ivTitlePicture;
    private EditText searchInput;
    private TextView toggleGrid, toggleList, tvTitle,tvDescription;
    private RecyclerView recyclerView;
    private VideoAdapter adapter;
    private LinearLayoutManager layoutManager;

    // Data
    private List<VideoModel> allVideos;
    private List<VideoModel> displayedVideos;
    private String currentView = "instagram";
    private long lastPauseTime = 0L; // or System.currentTimeMills();

    @Override
    protected void onPause() {
        lastPauseTime = System.currentTimeMillis();
        super.onPause();
    }

    @Override
    protected void onResume() {
        long pauseTime = System.currentTimeMillis() - lastPauseTime;
        if (pauseTime > TimeUnit.SECONDS.toMillis(10)) {
            //showOpenAdd();
        }
        super.onResume();
    }

    // اطلاعات واقعی ویدیوهای آپارات
    private String[][] aparatVideosData;

    AssetLoader assetLoader;
    boolean isOnHomePage=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assetLoader = new AssetLoader(this);
        aparatVideosData = assetLoader.loadVideosFromAssets();

        initViews();
        setupInitialData();
        setupRecyclerView();
        setupListeners();
        showHomePage();
        setupAddivery();


        // Add callback to handle back button/gesture
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(!isOnHomePage) {
                    showHomePage();
                }else {
                    finish();
                }
            }
        };
        // Register the callback
        getOnBackPressedDispatcher().addCallback(this, callback);


    }


    private void initViews() {
        pageHome = findViewById(R.id.pageHome);
        pageList = findViewById(R.id.pageList);
        homeSearchBtn = findViewById(R.id.homeSearchBtn);
        showVideosBtn = findViewById(R.id.showVideosBtn);
        commentMainBtn = findViewById(R.id.commentMainBtn);
        otherAppsBtn = findViewById(R.id.otherAppsBtn);
        backToHomeBtn = findViewById(R.id.backToHomeBtn);
        playHeroBtn = findViewById(R.id.playHeroBtn);
        searchInput = findViewById(R.id.searchInput);
        toggleGrid = findViewById(R.id.toggleGrid);
        toggleList = findViewById(R.id.toggleList);
        recyclerView = findViewById(R.id.recyclerView);
        ivTitlePicture = findViewById(R.id.ivTitlePicture);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        assetLoader.loadAllContent(ivTitlePicture, tvTitle, tvDescription);
    }

      private void setupInitialData() {
        allVideos = new ArrayList<>();

        // استفاده از داده‌های واقعی آپارات
        for (int i = 0; i < aparatVideosData.length; i++) {
            String[] video = aparatVideosData[i];
            allVideos.add(new VideoModel(
                    i + 1,
                    video[0],  // videoKey
                    video[1],  // title
                    video[2],  // views
                    video[3],  // duration
                    video[4],  // thumbnailName
                    video[5],  // username
                    video[6],  // likes
                    video[7]   // comments
            ));
        }

        displayedVideos = new ArrayList<>(allVideos);
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new VideoAdapter(this,this, displayedVideos, currentView, new VideoAdapter.OnVideoClickListener() {
            @Override
            public void onVideoClick(String videoKey) {
                // برای حالت لیست - باز کردن مستقیم
                showVideoOpenedAdd();
                openVideoInAparat(videoKey);
            }

            @Override
            public void onVideoFullscreen(String videoKey, String title, String views, String likes,
                                          String username, String thumbnail, String duration) {
                // باز کردن VideoPlayerActivity برای حالت اینستاگرام
                showVideoOpenedAdd();
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
        recyclerView.setAdapter(adapter);
    }

    private void openVideoInAparatDirectly(String videoKey) {
        String url = "https://www.aparat.com/v/" + videoKey;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
    private void openVideoInAparat(String videoKey) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_KEY, videoKey);

        // پیدا کردن اطلاعات کامل ویدیو
        for (VideoModel video : allVideos) {
            if (video.getVideoKey().equals(videoKey)) {
                intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_TITLE, video.getTitle());
                intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_VIEWS, video.getViews());
                intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_LIKES, video.getLikes());
                intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_USERNAME, video.getUsername());
                break;
            }
        }
        startActivity(intent);
    }

    private void setupListeners() {
        homeSearchBtn.setOnClickListener(v -> {
            showListPage();
            //showToast("🔍 جستجو در بین ویدیوها");
        });

        showVideosBtn.setOnClickListener(v -> {
            showListPage();
            //showToast("📱 نمایش همه ویدیوها");
        });

        commentMainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,CommentActivity.class);
                startActivity(intent);

            }
        });

        otherAppsBtn.setOnClickListener(v -> com.codestoon.koodakboom.StoreIntents.openDeveloperPage(MainActivity.this));

        playHeroBtn.setOnClickListener(v -> {
            // پخش ویدیوی اول (خبر اصلی)
            //if (!allVideos.isEmpty()) {
                isOnHomePage=false;
                showListPage();
                //openVideoInAparat(allVideos.get(0).getVideoKey());
            //}
        });

        backToHomeBtn.setOnClickListener(v -> showHomePage());

        toggleGrid.setOnClickListener(v -> {
            currentView = "instagram";
            toggleGrid.setBackgroundResource(R.drawable.toggle_active_bg);
            toggleGrid.setTextColor(Color.WHITE);
            toggleList.setBackgroundResource(R.drawable.toggle_inactive_bg);
            toggleList.setTextColor(Color.BLACK);
            adapter.setViewType(currentView);

        });

        toggleList.setOnClickListener(v -> {
            currentView = "list";
            toggleList.setBackgroundResource(R.drawable.toggle_active_bg);
            toggleGrid.setTextColor(Color.BLACK);
            toggleGrid.setBackgroundResource(R.drawable.toggle_inactive_bg);
            toggleList.setTextColor(Color.WHITE);
            adapter.setViewType(currentView);

        });

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
        ivTitlePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showListPage();
            }
        });
    }

    int addRewardedCount=0;
    void setupAddivery()
    {
        Adivery.configure(getApplication(), ADDIVERY_APP_ID);
        LoadBannerAdd();
        Adivery.prepareRewardedAd(MainActivity.this,ADDIVERY_REWARD_ID );
        //Adivery.prepareAppOpenAd(MainActivity.this, ADDIVERY_APPOPEN_ID);
        addAddiveryGlobalListener();

    }
    void LoadBannerAdd(){
        AdiveryBannerAdView bannerAd = findViewById(R.id.banner_ad);
        bannerAd.loadAd(ADDIVERY_BANNER_ID);
    }

    void addAddiveryGlobalListener() {
        Adivery.addGlobalListener(new AdiveryListener() {
            @Override
            public void onAppOpenAdLoaded(String placementId) {
            }

            @Override
            public void onInterstitialAdLoaded(String placementId) {
            }

            @Override
            public void onRewardedAdLoaded(String placementId) {
            }

            @Override
            public void onRewardedAdClosed(String placementId, boolean isRewarded) {
                if (!isRewarded) {
                    // بررسی کنید که آیا کاربر جایزه دریافت می‌کند یا خیر
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("🎁 پیشنهاد ویژه")
                            .setMessage("اگر سه بار تبلیغ رو تا آخر ببینی، دیگه تبلیغی بهت نشون داده نمیشه! 🙌\n\nلطفاً تبلیغ رو نبند و تا انتها تماشا کن.")
                            .setCancelable(false)
                            .setPositiveButton("👀 باشه، کامل می‌بینم", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // شروع نمایش تبلیغ ویدیویی
                                    // showVideoAd();
                                }
                            })
                            .setNegativeButton("❌ می‌بندمش", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // اگه باز هم ببنده، می‌تونی پیام دیگه‌ای نشون بدی
                                    //Toast.makeText(MainActivity.this, "⚠️ با بستن تبلیغ، سه‌بار تموم نمیشه و بازم تبلیغ میاد!", Toast.LENGTH_LONG).show();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();
                }else addRewardedCount++;
            }

            @Override
            public void log(String placementId, String log) {
                // پیغام را چاپ کنید
            }
        });
    }

    public void showVideoOpenedAdd()
    {
        if(addRewardedCount<3){
            showRewardAdd();
        }
    }

    public boolean showRewardAdd() {
         if (Adivery.isLoaded(ADDIVERY_REWARD_ID)) {
            Adivery.showAd(ADDIVERY_REWARD_ID);
            return true;
        }else Adivery.prepareRewardedAd(MainActivity.this, ADDIVERY_REWARD_ID);
        return false;
    }

    public boolean showOpenAdd()
    {
        if (Adivery.isLoaded(ADDIVERY_APPOPEN_ID)) {
            Adivery.showAd(ADDIVERY_APPOPEN_ID);
            return true;
        }else  Adivery.prepareAppOpenAd(MainActivity.this, ADDIVERY_APPOPEN_ID);
        return false;
    }

    private void filterVideos(String query) {
        displayedVideos.clear();
        if (query.isEmpty()) {
            displayedVideos.addAll(allVideos);
        } else {
            for (VideoModel video : allVideos) {
                if (video.getTitle().contains(query) || video.getUsername().contains(query)) {
                    displayedVideos.add(video);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showHomePage() {
        pageHome.setVisibility(View.VISIBLE);
        pageList.setVisibility(View.GONE);
        isOnHomePage=true;
    }

    private void showListPage() {
        pageHome.setVisibility(View.GONE);
        pageList.setVisibility(View.VISIBLE);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        isOnHomePage=false;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, LENGTH_SHORT).show();
    }
}