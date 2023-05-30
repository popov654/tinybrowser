package network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class DataPart {

    public DataPart() {}

    public DataPart(File f) {
        this("", f, "");
    }

    public DataPart(String prefix, File f, String postfix) {
        file = f;
        this.prefix = prefix;
        this.postfix = postfix;
        total = prefix.getBytes().length + f.length() + postfix.getBytes().length;
        try {
            reader = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DataPart.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public DataPart(byte[] data) {
        this("", data, "");
    }

    public DataPart(String prefix, byte[] data, String postfix) {
        this.prefix = prefix;
        this.postfix = postfix;
        total = prefix.getBytes().length + data.length + postfix.getBytes().length;
        content = data;
    }

    public byte[] nextChunk() {

        if (content != null) {
            byte[] prefix_bytes = prefix.getBytes();
            byte[] postfix_bytes = postfix.getBytes();
            byte[] result = new byte[prefix_bytes.length + content.length + postfix_bytes.length];

            int pos = 0;

            for (int i = 0; i < prefix_bytes.length; i++) {
                result[pos++] = prefix_bytes[i];
            }
            for (int i = 0; i < content.length; i++) {
                result[pos++] = content[i];
            }
            for (int i = 0; i < postfix_bytes.length; i++) {
                result[pos++] = postfix_bytes[i];
            }
            position = (int) total;

            return result;
        }

        int size = CHUNK_SIZE;
        int fsize = CHUNK_SIZE;
        if (position + size - prefix.getBytes().length >= file.length()) {
            fsize = (int) (file.length() - (position - prefix.getBytes().length));
            size = fsize + postfix.getBytes().length;
        }
        byte[] result = new byte[size];

        int pos = 0;

        if (position == 0) {
            byte[] prefix_bytes = prefix.getBytes();
            for (int i = 0; i < prefix_bytes.length; i++) {
                result[pos++] = prefix_bytes[i];
            }
            fsize -= prefix_bytes.length;
            position += prefix_bytes.length;
        }

        int len = -1;
        byte[] file_bytes = new byte[fsize];
        try {
            len = reader.read(file_bytes);
            for (int i = 0; i < file_bytes.length; i++) {
                result[pos++] = file_bytes[i];
            }
            position += len;
        } catch (Exception ex) {
            Logger.getLogger(DataPart.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (position - prefix.length() >= file.length() || len < fsize) {
            byte[] postfix_bytes = postfix.getBytes();
            for (int i = 0; i < postfix_bytes.length; i++) {
                result[pos++] = postfix_bytes[i];
            }
            position += postfix_bytes.length;
        }

        return result;
    }

    public boolean hasNextChunk() {
        return content == null && position == 0 || position < total;
    }

    public void reset() {
        position = 0;
    }

    public void seek(long pos) {
        position = pos;
    }

    public byte[] getBytes() {
        return getSliceBytes(0, total);
    }

    public byte[] getSliceBytes(long start, long end) {
        if (start >= end) {
            return null;
        }

        if (end > total ) {
            end = total;
        }

        byte[] data = new byte[(int)(end-start)];
        int pos = 0;
        if (content != null) {
            pos = (int) start;
            while (pos < end && pos < content.length) {
                data[(int)(pos-start)] = content[pos];
                pos++;
            }
        } else {
            long old_pos = position;
            position = (int) start;
            while (position < end) {
                byte[] slice = nextChunk();
                for (int i = 0; i < slice.length; i++) {
                    data[(int)(pos-start)] = slice[i];
                    pos++;
                    if (pos == end-start) break;
                }
            }
            position = old_pos;
        }

        return data;
    }

    public DataPart getSlice(long start, long end) {
        return new DataPart("", getSliceBytes(start, end), "");
    }

    public long getSize() {
        return total;
    }

    public void dispose() {
        content = null;
        file = null;
        total = 0;
        position = 0;
    }

    private FileInputStream reader;

    public String prefix = "";
    public byte[] content;
    public File file;
    public long position = 0;
    public long total = 0;
    public String postfix = "";

    public static int CHUNK_SIZE = 512000;
}
