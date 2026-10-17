package com.jarvis.v3;

public final class Command {
 public enum Kind { HELP, TIME, OPEN_APP, WEB_SEARCH, MAP_SEARCH, DIAL, TORCH_ON, TORCH_OFF, VOLUME_UP, VOLUME_DOWN, HOME, BACK, SCROLL_UP, SCROLL_DOWN, TAP_TEXT, TYPE_TEXT, READ_SCREEN, READ_NOTIFICATIONS, AI_QUESTION }
 public final Kind kind;
 public final String argument;
 public Command(Kind kind, String argument) { this.kind=kind; this.argument=argument==null?"":argument; }
 @Override public String toString() {return kind+"("+argument+")";}
}
