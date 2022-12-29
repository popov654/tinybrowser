package mediaplayer;

import render.*;
import com.sun.jna.NativeLibrary;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import uk.co.caprica.vlcj.component.AudioMediaPlayerComponent;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.FullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.windows.Win32FullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.x.XFullScreenStrategy;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

/**
 *
 * @author Alex
 */
public class MediaPlayer {

    public MediaPlayer() {
        initPlayer();
    }

    public void resumePlayback(int time) {
        fader = new Fader(-1);
        fader.run();
        if (type == AUDIO) {
            mediaPlayerComponent.getMediaPlayer().setTime(time);
        } else {
            avPlayerComponent.getMediaPlayer().setTime(time);
        }
        fader = new Fader(1);
        fader.run();
    }

    public void resumePlayback() {
        fader = new Fader(1);
        fader.run();
    }

    public void pausePlayback() {
        fader = new Fader(-1);
        fader.run();
    }

    public void changeTime(int time) {
        fader = new Fader(-1);
        fader.run();
        if (type == AUDIO) {
            mediaPlayerComponent.getMediaPlayer().setTime(time);
        } else {
            avPlayerComponent.getMediaPlayer().setTime(time);
        }
        fader = new Fader(1);
        fader.run();
    }

    private void initPlayer() {
        //boolean found = new NativeDiscovery().discover();
        //if (!found) {
            //NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), NATIVE_LIBRARY_SEARCH_PATH);
            NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), LOCAL_PATH);
        //}
        //System.out.println(LibVlc.INSTANCE.libvlc_get_version());


        if (type != VIDEO) {
            //mediaPlayerComponent = new AudioMediaPlayerComponent();
        }
        
        //mediaPlayerComponent.getMediaPlayer().setVolume(default_volume);
    }

    public MediaPlayerEventAdapter primaryAdapter;
    public ArrayList<MediaPlayerEventAdapter> adapters = new ArrayList<MediaPlayerEventAdapter>();

    public void setAdapter(MediaPlayerEventAdapter adapter) {
        addAdapter(adapter);
        primaryAdapter = adapter;
    }

    public void addAdapter(MediaPlayerEventAdapter adapter) {
        uk.co.caprica.vlcj.player.MediaPlayer player = null;
        if (type != VIDEO && mediaPlayerComponent != null) {
            player = mediaPlayerComponent.getMediaPlayer();
        } else if (avPlayerComponent != null) {
            player = avPlayerComponent.getMediaPlayer();
        }
        if (player != null) {
            player.addMediaPlayerEventListener(adapter);
        }
        adapters.add(adapter);
    }

    public void removeAdapter(MediaPlayerEventAdapter adapter) {
        uk.co.caprica.vlcj.player.MediaPlayer player = null;
        if (type != VIDEO && mediaPlayerComponent != null) {
            player = mediaPlayerComponent.getMediaPlayer();
        } else if (avPlayerComponent != null) {
            player = avPlayerComponent.getMediaPlayer();
        }
        if (player != null) {
            player.removeMediaPlayerEventListener(adapter);
        }
        adapters.remove(adapter);
    }

    public void removeSecondaryAdapters() {
        for (MediaPlayerEventAdapter adapter: adapters) {
            if (adapter != primaryAdapter) {
                removeAdapter(adapter);
            }
        }
    }

    public boolean isConnectedTo(MediaController controller) {
        for (MediaPlayerEventAdapter adapter: adapters) {
            if (adapter == controller.getAdapter()) {
                return true;
            }
        }
        return false;
    }

    public void setListeners(uk.co.caprica.vlcj.player.MediaPlayer player, MediaPlayerEventAdapter adapter) {
        player.addMediaPlayerEventListener(adapter);
    }

    private void removeListeners(uk.co.caprica.vlcj.player.MediaPlayer player, MediaPlayerEventAdapter adapter) {
        player.removeMediaPlayerEventListener(adapter);
    }

    public void updateVideoSize() {
        video_surface.setBounds(video._x_, video._y_, video.width, video.height);
    }

    public void open(String url) {

        //mediaPlayerComponent = new AudioMediaPlayerComponent();
        //mediaPlayerComponent.getMediaPlayer().prepareMedia(url);

        source = url;

        if (type == VIDEO && mediaPlayerComponent.getMediaPlayer().getVideoTrackCount() == -1) {


            BufferFormatCallback bufferFormatCallback = new BufferFormatCallback() {
                @Override
                public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                    return new RV32BufferFormat(video.width, video.height);
                }
            };

            BufferFormatCallback bufferFormatFullScreenCallback = new BufferFormatCallback() {
                @Override
                public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                    return new RV32BufferFormat(width, height);
                }

                private int width = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
                private int height = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
            };

            avPlayerComponent = new DirectMediaPlayerComponent(bufferFormatCallback) {
                @Override
                protected RenderCallback onGetRenderCallback() {
                    return new RenderCallbackAdapter();
                }
            };

            avPlayerComponentFullScreen = new DirectMediaPlayerComponent(bufferFormatFullScreenCallback) {
                @Override
                protected RenderCallback onGetRenderCallback() {
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    return new RenderFullScreenCallbackAdapter((int)screenSize.getWidth(), (int)screenSize.getHeight());
                }
            };

            for (int i = 0; i < adapters.size(); i++) {
                avPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(adapters.get(i));
                avPlayerComponentFullScreen.getMediaPlayer().addMediaPlayerEventListener(adapters.get(i));
            }

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            image = new BufferedImage(video.width, video.height, java.awt.image.BufferedImage.TYPE_4BYTE_ABGR);
            image_fullscreen = new BufferedImage((int)screenSize.getWidth(), (int)screenSize.getHeight(), java.awt.image.BufferedImage.TYPE_4BYTE_ABGR);
            video.removeAll();
            video_surface = new VideoRenderer(image);
            video_surface_fullscreen = new FastVideoRenderer(image_fullscreen);
            video.add(video_surface);
            video_surface.setBounds(video._x_, video._y_, video.width, video.height);

            ep = new EmbeddedMediaPlayerComponent();

            fullscreen_window = new JFrame();
            fullscreen_window.setBackground(Color.BLACK);
            fullscreen_window.add(ep);
            fullscreen_window.add(video_surface_fullscreen);
            ep.setBounds(0, 0, (int)screenSize.getWidth(), (int)screenSize.getHeight());
            video_surface_fullscreen.setBounds(0, 0, 0, 1);
            //video_surface_fullscreen.setBounds(0, 0, (int)screenSize.getWidth(), (int)screenSize.getHeight());
            fullscreen_window.setBounds(0, 0, video_surface_fullscreen.getWidth(), video_surface_fullscreen.getHeight());
            fullscreen_window.setUndecorated(true);

            correction_delta = System.getProperty("os.name").equals("Windows XP") ? 500 : 100;

            strategy = System.getProperty("os.name").toLowerCase().contains("win") ?
                new Win32FullScreenStrategy(fullscreen_window) : new XFullScreenStrategy(fullscreen_window);

            MouseListener ml = new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() > 1) {
                        if (!is_fullscreen) {
                            enterFullScreen();
                        } else {
                            exitFullScreen();
                        }
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }
            };

            int condition = JPanel.WHEN_IN_FOCUSED_WINDOW;
            InputMap inputMap = video_surface_fullscreen.getInputMap(condition);
            ActionMap actionMap = video_surface_fullscreen.getActionMap();
            KeyStroke escKeyStrokePressed = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
            String escPressed = "ESC";
            inputMap.put(escKeyStrokePressed , escPressed);
            actionMap.put(escPressed, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                     exitFullScreen();
                }
            });

            fullscreen_window.addWindowListener(new WindowListener() {

                @Override
                public void windowOpened(WindowEvent e) {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void windowClosing(WindowEvent e) {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    exitFullScreen();
                }

                @Override
                public void windowIconified(WindowEvent e) {
                    exitFullScreen();
                }

                @Override
                public void windowDeiconified(WindowEvent e) {
                    enterFullScreen();
                }

                @Override
                public void windowActivated(WindowEvent e) {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void windowDeactivated(WindowEvent e) {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }

            });

            video_surface.addMouseListener(ml);
            video_surface_fullscreen.addMouseListener(ml);
            

            avPlayerComponent.getMediaPlayer().prepareMedia(url);
            //avPlayerComponentFullScreen.getMediaPlayer().prepareMedia(url);
            ep.getMediaPlayer().prepareMedia(url);

            avPlayerComponent.getMediaPlayer().setVolume(volume);
            ep.getMediaPlayer().setVolume(volume);

            //setListeners(avPlayerComponent.getMediaPlayer());

            if (mediaPlayerComponent != null) {
                mediaPlayerComponent.release();
                mediaPlayerComponent = null;
            }
        } else if (avPlayerComponent != null && avPlayerComponent.getMediaPlayer().getVideoTrackCount() == 0 && type == VIDEO) {
            if (avPlayerComponent != null) {
                avPlayerComponent.release();
                avPlayerComponent = null;
            }
            mediaPlayerComponent = new AudioMediaPlayerComponent();
            mediaPlayerComponent.getMediaPlayer().prepareMedia(url);
            for (int i = 0; i < adapters.size(); i++) {
                mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(adapters.get(i));
            }
            //setListeners(mediaPlayerComponent.getMediaPlayer());
        } else {
            if (mediaPlayerComponent == null) {
                mediaPlayerComponent = new AudioMediaPlayerComponent();
                mediaPlayerComponent.getMediaPlayer().prepareMedia(url);
                for (int i = 0; i < adapters.size(); i++) {
                    mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(adapters.get(i));
                }
            }
            //setListeners(mediaPlayerComponent.getMediaPlayer());
        }
    }

    public void play() {
        if (type == AUDIO) {
            mediaPlayerComponent.getMediaPlayer().play();
        } else {
            if (!is_fullscreen) {
                avPlayerComponent.getMediaPlayer().play();
            } else {
                avPlayerComponentFullScreen.getMediaPlayer().play();
            }
        }
    }

    public void pause() {
        if (type == AUDIO) {
            mediaPlayerComponent.getMediaPlayer().pause();
        } else {
            if (!is_fullscreen) {
                avPlayerComponent.getMediaPlayer().pause();
            } else {
                avPlayerComponentFullScreen.getMediaPlayer().pause();
            }
        }
    }

    public void stop() {
        if (type == AUDIO) {
            mediaPlayerComponent.getMediaPlayer().stop();
        } else {
            if (!is_fullscreen) {
                avPlayerComponent.getMediaPlayer().stop();
            } else {
                avPlayerComponentFullScreen.getMediaPlayer().stop();
            }
        }
    }

    public void changeVolume(int value) {
        if (type == AUDIO) {
            mediaPlayerComponent.getMediaPlayer().setVolume(value);
        } else {
            if (!is_fullscreen) {
                avPlayerComponent.getMediaPlayer().setVolume(value);
            } else {
                avPlayerComponentFullScreen.getMediaPlayer().setVolume(value);
            }
        }
    }

    public void enterFullScreen() {
        fullscreen_window.setVisible(true);
        strategy.enterFullScreenMode();
        if (is_fullscreen) return;
        //removeListeners(avPlayerComponent.getMediaPlayer());
        //setListeners(avPlayerComponentFullScreen.getMediaPlayer());
        if (avPlayerComponent.getMediaPlayer().isPlaying()) {
            ep.getMediaPlayer().play();
        }
        ep.getMediaPlayer().setTime(avPlayerComponent.getMediaPlayer().getTime() - correction_delta);
        //avPlayerComponentFullScreen.getMediaPlayer().play();
        //avPlayerComponentFullScreen.getMediaPlayer().setTime(avPlayerComponent.getMediaPlayer().getTime() - correction_delta);
        avPlayerComponent.getMediaPlayer().pause();
        is_fullscreen = true;
        strategy.enterFullScreenMode();
    }

    public void exitFullScreen() {
        strategy.exitFullScreenMode();
        if (!is_fullscreen) return;
        //avPlayerComponent.getMediaPlayer().setTime(avPlayerComponentFullScreen.getMediaPlayer().getTime() - correction_delta);
        //avPlayerComponentFullScreen.getMediaPlayer().pause();
        avPlayerComponent.getMediaPlayer().setTime(ep.getMediaPlayer().getTime() - correction_delta);
        if (ep.getMediaPlayer().isPlaying()) avPlayerComponent.getMediaPlayer().play();
        ep.getMediaPlayer().pause();
        //removeListeners(avPlayerComponentFullScreen.getMediaPlayer());
        //setListeners(avPlayerComponent.getMediaPlayer());
        is_fullscreen = false;
        fullscreen_window.setVisible(false);
    }

    JFrame fullscreen_window;
    FullScreenStrategy strategy;

    boolean is_fullscreen = false;

    class RenderCallbackAdapter extends uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter {

        private RenderCallbackAdapter() {
            super(new int[video.width * video.height]);
        }

        @Override
        protected void onDisplay(DirectMediaPlayer mediaPlayer, int[] rgbBuffer) {
            if (image == null) return;
            image.setRGB(0, 0, video.width, video.height, rgbBuffer, 0, video.width);
            video_surface.repaint();
        }
    }

    class RenderFullScreenCallbackAdapter extends uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter {

        private RenderFullScreenCallbackAdapter(int width, int height) {
            super(new int[width * height]);
            this.width = width;
            this.height = height;
        }

        @Override
        protected void onDisplay(DirectMediaPlayer mediaPlayer, int[] rgbBuffer) {
            if (image_fullscreen == null) return;
            image_fullscreen.setRGB(0, 0, width, height, rgbBuffer, 0, width);
            video_surface_fullscreen.repaint();
        }

        private int width;
        private int height;
    }

    class VideoRenderer extends JPanel {

        VideoRenderer(BufferedImage image) {
            this.image = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D)g;
            g2d.drawImage(image, 0, 0, null);
        }

        BufferedImage image;
    }

    class FastVideoRenderer extends JPanel {

        FastVideoRenderer(BufferedImage image) {
            this.image = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D)g;
            g2d.drawImage(image, 0, 0, null);
        }

        BufferedImage image;
    }

    private BufferedImage image = null;
    private BufferedImage image_fullscreen = null;

    private int type = AUDIO;

    private static final int AUDIO = 0;
    private static final int VIDEO = 1;

    private int default_volume = 80;
    private int volume = default_volume;

    private static final String NATIVE_LIBRARY_SEARCH_PATH = "C:/Program Files/VideoLAN/VLC";
    private static final String LOCAL_PATH = "vlc";

    private Block ps;
    private Block progress;

    class Fader extends Thread {

        Fader(int dir) {
            direction = dir;
            value = dir > 0 ? volume : 0;
        }

        @Override
        public void run() {
            if (direction > 0) {
                if (type == AUDIO) {
                    mediaPlayerComponent.getMediaPlayer().play();
                } else {
                    avPlayerComponent.getMediaPlayer().play();
                }
            }
            while (mediaPlayerComponent != null && mediaPlayerComponent.getMediaPlayer().getVolume() != value ||
                    avPlayerComponent != null && avPlayerComponent.getMediaPlayer().getVolume() != value) {
                if (stop) break;
                if (direction > 0) {
                    if (type == AUDIO) {
                        mediaPlayerComponent.getMediaPlayer().setVolume(Math.min(mediaPlayerComponent.getMediaPlayer().getVolume() + step, value));
                    } else {
                        avPlayerComponent.getMediaPlayer().setVolume(Math.min(avPlayerComponent.getMediaPlayer().getVolume() + step, value));
                    }
                } else {
                    if (type == AUDIO) {
                        mediaPlayerComponent.getMediaPlayer().setVolume(Math.max(mediaPlayerComponent.getMediaPlayer().getVolume() - step, 0));
                    } else {
                        avPlayerComponent.getMediaPlayer().setVolume(Math.max(avPlayerComponent.getMediaPlayer().getVolume() - step, 0));
                    }
                }
                try {
                    Thread.sleep(28);
                } catch (InterruptedException ex) {}
            }
            if (type == AUDIO) {
                if (mediaPlayerComponent.getMediaPlayer().getVolume() == value && direction < 0) {
                    mediaPlayerComponent.getMediaPlayer().pause();
                }
            } else {
                if (avPlayerComponent.getMediaPlayer().getVolume() == value && direction < 0) {
                    avPlayerComponent.getMediaPlayer().pause();
                }
            }
        }

        public boolean stop = false;
        private int value;
        private int direction = 0;
        private int step = 5;
    }

    private Fader fader;

    public String getSourceURL() {
        return source;
    }

    public Block getContainer() {
        return container;
    }

    public AudioMediaPlayerComponent mediaPlayerComponent;
    public DirectMediaPlayerComponent avPlayerComponent;
    public DirectMediaPlayerComponent avPlayerComponentFullScreen;

    private String source = "";

    Block panel;
    Block video;
    VideoRenderer video_surface;
    FastVideoRenderer video_surface_fullscreen;
    Block container;
    int correction_delta = 0;

    EmbeddedMediaPlayerComponent ep;
}
