package com.devinotele.huawei.exampleapp;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.devinotele.huaweidevinosdk.sdk.DevinoLogsCallback;
import com.devinotele.huaweidevinosdk.sdk.DevinoSdk;

import java.util.Arrays;

import io.reactivex.subjects.ReplaySubject;

public class MainActivity extends AppCompatActivity implements MainActivityCallback {

    NavController navController;
    NavHostFragment navHostFragment;
    private DevinoLogsCallback logsCallback;
    public String logs = "";
    public ReplaySubject<String> logsRx = ReplaySubject.create();
    private final int REQUEST_CODE_FOREGROUND_GEO = 13;
    private final int REQUEST_CODE_BACKGROUND_GEO = 12;
    private final int REQUEST_CODE_NOTIFICATION = 14;
    private final int REQUEST_CODE_NOTIFICATION_AND_GEO = 15;
    private final int REQUEST_CODE_SEND_GEO = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        DevinoSdk.getInstance().requestLogs(getLogsCallback());
        DevinoSdk.getInstance().appStarted();
        DevinoSdk.getInstance().activateSubscription(true);

        checkPermission();

        if (savedInstanceState == null) {
            Log.d(getString(R.string.tag), "intent.data = " + getIntent().getData());
            if (getIntent().getData() != null) {
                if (getIntent().getData().toString()
                        .equals(getString(R.string.devino_default_deeplink))
                ) {
                    navController.navigate(R.id.homeFragment);
                }
            }
        }
    }

    private void createLogsCallback() {
        logsCallback = message -> runOnUiThread(
                () -> {
                    logs = "\n" + message.replaceAll("\"", "\'") + "\n";
                    logsRx.onNext(logs);
                    Log.d(getString(R.string.logs_tag), logs);
                }
        );
    }

    @Override
    protected void onDestroy() {
        DevinoSdk.getInstance().unsubscribeLogs();
        DevinoSdk.getInstance().stop();
        super.onDestroy();
    }

    @Override
    public DevinoLogsCallback getLogsCallback() {
        if (logsCallback == null) {
            createLogsCallback();
        }
        return logsCallback;
    }

    @Override
    public ReplaySubject<String> getLogs() {
        return logsRx;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_FOREGROUND_GEO, REQUEST_CODE_SEND_GEO -> {
                Log.d(getString(R.string.tag), "1 grantResults=" + Arrays.toString(grantResults));
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    logsCallback.onMessageLogged(getString(R.string.foreground_geo_permission_granted));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        DevinoSdk.getInstance().requestBackgroundGeoPermission(this, REQUEST_CODE_BACKGROUND_GEO);
                    } else {
                        startGeo();
                        if (requestCode == REQUEST_CODE_SEND_GEO) {
                            DevinoSdk.getInstance().sendCurrentGeo();
                        }
                    }
                } else {
                    logsCallback.onMessageLogged(getString(R.string.foreground_geo_permission_missing));
                }
            }
            case REQUEST_CODE_NOTIFICATION_AND_GEO -> {
                Log.d(getString(R.string.tag), "2 grantResults=" + Arrays.toString(grantResults));
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                ) {
                    logsCallback.onMessageLogged(getString(R.string.foreground_geo_permission_granted));
                    logsCallback.onMessageLogged(getString(R.string.notification_permission_granted));
                    startGeoOrRequestBackgroundGeoPermission();
                }
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED
                        && grantResults[1] == PackageManager.PERMISSION_DENIED
                        && grantResults[2] == PackageManager.PERMISSION_DENIED
                ) {
                    logsCallback.onMessageLogged(getString(R.string.foreground_geo_permission_missing));
                    logsCallback.onMessageLogged(getString(R.string.notification_permission_missing));
                }
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_DENIED
                ) {
                    logsCallback.onMessageLogged(getString(R.string.foreground_geo_permission_granted));
                    startGeoOrRequestBackgroundGeoPermission();
                    logsCallback.onMessageLogged(getString(R.string.notification_permission_missing));
                }
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_DENIED
                        || grantResults[1] == PackageManager.PERMISSION_DENIED)
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                ) {
                    logsCallback.onMessageLogged(getString(R.string.foreground_geo_permission_missing));
                    logsCallback.onMessageLogged(getString(R.string.notification_permission_granted));
                }
            }
            case REQUEST_CODE_NOTIFICATION -> {
                Log.d(getString(R.string.tag), "3 grantResults=" + Arrays.toString(grantResults));
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logsCallback.onMessageLogged(getString(R.string.notification_permission_granted));
                } else {
                    logsCallback.onMessageLogged(getString(R.string.notification_permission_missing));
                }
            }
            case REQUEST_CODE_BACKGROUND_GEO -> {
                Log.d(getString(R.string.tag), "4 grantResults=" + Arrays.toString(grantResults));
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logsCallback.onMessageLogged(getString(R.string.background_geo_permission_granted));
                    startGeo();
                    DevinoSdk.getInstance().sendCurrentGeo();
                } else {
                    logsCallback.onMessageLogged(getString(R.string.background_geo_permission_missing));
                }
            }
        }
    }

    private void startGeoOrRequestBackgroundGeoPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.background_geo_permission_missing))
                    .setMessage(getString(R.string.background_geo_permission_text))
                    .setPositiveButton(android.R.string.yes, (dialog, which) ->
                            DevinoSdk.getInstance().requestBackgroundGeoPermission(this, REQUEST_CODE_BACKGROUND_GEO)
                    )
                    .setNegativeButton(android.R.string.no, null)
                    .show();

        } else {
            startGeo();
        }
    }

    private void startGeo() {
        DevinoSdk.getInstance().subscribeGeo(this, 1);
        logsCallback.onMessageLogged(
                getString(R.string.subscribed_geo_interval, 1, getString(R.string.min))
        );
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, ACCESS_BACKGROUND_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
            ) {
                startGeo();
            } else {
                DevinoSdk.getInstance().requestGeoAndNotificationPermissions(
                        this,
                        REQUEST_CODE_NOTIFICATION_AND_GEO
                );
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, ACCESS_BACKGROUND_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
            ) {
                startGeo();
            } else {
                DevinoSdk.getInstance().requestForegroundGeoPermission(this, REQUEST_CODE_FOREGROUND_GEO);
            }
        }
    }
}