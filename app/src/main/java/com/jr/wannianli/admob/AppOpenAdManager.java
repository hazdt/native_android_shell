package com.jr.wannianli.admob;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.appopen.AppOpenAd;

public class AppOpenAdManager {

    private static final String TAG = "AppOpenAdManager";
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"; // 测试ID

    private AppOpenAd mAppOpenAd;
    private boolean isLoadingAd = false;
    private boolean isShowingAd = false;

    private final Context context;

    public AppOpenAdManager(Context context) {
        this.context = context.getApplicationContext();
    }

    // 预加载广告
    public void loadAd() {
        if (isLoadingAd || mAppOpenAd != null) return;

        isLoadingAd = true;
        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(context, AD_UNIT_ID, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        mAppOpenAd = ad;
                        isLoadingAd = false;
                        Log.d(TAG, "广告加载成功");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull com.google.android.gms.ads.LoadAdError loadAdError) {
                        isLoadingAd = false;
                        mAppOpenAd = null;
                        Log.e(TAG, "广告加载失败: " + loadAdError.getMessage());
                    }
                });
    }

    // 展示广告
    public void showAdIfAvailable(@NonNull Activity activity) {
        if (isShowingAd) return;

        if (mAppOpenAd == null) {
            Log.d(TAG, "广告还没准备好，重新加载...");
            loadAd();
            return;
        }

        mAppOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                isShowingAd = true;
                Log.d(TAG, "广告已展示");
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                isShowingAd = false;
                mAppOpenAd = null;
                Log.e(TAG, "广告展示失败: " + adError.getMessage());
                loadAd();
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                isShowingAd = false;
                mAppOpenAd = null;
                Log.d(TAG, "广告已关闭");
                loadAd(); // 展示完自动预加载下一条
            }
        });

        mAppOpenAd.show(activity);
    }
}

