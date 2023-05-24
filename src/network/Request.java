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
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
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

            if (http instanceof HttpURLConnection) {
                ((HttpURLConnection) http).setRequestMethod(method);
            } else if (http instanceof HttpsURLConnection) {
                ((HttpsURLConnection) http).setRequestMethod(method);
            }

            setRequestHeaders(http);
            http.setDoOutput(true);

            Vector<DataPart> parts = new Vector<DataPart>();

            if (!method.equals("GET")) {
                parts = prepareBody(http, params, charset, multipart);
            }

            String response = "";

            debug = true;

            StatusLogger logger = new StatusLogger();
            Thread t = new Thread(logger);
            t.start();

            if (debug) {
                logger.post("Connecting...");
            }

            http.connect();

            if (!method.equals("GET")) {
                sendData(http, parts);
            }

            if (http.getContentType() == null || http.getContentType().matches("^(image|audio|video|application)")) {
                return null;
            }

            if (debug) {
                logger.post("Started receiving data");
            }

            response = getResponse(http, charset);
            
            long end = System.currentTimeMillis();
            logger.post("Loaded file \"" + fullPath + "\" in " + (end - start) + " ms");

            debug = false;

            logger.stop();

            return response;
        } catch (MalformedURLException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static String getResponse(URLConnection con, String charset) {
        InputStream is = null;
        String response = null;
        try {
            is = con.getInputStream();
            byte[] bytes = getBytes(is, 10000000);
            response = new String(bytes, charset);
            Pattern p = Pattern.compile("<meta\\s+con-equiv=\"Content-Type\"\\s+content=\"text/html;\\s*charset=([a-zA-Z0-9-]+)\"");
            Matcher m = p.matcher(response);
            if (m.find()) {
                String explicitCharset = m.group(1);
                if (debug) {
                    System.out.println("Charset declaration found: " + explicitCharset.toLowerCase());
                }
                response = new String(bytes, charset);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return response;
    }

    public static Vector<DataPart> prepareBody(URLConnection http, Vector<FormEntry> params, String charset, boolean multipart) {
        byte[] out = new byte[0];

        for (FormEntry entry: params) {
            if (entry.getValue().startsWith("[filename=")) {
                multipart = true;
            }
        }

        if (params == null) params = new Vector<FormEntry>();
        Vector<String> parts = new Vector<String>();
        String boundary = multipart ? generateBoundary() : "";
        Vector<DataPart> data = new Vector<DataPart>();
        for (FormEntry entry: params) {
            try {
                String content = "";
                DataPart part;
                if (!multipart) {
                    content = URLEncoder.encode(entry.getKey(), charset) + "=" + URLEncoder.encode(entry.getValue(), charset);
                    part = new DataPart("", ((!data.isEmpty() ? "&" : "") + content).getBytes(Charset.forName(charset)), "");
                } else {
                    String header = "";
                    if (!data.isEmpty()) {
                        header += "\n";
                    }
                    header += "--" + boundary + "\n" +
                          "Content-Disposition: form-data; name=\"" + URLEncoder.encode(entry.getKey(), charset).replaceAll("%5B%5D$", "[]") + "\"";
                    
                    String postfix = (entry == params.lastElement()) ? "\n--" + boundary + "--\n" : "";

                    boolean isFile = entry.getValue().matches("\\[filename=\"[^\"]+\"\\]");
                    int pos = 0;
                    String contentType = "";
                    String filename = "";
                    if (isFile) {
                        pos = entry.getValue().indexOf("\"", 11);
                        String[] path = entry.getValue().substring(11, pos).split(File.separator.replace("\\", "\\\\"));
                        filename = path[path.length-1];
                        contentType = getMimeType(filename);
                        
                        header += "; filename=\"" + URLEncoder.encode(filename, charset) + "\"\n";
                        header += "Content-Type: " + contentType;
                        header += "\n\n";

                        File file = new File(entry.getValue().substring(11, pos));

                        byte[] b = null;
                        try {
                            b = Util.readFile(file);
                        } catch (IOException ex) {}
                            

                        String fileData = "";
                        if (contentType.startsWith("text/") || filename.matches(".*\\.(xml|json)$")) {
                            int limit = (int) Math.min(b.length, 500);
                            for (int i = 0; i < limit; i++) {
                                fileData += (char) b[i];
                            }
                            if (b.length > limit) {
                                fileData += "... (" + (b.length - limit) + " bytes more)";
                            }
                        } else {
                            int limit = (int) Math.min(b.length, 100);
                            for (int i = 0; i < limit; i++) {
                                fileData += Integer.toHexString(b[i]) + " ";
                            }
                            if (b.length > limit) {
                                fileData += "... (" + (b.length - limit) + " bytes more)";
                            }
                        }
                        
                        content += header + fileData + postfix;

                        part = new DataPart(header, file, postfix);
                    } else {
                        header += "\n\n";
                        part = new DataPart(header, (entry.getValue()).getBytes(Charset.forName(charset)), postfix);
                    }
                }

                data.add(part);
                parts.add(content);

                if (debug) {
                    System.out.println(content);
                }

            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        int length = 0;
        for (DataPart part: data) {
            length += part.total;
        }
        System.out.println();

        System.out.println(length + " bytes total");

        String contentType = multipart ? "multipart/form-data; boundary=" + boundary : "application/x-www-form-urlencoded;charset=" + charset.toLowerCase();

        if (http instanceof HttpURLConnection) {
            ((HttpURLConnection) http).setFixedLengthStreamingMode(length);
        } else if (http instanceof HttpsURLConnection) {
            ((HttpsURLConnection) http).setFixedLengthStreamingMode(length);
        }
        http.setRequestProperty("Content-Type", contentType);

        return data;
    }

    static class StatusLogger implements Runnable {
        @Override
        public void run() {
            while (!stopped) {
                while (messages.size() > pos) {
                    System.out.println(messages.get(pos++));
                }
                try {
                    Thread.sleep(30);
                } catch (Exception ex) {}
            }
        }

        public synchronized void stop() {
            stopped = true;
        }

        public synchronized void post(String message) {
            messages.add(message);
        }

        public Vector<String> messages = new Vector<String>();
        public int pos = 0;

        public volatile boolean stopped = false;
    }

    public static void sendData(URLConnection http, Vector<DataPart> parts) {
        OutputStream os = null;

        int total = 0;
        for (DataPart part: parts) {
            total += part.total;
        }
        if (debug) {
            String size = "";
            if (total >= 1024 * 1024 * 1024) {
                size = Math.floor((double) total / (1024 * 1024 * 1024) * 100) / 100 + " GB";
            } else if (total >= 1024 * 1024) {
                size = Math.floor((double) total / (1024 * 1024) * 100) / 100 + " MB";
            } else {
                size = total >= 1024 ? Math.floor((double) total / 1024 * 100) / 100 + " KB" : total + " bytes";
            }
            System.out.println("Started sending data (" + size + ")");
        }

        int chunkNumber = 0;
        int chunksTotal = 0;
        
        StatusLogger logger = new StatusLogger();
        Thread t = new Thread(logger);
        t.start();

        try {
            os = http.getOutputStream();

            for (DataPart part: parts) {
                chunkNumber = 0;
                chunksTotal = (int) Math.ceil((double) part.total / DataPart.CHUNK_SIZE);
                while (part.hasNextChunk()) {
                    chunkNumber++;
                    if (debug && part.file != null) {
                        logger.post("Sending chunk " + chunkNumber + " of " + chunksTotal + " (" + part.total + " bytes)");
                    }
                    byte[] data = part.nextChunk();
                    //bytesSent += data.length;
                    //if (debug) {
                    //    logger.post(bytesSent + " bytes actually sent");
                    //}
                    os.write(data);
                }
            }
            logger.stop();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (os != null) try {
                os.close();
            } catch (IOException ex) {}
        }

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
        } else if (filename.matches(".*\\.(docx?|xlsx?|pptx?|psd|7z|rar|zip|pdf|json|xml|exe|dmg|dms|iso|bin|pkg|dump|so)$")) {
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
            if (filename.endsWith(".exe")) {
                return "application/x-msdownload";
            }
            return "application/octet-stream";
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
        return URLConnection.guessContentTypeFromName(filename);
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

            URLConnection http;

            if (url.getProtocol().equals("https")) {
                http = (HttpsURLConnection)url.openConnection();
            } else {
                http = (HttpURLConnection)url.openConnection();
            }

            if (http instanceof HttpURLConnection) {
                ((HttpURLConnection) http).setRequestMethod(method);
            } else if (http instanceof HttpsURLConnection) {
                ((HttpsURLConnection) http).setRequestMethod(method);
            }

            setRequestHeaders(http);
            http.setDoOutput(true);

            Vector<DataPart> parts = new Vector<DataPart>();

            if (!method.equals("GET")) {
                parts = prepareBody(http, params, charset, multipart);
            }

            http.connect();

            if (!method.equals("GET")) {
                sendData(http, parts);
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

    public static void setRequestHeaders(URLConnection con) {
        con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        con.setRequestProperty("Accept-Encoding", "text/plain");
        con.setRequestProperty("User-Agent", "TinyBrowser " + tinybrowser.Main.getVersion());
        con.setRequestProperty("Cache-Control", "no-cache");
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
        final int buffer_size = 1024;
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
            is.close();
        }
        catch (Exception ex) {
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
