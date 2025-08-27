package io.dcloud.uniplugin.admob;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.LoadAdError;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

public class AdMobModule extends UniModule {
    private InterstitialAd mInterstitialAd;

    @UniJSMethod(uiThread = true)
    public void loadInterstitial(String adUnitId, final UniJSCallback callback) {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(mUniSDKInstance.getContext(), adUnitId, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        if (callback != null) callback.invoke("loaded");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        Log.d("AdMob", adError.toString());
                        if (callback != null) callback.invoke("failed");
                    }
                });
    }

    @UniJSMethod(uiThread = true)
    public void showInterstitial() {
        Activity activity = (Activity) mUniSDKInstance.getContext(); // 强转成 Activity
        if (mInterstitialAd != null && activity != null) {
            mInterstitialAd.show(activity);
        }
    }
}
