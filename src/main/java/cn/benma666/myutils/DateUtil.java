package cn.benma666.myutils;

import cn.benma666.myutils.StringUtil;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DateUtil extends DateUtils {

   private static Log log = LogFactory.getLog(DateUtil.class);
   public static final String DATE_FORMATTER_L = "yyyy-MM-dd HH:mm:ss";
   public static final String DATE_FORMATTER_S = "yyyy-MM-dd";
   public static final String DATE_FORMATTER14 = "yyyyMMddHHmmss";
   public static final String DATE_FORMATTER8 = "yyyyMMdd";
   public static final SimpleDateFormat sdf14 = new SimpleDateFormat("yyyyMMddHHmmss");
   public static final SimpleDateFormat sdf19 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


   public static Date parseDate(String dateStr) {
      if(StringUtil.isEmpty(dateStr)) {
         return null;
      } else {
         SimpleDateFormat format = null;
         String parse = dateStr.replaceFirst("^(18|19|20|21){1}[0-9]{2}([^0-9]?)", "yyyy$2");
         parse = parse.replaceFirst("^[0-9]{2}([^0-9]?)", "yy$1");
         parse = parse.replaceFirst("([^0-9]?)(1{1}[0-2]{1}|0?[1-9]{1})([^0-9]?)", "$1MM$3");
         parse = parse.replaceFirst("([^0-9]?)(3{1}[0-1]{1}|[0-2]?[0-9]{1})([^0-9]?)", "$1dd$3");
         parse = parse.replaceFirst("([^0-9]?)(2[0-3]{1}|[0-1]?[0-9]{1})([^0-9]?)", "$1HH$3");
         parse = parse.replaceFirst("([^0-9]?)[0-5]?[0-9]{1}([^0-9]?)", "$1mm$2");
         parse = parse.replaceFirst("([^0-9]?)[0-5]?[0-9]{1}([^0-9]?)", "$1ss$2");
         parse = parse.replaceFirst("([^0-9]?)[0-9]{1,3}([^0-9]*)", "$1SSS$2");

         try {
            format = new SimpleDateFormat(parse);
            format.setLenient(false);
            Date e = format.parse(dateStr);
            log.debug(String.format("原始字符串：%s,判断格式：%s,解析结果：%s", new Object[]{dateStr, parse, sdf19.format(e)}));
            return e;
         } catch (Exception var4) {
            log.error(String.format("日期解析出错：%s-->%s", new Object[]{parse, dateStr}));
            log.debug((Object)null, var4);
            return null;
         }
      }
   }

   public static String dateToStr14(Object value) {
      if(value == null) {
         return null;
      } else if(value instanceof Date) {
         return sdf14.format((Date)value);
      } else if(value instanceof java.sql.Date) {
         java.sql.Date val1 = (java.sql.Date)value;
         return sdf14.format(new Date(val1.getTime()));
      } else {
         Date val = parseDate(value.toString());
         return val == null?null:sdf14.format(val);
      }
   }

   public static int getSeasons(Date date) {
      int m = getMonth(date);
      return m <= 0?0:(m < 4?1:(m < 7?2:(m < 10?3:(m < 13?4:0))));
   }

   public static Date getNowSeasonsFirstDay(Date date) {
      int m = getSeasons(date);
      return m > 0?(m == 1?parseDate(getYear(date) + "-01-01"):(m == 2?parseDate(getYear(date) + "-04-01"):(m == 3?parseDate(getYear(date) + "-07-01"):parseDate(getYear(date) + "-10-01")))):null;
   }

   public static Date getYearLastDay(String year) {
      if(year != null && !"".equals(year)) {
         Date nd = parseDate(year + "-01-01");
         return addDays(addYears(nd, 1), -1);
      } else {
         return null;
      }
   }

   public static Date getNextMonthFirstDay(String year, String month) {
      if(year != null && !"".equals(year) && month != null && !"".equals(month)) {
         Date nd = parseDate(year + "-" + month + "-01");
         return addMonths(nd, 1);
      } else {
         return null;
      }
   }

   public static Date getMonthLastDay(String year, String month) {
      if(year != null && !"".equals(year) && month != null && !"".equals(month)) {
         Date nd = parseDate(year + "-" + month + "-01");
         return addDays(addMonths(nd, 1), -1);
      } else {
         return null;
      }
   }

   public static int compareMonth(Date st, Date end) {
      int y = Math.abs((getYear(end) < 0?0:getYear(end)) - (getYear(st) < 0?0:getYear(st)));
      boolean m = false;
      int var4;
      if(y > 0) {
         --y;
         var4 = Math.abs(12 - getMonth(st) + getMonth(end));
      } else {
         var4 = Math.abs(getMonth(end) - getMonth(st));
      }

      return y * 12 + var4;
   }

   public static long compare(Date start, Date end) {
      return start != null && end != null?end.getTime() - start.getTime():0L;
   }

   public static boolean compareDate(Date date) {
      return date != null?date.before(parseDate(doFormatDate(new Date(), "yyyy-MM-dd"))):false;
   }

   public static String doFormatDate(Date date, String format) {
      return date == null?null:(new SimpleDateFormat(format)).format(date);
   }

   public static int getYear() {
      return getYear(new Date());
   }

   public static int getYear(Date date) {
      return date == null?-1:DateToCalendar(date).get(1);
   }

   public static int getMonth() {
      return getMonth(new Date());
   }

   public static int getMonth(Date date) {
      return date == null?0:DateToCalendar(date).get(2) + 1;
   }

   public static int getDay() {
      return getDay(new Date());
   }

   public static int getDay(Date da) {
      return da == null?0:DateToCalendar(da).get(5);
   }

   public static Calendar DateToCalendar(Date dd) {
      Calendar cc = Calendar.getInstance();
      cc.setTime(dd);
      return cc;
   }

   public static Date longToDate(long datenum) {
      Calendar cc = Calendar.getInstance();
      cc.setTimeInMillis(datenum);
      return cc.getTime();
   }

   public static String longToDateString(long datenum) {
      return doFormatDate(longToDate(datenum), "yyyy-MM-dd HH:mm:ss");
   }

   public static Date getUpWeekDay(Date date) {
      if(date == null) {
         return null;
      } else {
         Calendar cc = Calendar.getInstance();
         cc.setTime(date);
         int week = cc.get(7);
         return DateUtils.addDays(date, 1 - week);
      }
   }

   public static Date getMonday(Date date) {
      if(date == null) {
         return null;
      } else {
         Calendar cc = Calendar.getInstance();
         cc.setTime(date);
         int week = cc.get(7);
         return DateUtils.addDays(date, 2 - week);
      }
   }

   public static int getWeek(Date date) {
      if(date == null) {
         return -1;
      } else {
         Calendar cc = Calendar.getInstance();
         cc.setTime(date);
         int week = cc.get(7);
         if(week == 1) {
            week = 7;
         } else {
            --week;
         }

         return week;
      }
   }

   public static Timestamp dateToTime(Date dt) {
      return dt == null?null:new Timestamp(dt.getTime());
   }

   public static Timestamp string2Time(String dateString) throws ParseException {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSS", Locale.ENGLISH);
      dateFormat.setLenient(false);
      Date ywybirt = DateFormat.getDateInstance().parse(dateString);
      Timestamp dateTime = new Timestamp(ywybirt.getTime());
      return dateTime;
   }

   public static java.sql.Date dateToSqlDate(Date de) {
      return new java.sql.Date(de.getTime());
   }

   public static String getDateTimeStr(String format) {
      return doFormatDate(new Date(), format);
   }

   public static String getDateTimeStr(String format, int jiaTian) {
      Calendar cal = Calendar.getInstance();
      cal.add(5, jiaTian);
      return doFormatDate(cal.getTime(), format);
   }

   public static String getDateTimeStr() {
      return getDateTimeStr("yyyy-MM-dd HH:mm:ss");
   }

   public static String getGabDate() {
      return getDateTimeStr("yyyyMMddHHmmss");
   }

}
