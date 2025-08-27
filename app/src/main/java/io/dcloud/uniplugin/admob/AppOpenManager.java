package io.dcloud.uniplugin.admob;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.Date;

/**
 * 适配 UniApp 的开屏广告管理器
 * 去掉 MainActivity 依赖
 */
public class AppOpenManager implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private static final String LOG_TAG = "AppOpenManager";
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"; // ✅ Google 官方测试ID

    private AppOpenAd appOpenAd = null;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private final Application myApplication;
    private Activity currentActivity;
    private long loadTime = 0;

    public AppOpenManager(Application myApplication) {
        this.myApplication = myApplication;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    /** 加载广告 */
    public void fetchAd() {
        if (isAdAvailable()) {
            return;
        }

        loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd ad) {
                AppOpenManager.this.appOpenAd = ad;
                AppOpenManager.this.loadTime = (new Date()).getTime();
                Log.d(LOG_TAG, "开屏广告加载成功");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d(LOG_TAG, "开屏广告加载失败: " + loadAdError.getMessage());
            }
        };

        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(
                myApplication, AD_UNIT_ID, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback
        );
    }

    /** 判断广告是否可用 */
    private boolean isAdAvailable() {
        return appOpenAd != null && (new Date()).getTime() - loadTime < 4 * 3600 * 1000;
    }

    /** 显示广告 */
    public void showAdIfAvailable() {
        if (!isAdAvailable() || currentActivity == null) {
            fetchAd();
            return;
        }

        appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                Log.d(LOG_TAG, "开屏广告关闭");
                appOpenAd = null;
                fetchAd(); // 再次预加载
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                Log.d(LOG_TAG, "开屏广告展示失败: " + adError.getMessage());
            }

            @Override
            public void onAdShowedFullScreenContent() {
                Log.d(LOG_TAG, "开屏广告展示成功");
            }
        });

        appOpenAd.show(currentActivity);
    }

    /** App 回到前台时触发 */
    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        showAdIfAvailable();
    }

    // ---------------- Activity 生命周期回调 ---------------- //
    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle bundle) {
        currentActivity = activity;
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {}

    @Override
    public void onActivityStopped(@NonNull Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (currentActivity == activity) {
            currentActivity = null;
        }
    }
}
