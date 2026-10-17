package com.jarvis.v3;
import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.View;
import android.widget.TextView;

public final class BubbleService extends Service {
 private WindowManager wm;
 private View bubble;
 @Override public void onCreate(){
  super.onCreate();
  if(!Settings.canDrawOverlays(this)){stopSelf();return;}
  String channel="jarvis-v3-bubble";
  NotificationManager mgr=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
  if(Build.VERSION.SDK_INT>=26)mgr.createNotificationChannel(new NotificationChannel(channel,"JARVIS floating mic",NotificationManager.IMPORTANCE_LOW));
  Intent open=new Intent(this,MainActivity.class);
  PendingIntent pi=PendingIntent.getActivity(this,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Notification n=new Notification.Builder(this,channel).setContentTitle("JARVIS floating mic enabled")
     .setContentText("Tap bubble to speak. Stop from inside JARVIS.")
     .setSmallIcon(android.R.drawable.ic_btn_speak_now).setContentIntent(pi).build();
  try{startForeground(43,n);}catch(RuntimeException e){stopSelf();return;}
  wm=(WindowManager)getSystemService(WINDOW_SERVICE);
  TextView view=new TextView(this);
  view.setText("🎤");view.setTextSize(26);view.setGravity(Gravity.CENTER);
  GradientDrawable background=new GradientDrawable();background.setColor(Color.rgb(18,66,99));background.setCornerRadius(100);
  view.setBackground(background);
  int px=Math.round(62*getResources().getDisplayMetrics().density);
  WindowManager.LayoutParams lp=new WindowManager.LayoutParams(px,px,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
  lp.gravity=Gravity.RIGHT|Gravity.CENTER_VERTICAL;
  view.setOnClickListener(v->{
   try {Intent voice=new Intent(this,VoiceActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("FROM_BUBBLE",true);startActivity(voice);}
   catch(RuntimeException ignored){}
  });
  try{wm.addView(view,lp);bubble=view;}catch(RuntimeException ex){stopSelf();}
 }
 @Override public void onDestroy(){
  if(wm!=null&&bubble!=null)try{wm.removeView(bubble);}catch(RuntimeException ignored){}
  super.onDestroy();
 }
 @Override public IBinder onBind(Intent i){return null;}
}
