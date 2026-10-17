package com.jarvis.v3;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class AIClient {
 interface Callback { void done(String text); }
 private static final ExecutorService IO=Executors.newSingleThreadExecutor();
 static void ask(Context context,String question,Callback callback) {
  final Context app=context.getApplicationContext();
  IO.execute(()->{
   String response;
   try {
    String key=SecretStore.get(app);
    if(key.isEmpty()) {
     response="I can perform local phone commands without AI. For general questions, add your own OpenAI API key in Settings.";
    } else {
     HttpURLConnection conn=null;
     try {
      conn=(HttpURLConnection)new URL("https://api.openai.com/v1/chat/completions").openConnection();
      conn.setRequestMethod("POST");conn.setConnectTimeout(12000);conn.setReadTimeout(25000);
      conn.setRequestProperty("Authorization","Bearer "+key);
      conn.setRequestProperty("Content-Type","application/json");
      conn.setDoOutput(true);
      JSONObject message=new JSONObject().put("role","user").put("content",question);
      JSONObject system=new JSONObject().put("role","system").put("content",
       "You are JARVIS Mobile. Be concise, helpful, and reply in the language of the user. "
       +"Do not claim to have controlled the phone. You cannot execute actions from this chat response.");
      JSONObject payload=new JSONObject().put("model","gpt-4.1-mini")
       .put("messages",new JSONArray().put(system).put(message)).put("max_tokens",300);
      byte[] body=payload.toString().getBytes(StandardCharsets.UTF_8);
      conn.getOutputStream().write(body);
      int status=conn.getResponseCode();
      InputStream in=status>=200&&status<300?conn.getInputStream():conn.getErrorStream();
      byte[] bytes=in==null?new byte[0]:readLimited(in,100000);
      if(status>=200&&status<300) {
       JSONObject j=new JSONObject(new String(bytes,StandardCharsets.UTF_8));
       response=j.getJSONArray("choices").getJSONObject(0).getJSONObject("message").optString("content","No reply");
      } else if(status==401||status==403) response="Cloud AI key was rejected. Check your key in Settings.";
      else if(status==429) response="Cloud AI quota or rate limit reached. Try again later.";
      else response="Cloud AI service unavailable (HTTP "+status+"). Try again later.";
     }finally { if(conn!=null)conn.disconnect(); }
    }
   } catch(Exception ex) {response="Cloud AI request failed. Check internet and API settings."; }
   final String result=response;
   new Handler(Looper.getMainLooper()).post(()->callback.done(result));
  });
 }
 private static byte[] readLimited(InputStream in,int max) throws Exception {
  try(InputStream input=in;java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()){
   byte[] buf=new byte[4096];int n;
   while((n=input.read(buf))!=-1){if(out.size()+n>max)throw new IllegalStateException("Response too large");out.write(buf,0,n);}
   return out.toByteArray();
  }
 }
}
