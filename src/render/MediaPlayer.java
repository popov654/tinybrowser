package render;

import com.sun.jna.NativeLibrary;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
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
        this(b, b.width > 0 ? (int) ((double) b.width / b.ratio) : default_audio_width, panel_height);
    }

    public MediaPlayer(Block b, int width) {
        this(b, width, panel_height);
    }

    public MediaPlayer(Block b, int width, int height) {
        container = b;
        b.isMedia = true;
        b.setMediaPlayer(this);

        b.document.ready = false;

        int h = height - panel_height > min_height ? height - panel_height : min_height - panel_height;
        video = new Block(b.document, b, width, h, 0, 0, new Color(0, 0, 0));
        b.addElement(video);
        video.setBackgroundColor(Color.BLACK);
        if (height == panel_height) {
            video.display_type = Block.Display.NONE;
        }
        if (width == -1) {
            width = b.width >= 0 ? b.width : (b.parent != null && b.parent.viewport_width > 0 ? b.parent.viewport_width : default_width);
        }
        if (height == -1) {
            height = (int) (width / ((double)16 / 9));
            video.height = (int) ((height - panel_height) * b.ratio);
        }
        panel = new Block(b.document, b, width, panel_height, 0, 0, new Color(0, 0, 0));
        b.addElement(panel);

        if (height > panel_height * b.ratio) {
            b.setWidthHeight(width, height);
            type = VIDEO;
        } else {
            b.setHeight(panel_height);
        }

        int scaled_width = (int) (width * b.ratio);

        video.width = scaled_width;
        video.viewport_width = scaled_width;
        video.max_width = scaled_width;

        play_btn = new Block(b.document, panel, panel_height, panel_height, 1, 0, new Color(190, 200, 203));
        b.setBackgroundColor(new Color(230, 230, 230));
        play_btn.display_type = Block.Display.INLINE_BLOCK;
        Vector<Color> c = new Vector<Color>();
        c.add(new Color(192, 223, 234, 7));
        c.add(new Color(238, 238, 238, 145));
        c.add(new Color(193, 213, 232, 180));
        Vector<Float> p = new Vector<Float>();
        p.add(0f);
        p.add(0.14f);
        p.add(1f);
        play_btn.setLinearGradient(c, p, 90);
        panel.addElement(play_btn);
        
        icon1 = new IconLayer(play_btn, "play");

        fullscreen_btn = new Block(b.document, panel, 28, panel_height, 0, 0, new Color(190, 200, 203));
        fullscreen_btn.display_type = (type == VIDEO) ? Block.Display.INLINE_BLOCK : Block.Display.NONE;
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
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}

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
                if (icon1 != null) icon1.redraw();
                if (fullscreen_btn.display_type != Block.Display.NONE) {
                    if (icon2 != null && e.getX() >= icon2._x_ - icon2.scroll_x && e.getX() <= icon2._x_ - icon2.scroll_x + icon2.width &&
                           e.getY() >= icon2._y_ - icon2.scroll_y && e.getY() <= icon2._y_ - icon2.scroll_y + icon2.height) {
                        icon2.bgr_state = 1;
                    } else if (icon2 != null) {
                        icon2.bgr_state = 0;
                    }
                    if (icon2 != null) icon2.redraw();
                }
                
                //System.err.println(e.getX() + ", " + e.getY());
                container.document.root.mouseMoved(e);
            }

        });
        int progress_width = b.width - play_btn.width - (fullscreen_btn.display_type != Block.Display.NONE ? fullscreen_btn.width + (int)Math.round(4 * b.ratio) : 0) - (int)Math.round(67 * b.ratio);
        progress = new Block(b.document, panel, progress_width, 6, 1, 3, new Color(190, 200, 203));
        progress.display_type = Block.Display.INLINE_BLOCK;
        progress.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        progress.width = progress.max_width = progress_width;
        progress.orig_width = (int) ((double)progress_width / progress.ratio);
        progress.setMargins(4, 7, 4, 10);
        progress.setBackgroundColor(new Color(241, 243, 245, 234));
        panel.addElement(progress);
        ps = new Block(b.document, progress, progress_width, 6, 1, 3, new Color(190, 200, 203));
        //ps.display_type = Block.Display.INLINE_BLOCK;
        //ps.setVerticalAlign(Block.VerticalAlign.ALIGN_MIDDLE);
        progress.addElement(ps);
        ps.width = progress.width;
        ps.orig_width = progress.orig_width;
        ps.setPositioning(Block.Position.RELATIVE);
        ps.top = -progress.borderWidth[0];
        ps.left = -progress.borderWidth[3];
        ps.setBackgroundColor(new Color(90, 173, 238, 235));
        ps.bg_clip_x = 0;

        trackListener = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {
                Block target = !is_fullscreen ? progress : progress_fullscreen;
                if (e.getX() >= target._x_ - target.scroll_x && e.getX() <= target._x_ - target.scroll_x + target.width &&
                       e.getY() >= target._y_ - target.scroll_y - 1 && e.getY() <= target._y_ - target.scroll_y + target.height + 2) {
                    long time = 0;
                    long length = 0;

                    if (type == AUDIO) {
                        length = mediaPlayerComponent.getMediaPlayer().getLength();
                        time = Math.round((double)(e.getX() - target._x_) / target.width * length);
                    } else if (!is_fullscreen) {
                        length = avPlayerComponent.getMediaPlayer().getLength();
                        time = Math.round((double)(e.getX() - target._x_) / target.width * length);
                    } else {
                        length = ep.getMediaPlayer().getLength();
                        time = Math.round((double)(e.getX() - target._x_) / target.width * length);
                    }

                    ps.bg_clip_x = (int)Math.round((double)time / length * ps.width);
                    psf.bg_clip_x = (int)Math.round((double)time / length * psf.width);

                    ps.clearBuffer();
                    ps.draw();
                    ps.document.repaint();

                    if (is_fullscreen) {
                        psf.clearBuffer();
                        psf.draw();
                        psf.document.repaint();
                    }

                    fader = new Fader(-1);
                    fader.run();
                    if (!is_fullscreen) {
                        if (type == AUDIO) {
                            mediaPlayerComponent.getMediaPlayer().setTime(time);
                        } else {
                            pendingTime = time;
                            avPlayerComponent.getMediaPlayer().setTime(time);
                        }
                    } else {
                        pendingTime = time;
                        ep.getMediaPlayer().setTime(time);
                    }
                    fader = new Fader(1);
                    fader.run();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}

        };
        b.addMouseListener(trackListener);

        controls = new JFrame() {
            @Override
            public Dimension getPreferredSize() {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                return new Dimension((int) (screenSize.getWidth() * 0.3), 100);
            }
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        controls.setVisible(false);

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
            public void mouseClicked(MouseEvent e) {}

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
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}

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
            public void mouseMoved(MouseEvent e) {}

        });

        panel.addElement(fullscreen_btn);

        b.document.ready = true;

//        if (container.getLayouter() != null) {
//            container.performLayout();
//            container.document.repaint();
//        }
//
//        else if (container.document != null && container.document.ready && container.document.isVisible()) {
//            container.document.root.performLayout();
//            container.forceRepaint();
//            container.document.repaint();
//        }

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
            if (is_fullscreen) {
                play_fullscreen.forceRepaint();
                play_fullscreen.document.repaint();
            }
        }

        @Override
        public void paused(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {
            icon1.icon_state = 0;
            icon1.redraw();
            if (is_fullscreen) {
                play_fullscreen.forceRepaint();
                play_fullscreen.document.repaint();
            }
        }

        @Override
        public void stopped(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {
            icon1.icon_state = 0;
            icon1.redraw();
            if (is_fullscreen) {
                play_fullscreen.forceRepaint();
                play_fullscreen.document.repaint();
            }
        }

        @Override
        public void timeChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, long new_time) {
            int secs = (int)(new_time / 1000) % 60;
            int mins = (int)((new_time / 1000 - secs) / 60 % 60);
            int hours = (int)((new_time / 1000 - secs - mins * 60) / 3600);
            //System.out.println((mins < 10 ? "0" + mins : mins) + ":" + (secs < 10 ? "0" + secs : secs));

            ps.bg_clip_x = (int)Math.round((double)new_time / mediaPlayer.getLength() * ps.width);
            psf.bg_clip_x = (int)Math.round((double)new_time / mediaPlayer.getLength() * psf.width);

            ps.clearBuffer();
            ps.forceRepaint();
            ps.document.repaint();

            if (is_fullscreen) {
                if (timeElapsed != null) {
                    boolean withHours = mediaPlayer.getLength() / 1000 >= 3600;
                    timeElapsed.setText((withHours ? hours + ":" : "") + (mins < 10 && withHours ? "0" + mins : mins) + ":" + (secs < 10 ? "0" + secs : secs));
                    long remaining_time = mediaPlayer.getLength() - new_time;
                    secs = (int)(remaining_time / 1000) % 60;
                    mins = (int)((remaining_time / 1000 - secs) / 60 % 60);
                    hours = (int)((remaining_time / 1000 - secs - mins * 60) / 3600);
                    timeRemains.setText("-" + (withHours ? hours + ":" : "") + (mins < 10 && withHours ? "0" + mins : mins) + ":" + (secs < 10 ? "0" + secs : secs));
                }

                psf.clearBuffer();
                psf.forceRepaint();
                psf.document.repaint();
            }
            
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

        if (mediaPlayerComponent.getMediaPlayer().getVideoTrackCount() == -1 && url.matches(".*\\.(avi|mp4|m2ts|ts|mkv|m4v|flv|3gp|ogv)$")) {

            if (container.height == panel.height) {
                switchToVideoMode();
            }

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

            WebDocument doc = new WebDocument();
            doc.width = (int) (screenSize.getWidth() * 0.3);
            doc.height = 100;
            doc.borderSize = 0;

            doc.root.width = doc.width;
            doc.root.height = doc.height;

            doc.setBounds(0, 0, doc.width, doc.height);
            doc.setBackground(new Color(0, 0, 0, 0));
            doc.root.setBackgroundColor(new Color(0, 0, 0, 0));
            
            Block b = new Block(doc);
            b.setWidth(-1);
            b.setHeight((int) Math.round((double) doc.height / b.ratio) - 2);
            b.setBorderRadius(8);
            b.setBackgroundColor(new Color(18, 18, 18));
            b.scale_borders = false;
            b.setBorderWidth(1);
            b.borderColor[0] = new Color(30, 32, 34);
            b.borderColor[3] = new Color(30, 30, 30);
            b.borderColor[1] = new Color(12, 12, 12);
            b.borderColor[2] = new Color(15, 15, 15);

            doc.root.addElement(b);

            progress_fullscreen = progress.clone();
            progress_fullscreen.changeDocumentRecursive(doc);
            b.addElement(progress_fullscreen);

            progress_fullscreen.setMargins(20, 26);
            progress_fullscreen.setMaxWidth(-1);
            progress_fullscreen.setWidth(-1);
            progress_fullscreen.setBackgroundColor(new Color(75, 75, 75));
            progress_fullscreen.setBorderWidth(0);
            progress_fullscreen.setHeight(4);

            psf = progress_fullscreen.children.get(0);
            psf.setBorderWidth(0);
            psf.setHeight(4);

            psf.width = progress_fullscreen.width;
            psf.orig_width = progress_fullscreen.orig_width;
            psf.setPositioning(Block.Position.RELATIVE);
            psf.top = -progress_fullscreen.borderWidth[0];
            psf.left = -progress_fullscreen.borderWidth[3];
            psf.setBackgroundColor(new Color(90, 173, 238, 235));
            psf.bg_clip_x = 0;

            int panelWidth = (int) (screenSize.getWidth() * 0.3);
            int timeLabelsXOffset = (int) (26 * b.ratio);
            int timeLabelsYOffset = (int) (20 * b.ratio) + progress_fullscreen.height + 10;
            
            JPanel controlsPane = (JPanel)controls.getContentPane();
            controlsPane.setBackground(new Color(0, 0, 0));
            //controlsPane.setBorder(new RoundedBorder(b, 2, 8, new Color(38, 38, 38)));

            controlsPane.removeAll();
            controlsPane.add(doc);

            if (enableTimeDisplay) {
                timeElapsed = new JLabel("0:00");
                timeRemains = new JLabel("-0:00");
                Color labelsColor = new Color(218, 218, 218);
                timeElapsed.setForeground(labelsColor);
                timeRemains.setForeground(labelsColor);
                timeElapsed.setHorizontalAlignment(JLabel.LEFT);
                timeRemains.setHorizontalAlignment(JLabel.RIGHT);
                doc.panel.add(timeElapsed, 0);
                doc.panel.add(timeRemains, 1);
                int timeLabelsWidth = controlsPane.getFontMetrics(timeElapsed.getFont()).stringWidth("0:00:00") + 10;
                timeElapsed.setBounds(timeLabelsXOffset, timeLabelsYOffset, timeLabelsWidth, timeElapsed.getPreferredSize().height);
                timeRemains.setBounds(panelWidth - timeLabelsXOffset - timeLabelsWidth, timeLabelsYOffset, timeLabelsWidth, timeElapsed.getPreferredSize().height);
            }

            controls.setUndecorated(true);
            controls.setAlwaysOnTop(true);

            doc.root.setBounds(0, 0, doc.width, doc.height);
            doc.panel.setBounds(0, 0, doc.width, doc.height);
            
            play_fullscreen = new Block(doc, b, 23, 23, 0, 0, Color.BLACK) {
                @Override
                public void draw(Graphics g) {
                    boolean is_playing = ep != null && ep.getMediaPlayer().isPlaying();

                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int size = width;

                    g2d.setColor(new Color(225, 225, 225, (int) (alpha * 255)));
                    
                    if (!is_playing) {
                        g2d.fillPolygon(new int[] { 6, size - 6, 6 }, new int[] { 6, size / 2, size - 6 }, 3);
                    } else {
                        int w = (int) Math.round(5 * ratio)-1;
                        int offset_x = (int) Math.round(3 * ratio);
                        int offset_y = w+3;
                        g2d.fillRect(offset_x, offset_y-2, w, size-offset_y-2);
                        g2d.fillRect(size-w-offset_x, offset_y-2, w, size-offset_y-2);
                    }
                }
            };
            String opacity = "0.78";
            play_fullscreen.cssStyles.put("opacity", opacity);
            //play_fullscreen.setCursor("pointer");
            doc.root.addMouseMotionListener(new MouseMotionListener() {

                boolean hover = false;

                @Override
                public void mouseDragged(MouseEvent e) {}

                @Override
                public void mouseMoved(MouseEvent e) {
                    if (!hover && e.getX() >= play_fullscreen._x_ && e.getX() <= play_fullscreen._x_ + play_fullscreen.width &&
                            e.getY() >= play_fullscreen._y_ && e.getY() <= play_fullscreen._y_ + play_fullscreen.height) {
                        hover = true;
                        Transition t = new Transition(play_fullscreen, "opacity", 300, null, "0.92");
                        t.start();
                    } else if (hover && !(e.getX() >= play_fullscreen._x_ && e.getX() <= play_fullscreen._x_ + play_fullscreen.width &&
                            e.getY() >= play_fullscreen._y_ && e.getY() <= play_fullscreen._y_ + play_fullscreen.height)) {
                        hover = false;
                        Transition t = new Transition(play_fullscreen, "opacity", 300, null, "0.78");
                        t.start();
                    }
                }
            });

            doc.root.addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (ep == null) {
                        return;
                    }
                    if (e.getX() >= play_fullscreen._x_ && e.getX() <= play_fullscreen._x_ + play_fullscreen.width &&
                           e.getY() >= play_fullscreen._y_ && e.getY() <= play_fullscreen._y_ + play_fullscreen.height) {
                        if (ep.getMediaPlayer().isPlaying()) {
                            if (fader != null) {
                                fader.stop = true;
                            }
                            fader = new Fader(-1);
                            (new Thread(fader)).start();
                        } else {
                            if (fader != null) {
                                fader.stop = true;
                            }
                            fader = new Fader(1);
                            (new Thread(fader)).start();
                        }
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    play_fullscreen.document.root.mousePressed(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    play_fullscreen.document.root.mouseReleased(e);
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

            controls.setSize((int) (screenSize.getWidth() * 0.3), 100);
            b.addElement(play_fullscreen);
            play_fullscreen.setAlpha(Float.parseFloat(opacity));
            play_fullscreen.setWidthHeight(23, 23);
            play_fullscreen.setAutoXMargin();

            play_fullscreen.margins[0] = -18;

            doc.root.addMouseListener(trackListener);

            doc.root.performLayout();
            doc.root.forceRepaint();
            doc.repaint();
            
            controls.setLocation((int) (screenSize.getWidth() * 0.35), (int) (screenSize.getHeight() - 140));

            fullscreen_window.add(video_surface_fullscreen);
            //fullscreen_window.add(controls);
            //fullscreen_window.setComponentZOrder(controls, 0);
            controls.setVisible(false);

            hideControlsTimer = new Timer(1800, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    hideControls();
                }
            });
            hideControlsTimer.setRepeats(false);

            MouseMotionListener cml = new MouseMotionListener() {

                @Override
                public void mouseDragged(MouseEvent e) {}

                @Override
                public void mouseMoved(MouseEvent e) {
                    showControls();
                }
                
            };
            
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
            inputMap.put(escKeyStrokePressed, escPressed);
            actionMap.put(escPressed, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                     exitFullScreen();
                }
            });

            KeyStroke spaceKeyStrokePressed = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false);
            String spacePressed = "SPACE";
            inputMap.put(spaceKeyStrokePressed, spacePressed);
            actionMap.put(spacePressed, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if (is_fullscreen) {
                        if (ep != null && ep.getMediaPlayer().isPlaying()) {
                            pause();
                        } else {
                            play();
                        }
                    }
                }
            });

            controlsPane.getInputMap(condition).put(escKeyStrokePressed, escPressed);
            controlsPane.getInputMap(condition).put(spaceKeyStrokePressed, spacePressed);
            controlsPane.setActionMap(actionMap);

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

            mouseTracker = new MouseTracker();
            toggleControlsTimer = new Timer(50, mouseTracker);

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

            switchToAudioMode();
        } else {
            if (mediaPlayerComponent == null) {
                mediaPlayerComponent = new AudioMediaPlayerComponent();
                mediaPlayerComponent.getMediaPlayer().prepareMedia(url);
            }
            setListeners(mediaPlayerComponent.getMediaPlayer());

            switchToAudioMode();
        }
    }

    private void switchToVideoMode() {
        video.display_type = Block.Display.BLOCK;
        if (container.width < default_width) {
            container.width = container.viewport_width = default_width;
            panel.width = panel.viewport_width = default_width;
            video.width = video.viewport_width = container.width;
            video.height = video.viewport_height = (int) (video.width / ((double) 16 / 9));
        }
        container.height = container.viewport_height = video.height + panel.height;
        int width = (int) ((double)(container.width - play_btn.width - (fullscreen_btn.width + (int)Math.round(4 * container.ratio)) - (int)Math.round(67 * container.ratio)) / container.ratio);

        container.document.ready = false;
        progress.setMaxWidth(width);
        progress.setWidth(width);
        ps.setWidth(width);
        container.document.ready = true;

        fullscreen_btn.display_type = Block.Display.INLINE_BLOCK;
        type = VIDEO;

        container.document.root.performLayout();
        container.forceRepaint();
        container.document.repaint();
    }

    private void switchToAudioMode() {
        video.display_type = Block.Display.NONE;
        container.height = container.viewport_height = panel.height;

        if (container.width > default_width) {
            container.width = container.viewport_width = default_width;
            panel.width = panel.viewport_width = default_width;
            video.width = video.viewport_width = container.width;
            video.height = video.viewport_height = (int) (video.width / ((double) 16 / 9));
        }

        int width = (int) ((double)(container.width - play_btn.width - ((int)Math.round(4 * container.ratio)) - (int)Math.round(67 * container.ratio)) / container.ratio);

        container.document.ready = false;
        progress.setMaxWidth(width);
        progress.setWidth(width);
        ps.setWidth(width);
        container.document.ready = true;

        fullscreen_btn.display_type = Block.Display.NONE;
        type = AUDIO;

        container.forceRepaint();
        container.document.repaint();
    }

    private void createNewPlayer(String url) {
        container.removeAllElements();
        int width = (int) Math.max((double) default_width / container.ratio, (double) video.width / container.ratio);
        MediaPlayer mp = new MediaPlayer(container, width, -1);
        mp.open(url);
        container.document.root.performLayout();
        container.forceRepaint();
        container.document.repaint();
        type = VIDEO;
    }

    public void play() {
        if (type == AUDIO) {
            mediaPlayerComponent.getMediaPlayer().play();
            mediaPlayerComponent.getMediaPlayer().setTime(pendingTime);
        } else {
            if (!is_fullscreen) {
                avPlayerComponent.getMediaPlayer().play();
                avPlayerComponent.getMediaPlayer().setTime(pendingTime);
            } else {
                ep.getMediaPlayer().play();
                ep.getMediaPlayer().setTime(pendingTime);
            }
        }
        pendingTime = 0;
    }

    public void pause() {
        if (type == AUDIO) {
            mediaPlayerComponent.getMediaPlayer().pause();
            pendingTime = mediaPlayerComponent.getMediaPlayer().getTime();
        } else {
            if (!is_fullscreen) {
                avPlayerComponent.getMediaPlayer().pause();
                pendingTime = avPlayerComponent.getMediaPlayer().getTime();
            } else {
                ep.getMediaPlayer().pause();
                pendingTime = ep.getMediaPlayer().getTime();
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
                ep.getMediaPlayer().stop();
            }
        }
    }

    public void enterFullScreen() {
        container.document.is_video_fullscreen = true;
        fullscreen_window.setVisible(true);
        strategy.enterFullScreenMode();
        if (is_fullscreen) return;
        removeListeners(avPlayerComponent.getMediaPlayer());
        setListeners(ep.getMediaPlayer());
        
        ep.getMediaPlayer().setTime(avPlayerComponent.getMediaPlayer().getTime() - correction_delta);
        //avPlayerComponentFullScreen.getMediaPlayer().play();
        //avPlayerComponentFullScreen.getMediaPlayer().setTime(avPlayerComponent.getMediaPlayer().getTime() - correction_delta);
        showControls();

        final WebDocument doc = (WebDocument) controls.getContentPane().getComponent(0);
        doc.root.performLayout();
        doc.root.forceRepaint();
        doc.repaint();
        
        Timer t = new Timer(200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doc.root.forceRepaint();
                doc.repaint();
            }
        });
        t.setRepeats(false);
        t.start();

        final boolean is_playing = avPlayerComponent.getMediaPlayer().isPlaying();

        avPlayerComponent.getMediaPlayer().pause();
        pendingTime = avPlayerComponent.getMediaPlayer().getTime() - correction_delta;

        Timer t2 = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pendingTime = avPlayerComponent.getMediaPlayer().getTime() - correction_delta;
                if (is_playing) {
                    ep.getMediaPlayer().play();
                    ep.getMediaPlayer().setTime(pendingTime);
                }
            }
        });
        t2.setRepeats(false);
        t2.start();
        
        is_fullscreen = true;
        strategy.enterFullScreenMode();
    }

    public void exitFullScreen() {
        container.document.is_video_fullscreen = false;
        strategy.exitFullScreenMode();
        if (!is_fullscreen) return;
        //avPlayerComponent.getMediaPlayer().setTime(avPlayerComponentFullScreen.getMediaPlayer().getTime() - correction_delta);
        //avPlayerComponentFullScreen.getMediaPlayer().pause();
        boolean is_playing = ep.getMediaPlayer().isPlaying();
        pendingTime = ep.getMediaPlayer().getTime() - correction_delta;
        avPlayerComponent.getMediaPlayer().setTime(pendingTime);
        if (is_playing) {
            avPlayerComponent.getMediaPlayer().play();
            avPlayerComponent.getMediaPlayer().setTime(pendingTime);
        }
        ep.getMediaPlayer().pause();
        removeListeners(ep.getMediaPlayer());
        setListeners(avPlayerComponent.getMediaPlayer());
        is_fullscreen = false;
        toggleControlsTimer.stop();
        hideControls(true);
        fullscreen_window.setVisible(false);
    }

    public void showControls() {
        if (hideControlsTimer.isRunning()) {
            hideControlsTimer.stop();
        }

        controls.setVisible(true);

        WebDocument doc = (WebDocument) controls.getContentPane().getComponent(0);
        doc.root.performLayout();
        doc.root.forceRepaint();
        doc.repaint();
        
        fullscreen_window.requestFocus();
        video_surface_fullscreen.requestFocus();
        toggleControlsTimer.start();
    }

    public void hideControls() {
        hideControls(false);
    }

    public void hideControls(boolean exit) {
        WebDocument doc = (WebDocument) controls.getContentPane().getComponent(0);
        doc.root.flushBuffersRecursively();
        controls.setVisible(false);
        video_surface_fullscreen.requestFocus();
        if (!exit) {
            mouseTracker.updateLastPoint();
            toggleControlsTimer.start();
        }
    }

    long pendingTime = 0;

    JFrame fullscreen_window;
    JFrame controls;
    JLabel timeElapsed;
    JLabel timeRemains;
    boolean enableTimeDisplay = true;
    FullScreenStrategy strategy;
    Timer toggleControlsTimer;
    Timer hideControlsTimer;
    MouseTracker mouseTracker;

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
            //g2d.setClip(clip_rect);
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

    public static final int AUDIO = 0;
    public static final int VIDEO = 1;

    private int default_volume = 80;
    private int volume = default_volume;

    public static int default_width = 380;
    public static int default_audio_width = 230;
    public static int min_height = 140;
    public static int panel_height = 22;

    private static final String NATIVE_LIBRARY_SEARCH_PATH = "C:/Program Files/VideoLAN/VLC";
    private static final String LOCAL_PATH = Util.getInstallPath() + "vlc";

    private IconLayer icon1;
    private IconLayer icon2;
    private Segments s;
    private Block ps;
    private Block progress;
    private Block play_btn;
    private Block fullscreen_btn;
    private Block psf;
    private Block progress_fullscreen;
    private Block play_fullscreen;


    class Fader extends Thread {

        Fader(int dir) {
            direction = dir;
            value = dir > 0 ? volume : 0;
        }

        @Override
        public void run() {
            if (direction > 0) {
                if (!is_fullscreen && type == AUDIO) {
                    mediaPlayerComponent.getMediaPlayer().play();
                    mediaPlayerComponent.getMediaPlayer().setTime(pendingTime);
                } else if (!is_fullscreen) {
                    avPlayerComponent.getMediaPlayer().play();
                    avPlayerComponent.getMediaPlayer().setTime(pendingTime);
                } else {
                    ep.getMediaPlayer().play();
                    ep.getMediaPlayer().setTime(pendingTime);
                }
            } else {
                icon1.icon_state = 0;
                icon1.redraw();
                if (is_fullscreen) {
                    play_fullscreen.forceRepaint();
                }
            }
            while (!is_fullscreen && (mediaPlayerComponent != null && mediaPlayerComponent.getMediaPlayer().getVolume() != value ||
                    avPlayerComponent != null && avPlayerComponent.getMediaPlayer().getVolume() != value) ||
                    is_fullscreen && ep != null && ep.getMediaPlayer().getVolume() != value) {
                if (stop) break;
                if (direction > 0) {
                    if (!is_fullscreen && type == AUDIO) {
                        mediaPlayerComponent.getMediaPlayer().setVolume(Math.min(mediaPlayerComponent.getMediaPlayer().getVolume() + step, value));
                    } else if (!is_fullscreen) {
                        avPlayerComponent.getMediaPlayer().setVolume(Math.min(avPlayerComponent.getMediaPlayer().getVolume() + step, value));
                    } else {
                        ep.getMediaPlayer().setVolume(Math.min(ep.getMediaPlayer().getVolume() + step, value));
                    }
                } else {
                    if (!is_fullscreen && type == AUDIO) {
                        mediaPlayerComponent.getMediaPlayer().setVolume(Math.max(mediaPlayerComponent.getMediaPlayer().getVolume() - step, value));
                    } else if (!is_fullscreen) {
                        avPlayerComponent.getMediaPlayer().setVolume(Math.max(avPlayerComponent.getMediaPlayer().getVolume() - step, value));
                    } else {
                        ep.getMediaPlayer().setVolume(Math.max(ep.getMediaPlayer().getVolume() - step, value));
                    }
                }
                try {
                    Thread.sleep(28);
                } catch (InterruptedException ex) {}
            }
            if (!is_fullscreen && type == AUDIO && mediaPlayerComponent.getMediaPlayer().getVolume() == value && direction < 0) {
                pendingTime = mediaPlayerComponent.getMediaPlayer().getTime();
                mediaPlayerComponent.getMediaPlayer().pause();
            } else if (!is_fullscreen && avPlayerComponent.getMediaPlayer().getVolume() == value && direction < 0) {
                pendingTime = avPlayerComponent.getMediaPlayer().getTime();
                avPlayerComponent.getMediaPlayer().pause();
            } else if (is_fullscreen && ep.getMediaPlayer().getVolume() == value && direction < 0) {
                pendingTime = ep.getMediaPlayer().getTime();
                ep.getMediaPlayer().pause();
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
            orig_width = (int) ((double)width / block.ratio);
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

    class MouseTracker implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Point p = MouseInfo.getPointerInfo().getLocation();
            if (!p.equals(last_point) && is_fullscreen) {
                if (!controls.isVisible()) {
                    showControls();
                } else if (controls.isVisible()) {
                    if (!controls.getBounds().contains(p)) {
                        hideControlsTimer.start();
                    } else {
                        hideControlsTimer.stop();
                    }
                }
            }
            last_point = p;
        }

        public void updateLastPoint() {
            last_point = MouseInfo.getPointerInfo().getLocation();
        }

        Point last_point = MouseInfo.getPointerInfo().getLocation();
    }

    AudioMediaPlayerComponent mediaPlayerComponent;
    DirectMediaPlayerComponent avPlayerComponent;
    DirectMediaPlayerComponent avPlayerComponentFullScreen;

    MouseListener trackListener;

    Block panel;
    Block video;
    VideoRenderer video_surface;
    FastVideoRenderer video_surface_fullscreen;
    Block container;
    int correction_delta = 0;

    EmbeddedMediaPlayerComponent ep;
}
