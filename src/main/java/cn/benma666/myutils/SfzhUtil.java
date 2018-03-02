package cn.benma666.myutils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SfzhUtil {

   public static final int CHINA_ID_MIN_LENGTH = 15;
   public static final int CHINA_ID_MAX_LENGTH = 18;
   public static final String[] cityCode = new String[]{"11", "12", "13", "14", "15", "21", "22", "23", "31", "32", "33", "34", "35", "36", "37", "41", "42", "43", "44", "45", "46", "50", "51", "52", "53", "54", "61", "62", "63", "64", "65", "71", "81", "82", "91"};
   public static final int[] power = new int[]{7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
   public static final String[] verifyCode = new String[]{"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
   public static final int MIN = 1930;
   public static Map cityCodes = new HashMap();
   public static Map twFirstCode = new HashMap();
   public static Map hkFirstCode = new HashMap();


   public static String conver15CardTo18(String idCard) {
      String idCard18 = "";
      if(idCard.length() != 15) {
         return null;
      } else if(isNum(idCard)) {
         String birthday = idCard.substring(6, 12);
         Date birthDate = null;

         try {
            birthDate = (new SimpleDateFormat("yyMMdd")).parse(birthday);
         } catch (ParseException var10) {
            var10.printStackTrace();
         }

         Calendar cal = Calendar.getInstance();
         if(birthDate != null) {
            cal.setTime(birthDate);
         }

         String sYear = String.valueOf(cal.get(1));
         idCard18 = idCard.substring(0, 6) + sYear + idCard.substring(8);
         char[] cArr = idCard18.toCharArray();
         if(cArr != null) {
            int[] iCard = converCharToInt(cArr);
            int iSum17 = getPowerSum(iCard);
            String sVal = getCheckCode18(iSum17);
            if(sVal.length() <= 0) {
               return null;
            }

            idCard18 = idCard18 + sVal;
         }

         return idCard18;
      } else {
         return null;
      }
   }

   public static boolean validateCard(String idCard) {
      String card = idCard.trim();
      if(validateIdCard18(card)) {
         return true;
      } else if(validateIdCard15(card)) {
         return true;
      } else {
         String[] cardval = validateIdCard10(card);
         return cardval != null && cardval[2].equals("true");
      }
   }

   public static boolean validateIdCard18(String idCard) {
      boolean bTrue = false;
      if(idCard.length() == 18) {
         String code17 = idCard.substring(0, 17);
         String code18 = idCard.substring(17, 18);
         if(isNum(code17)) {
            char[] cArr = code17.toCharArray();
            if(cArr != null) {
               int[] iCard = converCharToInt(cArr);
               int iSum17 = getPowerSum(iCard);
               String val = getCheckCode18(iSum17);
               if(val.length() > 0 && val.equalsIgnoreCase(code18)) {
                  bTrue = true;
               }
            }
         }
      }

      return bTrue;
   }

   public static boolean validateIdCard15(String idCard) {
      if(idCard.length() != 15) {
         return false;
      } else if(isNum(idCard)) {
         String proCode = idCard.substring(0, 2);
         if(cityCodes.get(proCode) == null) {
            return false;
         } else {
            String birthCode = idCard.substring(6, 12);
            Date birthDate = null;

            try {
               birthDate = (new SimpleDateFormat("yy")).parse(birthCode.substring(0, 2));
            } catch (ParseException var5) {
               var5.printStackTrace();
            }

            Calendar cal = Calendar.getInstance();
            if(birthDate != null) {
               cal.setTime(birthDate);
            }

            return valiDate(cal.get(1), Integer.valueOf(birthCode.substring(2, 4)).intValue(), Integer.valueOf(birthCode.substring(4, 6)).intValue());
         }
      } else {
         return false;
      }
   }

   public static String[] validateIdCard10(String idCard) {
      String[] info = new String[3];
      String card = idCard.replaceAll("[\\(|\\)]", "");
      if(card.length() != 8 && card.length() != 9 && idCard.length() != 10) {
         return null;
      } else {
         if(idCard.matches("^[a-zA-Z][0-9]{9}$")) {
            info[0] = "台湾";
            System.out.println("11111");
            String char2 = idCard.substring(1, 2);
            if(char2.equals("1")) {
               info[1] = "M";
               System.out.println("MMMMMMM");
            } else {
               if(!char2.equals("2")) {
                  info[1] = "N";
                  info[2] = "false";
                  System.out.println("NNNN");
                  return info;
               }

               info[1] = "F";
               System.out.println("FFFFFFF");
            }

            info[2] = validateTWCard(idCard)?"true":"false";
         } else if(idCard.matches("^[1|5|7][0-9]{6}\\(?[0-9A-Z]\\)?$")) {
            info[0] = "澳门";
            info[1] = "N";
         } else {
            if(!idCard.matches("^[A-Z]{1,2}[0-9]{6}\\(?[0-9A]\\)?$")) {
               return null;
            }

            info[0] = "香港";
            info[1] = "N";
            info[2] = validateHKCard(idCard)?"true":"false";
         }

         return info;
      }
   }

   public static boolean validateTWCard(String idCard) {
      String start = idCard.substring(0, 1);
      String mid = idCard.substring(1, 9);
      String end = idCard.substring(9, 10);
      Integer iStart = (Integer)twFirstCode.get(start);
      Integer sum = Integer.valueOf(iStart.intValue() / 10 + iStart.intValue() % 10 * 9);
      char[] chars = mid.toCharArray();
      Integer iflag = Integer.valueOf(8);
      char[] arr$ = chars;
      int len$ = chars.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         char c = arr$[i$];
         sum = Integer.valueOf(sum.intValue() + Integer.valueOf(c + "").intValue() * iflag.intValue());
         iflag = Integer.valueOf(iflag.intValue() - 1);
      }

      return (sum.intValue() % 10 == 0?0:10 - sum.intValue() % 10) == Integer.valueOf(end).intValue();
   }

   public static boolean validateHKCard(String idCard) {
      String card = idCard.replaceAll("[\\(|\\)]", "");
      Integer sum = Integer.valueOf(0);
      if(card.length() == 9) {
         sum = Integer.valueOf((Integer.valueOf(card.substring(0, 1).toUpperCase().toCharArray()[0]).intValue() - 55) * 9 + (Integer.valueOf(card.substring(1, 2).toUpperCase().toCharArray()[0]).intValue() - 55) * 8);
         card = card.substring(1, 9);
      } else {
         sum = Integer.valueOf(522 + (Integer.valueOf(card.substring(0, 1).toUpperCase().toCharArray()[0]).intValue() - 55) * 8);
      }

      String mid = card.substring(1, 7);
      String end = card.substring(7, 8);
      char[] chars = mid.toCharArray();
      Integer iflag = Integer.valueOf(7);
      char[] arr$ = chars;
      int len$ = chars.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         char c = arr$[i$];
         sum = Integer.valueOf(sum.intValue() + Integer.valueOf(c + "").intValue() * iflag.intValue());
         iflag = Integer.valueOf(iflag.intValue() - 1);
      }

      if(end.toUpperCase().equals("A")) {
         sum = Integer.valueOf(sum.intValue() + 10);
      } else {
         sum = Integer.valueOf(sum.intValue() + Integer.valueOf(end).intValue());
      }

      return sum.intValue() % 11 == 0;
   }

   public static int[] converCharToInt(char[] ca) {
      int len = ca.length;
      int[] iArr = new int[len];

      try {
         for(int e = 0; e < len; ++e) {
            iArr[e] = Integer.parseInt(String.valueOf(ca[e]));
         }
      } catch (NumberFormatException var4) {
         var4.printStackTrace();
      }

      return iArr;
   }

   public static int getPowerSum(int[] iArr) {
      int iSum = 0;
      if(power.length == iArr.length) {
         for(int i = 0; i < iArr.length; ++i) {
            for(int j = 0; j < power.length; ++j) {
               if(i == j) {
                  iSum += iArr[i] * power[j];
               }
            }
         }
      }

      return iSum;
   }

   public static String getCheckCode18(int iSum) {
      String sCode = "";
      switch(iSum % 11) {
      case 0:
         sCode = "1";
         break;
      case 1:
         sCode = "0";
         break;
      case 2:
         sCode = "x";
         break;
      case 3:
         sCode = "9";
         break;
      case 4:
         sCode = "8";
         break;
      case 5:
         sCode = "7";
         break;
      case 6:
         sCode = "6";
         break;
      case 7:
         sCode = "5";
         break;
      case 8:
         sCode = "4";
         break;
      case 9:
         sCode = "3";
         break;
      case 10:
         sCode = "2";
      }

      return sCode;
   }

   public static int getAgeByIdCard(String idCard) {
      boolean iAge = false;
      if(idCard.length() == 15) {
         idCard = conver15CardTo18(idCard);
      }

      String year = idCard.substring(6, 10);
      Calendar cal = Calendar.getInstance();
      int iCurrYear = cal.get(1);
      int iAge1 = iCurrYear - Integer.valueOf(year).intValue();
      return iAge1;
   }

   public static String getBirthByIdCard(String idCard) {
      Integer len = Integer.valueOf(idCard.length());
      if(len.intValue() < 15) {
         return null;
      } else {
         if(len.intValue() == 15) {
            idCard = conver15CardTo18(idCard);
         }

         return idCard.substring(6, 14);
      }
   }

   public static Short getYearByIdCard(String idCard) {
      Integer len = Integer.valueOf(idCard.length());
      if(len.intValue() < 15) {
         return null;
      } else {
         if(len.intValue() == 15) {
            idCard = conver15CardTo18(idCard);
         }

         return Short.valueOf(idCard.substring(6, 10));
      }
   }

   public static Short getMonthByIdCard(String idCard) {
      Integer len = Integer.valueOf(idCard.length());
      if(len.intValue() < 15) {
         return null;
      } else {
         if(len.intValue() == 15) {
            idCard = conver15CardTo18(idCard);
         }

         return Short.valueOf(idCard.substring(10, 12));
      }
   }

   public static Short getDateByIdCard(String idCard) {
      Integer len = Integer.valueOf(idCard.length());
      if(len.intValue() < 15) {
         return null;
      } else {
         if(len.intValue() == 15) {
            idCard = conver15CardTo18(idCard);
         }

         return Short.valueOf(idCard.substring(12, 14));
      }
   }

   public static String getGenderByIdCard(String idCard) {
      String sGender = "未知";
      if(idCard.length() == 15) {
         idCard = conver15CardTo18(idCard);
      }

      String sCardNum = idCard.substring(16, 17);
      if(Integer.parseInt(sCardNum) % 2 != 0) {
         sGender = "男";
      } else {
         sGender = "女";
      }

      return sGender;
   }

   public static String getProvinceByIdCard(String idCard) {
      int len = idCard.length();
      String sProvince = null;
      String sProvinNum = "";
      if(len == 15 || len == 18) {
         sProvinNum = idCard.substring(0, 2);
      }

      sProvince = (String)cityCodes.get(sProvinNum);
      return sProvince;
   }

   public static boolean isNum(String val) {
      return val != null && !"".equals(val)?val.matches("^[0-9]*$"):false;
   }

   public static boolean valiDate(int iYear, int iMonth, int iDate) {
      Calendar cal = Calendar.getInstance();
      int year = cal.get(1);
      if(iYear >= 1930 && iYear < year) {
         if(iMonth >= 1 && iMonth <= 12) {
            int datePerMonth;
            switch(iMonth) {
            case 2:
               boolean dm = (iYear % 4 == 0 && iYear % 100 != 0 || iYear % 400 == 0) && iYear > 1930 && iYear < year;
               datePerMonth = dm?29:28;
               break;
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            default:
               datePerMonth = 31;
               break;
            case 4:
            case 6:
            case 9:
            case 11:
               datePerMonth = 30;
            }

            return iDate >= 1 && iDate <= datePerMonth;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   static {
      cityCodes.put("11", "北京");
      cityCodes.put("12", "天津");
      cityCodes.put("13", "河北");
      cityCodes.put("14", "山西");
      cityCodes.put("15", "内蒙古");
      cityCodes.put("21", "辽宁");
      cityCodes.put("22", "吉林");
      cityCodes.put("23", "黑龙江");
      cityCodes.put("31", "上海");
      cityCodes.put("32", "江苏");
      cityCodes.put("33", "浙江");
      cityCodes.put("34", "安徽");
      cityCodes.put("35", "福建");
      cityCodes.put("36", "江西");
      cityCodes.put("37", "山东");
      cityCodes.put("41", "河南");
      cityCodes.put("42", "湖北");
      cityCodes.put("43", "湖南");
      cityCodes.put("44", "广东");
      cityCodes.put("45", "广西");
      cityCodes.put("46", "海南");
      cityCodes.put("50", "重庆");
      cityCodes.put("51", "四川");
      cityCodes.put("52", "贵州");
      cityCodes.put("53", "云南");
      cityCodes.put("54", "西藏");
      cityCodes.put("61", "陕西");
      cityCodes.put("62", "甘肃");
      cityCodes.put("63", "青海");
      cityCodes.put("64", "宁夏");
      cityCodes.put("65", "新疆");
      cityCodes.put("71", "台湾");
      cityCodes.put("81", "香港");
      cityCodes.put("82", "澳门");
      cityCodes.put("91", "国外");
      twFirstCode.put("A", Integer.valueOf(10));
      twFirstCode.put("B", Integer.valueOf(11));
      twFirstCode.put("C", Integer.valueOf(12));
      twFirstCode.put("D", Integer.valueOf(13));
      twFirstCode.put("E", Integer.valueOf(14));
      twFirstCode.put("F", Integer.valueOf(15));
      twFirstCode.put("G", Integer.valueOf(16));
      twFirstCode.put("H", Integer.valueOf(17));
      twFirstCode.put("J", Integer.valueOf(18));
      twFirstCode.put("K", Integer.valueOf(19));
      twFirstCode.put("L", Integer.valueOf(20));
      twFirstCode.put("M", Integer.valueOf(21));
      twFirstCode.put("N", Integer.valueOf(22));
      twFirstCode.put("P", Integer.valueOf(23));
      twFirstCode.put("Q", Integer.valueOf(24));
      twFirstCode.put("R", Integer.valueOf(25));
      twFirstCode.put("S", Integer.valueOf(26));
      twFirstCode.put("T", Integer.valueOf(27));
      twFirstCode.put("U", Integer.valueOf(28));
      twFirstCode.put("V", Integer.valueOf(29));
      twFirstCode.put("X", Integer.valueOf(30));
      twFirstCode.put("Y", Integer.valueOf(31));
      twFirstCode.put("W", Integer.valueOf(32));
      twFirstCode.put("Z", Integer.valueOf(33));
      twFirstCode.put("I", Integer.valueOf(34));
      twFirstCode.put("O", Integer.valueOf(35));
      hkFirstCode.put("A", Integer.valueOf(1));
      hkFirstCode.put("B", Integer.valueOf(2));
      hkFirstCode.put("C", Integer.valueOf(3));
      hkFirstCode.put("R", Integer.valueOf(18));
      hkFirstCode.put("U", Integer.valueOf(21));
      hkFirstCode.put("Z", Integer.valueOf(26));
      hkFirstCode.put("X", Integer.valueOf(24));
      hkFirstCode.put("W", Integer.valueOf(23));
      hkFirstCode.put("O", Integer.valueOf(15));
      hkFirstCode.put("N", Integer.valueOf(14));
   }
}
