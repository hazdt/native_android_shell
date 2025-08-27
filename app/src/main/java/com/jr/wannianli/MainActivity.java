package com.jr.wannianli;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.jr.wannianli.admob.AppOpenAdManager;

public class MainActivity extends AppCompatActivity {

    private AppOpenAdManager appOpenAdManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化广告管理器
        appOpenAdManager = new AppOpenAdManager(getApplication());
        appOpenAdManager.loadAd(); // 预加载广告
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 前台展示开屏广告，传入 Activity
        if (appOpenAdManager != null) {
            appOpenAdManager.showAdIfAvailable(this, () -> {
                Log.d("MainActivity", "开屏广告展示完成");
            });
        }
    }
}
