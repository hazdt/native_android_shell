package com.jr.wannianli.admob;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.Date;

public class AppOpenAdManager implements Application.ActivityLifecycleCallbacks {

    private static final String LOG_TAG = "AppOpenAdManager";

    private final Context context;
    private AppOpenAd appOpenAd = null;
    private long loadTime = 0;
    private boolean isShowingAd = false;

    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/3419835294";

    public AppOpenAdManager(Context context) {
        this.context = context;
        if (context instanceof Application) {
            ((Application) context).registerActivityLifecycleCallbacks(this);
        }
    }

    /** 加载广告 */
    public void loadAd() {
        if (isAdAvailable()) return;

        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(
                context,
                AD_UNIT_ID,
                request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        Log.d(LOG_TAG, "开屏广告加载成功");
                        appOpenAd = ad;
                        loadTime = (new Date()).getTime();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.e(LOG_TAG, "开屏广告加载失败: " + loadAdError.getMessage());
                    }
                });
    }

    /** 展示广告，需要传 Activity */
    public void showAdIfAvailable(Activity activity, final AdCompleteListener listener) {
        if (isShowingAd) return;
        if (!isAdAvailable()) {
            loadAd();
            if (listener != null) listener.onAdComplete();
            return;
        }

        appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                appOpenAd = null;
                isShowingAd = false;
                loadAd(); // 关闭后预加载下一个广告
                if (listener != null) listener.onAdComplete();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                appOpenAd = null;
                isShowingAd = false;
                if (listener != null) listener.onAdComplete();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                isShowingAd = true;
            }
        });

        appOpenAd.show(activity);
    }

    private boolean isAdAvailable() {
        return appOpenAd != null && (new Date().getTime() - loadTime) < 30 * 60 * 1000;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle bundle) { }
    @Override
    public void onActivityStarted(@NonNull Activity activity) { }
    @Override
    public void onActivityResumed(@NonNull Activity activity) { }
    @Override
    public void onActivityPaused(@NonNull Activity activity) { }
    @Override
    public void onActivityStopped(@NonNull Activity activity) { }
    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) { }
    @Override
    public void onActivityDestroyed(@NonNull Activity activity) { }

    public interface AdCompleteListener { void onAdComplete(); }
}
