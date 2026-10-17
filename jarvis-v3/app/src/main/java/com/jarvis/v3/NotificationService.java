package com.jarvis.v3;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.app.Notification;

public final class NotificationService extends NotificationListenerService {
 private static volatile NotificationService active;
 @Override public void onListenerConnected(){super.onListenerConnected();active=this;}
 @Override public void onListenerDisconnected(){if(active==this)active=null;super.onListenerDisconnected();}
 @Override public void onDestroy(){if(active==this)active=null;super.onDestroy();}
 static String summarize(){
  NotificationService s=active;if(s==null)return "Notification access is off. Enable it in Android Settings if you want this feature.";
  try{
   StatusBarNotification[] notes=s.getActiveNotifications();
   if(notes==null||notes.length==0)return "No active notifications.";
   StringBuilder result=new StringBuilder("Recent notifications: ");int shown=0;
   for(StatusBarNotification item:notes){
    if(shown>=5)break;
    String pkg=item.getPackageName().toLowerCase();
    if(pkg.contains("bank")||pkg.contains("auth")||pkg.contains("wallet")||pkg.contains("pay"))continue;
    Notification n=item.getNotification();
    if(n==null||n.extras==null)continue;
    String title=n.extras.getCharSequence(Notification.EXTRA_TITLE,"").toString();
    String message=n.extras.getCharSequence(Notification.EXTRA_TEXT,"").toString();
    if((title+" "+message).toLowerCase().matches("(?s).*(otp|verification code|security code|password|passcode|cvv).*"))continue;
    if(!title.isEmpty()){result.append(title.substring(0,Math.min(48,title.length()))).append(". ");shown++;}
   }
   return shown==0?"No safe notification summaries available.":result.toString();
  }catch(RuntimeException e){return "Cannot read notifications right now.";}
 }
}
