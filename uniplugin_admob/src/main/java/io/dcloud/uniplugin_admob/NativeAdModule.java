package io.dcloud.uniplugin_admob;

import android.app.Activity;
import android.util.Log;
import android.widget.FrameLayout;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

import org.json.JSONObject;
import org.json.JSONException;

public class NativeAdModule extends UniModule {

    @UniJSMethod(uiThread = true)
    public void showNativeAd(String adUnitId, UniJSCallback callback) {
        Activity activity = mUniSDKInstance.getContext() instanceof Activity ?
                (Activity) mUniSDKInstance.getContext() : null;

        if (activity == null) {
            if (callback != null) callback.invoke("Activity null");
            return;
        }

        MobileAds.initialize(activity, initializationStatus -> {});

        int containerId = activity.getResources().getIdentifier(
                "native_ad_container", "id", activity.getPackageName()
        );
        FrameLayout container = null;
        if (containerId != 0) container = activity.findViewById(containerId);

        if (container == null) {
            container = new FrameLayout(activity);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            container.setLayoutParams(params);
            activity.addContentView(container, params);
        }

        FrameLayout finalContainer = container;

        AdLoader adLoader = new AdLoader.Builder(activity, adUnitId)
                .forNativeAd(nativeAd -> {

                    NativeAdView adView = new NativeAdView(activity);
                    adView.setLayoutParams(new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));

                    // headline
                    TextView headlineView = new TextView(activity);
                    headlineView.setText(nativeAd.getHeadline());
                    adView.addView(headlineView);
                    adView.setHeadlineView(headlineView);

                    // body
                    if (nativeAd.getBody() != null) {
                        TextView bodyView = new TextView(activity);
                        bodyView.setText(nativeAd.getBody());
                        adView.addView(bodyView);
                        adView.setBodyView(bodyView);
                    }

                    // call to action
                    if (nativeAd.getCallToAction() != null) {
                        Button ctaButton = new Button(activity);
                        ctaButton.setText(nativeAd.getCallToAction());
                        adView.addView(ctaButton);
                        adView.setCallToActionView(ctaButton);
                    }

                    // icon
                    if (nativeAd.getIcon() != null) {
                        ImageView iconView = new ImageView(activity);
                        iconView.setImageDrawable(nativeAd.getIcon().getDrawable());
                        adView.addView(iconView);
                        adView.setIconView(iconView);
                    }

                    // media view
                    MediaView mediaView = new MediaView(activity);
                    adView.addView(mediaView);
                    adView.setMediaView(mediaView);

                    adView.setNativeAd(nativeAd);

                    // 替换容器内容
                    finalContainer.removeAllViews();
                    finalContainer.addView(adView);

                    // 返回广告实际高度给 JS
                    int adHeight = adView.getMeasuredHeight();
                    JSONObject result = new JSONObject();
                    try {
                        result.put("height", adHeight);
                        Log.d("height:", String.valueOf(adHeight));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (callback != null) callback.invoke(result);

                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }
}
