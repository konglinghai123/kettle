package cn.benma666.myutils;

import java.io.IOException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class DesUtil {

   private static final String DES = "DES";


   public static void main(String[] args) throws Exception {
      String key = "s2&%34%7(*7s!@8#";
      System.err.println(decrypt("bwdEW1ceMigQZPLlrpjKLg==", key));
   }

   public static String encrypt(String data, String key) {
      byte[] bt = null;

      try {
         bt = encrypt(data.getBytes("utf-8"), key.getBytes());
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      String strs = (new BASE64Encoder()).encode(bt);
      return strs;
   }

   public static String decrypt(String data, String key) throws IOException, Exception {
      if(data == null) {
         return null;
      } else {
         BASE64Decoder decoder = new BASE64Decoder();
         byte[] buf = decoder.decodeBuffer(data);
         byte[] bt = decrypt(buf, key.getBytes());
         return new String(bt, "utf-8");
      }
   }

   private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
      SecureRandom sr = new SecureRandom();
      DESKeySpec dks = new DESKeySpec(key);
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
      SecretKey securekey = keyFactory.generateSecret(dks);
      Cipher cipher = Cipher.getInstance("DES");
      cipher.init(1, securekey, sr);
      return cipher.doFinal(data);
   }

   private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
      SecureRandom sr = new SecureRandom();
      DESKeySpec dks = new DESKeySpec(key);
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
      SecretKey securekey = keyFactory.generateSecret(dks);
      Cipher cipher = Cipher.getInstance("DES");
      cipher.init(2, securekey, sr);
      return cipher.doFinal(data);
   }
}
