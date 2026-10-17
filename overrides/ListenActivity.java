package com.jarvis.mobile;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.InputType;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

/** Robust push-to-talk: device voice dialog, embedded recognizer, then typed input. */
public final class ListenActivity extends Activity implements RecognitionListener {
    private static final int VOICE = 301, MIC = 302;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SpeechRecognizer recognizer;
    private TextView status;
    private EditText typed;
    private boolean completed;
    private boolean destroyed;

    private int dp(int v) { return Math.round(getResources().getDisplayMetrics().density*v); }
    private Button button(LinearLayout layout, String text) {
        Button b = new Button(this);
        b.setText(text); b.setAllCaps(false); b.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,dp(48));
        lp.topMargin=dp(8); layout.addView(b,lp); return b;
    }
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setDimAmount(.25f);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20),dp(20),dp(20),dp(20));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(16,29,49)); bg.setCornerRadius(dp(22));
        root.setBackground(bg);
        TextView title = new TextView(this);
        title.setText("🎤 JARVIS VOICE"); title.setTextColor(Color.rgb(78,229,213));
        title.setGravity(Gravity.CENTER); title.setTextSize(22); root.addView(title);
        status = new TextView(this);
        status.setText("Preparing voice input…"); status.setTextColor(Color.WHITE);
        status.setTextSize(15); status.setGravity(Gravity.CENTER);
        status.setPadding(0,dp(16),0,dp(12)); root.addView(status);
        typed = new EditText(this);
        typed.setSingleLine(true); typed.setTextColor(Color.WHITE);
        typed.setHintTextColor(Color.LTGRAY); typed.setHint("Or type your command here");
        typed.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        root.addView(typed,new LinearLayout.LayoutParams(-1,dp(52)));
        button(root,"RUN TYPED COMMAND").setOnClickListener(v -> execute(typed.getText().toString()));
        button(root,"TRY VOICE AGAIN").setOnClickListener(v -> startVoice());
        button(root,"CLOSE").setOnClickListener(v -> finish());
        setContentView(root);
        getWindow().setLayout(getResources().getDisplayMetrics().widthPixels-dp(30),WindowManager.LayoutParams.WRAP_CONTENT);
        handler.postDelayed(this::startVoice,300);
    }
    private Intent speechIntent() {
        boolean english=getSharedPreferences("settings",MODE_PRIVATE).getBoolean("english",false);
        String locale=english?"en-IN":"hi-IN";
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,locale);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,locale);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak a Hindi or English Jarvis command");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,3);
        if(getSharedPreferences("settings",MODE_PRIVATE).getBoolean("offline",false))
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true);
        return intent;
    }
    private void startVoice() {
        if (completed || destroyed || isFinishing()) return;
        releaseRecognizer();
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) {
            status.setText("Microphone permission needed. You can type a command instead.");
            if(Build.VERSION.SDK_INT>=23) requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},MIC);
            return;
        }
        status.setText("Opening Android voice recognition…");
        // Using the device-provided UI works on OEM phones that lack a direct recognition service.
        try {
            startActivityForResult(speechIntent(),VOICE);
        } catch (ActivityNotFoundException | SecurityException ex) {
            embeddedVoice();
        } catch (RuntimeException ex) {
            embeddedVoice();
        }
    }
    private void embeddedVoice() {
        status.setText("Trying fallback speech engine…");
        try {
            if(!SpeechRecognizer.isRecognitionAvailable(this)) {
                status.setText("No speech engine found. Enable Google/Speech Services in Android settings, or type a command below.");
                return;
            }
            boolean offline=getSharedPreferences("settings",MODE_PRIVATE).getBoolean("offline",false);
            if(offline && Build.VERSION.SDK_INT>=31 && SpeechRecognizer.isOnDeviceRecognitionAvailable(this))
                recognizer=SpeechRecognizer.createOnDeviceSpeechRecognizer(this);
            else
                recognizer=SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(this);
            recognizer.startListening(speechIntent());
            status.setText("Listening now…");
        } catch(SecurityException ex) {
            releaseRecognizer();
            status.setText("Microphone access blocked. Allow it in settings, or type below.");
        } catch(RuntimeException ex) {
            releaseRecognizer();
            status.setText("Speech engine unavailable. Enable/update Speech Services, or type below.");
        }
    }
    @Override public void onRequestPermissionsResult(int request, String[] p, int[] results) {
        super.onRequestPermissionsResult(request,p,results);
        if(request==MIC) {
            if(results.length>0 && results[0]==PackageManager.PERMISSION_GRANTED)
                handler.post(this::startVoice);
            else status.setText("Microphone permission denied. Typed commands still work.");
        }
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request,result,data);
        if(request!=VOICE || completed || destroyed) return;
        if(result==RESULT_OK && data!=null) {
            ArrayList<String> texts=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(texts!=null && !texts.isEmpty() && texts.get(0)!=null) {
                execute(texts.get(0)); return;
            }
        }
        status.setText("No speech received. Tap TRY VOICE AGAIN or type a command.");
    }
    private void execute(String raw) {
        if(completed || destroyed) return;
        final String text=raw==null?"":raw.trim();
        if(text.isEmpty()) { status.setText("Please say or type a command."); return; }
        completed=true;
        releaseRecognizer();
        status.setText("✓ "+text);
        final android.content.Context app=getApplicationContext();
        // Use a separate handler: onDestroy clears the dialog's own callbacks.
        handler.postDelayed(() -> {
            finish();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    CommandEngine.execute(app,text,answer -> Speaker.speak(app,answer));
                } catch(RuntimeException ex) {
                    app.getSharedPreferences("settings",MODE_PRIVATE).edit()
                        .putString("last_response","Command failed safely. Try a different command.").apply();
                }
            },300);
        },150);
    }
    private void releaseRecognizer() {
        if(recognizer!=null) {
            try { recognizer.cancel(); recognizer.destroy(); }
            catch(RuntimeException ignored) { }
            recognizer=null;
        }
    }
    @Override public void onReadyForSpeech(Bundle p) { if(!completed) status.setText("Listening…"); }
    @Override public void onBeginningOfSpeech() { if(!completed) status.setText("Hearing you…"); }
    @Override public void onRmsChanged(float level) { }
    @Override public void onBufferReceived(byte[] buffer) { }
    @Override public void onEndOfSpeech() { if(!completed) status.setText("Understanding…"); }
    @Override public void onError(int error) {
        if(completed || destroyed || isFinishing()) return;
        String detail;
        switch(error) {
            case SpeechRecognizer.ERROR_NETWORK:
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                detail="Speech network unavailable. Check internet or offline speech pack."; break;
            case SpeechRecognizer.ERROR_NO_MATCH: detail="Didn't catch that. Please retry."; break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: detail="No speech heard. Please retry."; break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: detail="Microphone permission missing."; break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: detail="Speech engine busy. Wait, then retry."; break;
            case SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED:
            case SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE:
                detail="Selected language unavailable. Try English or download Hindi speech pack."; break;
            case SpeechRecognizer.ERROR_SERVER_DISCONNECTED: detail="Speech provider disconnected. Update Speech Services."; break;
            case SpeechRecognizer.ERROR_CLIENT: detail="Speech provider refused request. Type a command or retry."; break;
            default: detail="Speech error ("+error+"). Retry or type a command.";
        }
        releaseRecognizer();
        status.setText(detail);
    }
    @Override public void onResults(Bundle data) {
        if(data==null) { status.setText("No result. Try again or type."); return; }
        ArrayList<String> texts=data.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if(texts!=null && !texts.isEmpty()) execute(texts.get(0));
        else status.setText("No speech result. Try again or type.");
    }
    @Override public void onPartialResults(Bundle data) { }
    @Override public void onEvent(int eventType,Bundle data) { }
    @Override public void onDestroy() {
        destroyed=true;
        handler.removeCallbacksAndMessages(null);
        releaseRecognizer();
        super.onDestroy();
    }
}
