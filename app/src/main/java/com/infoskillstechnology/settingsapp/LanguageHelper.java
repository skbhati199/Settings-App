package com.infoskillstechnology.settingsapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public class LanguageHelper {
    private static final String TAG = LanguageHelper.class.getSimpleName();
    private static ArrayList<Locale> languages;
    private static TextToSpeech textToSpeech;
    private static LanguageHelper instance = null;

    public static LanguageHelper newInstace() {
        languages = new ArrayList<>();
        if (null == instance) {
            return new LanguageHelper();
        }
        return instance;
    }

    private LanguageHelper() {
    }


    public static ArrayList<Locale> getLanguages() {
        return languages;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void initSupportedLanguagesLollipop() {
        Set<Locale> availableLocales = null;
        availableLocales = textToSpeech.getAvailableLanguages();
        languages.addAll(availableLocales);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void initSupportedLanguagesLegacy() {
        Locale[] allLocales = Locale.getAvailableLocales();
        for (Locale locale : allLocales) {
            try {
                int res = textToSpeech.isLanguageAvailable(locale);
                boolean hasVariant = (null != locale.getVariant() && locale.getVariant().length() > 0);
                boolean hasCountry = (null != locale.getCountry() && locale.getCountry().length() > 0);

                boolean isLocaleSupported =
                        false == hasVariant && false == hasCountry && res == TextToSpeech.LANG_AVAILABLE ||
                                false == hasVariant && true == hasCountry && res == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                                res == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE;

                Log.d(TAG, "TextToSpeech Engine isLanguageAvailable " + locale + " (supported=" + isLocaleSupported + ",res=" + res + ", country=" + locale.getCountry() + ", variant=" + locale.getVariant() + ")");

                if (true == isLocaleSupported) {
                    languages.add(locale);
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error checking if language is available for TTS (locale=" + locale + "): " + ex.getClass().getSimpleName() + "-" + ex.getMessage());
            }
        }
    }
}
