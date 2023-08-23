package com.devinotele.huawei.exampleapp;

import com.devinotele.huaweidevinosdk.sdk.DevinoLogsCallback;

import io.reactivex.subjects.ReplaySubject;

public interface MainActivityCallback {
    DevinoLogsCallback getLogsCallback();
    ReplaySubject<String> getLogs();
}