package com.jr.wannianli.admob;

import android.app.Activity;
import android.content.Context;

public class AdmobModule {

    private AppOpenAdManager appOpenAdManager;

    public void init(Context context) {
        if (appOpenAdManager == null) {
            appOpenAdManager = new AppOpenAdManager(context.getApplicationContext());
            appOpenAdManager.loadAd();
        }
    }

    public void showAppOpenAd(Activity activity) {
        if (appOpenAdManager != null) {
            appOpenAdManager.showAdIfAvailable(activity, () -> {
                // 广告关闭后的回调
            });
        }
    }
}
