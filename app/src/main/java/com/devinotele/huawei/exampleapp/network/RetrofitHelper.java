package com.devinotele.huawei.exampleapp.network;


import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import com.devinotele.huawei.exampleapp.BuildConfig;
import com.devinotele.huaweidevinosdk.sdk.DevinoLogsCallback;
import com.huawei.agconnect.AGConnectOptions;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class RetrofitHelper {

    private DevinoPushApi devinoPushApi;
    private DevinoLogsCallback callback;

    public RetrofitHelper(DevinoLogsCallback callback) {
        devinoPushApi = RetrofitClientInstance.getRetrofitInstanceForDevinoPush().create(DevinoPushApi.class);
        this.callback = callback;
    }

    @SuppressLint("CheckResult")
    public void sendPushWithDevino(AGConnectOptions confg, HmsInstanceId hmsInstanceId, Boolean picture, Boolean sound, Boolean deepLink) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String tokenScope = "HCM";
                    String apAppId = confg.getString("client/app_id");
                    String token = hmsInstanceId.getToken(apAppId, tokenScope);

                    if (TextUtils.isEmpty(token)) return;
                    String message = "Simple push";
                    Log.d("Devino", token);

                    HashMap<String, Object> body = new HashMap<>();
                    body.put("platform", "huawei");
                    body.put("from", BuildConfig.DEVINO_APP_ID);
                    body.put("validity", 3600);
                    body.put("to", token);
                    body.put("title", "Devino Demo");
                    body.put("badge", 0);
                    body.put("priority", "HIGH");
                    body.put("silentPush", false);

                    HashMap<String, Object> options = new HashMap<>();
                    body.put("options", options);

                    HashMap<String, Object> android = new HashMap<>();

                    android.put("action", "devino://default-push-action");
                    android.put("iconColor", "iconColor");
                    android.put("sound", "sound");
                    android.put("androidChannelId", "androidChannelId");
                    android.put("tag", "tag");
                    android.put("collapseKey", "type_a");


                    if (deepLink) {
                        android.put("action", "devino://first_screen");
                        message += " & Button";
                        HashMap<String, Object> button1 = new HashMap<>();
                        button1.put("caption", "ACTION");
                        button1.put("action", "devino://first_screen");
                        android.put("buttons", new HashMap[]{button1});
                    }

                    if (picture) {
                        message += " & Picture";
                        android.put("image", "https://cdn.ren.tv/cache/960x540/media/img/14/46/144659e6d12aa348c7eae2170d1d6e04f3d2d1da.jpg");
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
                                        System.out.println(object.toString());
                                    },
                                    error -> error.printStackTrace()
                            );
                } catch (ApiException ex) {
                    try {
                        callback.onMessageLogged("Send Push Error" + ex.getMessage());
                    } catch (Throwable error) {
                        error.printStackTrace();
                    }
                }
            }
        }.start();

    }

}
