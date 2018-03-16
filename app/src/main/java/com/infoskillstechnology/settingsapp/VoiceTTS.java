/*

Copyright 2015, vyastech

This file is part of Vaani.

    Vaani is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Vaani is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Vaani.  If not, see <http://www.gnu.org/licenses/>.

*/
package com.infoskillstechnology.settingsapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import android.Manifest;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
//import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.metroinfrasys.hindiassistants.constants.Constant;
import com.metroinfrasys.hindiassistants.constants.RequestCode;
import com.metroinfrasys.hindiassistants.interfaces.TollServices;
import com.metroinfrasys.hindiassistants.model.VirtualResponse;
import com.metroinfrasys.hindiassistants.model.Weather;
import com.metroinfrasys.hindiassistants.utils.TranslateLang;
import com.metroinfrasys.hindiassistants.virtualassistant.IntentCall;

import org.json.JSONException;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.RequestExtras;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.metroinfrasys.hindiassistants.constants.RequestCode.CHECK_TTS_DATA;

public class MainActivity extends ActionBarActivity implements
        RecognitionListener, TextToSpeech.OnInitListener {


    private static final String TAG = MainActivity.class.getSimpleName();
    /* API AI init code */
    private final AIConfiguration config = new AIConfiguration("9723faf15b514a4cbf6cd8fbe580f0ac ",
            AIConfiguration.SupportedLanguages.English,
            AIConfiguration.RecognitionEngine.System);

    AIDataService aiDataService;

    private IntentCall intentCall;
    private TextView returnedText;
    private TextView outputText;
    private ToggleButton toggleButton;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private final String LOG_TAG = "VoiceRecognition";
    private String input;
    private TextToSpeech tts;
    private String speak;
    private EditText userInputText;
    private boolean isApiContaxtReset = true;
    private boolean isVoiceCallAgain = true;
    private Retrofit mRetrofit;

    private TextToSpeech textToSpeech;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aiDataService = new AIDataService(this, config);

//        Intent checkTtsDataIntent = new Intent();
//        checkTtsDataIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
//        startActivityForResult(checkTtsDataIntent, CHECK_TTS_DATA);

        intentCall = new IntentCall(this);
        returnedText = (TextView) findViewById(R.id.textView1);
        outputText = (TextView) findViewById(R.id.textView2);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);
        userInputText = (EditText) findViewById(R.id.input);
        userInputText.setInputType(InputType.TYPE_NULL);

        progressBar.setVisibility(View.INVISIBLE);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
//                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
//        recognizerIntent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", new String[]{"en"});
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "hi-IN");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        tts = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR) {
                            tts.setLanguage(new Locale("hi_IN"));
                            tts.setSpeechRate(0.95f);
                        }
                    }
                });
        returnedText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, returnedText.getText());
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivity(intent);

            }
        });

        toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    speech.startListening(recognizerIntent);
                    outputText.setText("");
                } else {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisibility(View.INVISIBLE);
                    speech.stopListening();
                }
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent checkTtsDataIntent = new Intent();
        checkTtsDataIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTtsDataIntent, CHECK_TTS_DATA);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Intent checkTtsDataIntent = new Intent();
        checkTtsDataIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTtsDataIntent, CHECK_TTS_DATA);

    }

    @Override
    public void onResume() {
        super.onResume();



    }

    @Override
    protected void onPause() {
        super.onPause();
    }

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

    @Override
    protected void onStop() {
        super.onStop();
        if (speech != null) {
//            speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        progressBar.setIndeterminate(true);
        toggleButton.setChecked(false);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        returnedText.setText(errorMessage);
        toggleButton.setChecked(false);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle quickResult) {
        Log.i(LOG_TAG, "onPartialResults");
        ArrayList<String> matches = quickResult
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        try {
            input = matches.get(0);
            userInputText.setText(input);
        } catch (Exception exception) {
            Log.e(TAG, "input data not available.");
        }


    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");

    }

    @Override
    public void onResults(Bundle results) {

        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        for (String result : matches)
            Log.i(LOG_TAG, "onResults" + result);
        input = matches.get(0);
     /*   for (String result : matches)
            text += result + "\n";*/

        returnedText.setText(input);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //  performAction(input)
                callApi(input);
            }
        }).start();

        userInputText.setText("");
    }

    private void performAction(String input) {

        String[] verbs = {"कॉल", "फोन", "डायल", "संपर्क", "मेसेज", "सन्देश", "संदेश", "संदेसा", "ओपन", "खोलो", "खोल", "मौसम", "मोसम", "वेदर", "तापमान", "weather", "नमस्ते", "नमस्कार", "कौन", "हेलो", "हेल्लो"};
        String[] actions = {"call", "call", "call", "call", "msg", "msg", "msg", "msg", "open", "open", "open", "weather", "weather", "weather", "weather", "weather", "greet", "greet", "greet", "greet", "greet"};
        String[] sentence = input.split(" ");
        String action = "", noun = "", result = "";
        int verbindex = -1, nounindex = -1;
        for (String word : sentence) {

            //out.println(word);

            for (int i = 0; i < verbs.length; i++) {
                //System.out.println(verbs[i]);
                if (word.equalsIgnoreCase(verbs[i])) verbindex = i;
            }

            if (verbindex >= 0) {
                //	out.println("true");
                action = actions[verbindex];
                System.out.println(action);
                break;
            }

        }
        for (int j = 0; j < sentence.length; j++) {

            if (sentence[j].equalsIgnoreCase("को")) {
                nounindex = j - 1;
            }
        }
        if (nounindex >= 0) noun = sentence[nounindex];
        String search = "google!";
        if (action.equals("")/*||noun.equals("")*/) {
            tts.speak(search, TextToSpeech.QUEUE_FLUSH, null);
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, returnedText.getText());
            if (intent.resolveActivity(getPackageManager()) != null)
                startActivity(intent);
        } else if (action.equals("call")) doCall(noun);
        else if (action.equals("msg")) doMsg(noun);
        else if (action.equals("open")) doOpen(sentence);
        else if (action.equals("weather")) doWeather();
        else if (action.equals("greet")) doGreet();
        tts.speak(result, TextToSpeech.QUEUE_FLUSH, null);


    }

    private void doGreet() {
        Random random = new Random();
        int r = random.nextInt(3);
        String[] greetings = {"नमस्ते, मेरा नाम वाणी है!", "नमस्कार, वाणी आपकी सेवा में तत्पर है", "नमस्ते. मैं वाणी हूँ! मैं एक हिंदी डिजिटल असिस्टेंट हूँ!"};
        tts.speak(greetings[r], TextToSpeech.QUEUE_FLUSH, null);

    }

    private void doWeather() {
        System.out.println("weather");
        new JSONWeatherTask().execute("mumbai");
       /* speak = "मुंबई का मौसम, \nतापमान 31.23 अंक सेल्सियस\nदबाव 77 मिलीमीटर \nआर्द्रता 80  प्रति शत !";
        tts.speak(speak ,TextToSpeech.QUEUE_FLUSH, null);
        outputText.setText(speak);*/
    }

    private void doOpen(String[] sentence) {
        System.out.println("open");
        for (String word : sentence) {
            switch (word) {
                //फेसबुक व्हाट्सप्प कैमरा कमरा
                case "फेसबुक":
                case "facebook":
                    //open facebook
                    Intent fbintent = new Intent();
                    fbintent.setAction(Intent.ACTION_VIEW);
                    fbintent.setData(Uri.parse("http://www.facebook.com"));
                    startActivity(fbintent);
                    break;
                case "whatsapp":
                case "व्हाट्सप्प":
                    //todo run whatsapp
                    break;
                case "camera":
                case "कैमरा":
                case "कमरा":
                    //open camera
                    Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    try {
                        PackageManager pm = getPackageManager();

                        final ResolveInfo mInfo = pm.resolveActivity(i, 0);

                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(mInfo.activityInfo.packageName, mInfo.activityInfo.name));
                        intent.setAction(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);

                        startActivity(intent);
                    } catch (Exception e) {
                        Log.i("open", "Unable to launch camera: " + e);
                    }
                    break;
                case "browser":
                case "ब्राउज़र":
                    // open browser
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://www.google.com"));
                    startActivity(intent);
                    break;
                case "youtube":
                case "युट्युब":
                    //run youtube
                    Intent ytintent = new Intent();
                    ytintent.setAction(Intent.ACTION_VIEW);
                    ytintent.setData(Uri.parse("http://www.youtube.com"));
                    startActivity(ytintent);
                    break;


            }
        }

    }

    private void doMsg(String noun) {
        System.out.println("msg");


        Cursor cur = null;
        ContentResolver cr = null;

        try {
            cr = getContentResolver();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null,
                    null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            boolean called = false;
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {

                    String id = cur.getString(cur
                            .getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur
                            .getString(cur
                                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    // Log.i("Names", name);
                    if (Integer
                            .parseInt(cur.getString(cur
                                    .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        // Query phone here. Covered next
                        Cursor phones = getContentResolver()
                                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                                + " = " + id, null, null);
                        while (phones.moveToNext()) {
                            String phoneNumberX = phones
                                    .getString(phones
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            // Log.i("Number", phoneNumber);
                            boolean a = (name.equalsIgnoreCase(noun));
                            System.out.println(name + "  " + a + "  "
                                    + phoneNumberX);

                            if (a)
                                System.out.println(phoneNumberX);


                            if (a) {
                                String b = "sms:";
                                String smsUri = b + phoneNumberX;
                                Intent smsIntent = new Intent(
                                        Intent.ACTION_VIEW);
                                smsIntent
                                        .setData(Uri.parse(smsUri));
                                called = true;
                                startActivity(smsIntent);
                                tts.speak(noun + " को सन्देश भेजो!", TextToSpeech.QUEUE_FLUSH, null);

                            }


                        }
                        phones.close();

                    }

                }

                if (!called)
                    tts.speak(noun + " आपकी संपर्क सूची में नहीं है!", TextToSpeech.QUEUE_FLUSH, null);

            }

        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    private void doCall(String noun) {
        System.out.println("call");
        System.out.println(noun);


        Cursor cur = null;
        ContentResolver cr = null;

        try {
            cr = getContentResolver();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null,
                    null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            boolean called = false;
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {

                    String id = cur.getString(cur
                            .getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur
                            .getString(cur
                                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    // Log.i("Names", name);
                    if (Integer
                            .parseInt(cur.getString(cur
                                    .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        // Query phone here. Covered next
                        Cursor phones = getContentResolver()
                                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                                + " = " + id, null, null);
                        while (phones.moveToNext()) {
                            String phoneNumberX = phones
                                    .getString(phones
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            // Log.i("Number", phoneNumber);
                            boolean a = (name.equalsIgnoreCase(noun));
                            System.out.println(name + "  " + a + "  "
                                    + phoneNumberX);

                            if (a)
                                System.out.println(phoneNumberX);


                            if (a) {
                                String b = "tel:";
                                String phoneCallUri = b + phoneNumberX;
                                Intent phoneCallIntent = new Intent(
                                        Intent.ACTION_CALL);
                                phoneCallIntent
                                        .setData(Uri.parse(phoneCallUri));
                                called = true;
                                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                startActivity(phoneCallIntent);

                            }


                        }
                        phones.close();

                    }

                }

                if (!called)
                    tts.speak(noun + " आपकी संपर्क सूची में नहीं है!", TextToSpeech.QUEUE_FLUSH, null);

            }

        } catch (Exception ex) {
            ex.printStackTrace();

        }
        tts.speak(noun + " आपकी संपर्क सूची में नहीं है!", TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), DoTheTrickActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            if (textToSpeech.isLanguageAvailable(new Locale("hi")) == TextToSpeech.LANG_AVAILABLE) {
                textToSpeech.setLanguage(new Locale("hi"));
                textToSpeech.setSpeechRate(0.9f);
            } else {
                Toast.makeText(this, "Hindi Voice not install update voice", Toast.LENGTH_SHORT).show();
            }
        } else if (status == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry Text To Speach faild", Toast.LENGTH_SHORT).show();
        }
    }

    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {
        @Override
        protected Weather doInBackground(String... params) {
            Weather weather = new Weather();
            String data = ((new WeatherHttpClient()).getWeatherData(params[0]));
            try {
                weather = JSONWeatherParser.getWeather(data);
// Let's retrieve the icon
                weather.iconData = ((new WeatherHttpClient()).getImage(weather.currentCondition.getIcon()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);
            String temp = String.format("%.2f", (weather.temperature.getTemp() - 273.15));
            // String max = ""+ (weather.temperature.getMaxTemp() - 273.15);
            // String min = ""+ (weather.temperature.getMinTemp() - 273.15);
            String hum = "" + Math.round(weather.currentCondition.getHumidity());
            String speak = "मुंबई का मौसम\nतापमान  " + temp + " अंश सेल्सियस\nआर्द्रता " + hum + " प्रतिशत ";
            outputText.setText(speak);
            tts.speak(speak, TextToSpeech.QUEUE_FLUSH, null);
/*
            cityText.setText(weather.location.getCity() + "," + weather.location.getCountry());
            condDescr.setText(weather.currentCondition.getCondition() + "(" + weather.currentCondition.getDescr() + ")");
            temp.setText("" + Math.round((weather.temperature.getTemp() - 273.15)) + "�C");
            hum.setText("" + weather.currentCondition.getHumidity() + "%");
            press.setText("" + weather.currentCondition.getPressure() + " hPa");
            windSpeed.setText("" + weather.wind.getSpeed() + " mps");
            windDeg.setText("" + weather.wind.getDeg() + "�");*/
        }
    }


    // Api Ai
    public void callApi(String question) {
        // showProgressDialog("Please Wait ....");
        Log.i(TAG, question);

        final AIRequest aiRequest = new AIRequest();
        if (isApiContaxtReset)
            aiRequest.setResetContexts(isApiContaxtReset);
        aiRequest.setQuery(question);

        new AsyncTask<AIRequest, Void, AIResponse>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();


                /*if(virtual_dialog!=null){
                    initalizing_incremental.setVisibility(View.GONE);
                    speak_now.setVisibility(View.GONE);
                    result.setVisibility(View.VISIBLE);
                    virtual_dialog.show();
                }else {
                    dialogAV();
                    initalizing_incremental.setVisibility(View.GONE);
                    speak_now.setVisibility(View.GONE);
                    result.setVisibility(View.VISIBLE);
                    virtual_dialog.show();
                }*/

                //   result  dialog
                // showProgressDialog("Please wait for result.....");
            }

            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                //final AIRequest request = requests[0];
                isApiContaxtReset = false;
                try {
                    final AIResponse response = aiDataService.request(aiRequest);
                    return response;
                } catch (AIServiceException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(AIResponse response) {
                if (response != null) {

//                    returnedText.setText("");
                    Result result = response.getResult();

                    switch (result.getAction().toLowerCase().trim()) {

                        /*case Constant.Emergencycall:
                            if (result.getParameters() != null && !result.getParameters().isEmpty()) {
                                String parameterString = result.getParameters().toString().replace('=', ':');
                                parameterString = parameterString.replace("]", "");
                                parameterString = parameterString.replace("[", "");
                                gotVirtualResponse(parameterString, result.getAction().toLowerCase().trim());
                                openMapActivity(3, parameterString, result.getAction().toLowerCase().trim());
                            } else {
                                //vehicleSpeedVoice.speech(getApplicationContext(), "Something went wrong please try again", true);
                                speechVoice("Something went wrong please try again", RequestCode.utteranceId);
                            }
                            isApiContaxtReset = true;

                            break;
*/
                        case Constant.MATH:
                            Toast.makeText(getApplicationContext(), "" + result.getFulfillment().getSpeech().toUpperCase().trim(), Toast.LENGTH_SHORT).show();
                            //  hideDialog();
                            //  m_syn.SpeakToAudio("" + result.getFulfillment().getSpeech().toUpperCase().trim());
                            // vehicleSpeedVoice.speech(getApplicationContext(), "" + result.getFulfillment().getSpeech().toUpperCase().trim(), true);
                            speechVoice("" + result.getFulfillment().getSpeech().toUpperCase().trim(), RequestCode.utteranceId);
                            isApiContaxtReset = true;
                            break;

                        case Constant.APPS:
                            if (result.getStringParameter("app_name") != null)
                                intentCall.openApps(MainActivity.this, result.getStringParameter("app_name"));
                            isApiContaxtReset = true;
                            break;
                        case Constant.MAPOPEN:
                            intentCall.openApps(MainActivity.this, "map");
                            isApiContaxtReset = true;
                            break;
                        case Constant.BROWSE:
                            if (result.getStringParameter("website") != null)
                                intentCall.openApps(MainActivity.this, result.getStringParameter("website"));
                            isApiContaxtReset = true;
                            break;
                        case Constant.MEDIA:
                        case Constant.MEDIA_OPEN:
                            if (result.getStringParameter("audio") != null) {
                                if (result.getParameters().containsKey("title")) {
                                    intentCall.openMusic(result.getStringParameter("title"), "audio");
                                } else if (result.getParameters().containsKey("q")) {
                                    intentCall.openMusic(result.getStringParameter("q"), "audio");
                                } else {
                                    intentCall.openMusic("", "audio");
                                }
                            }
                            isApiContaxtReset = true;
                            break;
                        case Constant.MAPNAVIGATION:
                            if (result.getStringParameter("to") != null)
                                intentCall.openMapNavigate(MainActivity.this, result.getStringParameter("to"));
                            isApiContaxtReset = true;
                            break;
                        case Constant.WEBSEARCH:
                            if (result.getStringParameter("q") != null)
                                intentCall.openMapNavigate(MainActivity.this, result.getStringParameter("q"));
                            isApiContaxtReset = true;
                            break;
                        case Constant.TRIP:
                            if (result.getStringParameter("any") != null)
                                intentCall.openMapNavigate(MainActivity.this, result.getStringParameter("any"));
                            isApiContaxtReset = true;
                            break;
                        case Constant.CALL:
                            if (result.getStringParameter("q") != null)
                                intentCall.phoneCall(result.getStringParameter("q"), true);
                            isApiContaxtReset = true;
                            break;
                      /*  case Constant.ALARM_SET:
                            if (result.getStringParameter("time") != null)
                                if (result.getParameters() != null && result.getParameters().containsKey("time")) {
                                    intentCall.setAlarm(result.getParameters().get("time").getAsString(), "New Alarm " + count++);
                                } else {
                                    Toast.makeText(MainActivity.this, "Required Time.", Toast.LENGTH_SHORT).show();
                                }
                            isApiContaxtReset = true;
                            break;*/
                        case Constant.MESSAGE_WRITE:
                            if (result.getParameters().containsKey("q")) {
                                intentCall.sendService(result);

                            } else if (result.getParameters().containsKey("recipient")) {
                                intentCall.sendService(result);
                            } else {
                                Toast.makeText(MainActivity.this, "No message write", Toast.LENGTH_SHORT).show();
                            }
                            isApiContaxtReset = true;
                            break;


                       /* case Constant.MAPPLACES:
                            if (mCurrentLocation != null)
                                intentCall.openNearestPlace(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                                        result.getParameters().get("venue_type").toString());
                            isApiContaxtReset = true;
                            break;
*/
                        default:
                            //parameterStrin
                            if (result.getFulfillment().getSpeech() != null) {

                                if (result.getFulfillment().getSpeech().toString() == "") {

                                    if (result.getParameters() != null && !result.getParameters().isEmpty()) {
                                        String parameterString = result.getParameters().toString().replace('=', ':');
                                        parameterString = parameterString.replace("]", "");
                                        parameterString = parameterString.replace("[", "");
                                        gotVirtualResponse(parameterString, result.getAction().toLowerCase().trim());
                                    } else {
                                        //vehicleSpeedVoice.speech(getApplicationContext(), "Something went wrong please try again", true);
                                        speechVoice("कुछ गलत हो गया। कृपया पुन: प्रयास करें", RequestCode.utteranceId);
                                    }
                                    isApiContaxtReset = true;
                                } else {
                                    if (result.getContexts() != null) {
                                        if (result.getContexts().size() > 0)
                                            isVoiceCallAgain = true;
                                    }
                                    //  hideDialog();
                                    // m_syn.SpeakToAudio(result.getFulfillment().getSpeech().toUpperCase().trim());
                                    // vehicleSpeedVoice.speech(getApplicationContext(), "" + result.getFulfillment().getSpeech().toUpperCase().trim(), true);
                                    speechVoice("" + result.getFulfillment().getSpeech().toUpperCase().trim(), RequestCode.utteranceId);

                                }
                            } else {
                                //vehicleSpeedVoice.speech(getApplicationContext(), "Server is not responding.", true);
                                speechVoice("सर्वर प्रतिक्रिया नहीं कर रहा है।", RequestCode.utteranceId);
                            }
                            break;
                    }


                }

            }
        }.execute(aiRequest);
    }

    private void gotVirtualResponse(String queryJson, String action) {

        mRetrofit = App.getRetrofit(Constant.HTTP_URL);

        TollServices tollServices = mRetrofit.create(TollServices.class);
        Call<VirtualResponse> dataResponse = tollServices.virtualAssistantVoice(queryJson, action);

        dataResponse.enqueue(new Callback<VirtualResponse>() {
            @Override
            public void onResponse(Call<VirtualResponse> call, Response<VirtualResponse> response) {


                if (response.body() == null) {
                    // m_syn.SpeakToAudio("Please Try Again");
                    //  vehicleSpeedVoice.speech(getApplicationContext(), "Please Try Again", true);
                    speechVoice("कृपया पुन: प्रयास करें", RequestCode.utteranceId);

                } else {
                    if (response.body().getSuccess()) {
                        if (response.body().getData().size() > 0)
                            //   m_syn.SpeakToAudio(response.body().getData().get(0).toString());
                            if (response.body().getData().get(0).getOutput() != null) {
                                String regex = "^[0-9]{10}$";
                                if (response.body().getData().get(0).getOutput().matches(regex)) {
                                    intentCall.phoneCall(response.body().getData().get(0).getOutput().toString(), false);
                                } else {
                                    // vehicleSpeedVoice.speech(getApplicationContext(), "" + response.body().getData().get(0).getOutput().toString(), true);
                                    speechVoice("" + response.body().getData().get(0).getOutput().toString(), RequestCode.utteranceId);
                                }
                            } else {
                                //vehicleSpeedVoice.speech(getApplicationContext(), "Sorry Please Try Again", true);
                                speechVoice("क्षमा करें पुन: प्रयास करें", RequestCode.utteranceId);

                            }


                    } else {
                        speechVoice("क्षमा करें पुन: प्रयास करें", RequestCode.utteranceId);
                        // vehicleSpeedVoice.speech(getApplicationContext(), "Sorry Please Try Again", true);
                    }
                }


            }

            @Override
            public void onFailure(Call<VirtualResponse> call, Throwable t) {
            }


        });
    }


    private void speechVoice(final String tts, String utteranceId) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
        if (textToSpeech.isLanguageAvailable(new Locale("hi")) == TextToSpeech.LANG_AVAILABLE) {
            textToSpeech.setLanguage(new Locale("hi"));
            textToSpeech.setSpeechRate(0.95f);
//            TranslateLang.newInstanceLang().translateText(tts);
            textToSpeech.speak(tts, TextToSpeech.QUEUE_FLUSH, params);
        } else {
            Toast.makeText(this, "हिंदी आवाज डेटा उपलब्ध नहीं है।", Toast.LENGTH_SHORT).show();
        }
    }
}
