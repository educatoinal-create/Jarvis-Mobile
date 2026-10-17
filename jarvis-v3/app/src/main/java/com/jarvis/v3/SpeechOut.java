package com.jarvis.v3;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

final class SpeechOut {
 private static TextToSpeech tts;
 private static boolean ready=false;
 static synchronized void say(Context context,String words) {
  if(words==null||words.isEmpty()||words.length()>1500) return;
  if(tts==null) {
   final Context app=context.getApplicationContext();
   tts=new TextToSpeech(app,status->{
    ready=status==TextToSpeech.SUCCESS;
    if(ready) {
     String lang=app.getSharedPreferences("jarvis_prefs",0).getString("lang","hi-IN");
     tts.setLanguage(Locale.forLanguageTag(lang));
     try {tts.speak(words,TextToSpeech.QUEUE_FLUSH,null,"jarvis-v3");}catch(RuntimeException ignored){}
    }
   });
  } else if(ready) {
   try {tts.speak(words,TextToSpeech.QUEUE_FLUSH,null,"jarvis-v3");}catch(RuntimeException ignored){}
  }
 }
 static synchronized void stop() {if(tts!=null)try{tts.stop();}catch(RuntimeException ignored){}}
}
