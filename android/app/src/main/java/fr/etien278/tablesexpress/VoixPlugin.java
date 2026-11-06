package fr.etien278.tablesexpress;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
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
 *
 * Écoute en continu pendant toute la partie : dès qu'une phrase est comprise
 * (ou après un silence), l'écoute repart aussitôt, côté Android, sans attendre la page.
 * Événements envoyés à la page : ready, speech, partial, result, error, end.
 */
@CapacitorPlugin(
    name = "Voix",
    permissions = { @Permission(alias = "micro", strings = { Manifest.permission.RECORD_AUDIO }) }
)
public class VoixPlugin extends Plugin {

    private SpeechRecognizer recognizer;
    private final Handler main = new Handler(Looper.getMainLooper());
    private int session = 0;
    private boolean running = false;
    private String lang = "fr-FR";
    private int failures = 0;
    // reconnaissance sur le téléphone (plus rapide, guidée vers les nombres) si disponible
    private boolean onDevice = false;
    private boolean onDeviceTried = false;

    private static final ArrayList<String> NUMBERS = new ArrayList<>();
    static {
        String[] w = {"zéro","un","deux","trois","quatre","cinq","six","sept","huit","neuf","dix","onze","douze",
            "treize","quatorze","quinze","seize","vingt","trente","quarante","cinquante","soixante",
            "soixante-dix","quatre-vingt","quatre-vingt-dix","cent","cent dix","cent vingt et un"};
        for (String x : w) NUMBERS.add(x);
    }

    @PluginMethod
    public void available(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("available", SpeechRecognizer.isRecognitionAvailable(getContext()));
        call.resolve(ret);
    }

    /** Démarre l'écoute continue (sans effet si elle tourne déjà). */
    @PluginMethod
    public void start(PluginCall call) {
        lang = call.getString("language", "fr-FR");
        main.post(() -> {
            try {
                failures = 0;
                if (!running) {
                    running = true;
                    listen(false);
                }
                call.resolve();
            } catch (Exception e) {
                call.reject("start: " + e.getMessage());
            }
        });
    }

    /** Coupe la phrase en cours et repart aussitôt sur une écoute neuve. */
    @PluginMethod
    public void reset(PluginCall call) {
        main.post(() -> {
            if (running) {
                session++;
                try { if (recognizer != null) { recognizer.cancel(); recognizer.destroy(); } } catch (Exception ignored) {}
                recognizer = null;
                restart(300, true);
            }
            call.resolve();
        });
    }

    @PluginMethod
    public void stop(PluginCall call) {
        main.post(() -> {
            running = false;
            session++;
            main.removeCallbacksAndMessages(null);
            try { if (recognizer != null) recognizer.cancel(); } catch (Exception ignored) {}
            call.resolve();
        });
    }

    @Override
    protected void handleOnPause() {
        main.post(() -> {
            session++;
            try { if (recognizer != null) recognizer.cancel(); } catch (Exception ignored) {}
        });
    }

    @Override
    protected void handleOnResume() {
        main.post(() -> { if (running) listen(true); });
    }

    @Override
    protected void handleOnDestroy() {
        main.post(() -> {
            running = false;
            if (recognizer != null) {
                recognizer.destroy();
                recognizer = null;
            }
        });
    }

    private void listen(boolean fresh) {
        if (!running) return;
        try {
            // nouveau numéro d'abord : les messages de l'ancien module seront ignorés
            final int id = ++session;
            if (fresh && recognizer != null) {
                try { recognizer.cancel(); } catch (Exception ignored) {}
                recognizer.destroy();
                recognizer = null;
            }
            if (recognizer == null) {
                if (!onDeviceTried) {
                    onDeviceTried = true;
                    onDevice = Build.VERSION.SDK_INT >= 33 && SpeechRecognizer.isOnDeviceRecognitionAvailable(getContext());
                    JSObject m = new JSObject();
                    m.put("mode", onDevice ? "téléphone" : "en ligne");
                    notifyListeners("mode", m);
                }
                recognizer = (onDevice && Build.VERSION.SDK_INT >= 33)
                    ? SpeechRecognizer.createOnDeviceSpeechRecognizer(getContext())
                    : SpeechRecognizer.createSpeechRecognizer(getContext());
            }
            recognizer.setRecognitionListener(new Listener(id));

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            // modèle « recherche » : meilleur pour les mots isolés comme « zéro » ou « neuf »
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, lang);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getContext().getPackageName());
            if (onDevice && Build.VERSION.SDK_INT >= 33) {
                intent.putStringArrayListExtra(RecognizerIntent.EXTRA_BIASING_STRINGS, NUMBERS);
            }
            recognizer.startListening(intent);
        } catch (Exception e) {
            JSObject ret = new JSObject();
            ret.put("code", -1);
            ret.put("message", String.valueOf(e.getMessage()));
            notifyListeners("error", ret);
            restart(500, true);
        }
    }

    private void restart(long delay, boolean fresh) {
        if (!running) return;
        final int expected = session;
        main.postDelayed(() -> { if (running && session == expected) listen(fresh); }, delay);
    }

    private class Listener implements RecognitionListener {
        private final int id;
        private boolean done = false;   // résultat ou erreur déjà reçus : la suite est ignorée
        Listener(int id) { this.id = id; }

        private boolean current() { return id == session && !done; }

        private JSObject withTime() {
            JSObject o = new JSObject();
            o.put("at", SystemClock.elapsedRealtime());
            return o;
        }

        private void send(String event, Bundle bundle) {
            JSObject ret = withTime();
            JSArray arr = new JSArray();
            if (bundle != null) {
                ArrayList<String> m = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (m != null) for (String s : m) arr.put(s);
            }
            ret.put("matches", arr);
            notifyListeners(event, ret);
        }

        @Override public void onReadyForSpeech(Bundle params) { if (current()) { failures = 0; notifyListeners("ready", withTime()); } }
        @Override public void onBeginningOfSpeech() { if (current()) notifyListeners("speech", withTime()); }
        @Override public void onRmsChanged(float rmsdB) {}
        @Override public void onBufferReceived(byte[] buffer) {}
        @Override public void onEndOfSpeech() { if (current()) notifyListeners("end", withTime()); }
        @Override public void onPartialResults(Bundle b) { if (current()) send("partial", b); }
        @Override public void onEvent(int eventType, Bundle params) {}

        @Override
        public void onResults(Bundle b) {
            if (!current()) return;
            send("result", b);
            done = true;
            // un module neuf après chaque phrase : c'est ce qui marchait le mieux
            restart(150, true);
        }

        @Override
        public void onError(int error) {
            if (!current()) return;
            done = true;
            JSObject ret = withTime();
            ret.put("code", error);
            notifyListeners("error", ret);
            switch (error) {
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:   // rien entendu
                case SpeechRecognizer.ERROR_NO_MATCH:         // pas compris
                    restart(150, true);
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    running = false;
                    break;
                case 12: // langue non prise en charge
                case 13: // langue pas encore téléchargée
                    if (onDevice) {
                        // le français n'est pas installé sur le téléphone : on passe en ligne
                        onDevice = false;
                        JSObject m = new JSObject();
                        m.put("mode", "en ligne");
                        notifyListeners("mode", m);
                        restart(150, true);
                    } else {
                        failures++;
                        restart(Math.min(2000L, 200L * failures), true);
                    }
                    break;
                default:
                    // micro occupé, service coupé, client... on recrée le module en laissant souffler le téléphone
                    failures++;
                    if (onDevice && failures >= 4) {
                        // la reconnaissance sur le téléphone est bloquée : on passe en ligne
                        onDevice = false;
                        failures = 0;
                        JSObject m = new JSObject();
                        m.put("mode", "en ligne (secours)");
                        notifyListeners("mode", m);
                    }
                    restart(Math.min(1500L, 400L * Math.max(1, failures)), true);
            }
        }
    }
}
