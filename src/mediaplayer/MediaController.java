package mediaplayer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

/**
 *
 * @author Alex
 */
public class MediaController extends JPanel {

    public MediaController() {
        playBtn = new PlayButton();
        setFont(new Font("Arial", Font.PLAIN, 14));
        songTitle = new JLabel("Artist - Title");

        slider = new Slider();
        volumeSlider = new VolumeSlider();
        slider.setForeground(new Color(93, 164, 227));
        
        volumeSlider = new VolumeSlider();
        volumeSlider.setBackground(new Color(183, 183, 188));
        volumeSlider.setForeground(new Color(78, 146, 217));

        initComponents();
        this.setPreferredSize(new Dimension(180, 68));
        setListeners();
    }

    public void setMediaPlayer(MediaPlayer player, boolean isMaster) {
        if (mediaPlayer == player) return;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.removeAdapter(adapter);
        }
        mediaPlayer = player;
        mediaPlayer.addAdapter(adapter);
        if (isMaster) {
            mediaPlayer.primaryAdapter = adapter;
        }
    }

    public void setMediaPlayer(MediaPlayer player) {
        setMediaPlayer(player, true);
    }

    public void setAsMasterController() {
        if (mediaPlayer == null) return;
        mediaPlayer.removeAdapter(adapter);
        mediaPlayer.setAdapter(adapter);
    }

    public void setListeners() {
        playBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mediaPlayer == null) return;
                if (mediaPlayer.mediaPlayerComponent.getMediaPlayer().isPlaying()) {
                    mediaPlayer.pausePlayback();
                } else {
                    mediaPlayer.resumePlayback();
                }
            }
        });

        slider.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                slider.setValue((int)((double)e.getX() / slider.getWidth() * slider.getMaximum()));
                if (mediaPlayer != null) mediaPlayer.changeTime((int)((double)e.getX() / slider.getWidth() * mediaPlayer.mediaPlayerComponent.getMediaPlayer().getLength()));
                audioPos = slider.getValue();
            }

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });

        volumeSlider.setValue(volume);
        if (mediaPlayer != null) mediaPlayer.changeVolume(volumeSlider.getValue());

        volumeSlider.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                volumeSlider.setValue((int)((double)e.getX() / volumeSlider.getWidth() * volumeSlider.getMaximum()));
                if (mediaPlayer != null) mediaPlayer.changeVolume(volumeSlider.getValue());
                volume = volumeSlider.getValue();
            }

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });
    }

    private void initComponents() {
        setOpaque(false);
        int buttonSize = 30;
        playBtn.setMinimumSize(new Dimension(buttonSize, buttonSize));
        playBtn.setPreferredSize(new Dimension(buttonSize, buttonSize));
        playBtn.size = buttonSize;
        add(playBtn);

        //audioplayer.setBackground(resourceMap.getColor("audioplayer.background")); // NOI18N
        setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        setMaximumSize(new java.awt.Dimension(32767, 60));
        setName("audioplayer"); // NOI18N
        setPreferredSize(new java.awt.Dimension(330, 60));
        setLayout(new java.awt.GridBagLayout());

        playBtn.setMargin(new java.awt.Insets(2, 8, 2, 8));
        playBtn.setName("playButton"); // NOI18N
        java.awt.GridBagConstraints gridBagConstraints;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(14, 8, 3, 6);
        add(playBtn, gridBagConstraints);

        songTitle.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        //songTitle.setMaximumSize(new java.awt.Dimension(190, 20));
        songTitle.setMinimumSize(new java.awt.Dimension(190, 20));
        songTitle.setName("songTitle"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 4, 0, 0);
        add(songTitle, gridBagConstraints);

        volumeSlider.setMaximumSize(new java.awt.Dimension(60, 14));
        volumeSlider.setMinimumSize(new java.awt.Dimension(37, 14));
        volumeSlider.setName("volumeSlider1"); // NOI18N
        volumeSlider.setPreferredSize(new java.awt.Dimension(60, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 8, 0, 8);
        add(volumeSlider, gridBagConstraints);

        slider.setMaximumSize(new java.awt.Dimension(32767, 6));
        slider.setMinimumSize(new java.awt.Dimension(10, 6));
        slider.setName("slider1"); // NOI18N
        slider.setOpaque(true);
        slider.setPreferredSize(new java.awt.Dimension(146, 6));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 8, 8, 8);
        add(slider, gridBagConstraints);

        doLayout();
    }

    private void setDisplayMode(int mode) {
        if (mode == 1) {
            java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
            gridBagConstraints.insets = new java.awt.Insets(6, 2, 5, 2);
            add(slider, gridBagConstraints);
            songTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 6, 0));
            volumeSlider.setPreferredSize(new Dimension(48, 17));
        } else {
            java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
            gridBagConstraints.insets = new java.awt.Insets(2, 8, 12, 8);
            add(slider, gridBagConstraints);
            songTitle.setBorder(null);
            volumeSlider.setPreferredSize(new Dimension(60, 14));
        }
    }

    public void setSongTitle(String title) {
        songTitle.setText(title);
    }

    public void setVolume(int value) {
        volume = value;
        volumeSlider.setValue(value);
        if (mediaPlayer != null) {
            mediaPlayer.changeVolume(value);
        }
    }

    public void play() throws NoMediaPlayerException {
        if (mediaPlayer.mediaPlayerComponent.getMediaPlayer().isPlayable()) {
            mediaPlayer.play();
        } else {
            throw new NoMediaPlayerException();
        }
    }

    public MediaPlayerEventAdapter getAdapter() {
        return adapter;
    }

    private MediaPlayerEventAdapter adapter = new MediaPlayerEventAdapter() {

        @Override
        public void playing(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {
            playBtn.state = 1;
            playBtn.repaint();
            audioPaused = false;
        }

        @Override
        public void paused(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {
            playBtn.state = 0;
            playBtn.repaint();
            audioPaused = true;
        }

        @Override
        public void stopped(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {
            playBtn.state = 0;
            playBtn.repaint();
            audioPaused = true;
        }

        @Override
        public void timeChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, long new_time) {
            int secs = (int)(new_time / 1000) % 60;
            int mins = (int)((new_time / 1000 - secs) / 60 % 60);
            int hours = (int)((new_time / 1000 - secs - mins * 60) / 3600);
            //System.out.println((mins < 10 ? "0" + mins : mins) + ":" + (secs < 10 ? "0" + secs : secs));

            slider.setValue((int)((double)new_time / mediaPlayer.getLength() * slider.getMaximum()));
            if (Math.abs(slider.getValue() - audioPos) <= 2) {
                audioPos = slider.getValue();
            }
            slider.repaint();
            if (new_time >= mediaPlayer.getLength()) {
                mediaPlayer.stop();
                audioPaused = true;
            }
        }

        @Override
        public void finished(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {
            playBtn.state = 0;
            playBtn.repaint();
        }

    };

    public void openSource(String url) throws NoMediaPlayerException {
        source = url;
        if (mediaPlayer != null) {
            mediaPlayer.open(url);
        } else {
            throw new NoMediaPlayerException();
        }
    }

    public String getSource() {
        return source;
    }

    private String source;

    private PlayButton playBtn;
    private JLabel songTitle;
    private Slider slider;
    private VolumeSlider volumeSlider;
    private MediaPlayer mediaPlayer;

    private int volume = 80;
    private int audioPos = 0;

    private boolean audioPaused = true;
}
