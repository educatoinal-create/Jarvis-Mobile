package com.jarvis.v3;
import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.InputType;
import java.util.ArrayList;

/** Device speech dialog -> embedded recognizer -> reliable typed command fallback. */
public final class VoiceActivity extends Activity implements RecognitionListener {
 private static final int VOICE_REQUEST=419,PERMISSION_REQUEST=420;
 private TextView feedback;
 private EditText typed;
 private SpeechRecognizer recognizer;
 private boolean resolved=false,alive=true;
 private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
 @Override public void onCreate(Bundle saved){
  super.onCreate(saved);
  LinearLayout layout=new LinearLayout(this);layout.setOrientation(LinearLayout.VERTICAL);
  layout.setPadding(dp(20),dp(25),dp(20),dp(25));layout.setBackgroundColor(Color.rgb(8,19,37));
  TextView title=new TextView(this);title.setText("🎤  JARVIS VOICE");title.setTextColor(Color.rgb(68,239,211));
  title.setTextSize(24);title.setGravity(Gravity.CENTER);layout.addView(title);
  feedback=new TextView(this);feedback.setTextColor(Color.WHITE);feedback.setTextSize(16);
  feedback.setText("Starting voice input…");feedback.setPadding(0,dp(20),0,dp(16));layout.addView(feedback);
  typed=new EditText(this);typed.setTextColor(Color.WHITE);typed.setHintTextColor(Color.LTGRAY);
  typed.setHint("Type a command if voice fails");typed.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
  typed.setSingleLine(true);layout.addView(typed);
  addButton(layout,"RUN TYPED COMMAND",()->submit(typed.getText().toString()));
  addButton(layout,"RETRY MICROPHONE",this::startVoice);
  addButton(layout,"CLOSE",this::finish);
  setContentView(layout);
  new Handler(Looper.getMainLooper()).postDelayed(this::startVoice,350);
 }
 private void addButton(LinearLayout layout,String title,Runnable action){
  Button b=new Button(this);b.setText(title);b.setAllCaps(false);
  layout.addView(b);b.setOnClickListener(v->action.run());
 }
 private Intent speechIntent(){
  String lang=getSharedPreferences("jarvis_prefs",MODE_PRIVATE).getString("lang","hi-IN");
  Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
  i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
  i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,lang);
  i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,lang);
  i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,3);
  i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to JARVIS in Hindi or English");
  return i;
 }
 private void startVoice(){
  if(!alive||resolved||isFinishing())return;
  release();
  if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
   feedback.setText("Microphone permission needed. Typed commands still work.");
   requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},PERMISSION_REQUEST);
   return;
  }
  feedback.setText("Opening device speech recognition…");
  try{startActivityForResult(speechIntent(),VOICE_REQUEST);}
  catch(ActivityNotFoundException|SecurityException e){embedded();}
  catch(RuntimeException e){embedded();}
 }
 private void embedded(){
  try{
   if(!SpeechRecognizer.isRecognitionAvailable(this)){
    feedback.setText("No voice recognition service. Enable or update Speech Services by Google, or use typed command.");
    return;
   }
   recognizer=SpeechRecognizer.createSpeechRecognizer(this);
   recognizer.setRecognitionListener(this);
   recognizer.startListening(speechIntent());
   feedback.setText("Listening…");
  }catch(RuntimeException ex){release();feedback.setText("Voice input unavailable. Please type a command or enable Speech Services.");}
 }
 private void submit(String value){
  if(resolved||!alive)return;
  if(value==null||value.trim().isEmpty()){feedback.setText("Say or type a command first.");return;}
  resolved=true;release();
  if(getIntent().getBooleanExtra("FROM_BUBBLE",false) && CommandRouter.isFull()){
   final Command action=CommandParser.parse(value);
   if(BubbleActions.supports(action.kind)){
    final android.content.Context app=getApplicationContext();
    finish();
    new Handler(Looper.getMainLooper()).postDelayed(()->{
      String outcome=BubbleActions.run(action);
      SpeechOut.say(app,outcome);
    },850);
    return;
   }
  }
  Intent main=new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
  main.putExtra("JARVIS_INPUT",value.trim());
  startActivity(main);
  finish();
 }
 @Override protected void onActivityResult(int request,int result,Intent data){
  super.onActivityResult(request,result,data);
  if(!alive||resolved||request!=VOICE_REQUEST)return;
  if(result==RESULT_OK&&data!=null){
   ArrayList<String> text=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
   if(text!=null&&!text.isEmpty()){submit(text.get(0));return;}
  }
  feedback.setText("No speech recognized. Tap Retry Microphone, or type a command.");
 }
 @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){
  super.onRequestPermissionsResult(request,permissions,results);
  if(request==PERMISSION_REQUEST){
   if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)
    new Handler(Looper.getMainLooper()).postDelayed(this::startVoice,200);
   else feedback.setText("Microphone access denied. You can still type.");
  }
 }
 @Override public void onReadyForSpeech(Bundle p){feedback.setText("Listening. Speak now…");}
 @Override public void onBeginningOfSpeech(){feedback.setText("Hearing you…");}
 @Override public void onRmsChanged(float rms){}
 @Override public void onBufferReceived(byte[] b){}
 @Override public void onEndOfSpeech(){feedback.setText("Processing…");}
 @Override public void onError(int e){
  if(!alive||resolved)return;
  release();
  String err;
  switch(e){
   case SpeechRecognizer.ERROR_NETWORK:case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:err="Network unavailable";break;
   case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:case SpeechRecognizer.ERROR_NO_MATCH:err="Didn't hear speech";break;
   case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:err="Microphone permission denied";break;
   case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:err="Speech engine is busy";break;
   case SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED:case SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE:err="Language pack unavailable";break;
   case SpeechRecognizer.ERROR_SERVER_DISCONNECTED:err="Speech provider disconnected";break;
   default:err="Speech engine error "+e;
  }
  feedback.setText(err+". Retry or type a command.");
 }
 @Override public void onResults(Bundle b){
  if(!alive||resolved)return;
  ArrayList<String> r=b==null?null:b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
  if(r!=null&&!r.isEmpty())submit(r.get(0));
  else feedback.setText("No result. Try again or type.");
 }
 @Override public void onPartialResults(Bundle b){}
 @Override public void onEvent(int type,Bundle b){}
 private void release(){if(recognizer!=null){try{recognizer.destroy();}catch(RuntimeException ignored){}recognizer=null;}}
 @Override protected void onDestroy(){alive=false;release();super.onDestroy();}
}
