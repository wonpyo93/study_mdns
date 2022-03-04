package com.example.study_mdns;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class ConnectSocketManager {

    private CompositeDisposable socketDisposable = new CompositeDisposable();

    public Observable<String> connectSocket(ObservableEmitter<String> stringEmitter, ConcurrentHashMap<String, NSDInfo> services) {
        return Observable.create(socketEmitter -> {
            try {
                //do something
            } catch (Exception e) {
                socketEmitter.tryOnError(e);
            }
        });
    }
}
