package service;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Alex
 */
public class WebpConverter extends Thread {

    String input;
    String output;

    String ffmpegPath;

    public WebpConverter(String input, String output) {
        this.input = input;
        this.output = output;
    }

    private void detectFileLocation() {
        ffmpegPath = tinybrowser.Main.getInstallPath() + File.separatorChar + "codecs" + File.separatorChar;
    }

    @Override
    public void run() {
        //if (started) return;
        detectFileLocation();
        try {
            if (input == null || output == null) return;
            if (!(new File(input)).exists()) return;
            if (!input.matches(".*\\.\\w+$")) input += ".webp";
            if (!output.matches(".*\\.\\w+$")) output += ".png";

            //System.out.println(ffmpegPath + File.separatorChar + "dwebp " + input + " -o " + output);
            proc = Runtime.getRuntime().exec(ffmpegPath + File.separatorChar + "dwebp " + input + " -o " + output);

            started = true;

            eg = new StreamGobbler(proc.getErrorStream(), "err");
            og = new StreamGobbler(proc.getInputStream(), "out");
            //sender = new StreamSender(System.console().reader(), proc.getOutputStream());

            eg.start();
            og.start();
            //sender.start();

            proc.waitFor();

            started = false;

            proc.destroy();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void finish() {
        try {
            proc.getOutputStream().write('q');
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        proc.destroy();
        started = false;
        instance = null;
        eg.stop();
        og.stop();
        stop();
    }

    public static boolean isStarted() {
        return started;
    }

    public static boolean launch(String in, String out) {
        if (started) return false;
        instance = new WebpConverter(in, out);
        instance.start();
        started = true;
        return true;
    }

    public static WebpConverter getInstance() {
        return instance;
    }

    public static synchronized void deleteInstance() {
        started = false;
        if (instance !=  null) {
            getInstance().finish();
        }
        instance = null;
        if (eg != null && og != null) {
            eg.stop();
            og.stop();
            eg = null;
            og = null;
        }
    }

    public static StreamGobbler getErrorStream() {
        return eg;
    }

    public static Process proc;
    private static WebpConverter instance;
    private static StreamGobbler eg;
    private static StreamGobbler og;
    private static volatile boolean started = false;
}
