package com.devinotele.huawei.exampleapp.application;

import android.app.Application;

import com.devinotele.huawei.exampleapp.BuildConfig;
import com.devinotele.huawei.exampleapp.R;
import com.devinotele.huaweidevinosdk.sdk.DevinoSdk;
import com.huawei.agconnect.AGConnectOptions;
import com.huawei.agconnect.AGConnectOptionsBuilder;
import com.huawei.hms.aaid.HmsInstanceId;


public class DevinoExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AGConnectOptions connectOptions = new AGConnectOptionsBuilder().build(this);
        HmsInstanceId hmsInstanceId = HmsInstanceId.getInstance(this);
        String appId = BuildConfig.DEVINO_APP_ID;
        String appVersion = BuildConfig.VERSION_NAME;

        DevinoSdk.Builder builder = new DevinoSdk.Builder(this, BuildConfig.DEVINO_API_KEY, appId, appVersion, hmsInstanceId, connectOptions);
        builder.build();

        DevinoSdk.getInstance().setDefaultNotificationIcon(R.drawable.ic_notify_black);
        DevinoSdk.getInstance().setDefaultNotificationIconColor(0x00FF00);
    }
}
