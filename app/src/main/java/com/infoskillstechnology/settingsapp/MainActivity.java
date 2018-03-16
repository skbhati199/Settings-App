package com.infoskillstechnology.settingsapp;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isAds = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.REMOVE_ADS,false);
        TextView ads_value = findViewById(R.id.ads_value);
        ads_value.setText(isAds + "");
    }

    public void onOpenSettings(View view) {
        Intent intent =new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
