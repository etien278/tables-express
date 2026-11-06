package fr.etien278.tablesexpress;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;

import java.util.ArrayList;

/**
 * Reconnaissance vocale du téléphone pour Tables Express.
 * Événements envoyés à la page : ready, partial, result, error, end.
 */
@CapacitorPlugin(
    name = "Voix",
    permissions = { @Permission(alias = "micro", strings = { Manifest.permission.RECORD_AUDIO }) }
)
public class VoixPlugin extends Plugin {

    private SpeechRecognizer recognizer;
    private final Handler main = new Handler(Looper.getMainLooper());
    private int session = 0;

    @PluginMethod
    public void available(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("available", SpeechRecognizer.isRecognitionAvailable(getContext()));
        call.resolve(ret);
    }

    @PluginMethod
    public void start(PluginCall call) {
        final String lang = call.getString("language", "fr-FR");
        main.post(() -> {
            try {
                if (recognizer == null) {
                    recognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
                } else {
                    recognizer.cancel();
                }
                final int id = ++session;
                recognizer.setRecognitionListener(new Listener(id));

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, lang);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
                intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getContext().getPackageName());
                recognizer.startListening(intent);
                call.resolve();
            } catch (Exception e) {
                call.reject("start: " + e.getMessage());
            }
        });
    }

    @PluginMethod
    public void stop(PluginCall call) {
        main.post(() -> {
            try {
                session++;
                if (recognizer != null) recognizer.cancel();
            } catch (Exception ignored) {}
            call.resolve();
        });
    }

    @Override
    protected void handleOnDestroy() {
        main.post(() -> {
            if (recognizer != null) {
                recognizer.destroy();
                recognizer = null;
            }
        });
    }

    private class Listener implements RecognitionListener {
        private final int id;
        Listener(int id) { this.id = id; }

        private boolean current() { return id == session; }

        private void send(String event, Bundle bundle) {
            if (!current()) return;
            JSObject ret = new JSObject();
            JSArray arr = new JSArray();
            if (bundle != null) {
                ArrayList<String> m = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (m != null) for (String s : m) arr.put(s);
            }
            ret.put("matches", arr);
            notifyListeners(event, ret);
        }

        @Override public void onReadyForSpeech(Bundle params) { if (current()) notifyListeners("ready", new JSObject()); }
        @Override public void onBeginningOfSpeech() {}
        @Override public void onRmsChanged(float rmsdB) {}
        @Override public void onBufferReceived(byte[] buffer) {}
        @Override public void onEndOfSpeech() { if (current()) notifyListeners("end", new JSObject()); }
        @Override public void onPartialResults(Bundle b) { send("partial", b); }
        @Override public void onResults(Bundle b) { send("result", b); }
        @Override public void onEvent(int eventType, Bundle params) {}

        @Override
        public void onError(int error) {
            if (!current()) return;
            JSObject ret = new JSObject();
            ret.put("code", error);
            notifyListeners("error", ret);
        }
    }
}
