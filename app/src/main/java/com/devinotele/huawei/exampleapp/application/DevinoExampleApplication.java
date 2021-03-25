package com.devinotele.huawei.exampleapp.application;

import android.app.Application;

import com.devinotele.huawei.exampleapp.BuildConfig;
import com.devinotele.huaweidevinosdk.sdk.DevinoSdk;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.aaid.HmsInstanceId;


public class DevinoExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AGConnectServicesConfig config = AGConnectServicesConfig.fromContext(this);
        HmsInstanceId hmsInstanceId = HmsInstanceId.getInstance(this);
        String appId = BuildConfig.DEVINO_APP_ID;

        DevinoSdk.Builder builder = new DevinoSdk.Builder(this, BuildConfig.DEVINO_API_KEY, appId, hmsInstanceId, config);
        builder.build();


    }
}
