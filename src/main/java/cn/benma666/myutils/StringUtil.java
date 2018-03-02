package cn.benma666.myutils;

import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

public class StringUtil extends StringUtils {

   public static String getUUIDUpperStr() {
      return UUID.randomUUID().toString().replace("-", "").toUpperCase();
   }

   public static String replace(String str) {
      if(StringUtils.isNotEmpty(str)) {
         str = str.replace("\'", "\'\'").replace("%", "\\%").replace("\\", "\\\\").replace("_", "\\_");
      }

      return str;
   }

   public static String removeEndStr(String str, String removeStr) {
      String str2;
      for(str2 = StringUtils.removeEnd(str, removeStr); !str2.equals(str); str2 = StringUtils.removeEnd(str2, removeStr)) {
         str = str2;
      }

      return str2;
   }

   public static Long[] convertStringToLongArray(String str) {
      String[] strArray = str.split(",");
      Long[] strLongArray = new Long[strArray.length];

      for(int i = 0; i < strArray.length; ++i) {
         strLongArray[i] = Long.valueOf(Long.parseLong(strArray[i]));
      }

      return strLongArray;
   }

   public static String getLimitLengthString(String str, int maxlength) throws UnsupportedEncodingException {
      int counterOfDoubleByte = 0;
      byte[] b = str.getBytes("gbk");
      if(b.length < maxlength) {
         return str;
      } else {
         for(int i = 0; i < maxlength; ++i) {
            if(b[i] < 0) {
               ++counterOfDoubleByte;
            }
         }

         if(counterOfDoubleByte % 2 == 0) {
            return new String(b, 0, maxlength, "gbk");
         } else {
            return new String(b, 0, maxlength - 1, "gbk");
         }
      }
   }

   public static String substring(String str, int srcPos, int specialCharsLength) {
      if(str != null && !"".equals(str) && specialCharsLength >= 1) {
         if(srcPos < 0) {
            srcPos = 0;
         }

         if(specialCharsLength <= 0) {
            return "";
         } else {
            char[] chars = str.toCharArray();
            if(srcPos > chars.length) {
               return "";
            } else {
               int charsLength = getCharsLength(chars, specialCharsLength);
               return new String(chars, srcPos, charsLength);
            }
         }
      } else {
         return "";
      }
   }

   private static int getCharsLength(char[] chars, int specialCharsLength) {
      int count = 0;
      int normalCharsLength = 0;

      for(int i = 0; i < chars.length; ++i) {
         int specialCharLength = getSpecialCharLength(chars[i]);
         if(count > specialCharsLength - specialCharLength) {
            break;
         }

         count += specialCharLength;
         ++normalCharsLength;
      }

      return normalCharsLength;
   }

   private static int getSpecialCharLength(char c) {
      return isLetter(c)?1:2;
   }

   public static boolean isLetter(char c) {
      short k = 128;
      return c / k == 0;
   }

   public static Boolean validateNumber(String str) {
      if(StringUtils.isBlank(str)) {
         return Boolean.valueOf(false);
      } else {
         Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?$");
         Matcher matcher = pattern.matcher(str);
         return Boolean.valueOf(matcher.matches());
      }
   }

   public static String subZeroAndDot(String str) {
      if(StringUtils.isNotBlank(str)) {
         if(validateNumber(str).booleanValue()) {
            if(!str.contains(".")) {
               return str;
            }

            str = str.replaceAll("[.]*?0+$", "");
            str = str.replaceAll("[.]$", "");
         }

         return str;
      } else {
         return "";
      }
   }

   public static String converDoubleToString(Double d) {
      DecimalFormat df = new DecimalFormat("0.00000000000000000000");
      return d != null?subZeroAndDot(df.format(d)):"";
   }

   public static int getChineseLength(String value) {
      int valueLength = 0;
      String chinese = "[Α-￥]";
      if(StringUtils.isNotBlank(value)) {
         for(int i = 0; i < value.length(); ++i) {
            String temp = value.substring(i, i + 1);
            if(temp.matches(chinese)) {
               valueLength += 2;
            } else {
               ++valueLength;
            }
         }
      }

      return valueLength;
   }

   public static Integer parseInt(Object obj) {
      return obj != null && !isBlank(obj.toString())?Integer.valueOf(Integer.parseInt(obj.toString())):Integer.valueOf(0);
   }

   public static long parseLong(Object obj) {
      return parseInt(obj).longValue();
   }

   public static String underlineTohump(String name) {
      if(isBlank(name)) {
         return null;
      } else {
         StringBuffer sb = new StringBuffer();
         String[] ns = name.toLowerCase().split("_");
         boolean isFirst = true;
         String[] arr$ = ns;
         int len$ = ns.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String n = arr$[i$];
            if(isFirst) {
               isFirst = false;
               sb.append(n);
            } else if(n.length() < 2) {
               sb.append(n.toUpperCase());
            } else {
               sb.append(n.substring(0, 1).toUpperCase());
               sb.append(n.substring(1));
            }
         }

         return sb.toString();
      }
   }

   public static String upperCaseFast(String name) {
      return isBlank(name)?null:name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
   }

   public static String whetherNot(Object value) {
      return "1".equals(whether(value))?"0":"1";
   }

   public static String whether(Object value) {
      return value != null && !isBlank(value.toString()) && (Boolean.parseBoolean(value.toString()) || "Y".equalsIgnoreCase(value.toString()) || "on".equalsIgnoreCase(value.toString()) || "YES".equalsIgnoreCase(value.toString()) || "1".equals(value))?"1":"0";
   }

   public static String buildDBLike(String field) {
      return isBlank(field)?"%":"%" + field + "%";
   }

   public static String buildDBStr(String str) {
      return "\'" + str + "\'";
   }

   public static String oracleTypeToJavaType(String oracleType) {
      String javaType = "string";
      if("NUMBER".equals(oracleType)) {
         javaType = "BigDecimal";
      } else if("DATE".equals(oracleType)) {
         javaType = "java.util.Date";
      }

      return javaType;
   }

   public static String getSimpleSpell(String source) {
      try{
         return isBlank(source)?null:PinyinHelper.getShortPinyin(source).toUpperCase();
      }catch(Exception ex){
         return null;
      }
//      return isBlank(source)?null:PinyinHelper.getShortPinyin(source).toUpperCase();
   }

   public static String getFullSpell(String source) {
      try{
         return isBlank(source)?null:PinyinHelper.convertToPinyinString(source, "", PinyinFormat.WITHOUT_TONE).toUpperCase();
      }catch(Exception ex){
         return null;
      }
//      return isBlank(source)?null:PinyinHelper.convertToPinyinString(source, "", PinyinFormat.WITHOUT_TONE).toUpperCase();
   }
}
