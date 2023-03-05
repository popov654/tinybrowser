package cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.Request;

/**
 *
 * @author Alex
 */
public class DefaultCache extends Cache {

    @Override
    public boolean contains(String url) {
        if (!url.startsWith("http")) {
            return false;
        }

        int pos = url.lastIndexOf("/");
        if (pos == -1) return false;

        String filename = url.substring(pos+1);
        url = url.substring(0, pos);

        String hash = getHash(url);

        String[] parts = new String[levels + 1];

        for (int i = 0; i < levels; i++) {
            parts[i] = hash.substring(0, length[0]);
            hash = hash.substring(length[0]);
        }
        parts[levels] = hash.substring(0, length[1]);

        File f = new File(cacheDir);
        if (!f.exists()) return false;

        String path = cacheDir + File.separatorChar;
        for (int i = 0; i < parts.length; i++) {
            path += parts[i] + File.separatorChar;
            f = new File(path);
            if (!f.exists()) return false;
        }

        path += filename;
        f = new File(path);

        return f.exists();
    }

    @Override
    public String get(String url) {

        if (!url.startsWith("http")) {
            return url;
        }

        int pos = url.lastIndexOf("/");
        if (pos == -1) return url;

        String filename = url.substring(pos+1);
        url = url.substring(0, pos);

        String hash = getHash(url);

        String[] parts = new String[levels + 1];

        for (int i = 0; i < levels; i++) {
            parts[i] = hash.substring(0, length[0]);
            hash = hash.substring(length[0]);
        }
        parts[levels] = hash.substring(0, length[1]);

        File f = new File(cacheDir);
        if (!f.exists()) f.mkdir();

        String path = cacheDir + File.separatorChar;
        for (int i = 0; i < parts.length; i++) {
            path += parts[i] + File.separatorChar;
            f = new File(path);
            if (!f.exists()) f.mkdir();
        }

        path += filename;
        f = new File(path);

        if (!f.exists() || f.lastModified() < System.currentTimeMillis() / 1000 - maxAge) {
            boolean binary = filename.matches(".*\\.(bmp|ico|jpg|jpeg|png|gif|webp|wav|mp3|aac|mp2|m4a|flac|ogg|avi|mp4|mov|3gpp|mpg|mpeg|m4v|mkv|docx?|xlsx?|pptx?|exe|msi|zip|rar|psd|tar|gz)$");
            if (!binary) {
                String content = Request.makeRequest(url + "/" + filename, true);
                // Network problem of malformed URL
                if (content == null) {
                    return f.exists() ? path : url + "/" + filename;
                }
                try {
                    f = new File(path);
                    f.createNewFile();
                    FileWriter fw = new FileWriter(f);
                    fw.append(content);
                    fw.close();
                } catch (IOException ex) {
                    Logger.getLogger(DefaultCache.class.getName()).log(Level.SEVERE, null, ex);
                    return url + "/" + filename;
                }
            } else {
                File file = Request.makeBinaryRequest(url + "/" + filename, true);
                // Network problem of malformed URL
                if (file == null) {
                    return f.exists() ? path : url + "/" + filename;
                }
                try {
                    InputStream in = new BufferedInputStream(new FileInputStream(file));
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(f));

                    byte[] buffer = new byte[1024];
                    int lengthRead;
                    while ((lengthRead = in.read(buffer)) > 0) {
                        out.write(buffer, 0, lengthRead);
                        out.flush();
                    }
                    file.delete();

                } catch (IOException ex) {
                    Logger.getLogger(DefaultCache.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return path;
    }

    private String getHash(String data) {
        String result = null;
        try {
            MessageDigest algorithm = MessageDigest.getInstance(digestAlg);
            byte[] messageDigest = algorithm.digest(data.getBytes());
            result = toHexString(messageDigest);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(DefaultCache.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private static String toHexString(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "x", bi);
    }

    public final int levels = 1;
    public int maxAge = 3 * 24 * 3600;

    public final int[] length = new int[] {2, 8};
    public final String digestAlg = "MD5";
    public final String cacheDir = "cache";
}
