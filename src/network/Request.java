/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package network;

import cache.Cache;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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
            String fullPath = baseURL + path;
            if (cache != null && !noCache) {
                fullPath = cache.get(fullPath);
                if (!fullPath.startsWith("http")) {
                    try {
                        long start = System.currentTimeMillis();

                        String content = "";
                        StringBuilder sb = new StringBuilder();
                        char[] buffer = new char[1000];
                        int offset = 0;
                        File f = new File(fullPath);
                        FileReader fr = new FileReader(f);
                        while (fr.ready() && offset < f.length()) {
                            int len = fr.read(buffer);
                            offset += len;
                            sb.append(buffer);
                        }
                        fr.close();

                        long end = System.currentTimeMillis();
                        System.out.println("Loaded file \"" + fullPath + "\" in " + (end - start) + " ms");

                        content = sb.toString().trim();
                        return content;
                    } catch (IOException ex) {
                        Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
                        return null;
                    }
                }
            }
            long start = System.currentTimeMillis();
            URL url = new URL(fullPath);
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

            if (http.getContentType() == null || http.getContentType().matches("^(image|audio|video|application)")) {
                return null;
            }

            InputStream is = http.getInputStream();
            response = new String(getBytes(is, 10000000), charset);
            
            long end = System.currentTimeMillis();
            System.out.println("Loaded file \"" + fullPath + "\" in " + (end - start) + " ms");

            return response;
        } catch (MalformedURLException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }


    public static File makeBinaryRequest(String path, String method, HashMap<String, String> params, String charset, boolean noCache) {
        try {
            String fullPath = baseURL + path;
            if (cache != null && !noCache) {
                fullPath = cache.get(fullPath);
                if (!fullPath.startsWith("http")) {
                    long start = System.currentTimeMillis();

                    File f = new File(fullPath);

                    long end = System.currentTimeMillis();
                    System.out.println("Loaded file \"" + fullPath + "\" in " + (end - start) + " ms");

                    return f;
                }
            }
            File f = File.createTempFile("tmp_", "");

            long start = System.currentTimeMillis();
            URL url = new URL(fullPath);
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod(method);
            http.setRequestProperty("User-Agent", "TinyBrowser");
            http.setDoOutput(true);

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

            InputStream is = http.getInputStream();
            int length = http.getContentLength();
            byte[] bytes = getBytes(is, length);

            long end = System.currentTimeMillis();
            System.out.println("Loaded file \"" + fullPath + "\" in " + (end - start) + " ms");

            FileOutputStream outputStream = new FileOutputStream(f);
            try {
                outputStream.write(bytes);
            } catch(IOException ex) {
                Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }

            return f;
        } catch (MalformedURLException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private static void readFileToBytes(String filePath) {
        File file = new File(filePath);
        byte[] bytes = new byte[(int) file.length()];

        FileInputStream fis = null;
        try {
          fis = new FileInputStream(file);
          fis.read(bytes);
        } catch(IOException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {}
        }
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

    public static File makeBinaryRequest(String path, String method, HashMap<String, String> params, boolean noCache) {
        return makeBinaryRequest(path, method, params, defaultCharset, noCache);
    }

    public static File makeBinaryRequest(String path) {
        return makeBinaryRequest(path, "GET", new HashMap<String, String>(), defaultCharset, false);
    }

    public static File makeBinaryRequest(String path, boolean noCache) {
        return makeBinaryRequest(path, "GET", new HashMap<String, String>(), defaultCharset, noCache);
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

    public static void setCache(Cache cache) {
        Request.cache = cache;
    }

    private static Cache cache;
}
