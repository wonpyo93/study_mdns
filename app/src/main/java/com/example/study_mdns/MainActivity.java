package com.example.study_mdns;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    public static WeakReference<MainActivity> weakActivity;
    public static MainActivity getmInstanceActivity() {
        return weakActivity.get();
    }
    private NSDListen mNSDListener;
    private NSDDiscover mNSDDiscover;

    private Button mRegisterBtn;
    private Button mDiscoverBtn;
    public TextView textView_Log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weakActivity = new WeakReference<>(MainActivity.this);

        Timber.plant(new Timber.DebugTree());
        Constants.theLog = "Log Starts.\n";
        setContentView(R.layout.activity_main);

        mNSDListener = new NSDListen(this);
        mNSDDiscover = new NSDDiscover(this, mDiscoveryListener);

        mRegisterBtn = (Button)findViewById(R.id.register);
        mRegisterBtn.setOnClickListener(v -> {
            Timber.e("Register Button Clicked.");
            Constants.theLog = "Register Button Clicked.\n" + Constants.theLog;
            addMessage();
            mNSDListener.registerDevice();
        });

        mDiscoverBtn = (Button)findViewById(R.id.discover);
        mDiscoverBtn.setOnClickListener(v -> {
            Timber.e("Discover Button Clicked.");
            Constants.theLog = "Discover Button Clicked.\n" + Constants.theLog;
            addMessage();
            mNSDDiscover.discoverServices();
        });

        Button mSayHelloBtn = (Button) findViewById(R.id.sayHello);
        mSayHelloBtn.setOnClickListener(v -> mNSDDiscover.sayHello());

        textView_Log = (TextView) findViewById(R.id.textView);
        textView_Log.setMovementMethod(new ScrollingMovementMethod());

        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.select_mode_dlg_msg))
                .setPositiveButton(getString(R.string.register_dlg_btn), (dialog, which) -> {
                    Timber.e("Register selected.");
                    Constants.theLog = "Register selected.\n" + Constants.theLog;
                    addMessage();
                    mDiscoverBtn.setVisibility(View.GONE);
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.discover_dlg_btn), (dialog, which) -> {
                    Timber.e("Discover selected.");
                    Constants.theLog = "Discover selected.\n" + Constants.theLog;
                    addMessage();
                    mRegisterBtn.setVisibility(View.GONE);
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    public void addMessage() {
        textView_Log.setText(Constants.theLog);
    }

    private final NSDDiscover.DiscoveryListener mDiscoveryListener = (host, port) -> {
        runOnUiThread(() -> findViewById(R.id.sayHello).setVisibility(View.VISIBLE));
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mNSDListener.shutdown();
        mNSDDiscover.shutdown();
    }
}