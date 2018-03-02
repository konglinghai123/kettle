package cn.benma666.myutils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.net.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HttpUtl {

   public static Log log = LogFactory.getLog(HttpUtl.class);


   public static String doUrlStr(String url) throws Exception {
      DefaultClientConfig config = new DefaultClientConfig();
      Client client = Client.create(config);
      WebResource webResource = client.resource(new URI(url));
      String result = (String)webResource.get(String.class);
      return result;
   }

   public static JSONObject doUrl(String url) throws Exception {
      String result = doUrlStr(url);
      JSONObject json = JSON.parseObject(result);
      return json;
   }

   public static JSONObject createResult(boolean success, String msg) {
      JSONObject result = new JSONObject();
      result.put("success", Boolean.valueOf(success));
      result.put("msg", msg);
      return result;
   }

}
