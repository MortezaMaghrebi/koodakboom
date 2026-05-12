package com.codestoon.koodakboom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;

public class ReviewPromptManager {
    private static final String PREF_NAME = "review_prefs";
    private static final String KEY_VIDEO_COUNT = "video_count";
    private static final String KEY_PROMPT_SHOWN = "prompt_shown";
    private static final int VIDEOS_NEEDED = 3;

    private Context context;
    private SharedPreferences prefs;

    public ReviewPromptManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void incrementAndCheck() {
        if (hasPromptBeenShown()) return;

        int count = prefs.getInt(KEY_VIDEO_COUNT, 0) + 1;
        prefs.edit().putInt(KEY_VIDEO_COUNT, count).apply();

        if (count >= VIDEOS_NEEDED) {
            showReviewDialog();
        }
    }

    private void showReviewDialog() {
        new AlertDialog.Builder(context)
                .setTitle("🌸 نظر شما برای ما مهم است")
                .setMessage("آیا کودک شما از تماشای کارتون‌های کودک بوم لذت برد؟\n\nبا یک امتیاز ۵ ستاره به ما انرژی بدید تا انیمیشن‌های بیشتری اضافه کنیم!")
                .setPositiveButton("👍 بله، عالی بود", (dialog, which) -> {
                    prefs.edit().putBoolean(KEY_PROMPT_SHOWN, true).apply();
                    StoreIntents.openStoreForComment(context);
                })
                .setNegativeButton("😐 بعداً", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("💡 پیشنهاد دارم", (dialog, which) -> {
                    Intent intent = new Intent(context, CommentNegativeActivity.class);
                    context.startActivity(intent);
                    prefs.edit().putBoolean(KEY_PROMPT_SHOWN, true).apply();
                })
                .setCancelable(true)
                .show();
    }

    private boolean hasPromptBeenShown() {
        return prefs.getBoolean(KEY_PROMPT_SHOWN, false);
    }
}