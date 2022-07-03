package com.example.adpole;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adpolelib.AdPole;
import com.example.adpolelib.Interfaces.AdPoleLoadDataListener;
import com.example.adpolelib.InterstitialActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initInApp();
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (InterstitialActivity.isLoaded())
                    InterstitialActivity.show();
                else
                    Toast.makeText(MainActivity.this, "Loading ...", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void initInApp() {
        AdPole.initialize(this, "apap-f8b0e74c-118e-40d3-a1fd-4217ac81d1b1");
        InterstitialActivity.init(this);
        InterstitialActivity.loadAd();

    }
}