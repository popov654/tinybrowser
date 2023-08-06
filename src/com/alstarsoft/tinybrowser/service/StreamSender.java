package com.alstarsoft.tinybrowser.service;

/**
 *
 * @author Alex
 */

import java.io.*;

class StreamSender extends Thread {
    private InputStream is;
    BufferedReader reader;
    OutputStream os;

    StreamSender(InputStream is, OutputStream os) {
        this.reader = new BufferedReader(new InputStreamReader(is));
        this.os = os;
    }

    StreamSender(Reader reader, OutputStream os) {
        this.reader = new BufferedReader(reader);
        this.os = os;
    }

    @Override
    public void run() {
        try {
            OutputStreamWriter osr = new OutputStreamWriter(os);
            PrintWriter pw = new PrintWriter(osr);
            String line = null;
            while (true) {
                if ((line = reader.readLine()) != null) {
                    pw.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
