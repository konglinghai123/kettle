package cn.benma666.myutils;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileUtil extends FileUtils {

   private static Log log = LogFactory.getLog(FileUtil.class);


   public static void closeInputStream(InputStream is) {
      try {
         if(is != null) {
            is.close();
         }
      } catch (IOException var2) {
         log.error("关闭文件流失败：" + is.toString(), var2);
      }

   }

}
