package com.jarvis.v3;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

/** Full edition only: user-enabled, active-window UI automation. */
public final class ScreenService extends AccessibilityService {
 private static volatile ScreenService active;
 public static boolean enabled(){return active!=null;}
 @Override protected void onServiceConnected(){super.onServiceConnected();active=this;}
 @Override public void onAccessibilityEvent(AccessibilityEvent e){}
 @Override public void onInterrupt(){}
 @Override public void onDestroy(){if(active==this)active=null;super.onDestroy();}
 static String home(){return action(GLOBAL_ACTION_HOME)?"Home":"Could not go Home";}
 static String back(){return action(GLOBAL_ACTION_BACK)?"Back":"Could not go Back";}
 private static boolean action(int which){ScreenService x=active;return x!=null&&x.performGlobalAction(which);}
 static String swipe(boolean down) {
  ScreenService s=active;
  if(s==null)return "Enable JARVIS Screen Control in Accessibility settings.";
  android.util.DisplayMetrics m=s.getResources().getDisplayMetrics();
  float x=m.widthPixels*.5f,y1=m.heightPixels*(down?.75f:.3f),y2=m.heightPixels*(down?.3f:.75f);
  Path path=new Path();path.moveTo(x,y1);path.lineTo(x,y2);
  GestureDescription g=new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path,0,380)).build();
  return s.dispatchGesture(g,null,null)?"Swiped":"Swipe could not start";
 }
 static String tap(String text) {
  ScreenService s=active;if(s==null)return "Enable Screen Control in Accessibility settings.";
  AccessibilityNodeInfo root=s.getRootInActiveWindow();
  if(root==null)return "No accessible screen. Open an app first.";
  try {
   List<AccessibilityNodeInfo> nodes=root.findAccessibilityNodeInfosByText(text);
   for(AccessibilityNodeInfo n:nodes) {
    if(n.isPassword())continue;
    AccessibilityNodeInfo candidate=n;
    for(int i=0;candidate!=null&&i<5;i++,candidate=candidate.getParent()){
     if(candidate.isClickable() && candidate.performAction(AccessibilityNodeInfo.ACTION_CLICK))return "Tapped "+text;
    }
   }
   return "Couldn't find a clickable item named "+text;
  } finally {root.recycle();}
 }
 static String type(String text) {
  ScreenService s=active;if(s==null)return "Enable Screen Control in Accessibility settings.";
  AccessibilityNodeInfo root=s.getRootInActiveWindow();
  if(root==null)return "No accessible screen.";
  try{
   AccessibilityNodeInfo focus=root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
   if(focus==null||!focus.isEditable()||focus.isPassword())return "Select a normal text field first.";
   Bundle b=new Bundle();b.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,text);
   return focus.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,b)?"Text inserted":"Cannot type in this field";
  }finally{root.recycle();}
 }
 static String describe(){
  ScreenService s=active;if(s==null)return "Enable Screen Control in Accessibility settings.";
  AccessibilityNodeInfo root=s.getRootInActiveWindow();if(root==null)return "No accessible window.";
  StringBuilder out=new StringBuilder();try{walk(root,out,0);}finally{root.recycle();}
  return out.length()==0?"No visible accessible text.":out.toString();
 }
 private static void walk(AccessibilityNodeInfo n,StringBuilder out,int depth){
  if(n==null||depth>12||out.length()>500||n.isPassword())return;
  CharSequence txt=n.getText();
  if(txt!=null&&txt.length()>0){String t=txt.toString().replace('\n',' ').trim();if(!t.isEmpty()&&!t.matches("(?i).*(otp|one.time.pass|verification code|cvv|password).*"))out.append(t,0,Math.min(t.length(),Math.min(80,600-out.length()))).append(". ");}
  for(int i=0;i<n.getChildCount()&&out.length()<500;i++)walk(n.getChild(i),out,depth+1);
 }
}
