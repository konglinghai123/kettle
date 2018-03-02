package cn.benma666.myutils;

import cn.benma666.myutils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FtpUtil {

   public static final String ISO_8859_1 = "ISO-8859-1";
   public static final String FILE_SEPARATOR = "/";
   private static final String EXCEPTION = "ftp处理中的异常";
   private static Log logger = LogFactory.getLog(FtpUtil.class);
   private FTPClient ftpClient;
   public static FtpUtil ftp;
   private static Map ftpList = new HashMap();


   public static FtpUtil use(String name) throws IOException {
      FtpUtil ftpUtil = (FtpUtil)ftpList.get(name);
      if(ftpUtil != null) {
         try {
            setPath(ftpUtil.ftpClient, "/");
         } catch (Exception var3) {
            ftpUtil.disConnection();
            ftpUtil = null;
         }
      }

      return ftpUtil;
   }

   public FtpUtil(String name, boolean isPrintCommmand) {
      this(name, isPrintCommmand, (String)null);
   }

   public FtpUtil(String name, boolean isPrintCommmand, String confJson) {
      this.ftpClient = new FTPClient();
      if(ftp == null) {
         ftp = this;
      }

      try {
         this.login(confJson);
      } catch (IOException var5) {
         logger.error("ftp登录失败：" + confJson, var5);
      }

      ftpList.put(name, this);
   }

   public boolean login(String confJson) throws IOException {
      if(confJson == null) {
         return false;
      } else {
         JSONObject conf = JSON.parseObject(confJson);
         return this.login(conf.getString("address"), conf.getIntValue("port"), conf.getString("username"), conf.getString("password"), conf.getString("encoding"), (String)null, (String)null);
      }
   }

   public boolean login(String host, int port, String username, String password, String code, String syst, String serverLanguageCode) throws IOException {
      if(StringUtil.isBlank(code)) {
         code = "utf8";
      }

      if(StringUtil.isBlank(syst)) {
         syst = "WINDOWS";
      }

      if(StringUtil.isBlank(serverLanguageCode)) {
         serverLanguageCode = "zh";
      }

      this.ftpClient.connect(host, port);
      if(FTPReply.isPositiveCompletion(this.ftpClient.getReplyCode()) && this.ftpClient.login(username, password)) {
         this.ftpClient.setControlEncoding(code);
         FTPClientConfig conf = new FTPClientConfig(syst);
         conf.setServerLanguageCode(serverLanguageCode);
         return true;
      } else {
         if(this.ftpClient.isConnected()) {
            this.ftpClient.disconnect();
         }

         return false;
      }
   }

   public void disConnection() throws IOException {
      if(this.ftpClient.isConnected()) {
         this.ftpClient.disconnect();
      }

   }

   public void printFileList(String pathName) throws IOException {
      setPath(this.ftpClient, pathName);
      String directory = repairDirEnd(pathName);
      FTPFile[] files = this.ftpClient.listFiles();

      for(int i = 0; i < files.length; ++i) {
         if(files[i].isFile()) {
            System.out.println("得到文件:" + directory + files[i].getName());
         } else if(files[i].isDirectory()) {
            this.printFileList(directory + files[i].getName());
         }
      }

   }

   public boolean downloadFile(String path, String fileName, String localPath) {
      return downloadFile(this.ftpClient, path, fileName, localPath);
   }

   public static boolean downloadFile(FTPClient ftp, String path, String fileName, String localPath) {
      boolean success = false;
      FileOutputStream is = null;

      try {
         File e = new File(localPath);
         if(!e.exists()) {
            e.mkdirs();
         }

         setPath(ftp, path);
         localPath = repairDirEnd(localPath);
         File localFile = new File(localPath + fileName);
         is = new FileOutputStream(localFile);
         ftp.retrieveFile(zhFileName(fileName), is);
         success = true;
      } catch (IOException var16) {
         logger.error("ftp处理中的异常", var16);
      } finally {
         if(is != null) {
            try {
               is.close();
            } catch (IOException var15) {
               var15.printStackTrace();
            }
         }

      }

      return success;
   }

   private static String zhFileName(String path) throws UnsupportedEncodingException {
      return new String(path.getBytes(), "ISO-8859-1");
   }

   public boolean downloadFileList(String path, String localPath) {
      return downloadFileList(this.ftpClient, path, localPath);
   }

   public static boolean downloadFileList(FTPClient ftp, String path, String localPath) {
      boolean success = false;
      FileOutputStream is = null;

      try {
         File e = new File(localPath);
         if(!e.exists()) {
            e.mkdirs();
         }

         setPath(ftp, path);
         localPath = repairDirEnd(localPath);
         path = repairDirEnd(path);
         FTPFile[] fs = ftp.listFiles();
         FTPFile[] arr$ = fs;
         int len$ = fs.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            FTPFile ff = arr$[i$];
            if(ff.isDirectory()) {
               downloadFileList(ftp, path + ff.getName(), localPath + ff.getName());
            } else {
               File localFile = new File(localPath + ff.getName());
               is = new FileOutputStream(localFile);
               ftp.retrieveFile(zhFileName(ff.getName()), is);
               is.close();
            }
         }

         success = true;
      } catch (IOException var20) {
         logger.error("ftp处理中的异常", var20);
      } finally {
         if(is != null) {
            try {
               is.close();
            } catch (IOException var19) {
               var19.printStackTrace();
            }
         }

      }

      return success;
   }

   public static String repairDirEnd(String path) {
      if(!path.endsWith("/")) {
         path = path + "/";
      }

      return path;
   }

   public static boolean downloadFiles(String url, int port, String username, String password, String remotePath, String fileName, String localPath) {
      boolean success = false;
      FTPClient ftp = new FTPClient();

      boolean var10;
      try {
         if(port > -1) {
            ftp.connect(url, port);
         } else {
            ftp.connect(url);
         }

         ftp.login(username, password);
         int e = ftp.getReplyCode();
         if(FTPReply.isPositiveCompletion(e)) {
            var10 = downloadFileList(ftp, remotePath, localPath);
            return var10;
         }

         ftp.disconnect();
         var10 = success;
      } catch (IOException var21) {
         logger.error("ftp处理中的异常", var21);
         return success;
      } finally {
         if(ftp.isConnected()) {
            try {
               ftp.disconnect();
            } catch (IOException var20) {
               logger.error("ftp处理中的异常", var20);
            }
         }

      }

      return var10;
   }

   public File uploadFile(String remotePath, List files) throws Exception {
      File fileIn = null;
      FileInputStream is = null;

      try {
         Iterator e = files.iterator();

         while(e.hasNext()) {
            File file = (File)e.next();
            logger.info("----进入文件上传到FTP服务器--->");
            setPath(this.ftpClient, remotePath);
            fileIn = file;
            is = new FileInputStream(file);
            this.ftpClient.storeFile(zhFileName(file.getName()), is);
         }
      } catch (Exception var10) {
         logger.error("上传FTP文件异常: ", var10);
      } finally {
         is.close();
      }

      return fileIn;
   }

   public boolean uploadFile(String path, String localPath) {
      return uploadFile(this.ftpClient, path, localPath);
   }

   public boolean uploadFile(String path, String filename, String localPath) {
      return uploadFile(this.ftpClient, path, filename, localPath);
   }

   public static boolean uploadFile(FTPClient ftp, String path, String localPath) {
      File file = new File(localPath);
      if(file.isFile()) {
         throw new RuntimeException("没有上传文件名");
      } else if(!file.isDirectory()) {
         return false;
      } else {
         makeDir(ftp, path);
         File[] arr$ = file.listFiles();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            File f = arr$[i$];
            uploadFile(ftp, path, f.getName(), f.getAbsolutePath());
         }

         return true;
      }
   }

   public static boolean uploadFile(FTPClient ftp, String path, String filename, String localPath) {
      try {
         File e = new File(localPath);
         if(e.isFile()) {
            return uploadFile(ftp, path, filename, (InputStream)(new FileInputStream(e)));
         }

         if(e.isDirectory()) {
            path = repairDirEnd(path);
            path = path + filename;
            makeDir(ftp, path);
            File[] arr$ = e.listFiles();
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               File f = arr$[i$];
               uploadFile(ftp, path, f.getName(), f.getAbsolutePath());
            }

            return true;
         }
      } catch (FileNotFoundException var9) {
         var9.printStackTrace();
      }

      return false;
   }

   public boolean uploadFile(String path, String filename, InputStream input) {
      return uploadFile(this.ftpClient, path, filename, input);
   }

   public static boolean uploadFile(FTPClient ftp, String path, String filename, InputStream input) {
      boolean success = false;

      try {
         if(!existDir(ftp, path)) {
            makeDir(ftp, path);
         }

         setPath(ftp, path);
         ftp.setFileType(2);
         ftp.storeFile(zhFileName(filename), input);
         input.close();
         success = true;
      } catch (IOException var6) {
         success = false;
         logger.error("ftp处理中的异常", var6);
      }

      return success;
   }

   public static void setPath(FTPClient ftp, String path) throws IOException, UnsupportedEncodingException {
      if(path.length() > 1 && path.endsWith("/")) {
         path = path.substring(0, path.length() - 1);
      }

      ftp.changeWorkingDirectory(zhFileName(path));
      if(!zhFileName(path).equals(ftp.printWorkingDirectory())) {
         throw new RuntimeException(path + ",目录不存在！当前目录：" + ftp.printWorkingDirectory());
      }
   }

   public static boolean uploadFile(String url, int port, String username, String password, String path, String filename, InputStream input) {
      boolean success = false;
      FTPClient ftp = new FTPClient();

      boolean var10;
      try {
         if(port > -1) {
            ftp.connect(url, port);
         } else {
            ftp.connect(url);
         }

         ftp.login(username, password);
         int e = ftp.getReplyCode();
         if(FTPReply.isPositiveCompletion(e)) {
            success = uploadFile(ftp, path, filename, input);
            return success;
         }

         ftp.disconnect();
         var10 = success;
      } catch (IOException var21) {
         success = false;
         logger.error("ftp处理中的异常", var21);
         return success;
      } finally {
         if(ftp.isConnected()) {
            try {
               ftp.disconnect();
            } catch (IOException var20) {
               logger.error("ftp处理中的异常", var20);
            }
         }

      }

      return var10;
   }

   public void deleteAllFile(String remotePath) {
      try {
         setPath(this.ftpClient, remotePath);
         remotePath = repairDirEnd(remotePath);
         FTPFile[] e = this.ftpClient.listFiles();
         FTPFile[] arr$ = e;
         int len$ = e.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            FTPFile file = arr$[i$];
            if(file.isDirectory()) {
               this.deleteAllFile(remotePath + file.getName());
            } else {
               this.ftpClient.deleteFile(zhFileName(file.getName()));
            }
         }

         this.ftpClient.removeDirectory(zhFileName(remotePath));
      } catch (Exception var7) {
         logger.error("从FTP服务器删除文件异常：", var7);
         var7.printStackTrace();
      }

   }

   public boolean deleteFtpFile(String remotePath, String fileName) {
      boolean success = false;

      try {
         remotePath = repairDirEnd(remotePath);
         success = this.ftpClient.deleteFile(zhFileName(remotePath + fileName));
      } catch (IOException var5) {
         logger.error("ftp处理中的异常", var5);
         success = false;
      }

      return success;
   }

   public static boolean deleteFtpFile(String url, int port, String username, String password, String remotePath, String fileName) {
      boolean success = false;
      FTPClient ftp = new FTPClient();

      boolean var9;
      try {
         if(port > -1) {
            ftp.connect(url, port);
         } else {
            ftp.connect(url);
         }

         ftp.login(username, password);
         int e = ftp.getReplyCode();
         if(FTPReply.isPositiveCompletion(e)) {
            remotePath = repairDirEnd(remotePath);
            success = ftp.deleteFile(zhFileName(remotePath + fileName));
            ftp.logout();
            return success;
         }

         ftp.disconnect();
         var9 = success;
      } catch (IOException var20) {
         logger.error("ftp处理中的异常", var20);
         success = false;
         return success;
      } finally {
         if(ftp.isConnected()) {
            try {
               ftp.disconnect();
            } catch (IOException var19) {
               logger.error("ftp处理中的异常", var19);
            }
         }

      }

      return var9;
   }

   public boolean makeDir(String dir) {
      return makeDir(this.ftpClient, dir);
   }

   public static boolean makeDir(FTPClient ftp, String dir) {
      boolean flag = true;

      try {
         flag = ftp.makeDirectory(zhFileName(dir));
         if(flag) {
            System.out.println("make Directory " + dir + " succeed");
         } else {
            System.out.println("make Directory " + dir + " false");
         }
      } catch (Exception var4) {
         logger.error("远程FTP生成目录异常:", var4);
         var4.printStackTrace();
      }

      return flag;
   }

   public boolean existDir(String dir) {
      return existDir(this.ftpClient, dir);
   }

   public static boolean existDir(FTPClient ftp, String dir) {
      try {
         setPath(ftp, dir);
         return true;
      } catch (Exception var3) {
         return false;
      }
   }

   public static void deleteDir(String fileName) {
      File file = new File(fileName);
      if(file.exists()) {
         if(file.isFile()) {
            file.delete();
         } else {
            File[] arr$ = file.listFiles();
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               File f1 = arr$[i$];
               if(f1.isDirectory()) {
                  deleteDir(f1.getAbsolutePath());
               } else {
                  f1.delete();
               }
            }

            file.delete();
         }
      }
   }

   public FTPClient getFtpClient() {
      return this.ftpClient;
   }

   public void setFtpClient(FTPClient ftpClient) {
      this.ftpClient = ftpClient;
   }

}
