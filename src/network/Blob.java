package network;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Alex
 */
public class Blob extends DataPart {

    public Blob(Blob[] parts) {
        this.parts = new Vector(Arrays.asList(parts));
        calculateSize();
        detectMimeType();
    }

    public Blob(ArrayList<Blob> parts) {
        this.parts = new Vector(parts);
        calculateSize();
        detectMimeType();
    }

    public Blob(Vector<Blob> parts) {
        this.parts = (Vector<Blob>) parts.clone();
        calculateSize();
        detectMimeType();
    }

    public Blob(File file) {
        super("", file, "");
        mimeType = Request.getMimeType(file.getName());
    }

    public Blob(byte[] bytes) {
        super("", bytes, "");
        mimeType = "application/octet-stream";
    }

    public Blob(byte[] bytes, String type) {
        super("", bytes, "");
        mimeType = type;
    }

    public Blob(String str) {
        super("", str.getBytes(), "");
        mimeType = "text/plain";
    }

    public Blob(Blob[] parts, String type) {
        this.parts = new Vector(Arrays.asList(parts));
        mimeType = type;
        calculateSize();
        detectMimeType();
    }

    public Blob(ArrayList<Blob> parts, String type) {
        this.parts = new Vector(parts);
        mimeType = type;
        calculateSize();
        detectMimeType();
    }

    public Blob(Vector<Blob> parts, String type) {
        this.parts = (Vector<Blob>) parts.clone();
        mimeType = type;
        calculateSize();
        detectMimeType();
    }

    public String getType() {
        return mimeType;
    }

    public void calculateSize() {
        contentLength = 0;
        for (Blob part: parts) {
            contentLength += part.getSize();
        }
        total = prefix.getBytes().length + contentLength + postfix.getBytes().length;
    }

    public void detectMimeType() {
        boolean found = false;
        boolean hasBinary = false;
        for (Blob part: parts) {
            if (!part.getType().equals(mimeType) && !found) {
                mimeType = part.getType();
                found = true;
            } else if (!part.getType().equals(mimeType)) {
                mimeType = hasBinary ? "application/octet-stream" : "text/plain";
            }
            if (!part.getType().startsWith("text/") && !part.getType().matches("application/(json|xml|javascript).*")) {
                hasBinary = true;
            }
        }
    }

    @Override
    public byte[] nextChunk() {
        if (parts.size() == 0) {
            return super.nextChunk();
        }

        int size = CHUNK_SIZE;
        int fsize = (int) Math.min(total, CHUNK_SIZE);
        if (position + size - prefix.getBytes().length >= contentLength) {
            fsize = (int) (contentLength - (position - prefix.getBytes().length));
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
            long offset = position - prefix.getBytes().length;
            len = (int) Math.min(contentLength - offset, CHUNK_SIZE);
            file_bytes = getSliceBytes(offset, offset + len);
            for (int i = 0; i < file_bytes.length; i++) {
                result[pos++] = file_bytes[i];
            }
            position += len;
        } catch (Exception ex) {
            Logger.getLogger(DataPart.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (position - prefix.length() >= contentLength || len < fsize) {
            byte[] postfix_bytes = postfix.getBytes();
            for (int i = 0; i < postfix_bytes.length; i++) {
                result[pos++] = postfix_bytes[i];
            }
            position += postfix_bytes.length;
        }

        return result;
    }

    @Override
    public String asBase64() {
        String result = "data:" + mimeType + ";base64,";
        try {
            Base64 b64 = new Base64();
            result += new String(b64.encode(getBytes()));
        } catch (EncoderException ex) {
            Logger.getLogger(Blob.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public byte[] getSliceBytes(long start, long end) {
        if (start >= end) {
            return null;
        }

        if (parts.size() == 0) {
            return super.getSliceBytes(start, end);
        }

        if (end > total) {
            end = total;
        }

        byte[] result = new byte[(int)(end-start)];
        int pos = 0;
        int wpos = 0;
        int index = 0;
        while (index < parts.size()-1 && start > pos + parts.get(index).getSize()-1) {
            pos += parts.get(index).getSize();
            index++;
        }
        if (start > pos) {
            if (end <= pos + parts.get(index).getSize()) {
                return parts.get(index).getSliceBytes(start-pos, end-pos);
            }
            byte[] b = parts.get(index).getSliceBytes(start-pos, parts.get(index).getSize());
            for (int i = 0; i < b.length; i++) {
                result[wpos++] = b[i];
            }
            pos += b.length;
            index++;
        }
        while (index < parts.size() && end > pos + parts.get(index).getSize()) {
            byte[] b = parts.get(index).getSliceBytes(0, parts.get(index).getSize());
            for (int i = 0; i < b.length; i++) {
                result[wpos++] = b[i];
            }
            pos += b.length;
            index++;
        }
        if (index < parts.size() && end > pos) {
            byte[] b = parts.get(index).getSliceBytes(0, end-pos);
            for (int i = 0; i < b.length; i++) {
                result[wpos++] = b[i];
            }
            pos += b.length;
        }

        return result;
    }

    @Override
    public Blob getSlice(long start, long end) {
        if (start >= end) {
            return null;
        }

        if (parts.size() == 0) {
            return new Blob(super.getSliceBytes(start, end));
        }
        
        Vector<Blob> p = new Vector<Blob>();
        long pos = 0;
        int index = 0;
        while (index < parts.size()-1 && start > pos + parts.get(index).getSize()-1) {
            pos += parts.get(index).getSize();
            index++;
        }
        if (start > pos) {
            if (end <= pos + parts.get(index).getSize()) {
                p.add(parts.get(index).getSlice(start-pos, end-pos));
                return new Blob(p);
            }
            p.add(parts.get(index).getSlice(start-pos, parts.get(index).getSize()));
            pos += parts.get(index).getSize();
            index++;
        }
        while (index < parts.size() && end > pos + parts.get(index).getSize()) {
            p.add((Blob) parts.get(index).clone());
            pos += parts.get(index).getSize();
            index++;
        }
        if (index < parts.size() && end > pos) {
            p.add(parts.get(index).getSlice(0, end-pos));
        }

        return p.size() == 1 && UNWRAP_SINGLE_ITEM_LISTS ? p.get(0) : new Blob(p);
    }

    public Blob getSlice(long start, long end, String contentType) {
        Blob b = getSlice(start, end);
        b.mimeType = contentType;
        return b;
    }

    @Override
    public Blob clone() {
        if (parts.size() == 0 && file != null) {
            return new Blob(file);
        } else if (parts.size() == 0 && content != null) {
            return new Blob(content);
        }
        return new Blob(parts, mimeType);
    }

    @Override
    public void dispose() {
        for (Blob part: parts) {
            part.dispose();
        }
        parts.clear();
        super.dispose();
    }
    
    public Vector<Blob> parts = new Vector<Blob>();
    public String mimeType = "application/octet-stream";

    public static boolean UNWRAP_SINGLE_ITEM_LISTS = true;
}
