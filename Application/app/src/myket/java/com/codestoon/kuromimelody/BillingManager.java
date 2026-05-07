package com.codestoon.koodakboom;

import android.util.Log;
import android.content.SharedPreferences;
import android.app.Activity;
import android.content.Context;

import ir.myket.billingclient.IabHelper;
import ir.myket.billingclient.util.IabResult;
import ir.myket.billingclient.util.Inventory;
import ir.myket.billingclient.util.Purchase;

import java.util.ArrayList;
import java.util.List;

public class BillingManager {

    private static final String TAG = "BillingManager";
    private static final String MY_PREFS_NAME = "PREFS_SILENT_SOUND";
    private static final String PREMIUM_KEY = "premium";

    private final Activity activity;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    private IabHelper mHelper;
    private boolean mSetupDone = false;

    private static BillingManager instance;

    public static synchronized BillingManager getInstance(Activity activity) {
        if (instance == null) {
            instance = new BillingManager(activity);
        }
        return instance;
    }

    private BillingManager(Activity activity) {
        this.activity = activity;
        prefs = activity.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void initializeBilling() {
        try {
            Log.d(TAG, "Creating Myket IAB helper.");
            mHelper = new IabHelper(activity, BuildConfig.IAB_PUBLIC_KEY);

            mHelper.enableDebugLogging(BuildConfig.ENABLE_DEBUG_LOGS);

            Log.d(TAG, "Starting Myket setup.");
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    Log.d(TAG, "Myket setup finished: " + result);

                    if (mHelper == null) return;

                    if (!result.isSuccess()) {
                        Log.e(TAG, "❌ Problem setting up Myket billing: " + result);
                        return;
                    }

                    mSetupDone = true;
                    Log.d(TAG, "✅ Myket setup successful. Querying inventory.");
                    queryInventory();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "❌ Error initializing Myket: " + e.getMessage());
        }
    }

    private void queryInventory() {
        if (mHelper == null || !mSetupDone) return;

        try {
            List<String> itemSkus = new ArrayList<>();
            itemSkus.add(PREMIUM_KEY);
            mHelper.queryInventoryAsync(true, itemSkus, mInventoryListener);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error querying inventory: " + e.getMessage());
        }
    }

    IabHelper.QueryInventoryFinishedListener mInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null) return;
            
            if (result.isFailure()) {
                Log.e(TAG, "❌ Failed to query inventory: " + result);
                return;
            }

            Purchase premiumPurchase = inventory.getPurchase(PREMIUM_KEY);
            boolean hasPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            
            if (hasPremium) {
                Log.d(TAG, "✅ Premium purchase found");
                activatePremiumFeatures();
            } else {
                deactivatePremiumIfNeeded();
            }
        }
    };

    IabHelper.OnIabPurchaseFinishedListener mPurchaseListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (mHelper == null) return;
            
            if (result.isFailure()) {
                Log.e(TAG, "❌ Purchase failed: " + result);
                ToastUtils.showSafeToast(activity, "❌ پرداخت انجام نشد");
                return;
            }
            
            if (purchase == null || !verifyDeveloperPayload(purchase) || !purchase.getSku().equals(PREMIUM_KEY)) {
                ToastUtils.showSafeToast(activity, "❌ خطا در تأیید اعتبار خرید");
                return;
            }
            
            Log.d(TAG, "✅ Purchase successful");
            activatePremiumFeatures();
            ToastUtils.showSafeToast(activity, "✅ پرداخت با موفقیت انجام شد");
        }
    };

    public void purchasePremium() {
        if (mHelper == null || !mSetupDone) {
            ToastUtils.showSafeToast(activity, "❌ سرویس پرداخت آماده نیست");
            return;
        }

        try {
            String payload = getOrCreatePayload();
            mHelper.launchPurchaseFlow(activity, PREMIUM_KEY, mPurchaseListener, payload);
        } catch (Exception e) {
            ToastUtils.showSafeToast(activity, "❌ خطا در شروع فرآیند پرداخت");
        }
    }

    private void activatePremiumFeatures() {
        editor.putBoolean("premium_activated", true);
        editor.apply();
        updateAppData();
    }

    private void deactivatePremiumIfNeeded() {
        if (prefs.getBoolean("premium_activated", false)) {
            editor.putBoolean("premium_activated", false);
            editor.apply();
        }
    }

    private void updateAppData() {
        try {
            editor.putInt("download_sound_counter", 2);
            editor.commit();
            // NetController.getInstance(activity).DownloadSoundList();
        } catch (Exception e) {
            Log.e(TAG, "❌ Error updating app data: " + e.getMessage());
        }
    }

    private String getOrCreatePayload() {
        String payload = prefs.getString("myket_payload", "");
        if (payload.isEmpty()) {
            payload = java.util.UUID.randomUUID().toString();
            editor.putString("myket_payload", payload);
            editor.apply();
        }
        return payload;
    }

    private boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        return payload != null && !payload.isEmpty();
    }

    public boolean isPremiumActivated() {
        return prefs.getBoolean("premium_activated", false);
    }

    public boolean isReady() {
        return mHelper != null && mSetupDone;
    }

    public void disconnect() {
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
        mSetupDone = false;
    }




    public void onDestroy() {
        disconnect();
    }
}