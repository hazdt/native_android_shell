package io.dcloud.uniplugin_admob;

import android.app.Activity;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.AdError;

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

    @UniJSMethod(uiThread = true)
    public void loadAd(String type, String adUnitId, final UniJSCallback callback) {
        Activity activity = (Activity) mUniSDKInstance.getContext();
        AdRequest adRequest = new AdRequest.Builder().build();

        switch (type.toLowerCase()) {
            case "interstitial":
                InterstitialAd.load(activity, adUnitId, adRequest,
                        new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull InterstitialAd ad) {
                                mInterstitialAd = ad;
                                invokeCallback(callback, "interstitial_loaded", null);
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                Log.d("AdMob", adError.toString());
                                invokeCallback(callback, "interstitial_failed", adError.getMessage());
                            }
                        });
                break;

            case "rewarded":
                RewardedAd.load(activity, adUnitId, adRequest,
                        new RewardedAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull RewardedAd ad) {
                                mRewardedAd = ad;
                                invokeCallback(callback, "rewarded_loaded", null);
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                Log.d("AdMob", adError.toString());
                                invokeCallback(callback, "rewarded_failed", adError.getMessage());
                            }
                        });
                break;

            case "banner":
                mBannerAd = new AdView(activity);
                mBannerAd.setAdUnitId(adUnitId);
                mBannerAd.setAdSize(AdSize.BANNER);
                mBannerAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        invokeCallback(callback, "banner_loaded", null);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        invokeCallback(callback, "banner_failed", adError.getMessage());
                    }
                });

                FrameLayout decorView = (FrameLayout) activity.getWindow().getDecorView();
                decorView.addView(mBannerAd);
                mBannerAd.loadAd(adRequest);
                break;

            case "appopen":
                AppOpenAd.load(activity, adUnitId, adRequest,
                        AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                        new AppOpenAd.AppOpenAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull AppOpenAd ad) {
                                mAppOpenAd = ad;
                                invokeCallback(callback, "appopen_loaded", null);
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                Log.e("AdMob", "AppOpenAd 加载失败: " + adError.getMessage());
                                invokeCallback(callback, "appopen_failed", adError.getMessage());
                            }
                        });
                break;
        }
    }

    @UniJSMethod(uiThread = true)
    public void showAd(String type, final UniJSCallback callback) {
        Activity activity = (Activity) mUniSDKInstance.getContext();

        switch (type.toLowerCase()) {
            case "interstitial":
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(activity);
                    invokeCallback(callback, "interstitial_shown", null);
                } else {
                    invokeCallback(callback, "interstitial_not_ready", null);
                }
                break;

            case "rewarded":
                if (mRewardedAd != null) {
                    mRewardedAd.show(activity, rewardItem -> {
                        JSONObject result = new JSONObject();
                        try {
                            result.put("amount", rewardItem.getAmount());
                            result.put("type", rewardItem.getType());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        invokeCallback(callback, "rewarded_shown", result);
                    });
                } else {
                    invokeCallback(callback, "rewarded_not_ready", null);
                }
                break;

            case "appopen":
                if (mAppOpenAd != null) {
                    mAppOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdShowedFullScreenContent() {
                            invokeCallback(callback, "appopen_shown", null);
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            invokeCallback(callback, "appopen_failed", adError.getMessage());
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            mAppOpenAd = null;
                            invokeCallback(callback, "appopen_dismissed", null);
                        }
                    });
                    mAppOpenAd.show(activity);
                } else {
                    invokeCallback(callback, "appopen_not_ready", null);
                }
                break;

            case "banner":
                // Banner 自动展示
                break;
        }
    }

    // 统一回调方法，保证 JSON 返回、线程安全
    private void invokeCallback(UniJSCallback callback, String status, Object data) {
        if (callback == null) return;

        Activity activity = (Activity) mUniSDKInstance.getContext();
        activity.runOnUiThread(() -> {
            try {
                JSONObject res = new JSONObject();
                res.put("status", status);
                if (data != null) {
                    if (data instanceof String) res.put("message", data);
                    else if (data instanceof JSONObject) res.put("data", data);
                }
                callback.invoke(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }
}
