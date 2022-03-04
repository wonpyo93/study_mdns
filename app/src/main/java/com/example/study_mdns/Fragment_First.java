package com.example.study_mdns;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.text.method.ScrollingMovementMethod;
import android.widget.Toast;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import timber.log.Timber;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class Fragment_First extends Fragment {
    private static final String TAG = "Fragment1";
    public String theLog = "";
    public String m;

    private Button button1;
    private Button button2;
    private TextView textView1;

    private CompositeDisposable logDisposable = new CompositeDisposable();
    private ConnectController connectController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__first, container, false);
        button1 = (Button) view.findViewById(R.id.button1);
        button2 = (Button) view.findViewById(R.id.button2);
        textView1 = (TextView) view.findViewById(R.id.textView1);

        m = "onCreateView() started...";
        Timber.e(m);
        updateTextView1(m);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m = "Discover Button(button1) clicked...";
                Timber.e(m);
                updateTextView1(m);

                startDiscovery();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m = "button2 clicked...";
                Timber.e(m);
                updateTextView1(m);

                ((MainActivity)getActivity()).setViewPager(1);
            }
        });

        textView1.setMovementMethod(new ScrollingMovementMethod());

        /*
        만약 새로운 activity로 넘어가는 경우이면 밑에처럼 하면 된다
        Intent.intent = new Intent(getActivity(), SecondActivity.class);
        startActivity(intent);
        */

        return view;
    }

    public void startDiscovery() {
        connectController = new ConnectController();
        logDisposable.add(connectController.callDiscover(getContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(calledLog -> {
                    synchronized (Fragment_First.this) {
                        Timber.e("bringing calledLog");
                        updateTextView1(calledLog);
                    }
                }, Throwable::printStackTrace));
    }

    public void updateTextView1(String passedString) {
        Timber.e(passedString);
        theLog = theLog + "\n" + passedString;
        textView1.setText(theLog);
    }
}
