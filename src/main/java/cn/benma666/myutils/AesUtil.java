package cn.benma666.myutils;

import cn.benma666.myutils.StringUtil;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class AesUtil {

   public static void main(String[] args) throws Exception {
      String content = "杀破狼很暴力？？？？！！！！！Office很卡**（）@*@ qqqqQQQQQQQQQQ`````OVER";
      System.out.println("加密前：" + content);
      String key = "OFRna73m*aze01xY";
      System.out.println("加密密钥和解密密钥：" + key);
      String encrypt = aesEncrypt1(content, key);
      System.out.println("加密后：" + encrypt);
      String decrypt = aesDecrypt1(encrypt, key);
      System.out.println("解密后：" + decrypt);
   }

   public static String aesEncrypt1(String str, String key) throws Exception {
      if(str != null && key != null) {
         Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
         cipher.init(1, new SecretKeySpec(key.getBytes("utf-8"), "AES"));
         byte[] bytes = cipher.doFinal(str.getBytes("utf-8"));
         return (new BASE64Encoder()).encode(bytes);
      } else {
         return null;
      }
   }

   public static String aesDecrypt1(String str, String key) throws Exception {
      if(str != null && key != null) {
         Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
         cipher.init(2, new SecretKeySpec(key.getBytes("utf-8"), "AES"));
         byte[] bytes = (new BASE64Decoder()).decodeBuffer(str);
         bytes = cipher.doFinal(bytes);
         return new String(bytes, "utf-8");
      } else {
         return null;
      }
   }

   public static String binary(byte[] bytes, int radix) {
      return (new BigInteger(1, bytes)).toString(radix);
   }

   public static String base64Encode(byte[] bytes) {
      return (new BASE64Encoder()).encode(bytes);
   }

   public static byte[] base64Decode(String base64Code) throws Exception {
      return StringUtil.isEmpty(base64Code)?null:(new BASE64Decoder()).decodeBuffer(base64Code);
   }

   public static byte[] md5(byte[] bytes) throws Exception {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(bytes);
      return md.digest();
   }

   public static byte[] md5(String msg) throws Exception {
      return StringUtil.isEmpty(msg)?null:md5(msg.getBytes());
   }

   public static String md5Encrypt(String msg) throws Exception {
      return StringUtil.isEmpty(msg)?null:base64Encode(md5(msg));
   }

   public static byte[] aesEncryptToBytes(String content, String encryptKey) throws Exception {
      KeyGenerator kgen = KeyGenerator.getInstance("AES");
      kgen.init(128, new SecureRandom(encryptKey.getBytes()));
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(1, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));
      return cipher.doFinal(content.getBytes("utf-8"));
   }

   public static String aesEncrypt(String content, String encryptKey) throws Exception {
      return base64Encode(aesEncryptToBytes(content, encryptKey));
   }

   public static String aesDecryptByBytes(byte[] encryptBytes, String decryptKey) throws Exception {
      KeyGenerator kgen = KeyGenerator.getInstance("AES");
      kgen.init(128, new SecureRandom(decryptKey.getBytes()));
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(2, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));
      byte[] decryptBytes = cipher.doFinal(encryptBytes);
      return new String(decryptBytes);
   }

   public static String aesDecrypt(String encryptStr, String decryptKey) throws Exception {
      return StringUtil.isEmpty(encryptStr)?null:aesDecryptByBytes(base64Decode(encryptStr), decryptKey);
   }
}
