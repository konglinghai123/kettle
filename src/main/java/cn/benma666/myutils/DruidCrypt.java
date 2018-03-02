package cn.benma666.myutils;

import com.alibaba.druid.filter.config.ConfigTools;

public class DruidCrypt {

   public static String[] encrypt(String pwd) throws Exception {
      String[] result = new String[2];
      String[] arr = ConfigTools.genKeyPair(512);
      result[0] = ConfigTools.encrypt(arr[0], pwd);
      result[1] = arr[1];
      return result;
   }

   public static String decrypt(String crypt, String pk) throws Exception {
      return ConfigTools.decrypt(ConfigTools.getPublicKey(pk), crypt);
   }
}
