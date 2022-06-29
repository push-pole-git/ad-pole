package com.example.adpole;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.adpolelib.InAppAdvertise;
import com.example.adpolelib.Interfaces.AdPoleLoadDataListener;
import com.example.adpolelib.InterstitialActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //AdPole.initialize(this, "faf795df-b9fb-4ecb-8ed4-f20303f73267_CID_99fbb0fb-0d9c-4c0d-b324-9acbb355bdb2");
        initInApp();
    }
    private void initInApp() {
        InAppAdvertise.init(getApplicationContext());
        InterstitialActivity.loadAd(getApplicationContext(), "bb0b964d-268d-4ee6-b5fd-ba7e63684e20",false);
        InterstitialActivity.setAdPoleLoadDataListener(new AdPoleLoadDataListener() {
            @Override
            public void onAdLoaded() {
                if(InterstitialActivity.isLoaded)
                    InterstitialActivity.show(getApplicationContext());
            }
            @Override
            public void onAdFailedToLoad() {
                InterstitialActivity.loadAd(getApplicationContext(),"bb0b964d-268d-4ee6-b5fd-ba7e63684e20",false);
            }
        });


    }
}