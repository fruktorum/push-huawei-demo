package com.devinotele.huawei.exampleapp.network;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.devinotele.huawei.exampleapp.BuildConfig;
import com.devinotele.huawei.exampleapp.R;
import com.devinotele.huaweidevinosdk.sdk.DevinoLogsCallback;
import com.devinotele.huaweidevinosdk.sdk.DevinoSdk;
import com.huawei.agconnect.AGConnectOptions;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RetrofitHelper {

    private final DevinoPushApi devinoPushApi;
    private final DevinoLogsCallback callback;

    public RetrofitHelper(DevinoLogsCallback callback) {
        devinoPushApi = RetrofitClientInstance
                .getRetrofitInstanceForDevinoPush()
                .create(DevinoPushApi.class);
        this.callback = callback;
    }

    @SuppressLint("CheckResult")
    public void sendPushWithDevino(
            AGConnectOptions confg,
            HmsInstanceId hmsInstanceId,
            Boolean isPicture,
            Boolean isSound,
            Boolean isDeepLink,
            Boolean isAction,
            Context context
    ) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String tokenScope = "HCM";
                    String apAppId = confg.getString("client/app_id");
                    Log.d("DevinoPush", "apAppId = " + apAppId);

                    String token = hmsInstanceId.getToken(apAppId, tokenScope);
                    Log.d("DevinoPush", "token = " + token);

                    if (TextUtils.isEmpty(token)) return;
                    String message = "Simple push";
                    Log.d("DevinoPush", token);

                    HashMap<String, Object> body = new HashMap<>();
                    body.put("platform", "huawei");
                    body.put("from", BuildConfig.DEVINO_APP_ID);
                    body.put("validity", 3600);
                    body.put("to", token);
                    body.put("title", "Devino Demo");
                    body.put("badge", 0);
                    body.put("priority", "HIGH");
                    body.put("silentPush", false);

                    HashMap<String, Object> customData = new HashMap<>();
                    customData.put("login", "loginValue");
                    body.put("customData", customData);

                    HashMap<String, Object> android = new HashMap<>();

                    if (isAction) {
                        android.put("action", "devino://first_screen");
                    }

                    android.put("androidChannelId", "androidChannelId");
                    android.put("tag", "tag");
                    android.put("collapseKey", "type_a");
                    android.put("iconColor", "#0000FF");
                    android.put("smallIcon", "ic_baseline_android_24");

                    if (isDeepLink) {
                        message += " & Button";
                        HashMap<String, Object> button1 = new HashMap<>();
                        button1.put("caption", "ACTION");
                        button1.put("action", "devino://first_screen");
                        android.put("buttons", new HashMap[]{button1});
                    }

                    if (isPicture) {
                        message += " & Picture";
                        android.put(
                                "image",
                                "https://cdn-icons-png.flaticon.com/512/226/226770.png"
                        );
                    }

                    if (isSound) {
                        String sound = ContentResolver.SCHEME_ANDROID_RESOURCE
                                + "://"
                                + context.getPackageName()
                                + "/" + R.raw.push_sound;
                        Log.d("DevinoPush", "sound = " + sound);
                        android.put("sound", sound);
                        // or use method setCustomSound(sound):
                        DevinoSdk.getInstance().setCustomSound(Uri.parse(sound));
                    } else {
                        DevinoSdk.getInstance().useDefaultSound();
                    }

                    body.put("android", android);
                    body.put("text", message);

                    HashMap<String, Object>[] arr = new HashMap[1];
                    arr[0] = body;
                    devinoPushApi.sendPush(arr)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    object -> {
                                        callback.onMessageLogged(object.toString());
                                        System.out.println(object);
                                    },
                                    Throwable::printStackTrace
                            );
                } catch (ApiException ex) {
                    try {
                        callback.onMessageLogged("Demo Send Push Error: " + ex.getMessage());
                        Log.d("DevinoPush", "Demo Send Push Error: " + ex.getMessage());
                    } catch (Throwable error) {
                        error.printStackTrace();
                    }
                }
            }
        }.start();
    }
}