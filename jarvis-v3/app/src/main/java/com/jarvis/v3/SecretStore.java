package com.jarvis.v3;
import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.KeyStore;

/** Never write plaintext API keys to a SharedPreferences file. */
final class SecretStore {
 private static final String ALIAS="JarvisV3OptionalCloudKey";
 private static final String PREF="jarvis_secure";
 private static SecretKey key() throws Exception {
  KeyStore ks=KeyStore.getInstance("AndroidKeyStore"); ks.load(null);
  if(ks.containsAlias(ALIAS)) return ((KeyStore.SecretKeyEntry)ks.getEntry(ALIAS,null)).getSecretKey();
  KeyGenerator g=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
  g.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT)
    .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256).build());
  return g.generateKey();
 }
 static void put(Context c,String secret) throws Exception {
  if(secret==null||secret.trim().isEmpty()) {clear(c);return;}
  Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key());
  byte[] encrypted=cipher.doFinal(secret.trim().getBytes(StandardCharsets.UTF_8));
  SharedPreferences p=c.getSharedPreferences(PREF,Context.MODE_PRIVATE);
  p.edit().putString("cipher",Base64.encodeToString(encrypted,Base64.NO_WRAP))
   .putString("iv",Base64.encodeToString(cipher.getIV(),Base64.NO_WRAP)).apply();
 }
 static String get(Context c) {
  SharedPreferences p=c.getSharedPreferences(PREF,Context.MODE_PRIVATE);
  String enc=p.getString("cipher",""),iv=p.getString("iv","");
  if(enc.isEmpty()||iv.isEmpty())return "";
  try{
   Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
   cipher.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(iv,Base64.DEFAULT)));
   return new String(cipher.doFinal(Base64.decode(enc,Base64.DEFAULT)),StandardCharsets.UTF_8);
  }catch(Exception ex){return "";}
 }
 static void clear(Context c) { c.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().remove("cipher").remove("iv").apply(); }
}
