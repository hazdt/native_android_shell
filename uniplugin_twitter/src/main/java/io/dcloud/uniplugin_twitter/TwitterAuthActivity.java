package io.dcloud.uniplugin_twitter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;
import net.openid.appauth.AuthState;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TwitterAuthActivity extends Activity {

    public static final String EXTRA_CLIENT_ID = "extra_client_id";
    public static final String EXTRA_REDIRECT_URI = "extra_redirect_uri";
    public static final String EXTRA_SCOPE = "extra_scope";

    public static final int RESULT_OK_TOKEN = 0x100;
    public static final int RESULT_ERROR = 0x101;

    private AuthorizationService authService;
    private AuthState authState;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String clientId = getIntent().getStringExtra(EXTRA_CLIENT_ID);
        String redirectUri = getIntent().getStringExtra(EXTRA_REDIRECT_URI);
        String scope = getIntent().getStringExtra(EXTRA_SCOPE);
        if (scope == null) scope = "tweet.read users.read offline.access";

        AuthorizationServiceConfiguration serviceConfig =
                new AuthorizationServiceConfiguration(
                        Uri.parse("https://twitter.com/i/oauth2/authorize"),
                        Uri.parse("https://api.twitter.com/2/oauth2/token")
                );

        AuthorizationRequest authRequest = new AuthorizationRequest.Builder(
                serviceConfig,
                clientId,
                ResponseTypeValues.CODE,
                Uri.parse(redirectUri)
        ).setScope(scope)
                // PKCE: AppAuth 会自动生成 code_verifier/challenge
                .build();

        authService = new AuthorizationService(this);

        Intent authIntent = authService.getAuthorizationRequestIntent(authRequest);
        startActivityForResult(authIntent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            AuthorizationResponse resp = AuthorizationResponse.fromIntent(data);
            AuthorizationException ex = AuthorizationException.fromIntent(data);
            if (resp != null) {
                authState = new AuthState(resp, ex);
                // 交换 token（code -> token）
                authService.performTokenRequest(
                        resp.createTokenExchangeRequest(),
                        (tokenResponse, tokenEx) -> {
                            if (tokenResponse != null) {
                                authState.update(tokenResponse, tokenEx);
                                Intent out = new Intent();
                                out.putExtra("access_token", tokenResponse.accessToken);
                                out.putExtra("refresh_token", tokenResponse.refreshToken);
                                out.putExtra("id_token", tokenResponse.idToken);
                                setResult(RESULT_OK_TOKEN, out);
                                finish();
                            } else {
                                Intent out = new Intent();
                                out.putExtra("error", tokenEx != null ? tokenEx.errorDescription : "token_error");
                                setResult(RESULT_ERROR, out);
                                finish();
                            }
                        });
            } else {
                Intent out = new Intent();
                out.putExtra("error", ex != null ? ex.errorDescription : "auth_failed");
                setResult(RESULT_ERROR, out);
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (authService != null) authService.dispose();
        executor.shutdown();
    }
}
