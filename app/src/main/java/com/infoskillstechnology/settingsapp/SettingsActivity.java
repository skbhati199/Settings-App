package com.infoskillstechnology.settingsapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.Toast;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String SOUND_OFF_ON = "sound_off_on";
    public static final String REMOVE_ADS = "remove_ads";
    private SwitchPreference removeAdsPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        addPreferencesFromResource(R.xml.pref_notification);
        removeAdsPrefs = (SwitchPreference) findPreference(REMOVE_ADS);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        if(!sharedPreferences.getBoolean(REMOVE_ADS, false)){
            removeAdsPrefs.setEnabled(false);
        }

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SOUND_OFF_ON)){
            SwitchPreference exercisesPref = (SwitchPreference) findPreference(key);
            exercisesPref.setChecked(sharedPreferences.getBoolean(key, false));
            Toast.makeText(this, "sharedPreferences " + sharedPreferences.getBoolean(key, false), Toast.LENGTH_SHORT).show();
        } else if(key.equals(REMOVE_ADS)){
            SwitchPreference exercisesPref = (SwitchPreference) findPreference(key);
            exercisesPref.setChecked(sharedPreferences.getBoolean(key, false));
            if(!sharedPreferences.getBoolean(key, false)){
                exercisesPref.setEnabled(false);
            }
            Toast.makeText(this, "sharedPreferences " + sharedPreferences.getBoolean(key, false), Toast.LENGTH_SHORT).show();
        }


    }
}
