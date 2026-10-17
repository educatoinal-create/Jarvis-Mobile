package com.jarvis.v3;
import java.util.List;
public class ParserSmokeTest {
 static int count;
 static void check(String input, Command.Kind kind) {
  Command got=CommandParser.parse(input);if(got.kind!=kind) throw new AssertionError(input+" -> "+got); count++;
 }
 public static void main(String[] args) {
  check("help",Command.Kind.HELP);
  check("What can you do help",Command.Kind.HELP);
  check("what time is it",Command.Kind.TIME);
  check("कितने बजे हैं",Command.Kind.TIME);
  check("torch on",Command.Kind.TORCH_ON);
  check("torch off",Command.Kind.TORCH_OFF);
  check("टॉर्च चालू",Command.Kind.TORCH_ON);
  check("टॉर्च बंद",Command.Kind.TORCH_OFF);
  check("volume up",Command.Kind.VOLUME_UP);
  check("volume down",Command.Kind.VOLUME_DOWN);
  check("go home",Command.Kind.HOME);
  check("go back",Command.Kind.BACK);
  check("scroll down",Command.Kind.SCROLL_DOWN);
  check("scroll up",Command.Kind.SCROLL_UP);
  check("स्क्रीन पढ़ो",Command.Kind.READ_SCREEN);
  check("read notifications",Command.Kind.READ_NOTIFICATIONS);
  check("tap send",Command.Kind.TAP_TEXT);
  check("click Continue",Command.Kind.TAP_TEXT);
  check("type Hello Jarvis",Command.Kind.TYPE_TEXT);
  check("write नमस्ते",Command.Kind.TYPE_TEXT);
  check("open WhatsApp",Command.Kind.OPEN_APP);
  check("launch YouTube",Command.Kind.OPEN_APP);
  check("Chrome खोलो",Command.Kind.OPEN_APP);
  check("search web for android",Command.Kind.WEB_SEARCH);
  check("google best pizza",Command.Kind.WEB_SEARCH);
  check("navigate to India Gate",Command.Kind.MAP_SEARCH);
  check("call +919999999999",Command.Kind.DIAL);
  check("dial 100",Command.Kind.DIAL);
  check("explain black holes",Command.Kind.AI_QUESTION);
  check("",Command.Kind.HELP);
  check(null,Command.Kind.HELP);
  List<String> multiple=CommandParser.split("open Chrome then search for Java");
  if(multiple.size()!=2) throw new AssertionError(multiple.toString());count++;
  if(!CommandParser.parse("open Chrome").argument.equals("Chrome")) throw new AssertionError("argument");count++;
  System.out.println(count+" parser assertions passed.");
 }
}
