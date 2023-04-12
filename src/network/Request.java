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
import java.io.UnsupportedEncodingException;
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

    public static String makeRequest(String path, String method, HashMap<String, String> params, String charset, boolean noCache, boolean multipart) {
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
                out = prepareBody(http, params, charset, multipart);
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

    public static byte[] prepareBody(HttpURLConnection http, HashMap<String, String> params, String charset, boolean multipart) {
        byte[] out = new byte[0];

        if (params == null) params = new HashMap<String, String>();
        ArrayList<String> parts = new ArrayList<String>();
        String boundary = multipart ? generateBoundary() : "";
        for (Map.Entry<String, String> entry : params.entrySet()) {
            try {
                if (!multipart) {
                    parts.add(URLEncoder.encode(entry.getKey(), charset) + "=" + URLEncoder.encode(entry.getValue(), charset));
                } else {
                    String content = "--" + boundary + "\n" +
                          "Content-Disposition: form-data; name=\"" + URLEncoder.encode(entry.getKey(), charset) + "\"";
                    boolean isFile = entry.getValue().matches("\\[filename=\"[^\"]+\"\\].*");
                    int pos = 0;
                    if (isFile) {
                        pos = entry.getValue().indexOf("\"", 11);
                        String filename = entry.getValue().substring(11, pos);
                        content += "; filename=\"" + URLEncoder.encode(filename, charset) + "\"\n";
                        content += "Content-Type: " + getMimeType(filename);
                    }
                    content += "\n\n";
                    content += (isFile ? entry.getValue().substring(pos+2) : entry.getValue());
                    parts.add(content);
                }
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (multipart) {
            parts.add("--" + boundary + "--\n");
        }
        String listString = parts.toString();
        listString = listString.substring(1, listString.length() - 1).replaceAll(",\\s+", !multipart ? "&" : "\n");
        if (debug) {
            System.out.println(listString);
        }

        out = listString.getBytes(Charset.forName(charset));
        int length = out.length;

        String contentType = multipart ? "multipart/form-data; boundary=" + boundary : "application/x-www-form-urlencoded;charset=" + charset.toLowerCase();

        http.setFixedLengthStreamingMode(listString.length());
        http.setRequestProperty("Content-Type", contentType);

        return out;
    }


    public static String getMimeType(String filename) {
        if (filename.endsWith(".jpg")) {
            return "image/jpeg";
        }
        if (filename.endsWith(".mpg")) {
            return "video/mpeg";
        }
        String type = null;
        if (filename.matches(".*\\.(bmp|jpg|gif|png|tiff|ico)$")) {
            type = "image";
        } else if (filename.matches(".*\\.(wav|mp3|ogg|flac|alac|aac|ac3|m4a|mp2)$")) {
            type = "audio";
        } else if (filename.matches(".*\\.(avi|mp4|mpg|mkv|mov|ogv|flv|3gpp)$")) {
            type = "video";
        } else if (filename.matches(".*\\.(docx?|xlsx?|pptx?|psd|7z|rar|zip|pdf|json|xml)$")) {
            if (filename.matches(".*\\.docx?$")) {
                return filename.endsWith("x") ? "application/vnd.openxmlformats-officedocument.wordprocessingml.document" : "application/msword";
            }
            if (filename.matches(".*\\.xlsx?$")) {
                return filename.endsWith("x") ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" : "application/vnd.ms-excel";
            }
            if (filename.matches(".*\\.pptx?$")) {
                return filename.endsWith("x") ? "application/vnd.openxmlformats-officedocument.presentationml.presentation" : "application/vnd.ms-powerpoint";
            }
            if (filename.endsWith(".rar")) {
                return "application/vnd.rar";
            }
            if (filename.endsWith(".7z")) {
                return "application/x-7z-compressed";
            }
            type = "application";
        } else if (filename.matches(".*\\.(ttf|woff2?)$")) {
            type = "font";
        } else if (filename.matches(".*\\.(rtf|css|js|html?)$")) {
            if (filename.endsWith(".htm")) {
                return "text/html";
            }
            type = "text";
        }
        if (type != null && filename.indexOf(".") != -1) {
            String[] p = filename.split("\\.");
            return type + "/" + p[p.length-1];
        }
        return "text/plain";
    }

    public static String generateBoundary() {
        String result = "";
        int start = 'A';
        int start2 = 'a';
        for (int i = 0; i < boundarySize; i++) {
            int code = (int) Math.floor(Math.random() * 52);
            result += code < 26 ? (char)(start + code) : (char)(start2 + (code - 26));
        }
        return result;
    }


    public static File makeBinaryRequest(String path, String method, HashMap<String, String> params, String charset, boolean noCache, boolean multipart) {
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
                out = prepareBody(http, params, charset, multipart);
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

    public static byte[] readFileToBytes(String filePath) {
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
        return bytes;
    }
    
    public static String makeRequest(String path, String method, HashMap<String, String> params, boolean noCache) {
        return makeRequest(path, method, params, defaultCharset, noCache, false);
    }

    public static String makeRequest(String path, String method, HashMap<String, String> params, boolean noCache, boolean multipart) {
        return makeRequest(path, method, params, defaultCharset, noCache, multipart);
    }

    public static String makeRequest(String path) {
        return makeRequest(path, "GET", new HashMap<String, String>(), defaultCharset, false, false);
    }

    public static String makeRequest(String path, boolean noCache) {
        return makeRequest(path, "GET", new HashMap<String, String>(), defaultCharset, noCache, false);
    }

    public static File makeBinaryRequest(String path, String method, HashMap<String, String> params, boolean noCache) {
        return makeBinaryRequest(path, method, params, defaultCharset, noCache, false);
    }

    public static File makeBinaryRequest(String path, String method, HashMap<String, String> params, boolean noCache, boolean multipart) {
        return makeBinaryRequest(path, method, params, defaultCharset, noCache, multipart);
    }

    public static File makeBinaryRequest(String path) {
        return makeBinaryRequest(path, "GET", new HashMap<String, String>(), defaultCharset, false, false);
    }

    public static File makeBinaryRequest(String path, boolean noCache) {
        return makeBinaryRequest(path, "GET", new HashMap<String, String>(), defaultCharset, noCache, false);
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

    public static int boundarySize = 16;
    public static boolean debug = false;

    private static Cache cache;
}
