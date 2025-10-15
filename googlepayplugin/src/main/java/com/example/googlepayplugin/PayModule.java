package com.example.googlepayplugin;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.fastjson.JSONObject;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.button.ButtonOptions;
import com.google.android.gms.wallet.contract.TaskResultContracts.GetPaymentDataResult;
import com.google.android.gms.wallet.button.PayButton;
import com.example.googlepayplugin.viewmodel.CheckoutViewModel; // 你可以直接复制这个ViewModel类
import com.example.googlepayplugin.util.PaymentsUtil; // 同理可复用
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;

import java.util.Locale;

import io.dcloud.feature.uniapp.common.UniModule;

/**
 * UniApp Google Pay 插件模块
 */
public class PayModule extends UniModule {

    private CheckoutViewModel model;
    private ActivityResultLauncher<Task<PaymentData>> paymentDataLauncher;
    private JSCallback mCallback;

    @JSMethod(uiThread = true)
    public void pay(JSONObject params, JSCallback callback) {
        this.mCallback = callback;
        Activity activity = mWXSDKInstance.getContext() instanceof Activity ? (Activity) mWXSDKInstance.getContext() : null;

        if (activity == null) {
            if (callback != null) callback.invoke(buildResult("fail", "Activity not found"));
            return;
        }

        // 初始化 ViewModel
        model = new ViewModelProvider((androidx.fragment.app.FragmentActivity) activity).get(CheckoutViewModel.class);

        // 注册支付回调
        paymentDataLauncher = ((androidx.fragment.app.FragmentActivity) activity)
                .registerForActivityResult(new GetPaymentDataResult(), result -> {
                    int statusCode = result.getStatus().getStatusCode();
                    switch (statusCode) {
                        case CommonStatusCodes.SUCCESS:
                            handlePaymentSuccess(result.getResult());
                            break;
                        case CommonStatusCodes.CANCELED:
                            if (mCallback != null)
                                mCallback.invoke(buildResult("cancel", "User canceled"));
                            break;
                        default:
                            handleError(statusCode, result.getStatus().getStatusMessage());
                            break;
                    }
                });

        // 发起支付请求
        String price = params.getString("amount");
        if (price == null) price = "1.00";

        final Task<PaymentData> task = model.getLoadPaymentDataTask(price);
        task.addOnCompleteListener(paymentDataLauncher::launch);
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        try {
            String paymentInfo = paymentData.toJson();
            JSONObject paymentJson = JSONObject.parseObject(paymentInfo);
            JSONObject paymentMethodData = paymentJson.getJSONObject("paymentMethodData");

            String token = paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token");

            JSONObject result = new JSONObject();
            result.put("status", "success");
            result.put("token", token);
            result.put("data", paymentJson);

            if (mCallback != null) mCallback.invoke(result);
        } catch (Exception e) {
            Log.e("GooglePay", "Payment parse error: " + e.getMessage());
            if (mCallback != null)
                mCallback.invoke(buildResult("fail", e.getMessage()));
        }
    }

    private void handleError(int code, @Nullable String message) {
        Log.e("GooglePay", "Error code: " + code + ", message: " + message);
        if (mCallback != null)
            mCallback.invoke(buildResult("fail", message));
    }

    private JSONObject buildResult(String status, String msg) {
        JSONObject res = new JSONObject();
        res.put("status", status);
        res.put("message", msg);
        return res;
    }
}
