package com.example.study_mdns;

import android.content.Context;
import android.net.vcn.VcnGatewayConnectionConfig;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class ConnectController {

    private CompositeDisposable nsdDisposable = new CompositeDisposable();
    private ConnectMDNSManager connectMDNSManager;
    private ConnectSocketManager connectSocketManager;
    private final ConcurrentHashMap<String, NSDInfo> services = new ConcurrentHashMap<>();

    public Observable<String> callDiscover(Context context) {
        return Observable.create(stringEmitter -> {
            try {
                stringEmitter.onNext("callDiscover()...");
                connectMDNSManager = new ConnectMDNSManager();
                nsdDisposable.add(connectMDNSManager.startDiscover(stringEmitter, context)
                        .subscribeOn(Schedulers.io())
                        .subscribe(nsdInfo -> {
                            synchronized (ConnectController.this) {
                                //do something with nsdInfo... such as addSpeaker or removeSpeaker...
                                stringEmitter.onNext("nsdInfo --> " + nsdInfo.getUuid());
                                services.put(nsdInfo.getUuid(), nsdInfo);
                            }
                        }, Throwable::printStackTrace));

                connectSocketManager = new ConnectSocketManager();
                nsdDisposable.add(connectSocketManager.connectSocket(stringEmitter, services)
                        .subscribeOn(Schedulers.io())
                        .subscribe(doneFlag -> {
                            synchronized (ConnectController.this) {
                                Timber.e("connectSocket Done");
                            }
                        }, Throwable::printStackTrace));

            } catch (Exception e) {
                stringEmitter.tryOnError(e);
            }
        });
    }
}
