/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class Request {

    public static String baseURL = "";
    public static final String defaultCharset = "UTF-8";

    public static String makeRequest(String path, String method, HashMap<String, String> params, String charset, boolean noCache) {
        try {
            URL url = new URL(baseURL + path);
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod(method);
            http.setRequestProperty("User-Agent", "TinyBrowser");
            http.setDoOutput(true);

            String response = "";

            byte[] out = new byte[0];

            if (!method.equals("GET")) {
                if (params == null) params = new HashMap<String, String>();
                ArrayList<String> parts = new ArrayList<String>();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    parts.add(URLEncoder.encode(entry.getKey(), charset) + "="
                         + URLEncoder.encode(entry.getValue(), charset));
                }
                String listString = parts.toString();
                listString = listString.substring(1, listString.length() - 1).replaceAll(",\\s+", "&");

                out = listString.getBytes(Charset.forName(charset));
                int length = out.length;

                http.setFixedLengthStreamingMode(listString.length());
                http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset.toLowerCase());
            }

            http.connect();
            
            if (!method.equals("GET")) {

                OutputStream os = null;

                try {
                    os = http.getOutputStream();
                    os.write(out);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    if (os != null) os.close();
                }
            }

//            Map<String, List<String>> headers = http.getHeaderFields();
//            Set<Entry<String, List<String>>> entries = headers.entrySet();
//            for (Entry<String, List<String>> e: entries) {
//                System.out.println(e.getKey() + ": " + e.getValue());
//            }
//            if (str.length() > 0) {
//                if (str.contains("auth")) {
//                    Auth.auth(true);
//                    return makeRequest(path, method, params, charset);
//                }
//            }

            InputStream is = http.getInputStream();
            response = new String(getBytes(is, 10000000), charset);

            return response;
        } catch (MalformedURLException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
    
    public static String makeRequest(String path, String method, HashMap<String, String> params, boolean noCache) {
        return makeRequest(path, method, params, defaultCharset, noCache);
    }

    public static String makeRequest(String path) {
        return makeRequest(path, "GET", new HashMap<String, String>(), defaultCharset, false);
    }

    public static String makeRequest(String path, boolean noCache) {
        return makeRequest(path, "GET", new HashMap<String, String>(), defaultCharset, noCache);
    }

    public static byte[] getBytes(InputStream is, int size) {
        byte[] result = new byte[size];
        int index = 0;
        final int buffer_size=1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for(;;)
            {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                for (int i = 0; i < count; i++) {
                    result[index++] = bytes[i];
                }
            }
        }
        catch(Exception ex) {
            return null;
        }

        return Arrays.copyOf(result, index);
    }
}
