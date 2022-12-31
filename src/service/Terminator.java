/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package service;

/**
 *
 * @author Alex
 */
public class Terminator extends Thread {
    @Override
    public void run() {
        StreamGobbler.stop = true;
        WebpConverter sr = WebpConverter.getInstance();
        if (sr != null) sr.finish();
    }
}
