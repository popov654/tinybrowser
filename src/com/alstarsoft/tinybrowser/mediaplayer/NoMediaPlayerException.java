/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.alstarsoft.tinybrowser.mediaplayer;

/**
 *
 * @author Alex
 */
public class NoMediaPlayerException extends Exception {
    @Override
    public String getMessage() {
        return "No MediaPlayer is set up";
    }
}
