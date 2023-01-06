package render;

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

    public MediaPlayer(Block b) {
        this(b, 200, 22);
    }

    public MediaPlayer(Block b, int width) {
        this(b, width, 22);
    }

    public MediaPlayer(Block b, int width, int height) {
        container = b;
        
        video = new Block(b.document, b, width, height - 22 > min_height ? height-22 : min_height-22, 0, 0, new Color(0, 0, 0));
        b.addElement(video);
        video.setBackgroundColor(Color.BLACK);
        if (height == 22) {
            video.display_type = Block.Display.NONE;
        }
        panel = new Block(b.document, b, width, 22, 0, 0, new Color(0, 0, 0));
        b.addElement(panel);

        b.setWidth(width);
        if (height > 22) {
            b.setHeight(height);
            type = VIDEO;
        } else {
            b.setHeight(22);
        }

        Block play_btn = new Block(b.document, panel, 22, 22, 1, 0, new Color(190, 200, 203));
        b.setBackgroundColor(new Color(230, 230, 230));
        play_btn.display_type = Block.Display.INLINE_BLOCK;
        Vector<Color> c = new Vector<Color>();
        c.add(new Color(192, 223, 234, 7));
        c.add(new Color(238, 238, 238, 245));
        c.add(new Color(193, 213, 232, 180));
        Vector<Float> p = new Vector<Float>();
        p.add(0f);
        p.add(0.14f);
        p.add(1f);
        play_btn.setLinearGradient(c, p, 90);
        panel.addElement(play_btn);
        
        icon1 = new IconLayer(play_btn, "play");

        Block fullscreen_btn = null;

        if (type == VIDEO) {
            fullscreen_btn = new Block(b.document, panel, 28, 22, 0, 0, new Color(190, 200, 203));
            fullscreen_btn.display_type = Block.Display.INLINE_BLOCK;
            fullscreen_btn.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
            icon2 = new IconLayer(fullscreen_btn, "fullscreen");
            c = new Vector<Color>();
            c.add(new Color(192, 223, 234, 7));
            c.add(new Color(238, 238, 238, 245));
            c.add(new Color(193, 213, 232, 180));
            p = new Vector<Float>();
            p.add(0f);
            p.add(0.14f);
            p.add(1f);
            //fullscreen_btn.setLinearGradient(c, p, 90);
            fullscreen_btn.setMargins(0, 0, 0, 8);
        }
        
        b.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (mediaPlayerComponent == null && avPlayerComponent == null) {
                    return;
                }
                if (e.getX() >= icon1._x_ - icon1.scroll_x && e.getX() <= icon1._x_ - icon1.scroll_x + icon1.width &&
                       e.getY() >= icon1._y_ - icon1.scroll_y && e.getY() <= icon1._y_ - icon1.scroll_y + icon1.height) {
                    if (icon1.icon_state > 0) {
                        if (fader != null) {
                            fader.stop = true;
                        }
                        fader = new Fader(-1);
                        (new Thread(fader)).start();
                        //mediaPlayerComponent.getMediaPlayer().pause();
                    } else {
                        if (fader != null) {
                            fader.stop = true;
                        }
                        fader = new Fader(1);
                        (new Thread(fader)).start();
                        //mediaPlayerComponent.getMediaPlayer().play();
                    }
                }
                else if (icon2 != null && e.getX() >= icon2._x_ - icon2.scroll_x && e.getX() <= icon2._x_ - icon2.scroll_x + icon2.width &&
                       e.getY() >= icon2._y_ - icon2.scroll_y && e.getY() <= icon2._y_ - icon2.scroll_y + icon2.height) {
                    enterFullScreen();
                } else {
                    container.document.root.mouseClicked(e);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                container.document.root.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                container.document.root.mouseReleased(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

        });
        b.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                container.document.root.mouseDragged(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (icon1 != null && e.getX() >= icon1._x_ - icon1.scroll_x && e.getX() <= icon1._x_ - icon1.scroll_x + icon1.width &&
                       e.getY() >= icon1._y_ - icon1.scroll_y && e.getY() <= icon1._y_ - icon1.scroll_y + icon1.height) {
                    icon1.bgr_state = 1;
                } else if (icon1 != null) {
                    icon1.bgr_state = 0;
                }
                if (icon2 != null && e.getX() >= icon2._x_ - icon2.scroll_x && e.getX() <= icon2._x_ - icon2.scroll_x + icon2.width &&
                       e.getY() >= icon2._y_ - icon2.scroll_y && e.getY() <= icon2._y_ - icon2.scroll_y + icon2.height) {
                    icon2.bgr_state = 1;
                } else if (icon2 != null) {
                    icon2.bgr_state = 0;
                }
                if (icon1 != null) icon1.redraw();
                if (icon2 != null) icon2.redraw();
                //System.err.println(e.getX() + ", " + e.getY());
                container.document.root.mouseMoved(e);
            }

        });
        int progress_width = b.width - play_btn.width - (fullscreen_btn != null ? fullscreen_btn.width + (int)Math.round(4 * b.ratio) : 0) - (int)Math.round(67 * b.ratio);
        progress = new Block(b.document, panel, progress_width, 6, 1, 3, new Color(190, 200, 203));
        progress.display_type = Block.Display.INLINE_BLOCK;
        progress.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        progress.width = progress.max_width = progress_width;
        progress.setMargins(4, 7, 4, 10);
        progress.setBackgroundColor(new Color(241, 243, 245, 234));
        panel.addElement(progress);
        ps = new Block(b.document, progress, progress_width, 6, 1, 3, new Color(190, 200, 203));
        //ps.display_type = Block.Display.INLINE_BLOCK;
        //ps.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        progress.addElement(ps);
        ps.width = progress.width;
        ps.setPositioning(Block.Position.RELATIVE);
        ps.top = -progress.borderWidth[0];
        ps.left = -progress.borderWidth[3];
        ps.setBackgroundColor(new Color(90, 173, 238, 235));
        ps.bg_clip_x = 0;

        b.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void mousePressed(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getX() >= progress._x_ - progress.scroll_x && e.getX() <= progress._x_ - progress.scroll_x + progress.width &&
                       e.getY() >= progress._y_ - progress.scroll_y && e.getY() <= progress._y_ - progress.scroll_y + progress.height) {
                    long time = 0;
                    if (type == AUDIO) {
                        time = Math.round((double)(e.getX() - progress._x_) / progress.width * mediaPlayerComponent.getMediaPlayer().getLength());
                        ps.bg_clip_x = (int)Math.round((double)time / mediaPlayerComponent.getMediaPlayer().getLength() * ps.width);
                    } else {
                        time = Math.round((double)(e.getX() - progress._x_) / progress.width * avPlayerComponent.getMediaPlayer().getLength());
                        ps.bg_clip_x = (int)Math.round((double)time / avPlayerComponent.getMediaPlayer().getLength() * ps.width);
                    }
                    ps.clearBuffer();
                    ps.draw();
                    ps.document.repaint();
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
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

        });

        Block sb = new Block(b.document, panel, 36, 14, 0, 0, new Color(0, 0, 0));
        sb.display_type = Block.Display.INLINE_BLOCK;
        sb.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        sb.setMargins(0, 0, 0, 4);
        panel.addElement(sb);
        s = new Segments(sb, new Color(90, 173, 238, 235));
        //s.setBounds(s._x_, s._y_, s.width, s.height);
        s.percent = default_volume;
        b.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getX() >= s._x_ - s.scroll_x + s.v.get(0) && e.getX() <= s._x_ - s.scroll_x + s.v.lastElement() + s.getSegmentSize() &&
                       e.getY() >= s._y_ - s.scroll_y && e.getY() <= s._y_ - s.scroll_y + s.height) {
                    for (int i = 0; i < s.v.size(); i++) {
                        if (e.getX() >= s._x_ - s.scroll_x + s.v.get(i) && (i == s.v.size()-1 || e.getX() < s._x_ - s.scroll_x + s.v.get(i+1))) {
                            int p = (int)Math.floor(100 / s.v.size());
                            //System.err.println(p * i + (int)Math.floor(p / s.getSegmentSize() * Math.min(e.getX() - s.v.get(i), s.getSegmentSize())));
                            s.percent = p * i + (int)Math.floor(p / s.getSegmentSize() * Math.min(e.getX() - (s._x_ - s.scroll_x + s.v.get(i)), s.getSegmentSize()));
                            if (type == AUDIO) {
                                mediaPlayerComponent.getMediaPlayer().setVolume(s.percent);
                            } else {
                                avPlayerComponent.getMediaPlayer().setVolume(s.percent);
                                ep.getMediaPlayer().setVolume(s.percent);
                            }
                            volume = s.percent;
                            break;
                        }
                    }

                    s.redraw();
                }
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

        });

        b.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (e.getX() >= s._x_ - s.scroll_x + s.v.get(0) && e.getX() <= s._x_ - s.scroll_x + s.v.lastElement() + s.getSegmentSize() &&
                       e.getY() >= s._y_ - s.scroll_y && e.getY() <= s._y_ - s.scroll_y + s.height) {
                    for (int i = 0; i < s.v.size(); i++) {
                        if (e.getX() >= s._x_ - s.scroll_x + s.v.get(i) && (i == s.v.size()-1 || e.getX() < s._x_ - s.scroll_x + s.v.get(i+1))) {
                            int p = (int)Math.floor(100 / s.v.size());
                            //System.err.println(p * i + (int)Math.floor(p / s.getSegmentSize() * Math.min(e.getX() - s.v.get(i), s.getSegmentSize())));
                            s.percent = p * i + (int)Math.floor(p / s.getSegmentSize() * Math.min(e.getX() - (s._x_ - s.scroll_x + s.v.get(i)), s.getSegmentSize()));
                            if (type == AUDIO) {
                                mediaPlayerComponent.getMediaPlayer().setVolume(s.percent);
                            } else {
                                avPlayerComponent.getMediaPlayer().setVolume(s.percent);
                                ep.getMediaPlayer().setVolume(s.percent);
                            }
                            volume = s.percent;
                            break;
                        }
                    }
                    s.redraw();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

        });

        if (type == VIDEO) {
            panel.addElement(fullscreen_btn);
        }
        
        if (container.document != null && container.document.ready) {
            container.performLayout();
            while (b.parent != null && (b.parent.overflow == Block.Overflow.SCROLL || b.parent.auto_height) && b.getOffsetTop() + b.height + b.margins[2] + b.parent.paddings[2] > b.parent.height - b.parent.borderWidth[2]) {
                b.parent.performLayout(true);
                b = b.parent;
            }

            container.forceRepaint();
            container.document.repaint();
        }

        initPlayer();
    }

    private void initPlayer() {
        //boolean found = new NativeDiscovery().discover();
        //if (!found) {
            //NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), NATIVE_LIBRARY_SEARCH_PATH);
            NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), LOCAL_PATH);
        //}
        //System.out.println(LibVlc.INSTANCE.libvlc_get_version());


        //mediaPlayerComponent = new AudioMediaPlayerComponent();
        
        //mediaPlayerComponent.getMediaPlayer().setVolume(default_volume);
    }

    MediaPlayerEventAdapter adapter = new MediaPlayerEventAdapter() {

        @Override
        public void playing(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {
            icon1.icon_state = 1;
            icon1.redraw();
        }

        @Override
        public void paused(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {
            icon1.icon_state = 0;
            icon1.redraw();
        }

        @Override
        public void stopped(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {
            icon1.icon_state = 0;
            icon1.redraw();
        }

        @Override
        public void timeChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, long new_time) {
            int secs = (int)(new_time / 1000) % 60;
            int mins = (int)((new_time / 1000 - secs) / 60 % 60);
            int hours = (int)((new_time / 1000 - secs - mins * 60) / 3600);
            //System.out.println((mins < 10 ? "0" + mins : mins) + ":" + (secs < 10 ? "0" + secs : secs));

            ps.bg_clip_x = (int)Math.round((double)new_time / mediaPlayer.getLength() * ps.width);
            ps.clearBuffer();
            ps.forceRepaint();
            ps.document.repaint();
            if (new_time >= mediaPlayer.getLength()) {
                mediaPlayer.stop();
            }
        }

        @Override
        public void finished(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {
            icon1.icon_state = 0;
            icon1.redraw();
        }

    };

    public void setListeners(uk.co.caprica.vlcj.player.MediaPlayer player) {
        player.addMediaPlayerEventListener(adapter);
    }

    private void removeListeners(uk.co.caprica.vlcj.player.MediaPlayer player) {
        player.removeMediaPlayerEventListener(adapter);
    }

    public void updateVideoSize() {
        video_surface.setBounds(video._x_, video._y_, video.width, video.height);
    }

    public void open(String url) {

        mediaPlayerComponent = new AudioMediaPlayerComponent();
        mediaPlayerComponent.getMediaPlayer().prepareMedia(url);

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

            setListeners(avPlayerComponent.getMediaPlayer());

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
            setListeners(mediaPlayerComponent.getMediaPlayer());
        } else {
            if (mediaPlayerComponent == null) {
                mediaPlayerComponent = new AudioMediaPlayerComponent();
                mediaPlayerComponent.getMediaPlayer().prepareMedia(url);
            }
            setListeners(mediaPlayerComponent.getMediaPlayer());
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

    public void enterFullScreen() {
        fullscreen_window.setVisible(true);
        strategy.enterFullScreenMode();
        if (is_fullscreen) return;
        removeListeners(avPlayerComponent.getMediaPlayer());
        setListeners(avPlayerComponentFullScreen.getMediaPlayer());
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
        removeListeners(avPlayerComponentFullScreen.getMediaPlayer());
        setListeners(avPlayerComponent.getMediaPlayer());
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
            g2d.setClip(clip_rect);
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

    public static int min_height = 140;

    private static final String NATIVE_LIBRARY_SEARCH_PATH = "C:/Program Files/VideoLAN/VLC";
    private static final String LOCAL_PATH = "vlc";

    private IconLayer icon1;
    private IconLayer icon2;
    private Segments s;
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
            } else {
                icon1.icon_state = 0;
                icon1.redraw();
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

    private RoundedRect clip_rect = null;

    class IconLayer extends Block {

        IconLayer(Block b, String name) {
            super(b.document);
            button = b;
            tag = name;
            width = button.width - button.borderWidth[3] - button.borderWidth[1];
            height = button.height - button.borderWidth[0] - button.borderWidth[2];
            auto_height = false;
            //_x_ = button._x_ + button.borderWidth[3];
            //_y_ = button._y_ + button.borderWidth[0];
            b.addElement(this, true);
        }

        public void redraw() {
            clearBuffer();
            draw();
            document.repaint();
        }

        @Override
        public void draw(Graphics g) {

            super.draw(g);

            scroll_x = parent != null ? parent.scroll_x : 0;
            scroll_y = parent != null ? parent.scroll_y : 0;

            if (container.isPartlyHidden()) {

                Block clipping_block = container.clipping_block;

                int sx = clipping_block.parent != null ? clipping_block.parent.scroll_x : 0;
                int sy = clipping_block.parent != null ? clipping_block.parent.scroll_y : 0;

                int xc = clipping_block._x_ - _x_ + clipping_block.borderWidth[3] + parent.scroll_x;
                int yc = clipping_block._y_ - _y_ + clipping_block.borderWidth[0] + parent.scroll_y;
                int wc = clipping_block.viewport_width - clipping_block.borderWidth[3] - clipping_block.borderWidth[1];
                int hc = clipping_block.viewport_height - clipping_block.borderWidth[0] - clipping_block.borderWidth[2];

                double[] arcs = new double[4];
                arcs[0] = clipping_block.arc[0] / 2 - 1;
                arcs[1] = clipping_block.arc[1] / 2 - 1;
                arcs[2] = clipping_block.arc[2] / 2 - 1;
                arcs[3] = clipping_block.arc[3] / 2 - 1;

                adjustCorners(arcs, clipping_block);

                RoundedRect rect = new RoundedRect(xc, yc, wc, hc, arcs[0], arcs[1], arcs[2], arcs[3]);
                g.setClip(rect);

                clip_rect = new RoundedRect(clipping_block._x_ + clipping_block.borderWidth[3] - container._x_ + container.parent.scroll_x, clipping_block._y_ + clipping_block.borderWidth[0] - container._y_ + container.parent.scroll_y, wc, hc, arcs[0], arcs[1], arcs[2], arcs[3]);

                container.clipping_block = null;
            }

            if (bgr_state > 0) {
                g.setColor(tag.equals("play") ? new Color(94, 136, 230, 18) : new Color(92, 123, 213, 18));
                g.fillRect(0, 0, width, height);
            }
            if (tag.equals("play")) {
                if (icon_state > 0) {
                    g.setColor(new Color(74, 78, 94, 218));
                    g.fillRect((int)(button.width * 0.27), (int)(button.height * 0.29), (int)(button.width * 0.17), (int)(button.height * 0.46));
                    g.fillRect((int)(button.width * 0.55), (int)(button.height * 0.29), (int)(button.width * 0.17), (int)(button.height * 0.46));
                } else {
                    g.setColor(new Color(74, 78, 94, 218));
                    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int[] bw = { button.borderWidth[1] + button.borderWidth[3], button.borderWidth[0] + button.borderWidth[2]};
                    if (WebDocument.scale_borders) {
                        bw[0] = (int) Math.floor(bw[0] * button.ratio);
                        bw[1] = (int) Math.floor(bw[1] * button.ratio);
                    }
                    int btn_width = button.width - bw[0];
                    int[] x = { (int)(btn_width * 0.37), (int)(btn_width * 0.78), (int)(btn_width * 0.37) };
                    int[] y = { (int)(btn_width * 0.3), (int)(btn_width * 0.55), (int)(btn_width * 0.8) };
                    Polygon p = new Polygon(x, y, 3);
                    g.fillPolygon(p);
                }
            } else if (tag.equals("fullscreen")) {
                g.setColor(new Color(74, 78, 94, 218));
                int[] x1 = { (int)(button.width * 0.24), (int)(button.width * 0.36), (int)(button.width * 0.24) };
                int[] y1 = { (int)(button.height * 0.26), (int)(button.height * 0.26),(int)(button.height * 0.42) };
                Polygon p1 = new Polygon(x1, y1, 3);
                g.fillPolygon(p1);
                int[] x2 = { (int)(button.width * 0.67), (int)(button.width * 0.79), (int)(button.width * 0.79) };
                int[] y2 = { (int)(button.height * 0.26), (int)(button.height * 0.26),(int)(button.height * 0.42) };
                Polygon p2 = new Polygon(x2, y2, 3);
                g.fillPolygon(p2);
                int[] x3 = { (int)(button.width * 0.24), (int)(button.width * 0.36), (int)(button.width * 0.24) };
                int[] y3 = { (int)(button.height * 0.648), (int)(button.height * 0.868),(int)(button.height * 0.868) };
                Polygon p3 = new Polygon(x3, y3, 3);
                g.fillPolygon(p3);
                int[] x4 = { (int)(button.width * 0.636), (int)(button.width * 0.79), (int)(button.width * 0.79) };
                int[] y4 = { (int)(button.height * 0.868), (int)(button.height * 0.65),(int)(button.height * 0.868) };
                Polygon p4 = new Polygon(x4, y4, 3);
                g.fillPolygon(p4);
            }
        }

        private String tag;
        private Block button;
        public int bgr_state = 0;
        public int icon_state = 0;
    }

    class Segments extends Block {

        Segments(Block b, Color col) {
            super(b.document);
            block = b;
            color = col;
            width = block.width;
            height = block.height;
            auto_width = false;
            auto_height = false;
            _x_ = block._x_;
            _y_ = block._y_;
            w = (int)Math.round(w * block.ratio);
            b.addElement(this, true);
        }

        public void redraw() {
            clearBuffer();
            draw();
            document.repaint();
        }

        @Override
        public void draw(Graphics g) {

            super.draw(g);

            scroll_x = parent != null ? parent.scroll_x : 0;
            scroll_y = parent != null ? parent.scroll_y : 0;

            if (container.isPartlyHidden()) {

                Block clipping_block = container.clipping_block;

                int sx = clipping_block.parent != null ? clipping_block.parent.scroll_x : 0;
                int sy = clipping_block.parent != null ? clipping_block.parent.scroll_y : 0;

                int xc = clipping_block._x_ - _x_ + clipping_block.borderWidth[3] + parent.scroll_x;
                int yc = clipping_block._y_ - _y_ + clipping_block.borderWidth[0] + parent.scroll_y;
                int wc = clipping_block.viewport_width - clipping_block.borderWidth[3] - clipping_block.borderWidth[1];
                int hc = clipping_block.viewport_height - clipping_block.borderWidth[0] - clipping_block.borderWidth[2];

                double[] arcs = new double[4];
                arcs[0] = clipping_block.arc[0] / 2 - 1;
                arcs[1] = clipping_block.arc[1] / 2 - 1;
                arcs[2] = clipping_block.arc[2] / 2 - 1;
                arcs[3] = clipping_block.arc[3] / 2 - 1;

                adjustCorners(arcs, clipping_block);

                RoundedRect rect = new RoundedRect(xc, yc, wc, hc, arcs[0], arcs[1], arcs[2], arcs[3]);
                g.setClip(rect);

                clip_rect = new RoundedRect(clipping_block._x_ + clipping_block.borderWidth[3] - container._x_, clipping_block._y_ + clipping_block.borderWidth[0] - container._y_, wc, hc, arcs[0], arcs[1], arcs[2], arcs[3]);

                container.clipping_block = null;
            }

            g.setColor(color);
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int count = (int)Math.floor((block.width - 1 + w) / (2 * w - 1));
            int h = w;
            int delta = (int)Math.floor((block.height - h) / (count-1));
            int x = width - (count * w  + (count - 1) * (w - 1)) - 1;
            if (count % 2 == 0) x += w;

            double p = 100 / count;

            int n = (int)Math.floor(percent / (100 / count));

            v.clear();

            for (int i = 0; i < count; i++) {
                if (i > n) {
                    g.setColor(new Color(178, 178, 178));
                }
                if (i == n) {
                    for (int j = 0; j < w; j++) {
                        if (j >= (int)Math.floor((percent % p) / p * w)) {
                            g.setColor(new Color(178, 178, 178));
                        }
                        g.fillRect(x + j, block.height - h - delta * i, 1, h + delta * i);
                    }
                } else {
                    g.fillRect(x, block.height - h - delta * i, w, h + delta * i);
                }
                v.add(x);
                x += 2 * w - 1;
            }
        }

        public int getSegmentSize() {
            return w;
        }

        public Vector<Integer> v = new Vector<Integer>();

        private int percent = 100;
        private int w = 4;
        private Block block;
    }

    public Block getContainer() {
        return container;
    }

    AudioMediaPlayerComponent mediaPlayerComponent;
    DirectMediaPlayerComponent avPlayerComponent;
    DirectMediaPlayerComponent avPlayerComponentFullScreen;

    Block panel;
    Block video;
    VideoRenderer video_surface;
    FastVideoRenderer video_surface_fullscreen;
    Block container;
    int correction_delta = 0;

    EmbeddedMediaPlayerComponent ep;
}
