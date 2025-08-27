package io.dcloud.uniplugin;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

import io.dcloud.uniplugin.admob.AppOpenManager;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppOpenManager appOpenManager = new AppOpenManager(this);
    }
}

