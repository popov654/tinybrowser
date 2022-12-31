package service;

/**
 *
 * @author Alex
 */

import java.io.*;

public class StreamGobbler extends Thread {
    InputStream is;
    String type;
    OutputStream os;
    //long lastCheck = 0;

    public StreamGobbler(InputStream is, String type) {
        this(is, type, null);
    }
    public StreamGobbler(InputStream is, String type, OutputStream redirect) {
        this.is = is;
        this.type = type;
        this.os = redirect;
    }

    @Override
    public void run() {
        try {
            //int[] checkPoints = { 1527452, 3149213, 6741847 };
            PrintWriter pw = null;
            if (os != null) pw = new PrintWriter(os);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = br.readLine();
            while (line != null) {
                if (pw != null) pw.println(line);
                if (stop) {
                    stop = false;
                    continue;
                }
                /*if (tickCount > 40) continue;*/
                if (type.equals("out")) System.out.println(line);
                else if (type.equals("err")) System.out.println(line);
                String line_new = br.readLine();
                if (line_new == null || line_new.equals(line)) break;
                line = line_new;
                tickCount++;
                /*if (System.currentTimeMillis() - lastCheck > 30000) {
                    lastCheck = System.currentTimeMillis();

                }*/
                /*try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {}*/
            }
            if (pw != null) pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static volatile boolean stop = false;
    private static int tickCount = 0;
}