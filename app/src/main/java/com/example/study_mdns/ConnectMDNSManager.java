package com.example.study_mdns;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class ConnectMDNSManager {


    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager.ResolveListener resolveListener;

    private final CompositeDisposable connectDisposable = new CompositeDisposable();

    String m;
    private DISCOVERY_STATUS currentDiscoveryStatus;
    private enum DISCOVERY_STATUS{ ON, OFF }
    private final AtomicBoolean resolveListenerBusy = new AtomicBoolean(false);
    private final ConcurrentLinkedQueue<NsdServiceInfo> pendingNsdServices = new ConcurrentLinkedQueue<>();


    //1.1
    //Fragment_First의 startDiscovery()에서 subscribe하면서 호출.
    //참고로 Pair를 만들 경우 <Pair<NSDInfo, String>> 이런식으로 만들면 됌.
    public Observable<NSDInfo> startDiscover(ObservableEmitter<String> stringEmitter, Context context) {
        return Observable.create(connectEmitter -> {
            nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
            if(nsdManager != null) {
                try {
                    Timber.e("startDiscover()...");
                    stringEmitter.onNext("startDiscover()...");

                    connectEmitter.setCancellable(() -> stopDiscover(connectEmitter));
                    registerDiscoveryListener(stringEmitter);
                    Timber.e("registerDiscoveryListener done");
                    registerResolveListener(stringEmitter, connectEmitter);
                    Timber.e("registerResolveListener done");

                    if(currentDiscoveryStatus == DISCOVERY_STATUS.ON) {
                        Timber.e("do nothing");
                    }
                    else {
                        Timber.e("turn discover on");
                        currentDiscoveryStatus = DISCOVERY_STATUS.ON;
                        nsdManager.discoverServices(Constants.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
                    }
                    Timber.e("discoverServices done");
                } catch (Exception e) {
                    connectEmitter.tryOnError(e);
                }
            }
        });
    }


    //1.2
    //DiscoveryListener 인터페이스를 세부 정의 해주는 곳.
    private void registerDiscoveryListener(
            ObservableEmitter<String> stringEmitter)
    {
        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                m = "Service discovery started.";
                Timber.e(m);
                stringEmitter.onNext(m);
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                m = "onServiceFound(). Service discovery success " + service;
                Timber.e(m);
                stringEmitter.onNext(m);

                if (!service.getServiceType().equals(Constants.SERVICE_TYPE)) {
                    m = "Unknown Service Type: " + service.getServiceType();
                    Timber.e(m);
                    stringEmitter.onNext(m);
                } else {
                    m = "running resolveService().";
                    Timber.e(m);
                    stringEmitter.onNext(m);

                    if(resolveListenerBusy.compareAndSet(false, true)) {
                        m = "running resolveService().";
                        Timber.e(m);
                        stringEmitter.onNext(m);

                        nsdManager.resolveService(service, resolveListener);
                    }
                    else {
                        pendingNsdServices.add(service);
                    }
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                m = "service lost... %s" +  service + "\nchecking if there's any left in pendingNsdServices...";
                Timber.e(m);

                Iterator<NsdServiceInfo> iterator = pendingNsdServices.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().getServiceName().equals(service.getServiceName()))
                        iterator.remove();
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Timber.i("Discovery stopped: %s", serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Timber.e("Discovery failed: Error code:%s", errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Timber.e("Discovery failed: Error code:%s", errorCode);
                currentDiscoveryStatus = DISCOVERY_STATUS.OFF;
                nsdManager.stopServiceDiscovery(this);
            }

        };
    }

    //Discover 그만둔다
    private void stopDiscover(ObservableEmitter<NSDInfo> connectEmitter) {
        connectDisposable.add(Observable.defer(() -> Observable.just(1))
                .observeOn(Schedulers.io())
                .doFinally(connectDisposable::clear)
                .subscribe(t -> {
                    connectDisposable.clear();
                    connectEmitter.onComplete();
                }, connectEmitter::tryOnError));
    }

    //resolve Listener 등록하는 함수
    private void registerResolveListener(
            ObservableEmitter<String> stringEmitter,
            ObservableEmitter<NSDInfo> connectEmitter)
    {
        resolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                m = "Resolve Failed. trying next in queue" + errorCode;
                Timber.e(m);
                resolveNextInQueue(stringEmitter);
                stringEmitter.onNext(m);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                if(serviceInfo.getHost() != null) {
                    final NSDInfo nsdInfo = new NSDInfo();
                    try {
                        m = "\nResolve Succeeded...";
                        Timber.e(m);
                        stringEmitter.onNext(m);

                        m = "port number-->\t" + serviceInfo.getPort();
                        stringEmitter.onNext(m);

                        m = "host address-->\t" + serviceInfo.getHost().getHostAddress();
                        stringEmitter.onNext(m);

                        m = "service name-->\t" + serviceInfo.getServiceName();
                        stringEmitter.onNext(m);

                        m = "Found a Connection!";
                        stringEmitter.onNext(m);

                        nsdInfo.setData(serviceInfo);
                        connectEmitter.onNext(nsdInfo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //이거 없애면 discover 계속 루프(?)
                //mNsdManager.stopServiceDiscovery(mDiscoveryListener);
                resolveNextInQueue(stringEmitter);
            }

            private void resolveNextInQueue(ObservableEmitter<String> stringEmitter) {
                NsdServiceInfo nextNsdService = pendingNsdServices.poll();
                if (nextNsdService != null) {
                    m = "more left to resolve...";
                    stringEmitter.onNext(m);
                    nsdManager.resolveService(nextNsdService, resolveListener);
                } else {
                    m = "no more to resolve...";
                    stringEmitter.onNext(m);
                    resolveListenerBusy.set(false);
                }
            }
        };
    }
}
