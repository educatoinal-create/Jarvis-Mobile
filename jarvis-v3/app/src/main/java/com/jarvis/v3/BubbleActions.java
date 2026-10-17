package com.jarvis.v3;

/** Run full-edition screen actions after the speech activity closes, revealing the target app. */
final class BubbleActions {
 static boolean supports(Command.Kind kind) {
  switch(kind){
   case HOME:case BACK:case SCROLL_UP:case SCROLL_DOWN:
   case TAP_TEXT:case TYPE_TEXT:case READ_SCREEN:case READ_NOTIFICATIONS:return true;
   default:return false;
  }
 }
 static String run(Command cmd){
  if(!ScreenService.enabled() && cmd.kind!=Command.Kind.READ_NOTIFICATIONS)
   return "Screen Control isn't enabled. Please enable JARVIS in Android Accessibility settings.";
  try {
   switch(cmd.kind){
    case HOME:return ScreenService.home();
    case BACK:return ScreenService.back();
    case SCROLL_UP:return ScreenService.swipe(false);
    case SCROLL_DOWN:return ScreenService.swipe(true);
    case TAP_TEXT:
     if(cmd.argument.toLowerCase(java.util.Locale.ROOT).matches("(?s).*(send|pay|delete|buy|transfer|confirm|allow|submit|भेज|पेमेंट).*"))
      return "Sensitive taps must be confirmed from inside JARVIS. Open the main app.";
     return ScreenService.tap(cmd.argument);
    case TYPE_TEXT:return ScreenService.type(cmd.argument);
    case READ_SCREEN:return ScreenService.describe();
    case READ_NOTIFICATIONS:return NotificationService.summarize();
    default:return "Unsupported screen action.";
   }
  }catch(RuntimeException ex){return "Couldn't safely control the target screen.";}
 }
}
