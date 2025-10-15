package io.dcloud.uniplugin_twitter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSONObject;

public class LoginModule {

    private static final int REQ_CODE = 0x8888;
    private static LoginCallback sCallback;

    public interface LoginCallback {
        void onSuccess(String accessToken, String refreshToken, String idToken);
        void onError(String errorMsg);
    }

    // 启动授权 Activity（宿主 Activity）
    public static void startLogin(Activity activity, String clientId, String redirectUri, LoginCallback callback) {
        sCallback = callback;

        Intent intent = new Intent(activity, TwitterAuthActivity.class);
        intent.putExtra(TwitterAuthActivity.EXTRA_CLIENT_ID, clientId);
        intent.putExtra(TwitterAuthActivity.EXTRA_REDIRECT_URI, redirectUri);
        intent.putExtra(TwitterAuthActivity.EXTRA_SCOPE, "tweet.read users.read offline.access");

        activity.startActivityForResult(intent, REQ_CODE);
    }

    // 宿主 Activity 在 onActivityResult 中转发到这里
    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQ_CODE) return;

        if (resultCode == TwitterAuthActivity.RESULT_OK_TOKEN) {
            if (sCallback != null) {
                String access = data.getStringExtra("access_token");
                String refresh = data.getStringExtra("refresh_token");
                String idToken = data.getStringExtra("id_token");
                sCallback.onSuccess(access, refresh, idToken);
                sCallback = null;
            }
        } else {
            if (sCallback != null) {
                String err = data != null ? data.getStringExtra("error") : "unknown_error";
                sCallback.onError(err);
                sCallback = null;
            }
        }
    }
}
