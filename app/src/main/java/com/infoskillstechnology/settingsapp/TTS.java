package com.infoskillstechnology.settingsapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;

import java.util.Locale;

/**
 * Created by SKBHATI on 3/16/2018.
 */

public class TTS extends Service implements TextToSpeech.OnInitListener {

    private TextToSpeech textToSpeech;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        textToSpeech = new TextToSpeech(this,this);
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.ERROR) {
            textToSpeech.setLanguage(new Locale("hi_IN"));
            textToSpeech.setSpeechRate(0.95f);
        }
    }
}
