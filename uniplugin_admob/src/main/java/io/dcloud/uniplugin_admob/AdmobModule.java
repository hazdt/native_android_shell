package io.dcloud.uniplugin_admob;

import android.app.Activity;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.AdListener;

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import com.google.android.gms.ads.appopen.AppOpenAd;

import org.json.JSONException;
import org.json.JSONObject;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

public class AdmobModule extends UniModule {
    private InterstitialAd mInterstitialAd;
    private RewardedAd mRewardedAd;
    private AdView mBannerAd;
    private AppOpenAd mAppOpenAd;

    // 加载广告
    @UniJSMethod(uiThread = true)
    public void loadAd(String type, String adUnitId, final UniJSCallback callback) {
        Activity activity = (Activity) mUniSDKInstance.getContext();
        AdRequest adRequest = new AdRequest.Builder().build();

        switch (type.toLowerCase()) {
            case "interstitial":
                InterstitialAd.load(activity, adUnitId, adRequest,
                        new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(InterstitialAd ad) {
                                mInterstitialAd = ad;
                                if (callback != null) callback.invoke("interstitial_loaded");
                            }

                            @Override
                            public void onAdFailedToLoad(LoadAdError adError) {
                                Log.d("AdMob", adError.toString());
                                if (callback != null) callback.invoke("interstitial_failed");
                            }
                        });
                break;

            case "rewarded":
                RewardedAd.load(activity, adUnitId, adRequest,
                        new RewardedAdLoadCallback() {
                            @Override
                            public void onAdLoaded(RewardedAd ad) {
                                mRewardedAd = ad;
                                if (callback != null) callback.invoke("rewarded_loaded");
                            }

                            @Override
                            public void onAdFailedToLoad(LoadAdError adError) {
                                Log.d("AdMob", adError.toString());
                                if (callback != null) callback.invoke("rewarded_failed");
                            }
                        });
                break;

            case "banner":
                mBannerAd = new AdView(activity);
                mBannerAd.setAdUnitId(adUnitId);
                mBannerAd.setAdSize(AdSize.BANNER);
                mBannerAd.loadAd(adRequest);
                mBannerAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        if (callback != null) callback.invoke("banner_loaded");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        if (callback != null) callback.invoke("banner_failed");
                    }
                });
                // 将 banner 添加到页面
                FrameLayout decorView = (FrameLayout) activity.getWindow().getDecorView();
                decorView.addView(mBannerAd);
                break;

            case "appopen":
                AppOpenAd.load(activity, adUnitId, adRequest,
                        AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                        new AppOpenAd.AppOpenAdLoadCallback() {
                            @Override
                            public void onAdLoaded(AppOpenAd ad) {
                                mAppOpenAd = ad;
                                if (callback != null) callback.invoke("appopen_loaded");
                            }

                            @Override
                            public void onAdFailedToLoad(LoadAdError adError) {
                                Log.e("AdMob", "AppOpenAd 加载失败: " + adError.getMessage());
                                if (callback != null) callback.invoke("appopen_failed: " + adError.getMessage());
                            }
                        });
                break;
        }
    }

    // 展示广告
    @UniJSMethod(uiThread = true)
    public void showAd(String type, final UniJSCallback callback) {
        Activity activity = (Activity) mUniSDKInstance.getContext();
        switch (type.toLowerCase()) {
            case "interstitial":
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(activity);
                }
                break;
            case "rewarded":
                if (mRewardedAd != null) {
                    mRewardedAd.show(activity, rewardItem -> {
                        Log.d("AdMob", "用户获得奖励: " + rewardItem.getAmount());

                        if (callback != null) callback.invoke(rewardItem);
                    });
                }
                break;
            case "appopen":
                if (mAppOpenAd != null) {
                    JSONObject result = new JSONObject();

                    mAppOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdShowedFullScreenContent() {
                            Log.d("AppOpenAd", "广告已展示");

                            if (callback != null) {
                                try {
                                    result.put("ad_status", "is_show");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                callback.invoke(result);
                            }
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            Log.d("AppOpenAd", "广告展示失败: " + adError.getMessage());
                            mAppOpenAd = null;

                            if (callback != null) {
                                try {
                                    result.put("ad_status", "is_error");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                callback.invoke(result);
                            }
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            Log.d("AppOpenAd", "广告已关闭");
                            mAppOpenAd = null;
                            // 这里可以回调给 uniapp，让前端继续进入首页

                            if (callback != null) {
                                try {
                                    result.put("ad_status", "is_close");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                callback.invoke(result);
                            }
                        }
                    });

                    mAppOpenAd.show(activity);
                }
                break;
            case "banner":
                // Banner 广告是自动展示的，不需要 show
                break;
        }
    }
}
