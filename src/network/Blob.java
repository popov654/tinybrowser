package network;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class Blob extends DataPart {

    public Blob(Blob[] parts) {
        this.parts = new Vector(Arrays.asList(parts));
        calculateSize();
    }

    public Blob(ArrayList<Blob> parts) {
        this.parts = new Vector(parts);
        calculateSize();
    }

    public Blob(Vector<Blob> parts) {
        this.parts = (Vector<Blob>) parts.clone();
        calculateSize();
    }

    public Blob(File file) {
        super("", file, "");
    }

    public Blob(byte[] bytes) {
        super("", bytes, "");
    }

    public Blob(String str) {
        super("", str.getBytes(), "");
    }

    public Blob(Blob[] parts, String type) {
        this.parts = new Vector(Arrays.asList(parts));
        mimeType = type;
        calculateSize();
    }

    public Blob(ArrayList<Blob> parts, String type) {
        this.parts = new Vector(parts);
        mimeType = type;
        calculateSize();
    }

    public Blob(Vector<Blob> parts, String type) {
        this.parts = (Vector<Blob>) parts.clone();
        mimeType = type;
        calculateSize();
    }

    public String getType() {
        return mimeType;
    }

    private void calculateSize() {
        total = 0;
        for (Blob part: parts) {
            total += part.getSize();
        }
    }

    @Override
    public byte[] nextChunk() {
        if (parts.size() == 0) {
            return super.nextChunk();
        }
        
        int length = (int) Math.min(total - position, CHUNK_SIZE);
        byte[] result = getSliceBytes(position, position + length);
        position += length;

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
