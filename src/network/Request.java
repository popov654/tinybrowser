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
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import render.Util;

/**
 *
 * @author Alex
 */
public class Request {

    public static String baseURL = "";
    public static final String defaultCharset = "UTF-8";

    

    public static String makeRequest(String path, String method, Vector<FormEntry> params, String charset, boolean noCache, boolean multipart) {
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

            byte[] out = new byte[0];

            if (method.equals("GET")) {
                String query = "";
                for (FormEntry entry: params) {
                    if (query.length() > 0) {
                        query += "&";
                    }
                    query += URLEncoder.encode(entry.getKey(), charset) + "=" + URLEncoder.encode(entry.getValue(), charset);
                }
                fullPath += "?" + query;
            }

            long start = System.currentTimeMillis();
            URL url = new URL(fullPath);
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod(method);
            http.setRequestProperty("User-Agent", "TinyBrowser");
            http.setDoOutput(true);

            if (!method.equals("GET")) {
                out = prepareBody(http, params, charset, multipart);
            }

            String response = "";

            debug = true;

            if (debug) {
                System.out.println("Connecting...");
            }

            http.connect();
            
            if (!method.equals("GET")) {

                OutputStream os = null;

                try {
                    if (debug) {
                        String size = "";
                        if (out.length >= 1024 * 1024 * 1024) {
                            size = Math.floor((double) out.length / (1024 * 1024 * 1024) * 100) / 100 + " GB";
                        } else if (out.length >= 1024 * 1024) {
                            size = Math.floor((double) out.length / (1024 * 1024) * 100) / 100 + " MB";
                        } else {
                            size = out.length >= 1024 ? Math.floor((double) out.length / 1024 * 100) / 100 + " KB" : out.length + " bytes";
                        }
                        System.out.println("Started sending data (" + size + ")");
                    }
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

            if (debug) {
                System.out.println("Started receiving data");
            }

            InputStream is = http.getInputStream();
            byte[] bytes = getBytes(is, 10000000);
            response = new String(bytes, charset);

            Pattern p = Pattern.compile("<meta\\s+http-equiv=\"Content-Type\"\\s+content=\"text/html;\\s*charset=([a-zA-Z0-9-]+)\"");
            Matcher m = p.matcher(response);
            if (m.find()) {
                String explicitCharset = m.group(1);
                if (debug) {
                    System.out.println("Charset declaration found: " + explicitCharset.toLowerCase());
                }
                response = new String(bytes, charset);
            }
            
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

    public static byte[] prepareBody(HttpURLConnection http, Vector<FormEntry> params, String charset, boolean multipart) {
        byte[] out = new byte[0];

        if (params == null) params = new Vector<FormEntry>();
        ArrayList<String> parts = new ArrayList<String>();
        String boundary = multipart ? generateBoundary() : "";
        ArrayList<byte[]> data = new ArrayList<byte[]>();
        for (FormEntry entry: params) {
            try {
                String content = "";
                if (!multipart) {
                    content = URLEncoder.encode(entry.getKey(), charset) + "=" + URLEncoder.encode(entry.getValue(), charset);
                    data.add(((!data.isEmpty() ? "&" : "") + content).getBytes(Charset.forName(charset)));
                } else {
                    if (!data.isEmpty()) {
                        content += "\n";
                    }
                    content += "--" + boundary + "\n" +
                          "Content-Disposition: form-data; name=\"" + URLEncoder.encode(entry.getKey(), charset) + "\"";
                    boolean isFile = entry.getValue().matches("\\[filename=\"[^\"]+\"\\]");
                    int pos = 0;
                    String contentType = "";
                    String filename = "";
                    if (isFile) {
                        pos = entry.getValue().indexOf("\"", 11);
                        String[] path = entry.getValue().substring(11, pos).split(File.separator.replace("\\", "\\\\"));
                        filename = path[path.length-1];
                        contentType = getMimeType(filename);
                        content += "; filename=\"" + URLEncoder.encode(filename, charset) + "\"\n";
                        content += "Content-Type: " + contentType;
                        content += "\n\n";

                        File file = new File(entry.getValue().substring(11, pos));

                        byte[] b1 = content.getBytes(Charset.forName(charset));
                        byte[] b2 = null;
                        try {
                            b2 = Util.readFile(file);
                            byte[] b = new byte[b1.length + b2.length];
                            for (int i = 0; i < b1.length + b2.length; i++) {
                                b[i] = i < b1.length ? b1[i] : b2[i-b1.length];
                            }
                            data.add(b);
                        } catch (IOException ex) {
                            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        String fileData = "";
                        if (contentType.startsWith("text/") || filename.matches(".*\\.(xml|json)$")) {
                            for (int i = 0; i < b2.length; i++) {
                                fileData += (char) b2[i];
                            }
                        } else {
                            for (int i = 0; i < 100; i++) {
                                fileData += Integer.toHexString(b2[i]) + " ";
                            }
                            if (b2.length > 100) {
                                fileData += "... (" + (b2.length - 100) + " bytes more)";
                            }
                        }
                        content += fileData;
                    } else {
                        content += "\n\n";
                        content += entry.getValue();
                        data.add(content.getBytes(Charset.forName(charset)));
                    }
                }

                parts.add(content);

                if (debug) {
                    System.out.println(content);
                }

            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (multipart) {
            parts.add("--" + boundary + "--\n");
            data.add(("\n--" + boundary + "--\n").getBytes(Charset.forName(charset)));
        }

        int length = 0;
        for (byte[] b: data) {
            length += b.length;
        }
        System.out.println();
        out = new byte[length];
        int pos = 0;
        for (byte[] b: data) {
            for (int i = 0; i < b.length; i++) {
                out[pos++] = b[i];
            }
        }

        String contentType = multipart ? "multipart/form-data; boundary=" + boundary : "application/x-www-form-urlencoded;charset=" + charset.toLowerCase();

        http.setFixedLengthStreamingMode(length);
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


    public static File makeBinaryRequest(String path, String method, Vector<FormEntry> params, String charset, boolean noCache, boolean multipart) {
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
    
    public static String makeRequest(String path, String method, Vector<FormEntry> params, boolean noCache) {
        return makeRequest(path, method, params, defaultCharset, noCache, false);
    }

    public static String makeRequest(String path, String method, Vector<FormEntry> params, boolean noCache, boolean multipart) {
        return makeRequest(path, method, params, defaultCharset, noCache, multipart);
    }

    public static String makeRequest(String path) {
        return makeRequest(path, "GET", new Vector<FormEntry>(), defaultCharset, false, false);
    }

    public static String makeRequest(String path, boolean noCache) {
        return makeRequest(path, "GET", new Vector<FormEntry>(), defaultCharset, noCache, false);
    }

    public static File makeBinaryRequest(String path, String method, Vector<FormEntry> params, boolean noCache) {
        return makeBinaryRequest(path, method, params, defaultCharset, noCache, false);
    }

    public static File makeBinaryRequest(String path, String method, Vector<FormEntry> params, boolean noCache, boolean multipart) {
        return makeBinaryRequest(path, method, params, defaultCharset, noCache, multipart);
    }

    public static File makeBinaryRequest(String path) {
        return makeBinaryRequest(path, "GET", new Vector<FormEntry>(), defaultCharset, false, false);
    }

    public static File makeBinaryRequest(String path, boolean noCache) {
        return makeBinaryRequest(path, "GET", new Vector<FormEntry>(), defaultCharset, noCache, false);
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
