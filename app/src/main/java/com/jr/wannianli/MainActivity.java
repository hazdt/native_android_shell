package com.jr.wannianli;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.jr.wannianli.admob.AppOpenAdManager;

public class MainActivity extends Activity {

    private AppOpenAdManager appOpenAdManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(this, initializationStatus -> {});
        appOpenAdManager = new AppOpenAdManager(this);
        appOpenAdManager.loadAd(); // 预加载广告
    }

    @Override
    protected void onStart() {
        super.onStart();
        appOpenAdManager.showAdIfAvailable(this); // 有广告就展示
    }
}

