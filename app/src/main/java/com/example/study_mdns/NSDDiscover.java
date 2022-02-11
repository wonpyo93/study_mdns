package com.example.study_mdns;

import android.app.usage.ConfigurationStats;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import timber.log.Timber;

public class NSDDiscover {
//    public static final String TAG = "TrackingFlow";
    public String mDiscoveryServiceName = "WonpyoDiscoverer";
    private Context mContext;
    private NsdManager mNsdManager;
    private DiscoveryListener mListener;
    private String mHostFound;
    private int mPortFound;
    private DISCOVERY_STATUS mCurrentDiscoveryStatus = DISCOVERY_STATUS.OFF;
    MainActivity mainActivity = new MainActivity();

    private enum DISCOVERY_STATUS{
        ON,
        OFF
    }

    public NSDDiscover(Context context, DiscoveryListener listener) {
        this.mContext = context;
        this.mListener = listener;
        this.mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void discoverServices() {
        Timber.e("discoverServices()...");
        Constants.theLog = "discoverServices()...\n" + Constants.theLog;
        MainActivity.getmInstanceActivity().addMessage();
        if(mCurrentDiscoveryStatus == DISCOVERY_STATUS.ON) {
            Timber.e("  but DISCOVERY_STATUS == ON. Do Nothing.");
            Constants.theLog = "  but DISCOVERY_STATUS == ON. Do Nothing.\n\n" + Constants.theLog;
           MainActivity.getmInstanceActivity().addMessage();
            return;
        }
        else {
            Timber.e("  DISCOVERY_STATUS == OFF. Turing it ON.");
            Constants.theLog = "  DISCOVERY_STATUS == OFF. Turing it ON.\n\n" + Constants.theLog;
           MainActivity.getmInstanceActivity().addMessage();
            Toast.makeText(mContext, "Discover SERVICES!", Toast.LENGTH_LONG).show();
            mCurrentDiscoveryStatus = DISCOVERY_STATUS.ON;
            mNsdManager.discoverServices(Constants.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        }
    }

    public void sayHello(){
        if(mHostFound == null || mPortFound <= 0){
            Toast.makeText(mContext, mContext.getString(R.string.devices_not_found_toast), Toast.LENGTH_LONG).show();
            return;
        }

        new SocketConnection().sayHello(mHostFound, mPortFound);
    }

    NsdManager.ResolveListener mResolveListener = new NsdManager.ResolveListener() {
        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Timber.e("Resolve Failed. %s", errorCode);
            Constants.theLog = "Resolve Failed.\n\n" + Constants.theLog;
           MainActivity.getmInstanceActivity().addMessage();
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Timber.e("Resolve Succeeded...");
            Timber.e("  %s", serviceInfo);

            Constants.theLog = "port number--> " + serviceInfo.getPort() + "\n" + Constants.theLog;
            Constants.theLog = "host address--> " + serviceInfo.getHost().getHostAddress() + "\n" + Constants.theLog;
            Constants.theLog = "service name--> " + serviceInfo.getServiceName() + "\n" + Constants.theLog;

            Constants.theLog = "Resolve Succeeded...\n" + Constants.theLog;
           MainActivity.getmInstanceActivity().addMessage();

            if (serviceInfo.getServiceName().equals(mDiscoveryServiceName)) {
                Timber.e("Same IP.");
                Constants.theLog = "Same IP.\n\n" + Constants.theLog;
               MainActivity.getmInstanceActivity().addMessage();
                return;
            }
            Toast.makeText(mContext, "Found a Connection!", Toast.LENGTH_LONG).show();
            Timber.e("Found a Connection!");
            Constants.theLog = "Found a Connection!\n\n" + Constants.theLog;
           MainActivity.getmInstanceActivity().addMessage();

            //이거 없애면 discover 계속 루프
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            setHostAndPortValues(serviceInfo);
            if(mListener != null){
                mListener.serviceDiscovered(mHostFound, mPortFound);
            }
        }
    };

    private void setHostAndPortValues(NsdServiceInfo serviceInfo){
        mHostFound = serviceInfo.getHost().getHostAddress();
        mPortFound = serviceInfo.getPort();
    }

    private class SocketConnection {
        private Socket mSocket;
        public void sayHello(final String host, final int port){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mSocket = new Socket();
                    SocketAddress address = new InetSocketAddress(host, port);
                    try {
                        Timber.e("Trying to connect to: %s... port: %s", host, port);
                        Constants.theLog = "Trying to connect to... host: " + host + ", port: " + port + "\n\n" + Constants.theLog;
                       MainActivity.getmInstanceActivity().addMessage();
                        mSocket.connect(address);
                        DataOutputStream os = new DataOutputStream(mSocket.getOutputStream());
                        DataInputStream is = new DataInputStream(mSocket.getInputStream());
                        //Send a message...
                        os.write("Hello!".getBytes());
                        os.flush();
                        Timber.e("Message SENT!");
                        Constants.theLog = "Message SENT!\n\n" + Constants.theLog;
                       MainActivity.getmInstanceActivity().addMessage();

                        //Read the message
                        int bufferSize = 1024;
                        byte[] buffer = new byte[bufferSize];
                        StringBuilder sb = new StringBuilder();
                        int length = Integer.MAX_VALUE;
                        try {
                            while (length >= bufferSize) {
                                length = is.read(buffer);
                                sb.append(new String(buffer, 0, length));
                            }
                            final String receivedMessage = sb.toString();
                            new Handler(mContext.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, "Message received: " + receivedMessage, Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (Exception e) {e.printStackTrace();}
                        os.close();
                        is.close();

                    } catch (IOException e) {e.printStackTrace();}
                }
            }).start();
        }
    }

    private NsdManager.DiscoveryListener mDiscoveryListener = new NsdManager.DiscoveryListener() {
        @Override
        public void onDiscoveryStarted(String regType) {
            Timber.e("Service discovery started.");
            Constants.theLog = "Service discovery started.\n\n" + Constants.theLog;
           MainActivity.getmInstanceActivity().addMessage();
        }

        @Override
        public void onServiceFound(NsdServiceInfo service) {
            Timber.e("onServiceFound(). Service discovery success%s", service);
            Constants.theLog = "onServiceFound(). Service discovery success" + service + "\n\n" + Constants.theLog;
            MainActivity.getmInstanceActivity().addMessage();
            if (!service.getServiceType().equals(Constants.SERVICE_TYPE)) {
                Timber.e("Unknown Service Type: %s", service.getServiceType());
                Constants.theLog = "Unknown Service Type\n\n" + Constants.theLog;
                MainActivity.getmInstanceActivity().addMessage();

            } else if (service.getServiceName().equals(mDiscoveryServiceName)) {
                Timber.e("Same machine: %s", mDiscoveryServiceName);
            } else {
                Timber.e("running resolveService().");
                Constants.theLog = "running resolveService().\n\n" + Constants.theLog;
                MainActivity.getmInstanceActivity().addMessage();
                mNsdManager.resolveService(service, mResolveListener);
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo service) {
            Timber.e("service lost%s", service);
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            Timber.i("Discovery stopped: %s", serviceType);
        }

        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Timber.e("Discovery failed: Error code:%s", errorCode);
            mNsdManager.stopServiceDiscovery(this);
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Timber.e("Discovery failed: Error code:%s", errorCode);
            mCurrentDiscoveryStatus = DISCOVERY_STATUS.OFF;
            mNsdManager.stopServiceDiscovery(this);
        }
    };

    private NsdManager.RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {
        @Override
        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
            mDiscoveryServiceName = NsdServiceInfo.getServiceName();
            Timber.e("This device has been registered to be discovered through NSD...");
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
            Timber.e("onRegistrationFailed()");
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo arg0) {
            Timber.e("onServieUnregisterd()");
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Timber.e("onUnregistrationFailed()");
        }

    };

    public void shutdown(){
        Timber.e("shutdown()");
        try {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }catch(Exception e){e.printStackTrace();}
    }

    public interface DiscoveryListener {
        void serviceDiscovered(String host, int port);
    }
}
