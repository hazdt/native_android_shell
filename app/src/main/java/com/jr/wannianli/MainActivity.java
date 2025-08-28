package com.jr.wannianli;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.jr.wannianli.admob.AppOpenAdManager;

public class MainActivity extends Activity {

    private AppOpenAdManager appOpenAdManager;

    View splashMask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        splashMask = findViewById(R.id.splashMask);

        appOpenAdManager = new AppOpenAdManager(this);
        appOpenAdManager.loadAd();
    }

    @Override
    protected void onStart() {
        super.onStart();
        appOpenAdManager.showAdIfAvailable(this, () -> {
            // 广告结束，移除遮罩
            splashMask.setVisibility(View.GONE);
        });
    }

}

