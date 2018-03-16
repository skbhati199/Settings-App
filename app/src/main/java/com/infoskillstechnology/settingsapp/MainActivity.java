package com.infoskillstechnology.settingsapp;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final int CHECK_TTS_DATA = 101;
    private static final String TAG = MainActivity.class.getSimpleName();
    private TextToSpeech textToSpeech;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isAds = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.REMOVE_ADS,false);
        TextView ads_value = findViewById(R.id.ads_value);
        ads_value.setText(isAds + "");
    }

    private UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String s) {
            Log.i(TAG,"onStart");
        }

        @Override
        public void onDone(String s) {
            Log.i(TAG,"onDone: " + s);
        }

        @Override
        public void onError(String s) {
            Log.i(TAG,"onError");

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHECK_TTS_DATA:
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    textToSpeech = new TextToSpeech(this, this);
                    textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
                } else {
                    Intent installTtsIntent = new Intent();
                    installTtsIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installTtsIntent);
                }
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onOpenSettings(View view) {
        Intent intent =new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            if (textToSpeech.isLanguageAvailable(new Locale("hi")) == TextToSpeech.LANG_AVAILABLE) {
                textToSpeech.setLanguage(new Locale("hi"));
                textToSpeech.setSpeechRate(0.95f);
            } else {
                Toast.makeText(this, "Hindi Voice not install update voice", Toast.LENGTH_SHORT).show();
            }
        } else if (status == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry Text To Speach faild", Toast.LENGTH_SHORT).show();
        }
    }
}
