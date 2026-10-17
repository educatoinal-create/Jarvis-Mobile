package com.jarvis.v3;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

/** Executes only a small set of user-requested Android APIs, never raw model-generated shell/code. */
final class CommandRouter {
 interface Callback {void finished(String answer);}
 private CommandRouter(){}
 static boolean isFull(){return "full".equals(BuildConfig.FLAVOR);}
 static void run(Activity activity,Command command,Callback callback){
  try{
   switch(command.kind){
    case HELP:
     callback.finished("Try: Open WhatsApp; Search for New Delhi; Torch on; What time is it; Call 1234567890. Full mode also supports Back, Home, Scroll, Tap text and Type text.");
     break;
    case TIME:
     callback.finished(new SimpleDateFormat("h:mm a",Locale.getDefault()).format(new java.util.Date()));
     break;
    case OPEN_APP:callback.finished(openApp(activity,command.argument));break;
    case WEB_SEARCH:
     open(activity,Intent.ACTION_VIEW,"https://www.google.com/search?q="+enc(command.argument));
     callback.finished("Searching web for "+command.argument);break;
    case MAP_SEARCH:
     open(activity,Intent.ACTION_VIEW,"geo:0,0?q="+enc(command.argument));
     callback.finished("Opening maps for "+command.argument);break;
    case DIAL:
     String num=command.argument.trim();
     if(!num.matches("[+0-9() -]{3,25}")){callback.finished("For safety, please give a telephone number, or use Contacts to choose a person.");break;}
     open(activity,Intent.ACTION_DIAL,"tel:"+Uri.encode(num));
     callback.finished("Dialer opened. You choose whether to call.");break;
    case TORCH_ON:
    case TORCH_OFF:
     if(activity.checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED) {
      activity.requestPermissions(new String[]{Manifest.permission.CAMERA},MainActivity.REQUEST_CAMERA);
      callback.finished("Allow camera permission, then repeat the torch command."); break;
     }
     callback.finished(torch(activity,command.kind==Command.Kind.TORCH_ON));break;
    case VOLUME_UP:case VOLUME_DOWN:
     AudioManager audio=(AudioManager)activity.getSystemService(Activity.AUDIO_SERVICE);
     if(audio==null){callback.finished("Volume service unavailable.");break;}
     audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,command.kind==Command.Kind.VOLUME_UP?AudioManager.ADJUST_RAISE:AudioManager.ADJUST_LOWER,AudioManager.FLAG_SHOW_UI);
     callback.finished("Media volume updated.");break;
    case HOME:callback.finished(isFull()?ScreenService.home():"Home control is available in Full edition.");break;
    case BACK:callback.finished(isFull()?ScreenService.back():"Back control is available in Full edition.");break;
    case SCROLL_UP:callback.finished(isFull()?ScreenService.swipe(false):"Scrolling is available in Full edition.");break;
    case SCROLL_DOWN:callback.finished(isFull()?ScreenService.swipe(true):"Scrolling is available in Full edition.");break;
    case TAP_TEXT:callback.finished(isFull()?ScreenService.tap(command.argument):"Screen tapping is available in Full edition.");break;
    case TYPE_TEXT:callback.finished(isFull()?ScreenService.type(command.argument):"Screen typing is available in Full edition.");break;
    case READ_SCREEN:callback.finished(isFull()?ScreenService.describe():"Screen reading is available in Full edition.");break;
    case READ_NOTIFICATIONS:callback.finished(isFull()?NotificationService.summarize():"Reading notifications is available in Full edition.");break;
    case AI_QUESTION:AIClient.ask(activity,command.argument,callback::finished);break;
   }
  }catch(SecurityException ex){callback.finished("Android denied this permission. Check JARVIS settings.");}
   catch(Exception ex){callback.finished("Couldn't perform this command on this device. Try a different one.");}
 }
 private static String enc(String s){return Uri.encode(s);}
 private static void open(Activity a,String action,String uri){
  Intent intent=new Intent(action,Uri.parse(uri));
  intent.addCategory(Intent.CATEGORY_DEFAULT);
  a.startActivity(intent);
 }
 private static String torch(Activity a,boolean on){
  try{
   CameraManager manager=(CameraManager)a.getSystemService(Activity.CAMERA_SERVICE);
   for(String id:manager.getCameraIdList()){
    Boolean available=manager.getCameraCharacteristics(id).get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE);
    if(Boolean.TRUE.equals(available)){
     manager.setTorchMode(id,on);
     return on?"Flashlight on.":"Flashlight off.";
    }
   }
   return "This phone has no compatible flashlight.";
  }catch(Exception e){return "Torch unavailable. Another app may be using the camera.";}
 }
 private static String openApp(Activity a,String name) {
  PackageManager pm=a.getPackageManager();
  String lower=name.trim().toLowerCase(Locale.ROOT);
  Map<String,String> known=new HashMap<>();
  known.put("whatsapp","com.whatsapp");known.put("youtube","com.google.android.youtube");
  known.put("chrome","com.android.chrome");known.put("maps","com.google.android.apps.maps");
  known.put("google maps","com.google.android.apps.maps");known.put("instagram","com.instagram.android");
  known.put("telegram","org.telegram.messenger");known.put("spotify","com.spotify.music");
  String pkg=known.get(lower);
  if(pkg!=null){
   Intent launch=pm.getLaunchIntentForPackage(pkg);
   if(launch!=null){a.startActivity(launch);return "Opening "+name;}
  }
  Intent selector=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
  List<ResolveInfo> apps=pm.queryIntentActivities(selector,0);
  for(ResolveInfo r:apps){
   String label=r.loadLabel(pm).toString();
   if(label.equalsIgnoreCase(name)||label.toLowerCase(Locale.ROOT).startsWith(lower+" ")){
    Intent i=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
      .setClassName(r.activityInfo.packageName,r.activityInfo.name).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    a.startActivity(i);return "Opening "+label;
   }
  }
  return "Couldn't find "+name+". Please check the installed app name.";
 }
}
