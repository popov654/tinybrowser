package com.alstarsoft.tinybrowser.network;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Alex
 */
public class Base64Encoder {

    public Base64Encoder(DataPart p) {
        data = p;
    }

    public boolean hasNextChunk() {
        return data.hasNextChunk();
    }

    public byte[] nextChunk() {
        boolean old_value = data.base64;
        data.base64 = false;
        byte[] srcData = data.nextChunk();
        data.base64 = old_value;

        int len = data.hasNextChunk() ? (remainder.length + srcData.length) / 3 * 3 : remainder.length + srcData.length;
        byte[] src = new byte[len];
        int pos = 0;
        while (pos < remainder.length) {
            src[pos] = remainder[pos];
            pos++;
        }
        while (pos < len) {
            src[pos] = srcData[pos - remainder.length];
            pos++;
        }

        int remSize = srcData.length - (len - remainder.length);
        remainder = new byte[remSize];
        for (int i = 0; i < remSize; i++) {
            remainder[i] = srcData[srcData.length - remSize + i];
        }
        
        Base64 b64 = new Base64();
        try {
            return b64.encode(src);
        } catch (EncoderException ex) {}

        return null;
    }

    public String nextChunkAsString() {
        return new String(nextChunk());
    }
    
    private byte[] remainder = new byte[0];
    public DataPart data;
}
