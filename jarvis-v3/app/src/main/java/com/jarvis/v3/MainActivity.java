package com.jarvis.v3;
import android.app.Activity;
import android.app.AlertDialog;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.net.Uri;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;
import java.util.Locale;

/** Permission-first UI with in-app diagnostics and explicit controls. */
public final class MainActivity extends Activity {
 static final int REQUEST_CAMERA=611;
 private static final int TEXT=Color.rgb(230,246,250),MUTED=Color.rgb(161,185,202),ACCENT=Color.rgb(67,230,202);
 private TextView output,health;
 private EditText input;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private String pending="";
 private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
 private TextView label(LinearLayout host,String value,int size,int color){
  TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(11);host.addView(t,p);return t;
 }
 private Button button(LinearLayout parent,String title,Runnable task){
  Button b=new Button(this);b.setText(title);b.setAllCaps(false);
  b.setTextColor(Color.WHITE);b.setTextSize(14);
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(48));p.bottomMargin=dp(7);
  parent.addView(b,p);b.setOnClickListener(v->task.run());return b;
 }
 @Override public void onCreate(Bundle state){
  super.onCreate(state);
  getWindow().setStatusBarColor(Color.rgb(5,16,31));getWindow().setNavigationBarColor(Color.rgb(5,16,31));
  ScrollView scroll=new ScrollView(this);
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
  root.setBackgroundColor(Color.rgb(7,19,35));root.setPadding(dp(22),dp(28),dp(22),dp(26));
  scroll.addView(root);setContentView(scroll);
  TextView t=label(root,"◈  JARVIS 3",33,ACCENT);t.setTypeface(null,Typeface.BOLD);
  label(root,CommandRouter.isFull()?"FULL EDITION  •  ADVANCED PHONE CONTROLS":"LITE EDITION  •  SAFE VOICE + AI",12,MUTED);
  label(root,"Speak naturally. Commands run locally where possible. Cloud AI is optional.",15,TEXT);
  health=label(root,"Checking services…",13,MUTED);
  button(root,"🎤  START VOICE COMMAND",()->startActivity(new Intent(this,VoiceActivity.class)));
  input=new EditText(this);input.setTextColor(TEXT);input.setHintTextColor(MUTED);
  input.setSingleLine(true);input.setHint("Try: open Chrome / टॉर्च चालू");
  LinearLayout.LayoutParams inputParams=new LinearLayout.LayoutParams(-1,dp(55));
  root.addView(input,inputParams);
  button(root,"▶  RUN TYPED COMMAND",()->runInput(input.getText().toString()));
  label(root,"ASSISTANT RESPONSE",12,ACCENT);
  output=label(root,"Ready. Type a command or tap the microphone.",16,TEXT);
  label(root,"QUICK TESTS",12,ACCENT);
  button(root,"Test 1 · Time",()->runInput("what time is it"));
  button(root,"Test 2 · Open Chrome",()->runInput("open Chrome"));
  button(root,"Test 3 · Open maps",()->runInput("navigate to New Delhi"));
  button(root,"Test 4 · Volume up",()->runInput("volume up"));
  button(root,"Test 5 · AI answer",()->runInput("Tell me a short joke"));
  label(root,"SETTINGS & PERMISSIONS",12,ACCENT);
  button(root,"Language · Hindi / English",this::toggleLanguage);
  button(root,"AI API key · Add / Remove (optional)",this::editKey);
  if(CommandRouter.isFull()){
   label(root,"Full controls can view and operate the current screen. Enable only if you trust the app.",13,MUTED);
   button(root,"Enable Screen Control (Accessibility)",()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
   button(root,"Notification Listener Settings",()->startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));
   button(root,"Allow floating microphone bubble",()->{
    try{startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));}
    catch(RuntimeException ex){say("Overlay settings unavailable.");}
   });
   button(root,"Start floating microphone bubble",this::enableBubble);
   button(root,"Stop floating microphone bubble",()->{stopService(new Intent(this,BubbleService.class));say("Bubble stopped.");});
   button(root,"Test 6 · Read visible screen",()->runInput("read screen"));
   button(root,"Test 7 · Read notifications",()->runInput("read notifications"));
   button(root,"Test 8 · Scroll down",()->runInput("scroll down"));
  }
  label(root,"SAFETY",12,ACCENT);
  label(root,"No always-on background microphone. No automatic SMS sending or payments. Phone calls use the dialer. Screen tapping of sensitive controls requires confirmation.",13,MUTED);
  checkIntent(getIntent());
 }
 @Override protected void onResume(){super.onResume();if(health!=null)health.setText(
  "Speech: "+(android.speech.SpeechRecognizer.isRecognitionAvailable(this)?"available":"check phone setup")
  +"  •  Mic: "+(checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED?"allowed":"not allowed")
  +"  •  Screen: "+(CommandRouter.isFull()?(ScreenService.enabled()?"enabled":"not enabled"):"Lite"));}
 @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);checkIntent(intent);}
 private void checkIntent(Intent intent){
  if(intent!=null&&intent.hasExtra("JARVIS_INPUT")){
   String command=intent.getStringExtra("JARVIS_INPUT");intent.removeExtra("JARVIS_INPUT");
   handler.post(()->runInput(command));
  }
 }
 private void runInput(String text){
  if(text==null||text.trim().isEmpty()){say("Type or speak a command first.");return;}
  List<String> commands=CommandParser.split(text);
  if(commands.size()>3){say("Please use a maximum of 3 linked commands at once.");return;}
  executeStep(commands,0);
 }
 private void executeStep(List<String> commands,int index){
  if(index>=commands.size())return;
  Command command=CommandParser.parse(commands.get(index));
  Runnable work=()->{
   say("Running: "+commands.get(index));
   CommandRouter.run(this,command,response->{
    if(isFinishing()||isDestroyed())return;
    say(response);SpeechOut.say(this,response);
    if(index+1<commands.size())handler.postDelayed(()->executeStep(commands,index+1),800);
   });
  };
  if(command.kind==Command.Kind.TAP_TEXT && command.argument.toLowerCase(Locale.ROOT).matches("(?s).*(?:send|delete|pay|buy|confirm|allow|submit|transfer|भेज|हटाओ|पेमेंट).*")){
   new AlertDialog.Builder(this).setTitle("Confirm screen action").setMessage("Tap '"+command.argument+"' on the current screen? Inspect the app first.")
     .setPositiveButton("Yes, tap",(d,w)->work.run()).setNegativeButton("Cancel",(d,w)->say("Cancelled.")).show();
  }else work.run();
 }
 private void say(String message){if(output!=null)output.setText(message);}
 private void toggleLanguage(){
  android.content.SharedPreferences p=getSharedPreferences("jarvis_prefs",MODE_PRIVATE);
  String next="hi-IN".equals(p.getString("lang","hi-IN"))?"en-IN":"hi-IN";
  p.edit().putString("lang",next).apply();say("Voice language: "+next+". You may need installed speech language packs.");
 }
 private void editKey(){
  EditText edit=new EditText(this);edit.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);edit.setHint("sk-…");
  new AlertDialog.Builder(this).setTitle("Optional OpenAI API key")
   .setMessage("Stored encrypted in Android Keystore. Only AI questions use the cloud API. Never put API keys in GitHub.")
   .setView(edit).setPositiveButton("Save",(d,w)->{
     try{SecretStore.put(this,edit.getText().toString());say("API key saved securely (or cleared if empty).");}
     catch(Exception ex){say("Key storage failed on this device.");}
   }).setNeutralButton("Remove key",(d,w)->{SecretStore.clear(this);say("AI key removed.");})
   .setNegativeButton("Cancel",null).show();
 }
 private void enableBubble(){
  if(!CommandRouter.isFull()){say("Bubble is only available in Full edition.");return;}
  if(!Settings.canDrawOverlays(this)){say("Allow 'Display over other apps' first.");return;}
  try{
   Intent svc=new Intent(this,BubbleService.class);
   if(Build.VERSION.SDK_INT>=26)startForegroundService(svc);else startService(svc);
   say("Starting optional floating mic. Stop it here any time.");
  }catch(RuntimeException ex){say("Bubble could not start. Check Android notifications and battery restrictions.");}
 }
 @Override public void onRequestPermissionsResult(int req,String[] permissions,int[] results){
  super.onRequestPermissionsResult(req,permissions,results);
  if(req==REQUEST_CAMERA)say(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED?
    "Camera permission allowed. Try torch on again.":"Camera permission denied. Torch will not work.");
 }
 @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy();}
}
