package cn.benma666.myutils;

import cn.benma666.myutils.StringUtil;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

   private static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
   private static final String encodingAlgorithm = "MD5";
   private static String characterEncoding;


   public static String encode(String srcStr) {
      if(srcStr == null) {
         return null;
      } else {
         try {
            MessageDigest e = MessageDigest.getInstance("MD5");
            if(StringUtil.isNotBlank(characterEncoding)) {
               e.update(srcStr.getBytes(characterEncoding));
            } else {
               e.update(srcStr.getBytes());
            }

            byte[] digest = e.digest();
            return getFormattedText(digest);
         } catch (NoSuchAlgorithmException var3) {
            throw new SecurityException(var3);
         } catch (UnsupportedEncodingException var4) {
            throw new RuntimeException(var4);
         }
      }
   }

   private static String getFormattedText(byte[] bytes) {
      StringBuilder buf = new StringBuilder(bytes.length * 2);

      for(int j = 0; j < bytes.length; ++j) {
         buf.append(HEX_DIGITS[bytes[j] >> 4 & 15]);
         buf.append(HEX_DIGITS[bytes[j] & 15]);
      }

      return buf.toString();
   }

   public static String getCharacterEncoding() {
      return characterEncoding;
   }

   public static void setCharacterEncoding(String characterEncoding) {
      characterEncoding = characterEncoding;
   }

   public static String getEncodingalgorithm() {
      return "MD5";
   }

}
