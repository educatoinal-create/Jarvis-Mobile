package com.jarvis.v3;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

/** Offline, deterministic command parser; no Android dependencies and no remote execution. */
public final class CommandParser {
 private CommandParser() {}
 public static List<String> split(String input) {
   List<String> parts=new ArrayList<>();
   if(input==null) return parts;
   for(String s: input.split("(?i)\\s+(?:and then|then|और फिर|उसके बाद|फिर)\\s+")) {
     s=s.trim(); if(!s.isEmpty()) parts.add(s);
   }
   if(parts.isEmpty() && !input.trim().isEmpty()) parts.add(input.trim());
   return parts;
 }
 public static Command parse(String raw) {
   if(raw==null || raw.trim().isEmpty()) return new Command(Command.Kind.HELP,"");
   String s=raw.trim();
   String lower=s.toLowerCase(Locale.ROOT);
   if(lower.matches("(?is).*(?:help|commands|क्या कर सकते|मदद).*")) return new Command(Command.Kind.HELP,"");
   if(lower.matches("(?is).*(?:what time|current time|time now|समय|कितने बजे).*")) return new Command(Command.Kind.TIME,"");
   if(lower.matches("(?is).*(?:flashlight|torch|टॉर्च|फ़्लैशलाइट).*(?:off|बंद).*") || lower.matches("(?is).*(?:turn off|बंद करो).*(?:flashlight|torch|टॉर्च).*")) return new Command(Command.Kind.TORCH_OFF,"");
   if(lower.matches("(?is).*(?:flashlight|torch|टॉर्च|फ़्लैशलाइट).*(?:on|चालू|जलाओ).*") || lower.matches("(?is).*(?:turn on|चालू करो).*(?:flashlight|torch|टॉर्च).*")) return new Command(Command.Kind.TORCH_ON,"");
   if(lower.matches("(?is).*(?:volume up|increase volume|आवाज़ बढ़ाओ|वॉल्यूम बढ़ाओ).*")) return new Command(Command.Kind.VOLUME_UP,"");
   if(lower.matches("(?is).*(?:volume down|decrease volume|आवाज़ कम करो|वॉल्यूम कम करो).*")) return new Command(Command.Kind.VOLUME_DOWN,"");
   if(lower.matches("(?is).*(?:go home|home screen|होम स्क्रीन|घर वाली स्क्रीन).*")) return new Command(Command.Kind.HOME,"");
   if(lower.matches("(?is).*(?:go back|back button|पीछे जाओ|वापस जाओ).*")) return new Command(Command.Kind.BACK,"");
   if(lower.matches("(?is).*(?:scroll down|swipe up|नीचे स्क्रॉल).*")) return new Command(Command.Kind.SCROLL_DOWN,"");
   if(lower.matches("(?is).*(?:scroll up|swipe down|ऊपर स्क्रॉल).*")) return new Command(Command.Kind.SCROLL_UP,"");
   if(lower.matches("(?is).*(?:read screen|describe screen|स्क्रीन पढ़ो|स्क्रीन पर क्या).*")) return new Command(Command.Kind.READ_SCREEN,"");
   if(lower.matches("(?is).*(?:read notifications|my notifications|नोटिफिकेशन पढ़ो).*")) return new Command(Command.Kind.READ_NOTIFICATIONS,"");
   String x=extract(s,lower,new String[]{"tap ","click ","press ","क्लिक करो ","दबाओ "});
   if(x!=null) return new Command(Command.Kind.TAP_TEXT,x);
   x=extract(s,lower,new String[]{"type ","write ","लिखो ","टाइप करो "});
   if(x!=null) return new Command(Command.Kind.TYPE_TEXT,x);
   x=extract(s,lower,new String[]{"open ","launch ","खोलो ","ओपन करो "});
   if(x!=null) return new Command(Command.Kind.OPEN_APP,x);
   if(lower.endsWith(" खोलो")) return new Command(Command.Kind.OPEN_APP,s.substring(0,s.length()-" खोलो".length()).trim());
   x=extract(s,lower,new String[]{"search web for ","search for ","google ","search ","सर्च करो ","खोजो "});
   if(x!=null) return new Command(Command.Kind.WEB_SEARCH,x);
   x=extract(s,lower,new String[]{"navigate to ","directions to ","map ","maps ","रास्ता दिखाओ ","मैप पर "});
   if(x!=null) return new Command(Command.Kind.MAP_SEARCH,x);
   x=extract(s,lower,new String[]{"call ","dial ","कॉल करो ","फोन लगाओ "});
   if(x!=null) return new Command(Command.Kind.DIAL,x);
   return new Command(Command.Kind.AI_QUESTION,s);
 }
 private static String extract(String raw,String lower,String[] prefixes) {
   for(String x:prefixes) if(lower.startsWith(x)) {
     String value=raw.substring(x.length()).trim();
     if(!value.isEmpty()) return value;
   }
   return null;
 }
}
