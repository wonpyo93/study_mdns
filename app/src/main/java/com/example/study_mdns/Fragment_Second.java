package com.example.study_mdns;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import timber.log.Timber;

public class Fragment_Second extends Fragment {
    private static final String TAG = "Fragment2";

    private Button button3;
    private Button button4;
    private TextView textView2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__second, container, false);
        button3 = (Button) view.findViewById(R.id.button3);
        button4 = (Button) view.findViewById(R.id.button4);
        textView2 = (TextView) view.findViewById(R.id.textView1);
        Timber.e("onCreateView() started...");

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timber.e("button3 clicked...");
                //navigate to fragment method called...
                ((MainActivity)getActivity()).setViewPager(0);
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timber.e("button4 clicked...");
                //navigate to fragment method called...
                ((MainActivity)getActivity()).setViewPager(1);
            }
        });

        /*
        만약 새로운 activity로 넘어가는 경우이면 밑에처럼 하면 된다
        Intent.intent = new Intent(getActivity(), SecondActivity.class);
        startActivity(intent);
        */

        return view;
    }
}
