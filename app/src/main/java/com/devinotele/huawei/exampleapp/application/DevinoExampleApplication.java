package com.devinotele.huawei.exampleapp.application;

import android.app.Application;

import com.devinotele.devinosdk.sdk.DevinoSdk;
import com.devinotele.huawei.exampleapp.BuildConfig;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.aaid.HmsInstanceId;


public class DevinoExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        String agAppId = AGConnectServicesConfig.fromContext(this).getString("client/app_id");
        HmsInstanceId hmsInstanceId = HmsInstanceId.getInstance(this);
        String appId = BuildConfig.DEVINO_APP_ID;

        DevinoSdk.Builder builder = new DevinoSdk.Builder(this, BuildConfig.DEVINO_API_KEY, appId, hmsInstanceId, agAppId);
        builder.build();


    }
}
