package tinybrowser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;


/**
 *
 * @author Alex
 */
public class CharsetDetector {

    public static Charset detectCharset(byte[] data) {
        try {
        
            byte[][] bytes = {
                new String("à").getBytes("UTF-16LE"),
                new String("å").getBytes("UTF-16LE"),
                new String("è").getBytes("UTF-16LE"),
                new String("î").getBytes("UTF-16LE"),
                new String("ñ").getBytes("UTF-16LE"),
                new String("ò").getBytes("UTF-16LE"),
                new String("ï").getBytes("UTF-16LE")
            };

            if ((data[0] & 255) == 254 && (data[1] & 255) == 255) {
                return Charset.forName("UTF-16BE");
            }
            if ((data[0] & 255) == 255 && (data[1] & 255) == 254) {
                return Charset.forName("UTF-16LE");
            }

            for (int i = 0; i < data.length-1; i++) {
                if ((data[i] & 255) >= 192 && (data[i] & 255) < 224 && (data[i+1] & 255) >= 128 && (data[i+1] & 255) < 192) {
                    return Charset.forName("UTF-8");
                }
                for (int j = 0; j < bytes.length; j++) {
                    if ((bytes[j][0] & 255) == (data[i] & 255) && (bytes[j][1] & 255) == (data[i+1] & 255)) {
                        return Charset.forName("UTF-16BE");
                    }
                    if ((bytes[j][1] & 255) == (data[i] & 255) && (bytes[j][0] & 255) == (data[i+1] & 255)) {
                        return Charset.forName("UTF-16LE");
                    }
                }
            }

            return Charset.forName(System.getProperty("file.encoding"));

        } catch (Exception e) {
            return Charset.forName(System.getProperty("file.encoding"));
        }
    }

    public static Charset detectCharset(File f) {
        try {

            BufferedInputStream input = new BufferedInputStream(new FileInputStream(f));
            byte[] buffer = new byte[4096];
            input.read(buffer);
            input.close();

            return detectCharset(buffer);

        } catch (Exception e) {
            return Charset.forName(System.getProperty("file.encoding"));
        }
    }

    private boolean identify(byte[] bytes, CharsetDecoder decoder) {
        try {
            decoder.decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException e) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        File f = new File("html/files.htm");

        Charset charset = detectCharset(f);

        if (charset != null) {
            System.out.println(charset.displayName());
        } else {
            System.out.println("Unrecognized charset");
        }
    }
}