package com.devinotele.huawei.exampleapp.network;


import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import com.devinotele.huaweidevinosdk.sdk.DevinoLogsCallback;
import com.huawei.agconnect.config.AGConnectServicesConfig;
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
    public void sendPushWithDevino(AGConnectServicesConfig confg, HmsInstanceId hmsInstanceId, Boolean picture, Boolean sound, Boolean deepLink) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String tokenScope = "HCM";
                    String apAppId = confg.getString("client/app_id");
                    String token = hmsInstanceId.getToken(apAppId, tokenScope);

                    if (!TextUtils.isEmpty(token)) return;
                    String message = "Simple push";
                    Log.d("Devino", token);

                    HashMap<String, Object> body = new HashMap<>();

                    body.put("from", 12);
                    body.put("validity", 111);
                    body.put("to", token);
                    body.put("title", "Devino Demo");
                    body.put("badge", 0);
                    body.put("priority", "HIGH");
                    body.put("silentPush", false);

                    HashMap<String, Object> options = new HashMap<>();
                    options.put("icon", "https://avatars.mds.yandex.net/get-pdb/163339/224697a1-db7d-4d02-a12f-aa70383fadc3/s1200");
                    body.put("options", options);

                    HashMap<String, Object> android = new HashMap<>();

                    android.put("action", "devino://default-push-action");
                    android.put("iconColor", "iconColor");
                    android.put("sound", "sound");
                    android.put("androidChannelId", "androidChannelId");
                    android.put("tag", "tag");
                    android.put("collapseKey", "type_a");

                    if (deepLink) {
                        android.put("action", "devino://first");
                        message += " & Button";
                        HashMap<String, Object> button1 = new HashMap<>();
                        button1.put("caption", "ACTION");
                        button1.put("action", "devino://first");
                        android.put("buttons", new HashMap[]{button1});
                    }

                    if (picture) {
                        message += " & Picture";
                        android.put("icon", "https://avatars.mds.yandex.net/get-pdb/163339/224697a1-db7d-4d02-a12f-aa70383fadc3/s1200");
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
