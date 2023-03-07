package render;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.UIManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Alex
 */
public class RenderTests {

    public RenderTests() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        lt = new LayoutTests();
        document = lt.getDocument();
        document.forced_dpi = 1.5;
        document.root.ratio = 1.5;
    }

    @After
    public void tearDown() {
    }

    @Test
    public void basicLayoutTest() {
        lt.basicTest();
        lt.setVisible(true);
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {}

        assertEquals(6, document.root.getChildren().get(0)._x_);
        assertEquals(6, document.root.getChildren().get(0)._y_);
        
        assertEquals(  6, document.root.getChildren().get(1)._x_);
        assertEquals(132, document.root.getChildren().get(1)._y_);

        assertEquals(460, document.root.getChildren().get(0).width);
        assertEquals(120, document.root.getChildren().get(0).height);

        assertEquals(460, document.root.getChildren().get(1).width);
        assertEquals(120, document.root.getChildren().get(1).height);

        assertEquals(35, document.root.getChildren().get(0).getChildren().get(0).parts.get(0)._x_);
        assertEquals(41, document.root.getChildren().get(0).getChildren().get(0).parts.get(0)._y_);

        assertEquals(46, document.root.getChildren().get(0).getChildren().get(0).parts.get(0).width);
        assertEquals(26, document.root.getChildren().get(0).getChildren().get(0).parts.get(0).height);

        assertEquals(81, document.root.getChildren().get(0).getChildren().get(1).parts.get(0)._x_);
        assertEquals(32, document.root.getChildren().get(0).getChildren().get(1).parts.get(0)._y_);

        assertEquals(61, document.root.getChildren().get(0).getChildren().get(1).parts.get(0).width);
        assertEquals(44, document.root.getChildren().get(0).getChildren().get(1).parts.get(0).height);

        assertEquals(142, document.root.getChildren().get(0).getChildren().get(2).parts.get(0)._x_);
        assertEquals(41,  document.root.getChildren().get(0).getChildren().get(2).parts.get(0)._y_);

        assertEquals(153, document.root.getChildren().get(0).getChildren().get(2).parts.get(0).width);
        assertEquals(26,  document.root.getChildren().get(0).getChildren().get(2).parts.get(0).height);

        assertEquals(40,  document.root.getChildren().get(1).getChildren().get(0).width);
        assertEquals(26,  document.root.getChildren().get(1).getChildren().get(0).height);

        int[] data = new int[4];

        Robot robot;
        try {
            robot = new Robot();
            Point p = lt.getLocation();
            Rectangle rect = document.getBounds();
            int border = document.borderSize;

            Insets insets = lt.getInsets();
            if (insets != null) {
                p.x += insets.left;
                p.y += insets.top;
            }

            BufferedImage img = robot.createScreenCapture(new Rectangle(p.x + rect.x + border, p.y + rect.y + border, document.root.width, document.root.height));

            img.getData().getPixel(8, 45, data);
            assertEquals(255, data[0]);
            assertEquals(45,  data[1]);
            assertEquals(255, data[2]);
            
            img.getData().getPixel(41, 47, data);
            assertEquals(0, data[0]);
            assertEquals(0, data[1]);
            assertEquals(0, data[2]);

            img.getData().getPixel(110, 40, data);
            assertEquals(255, data[0]);
            assertEquals(210, data[1]);
            assertEquals(0,   data[2]);
            //System.err.println(data[0] + "," + data[1] + "," + data[2] + "," + data[3]);

            //javax.imageio.ImageIO.write(img, "png", new File("snapshot.png"));
        } catch (Exception ex) {}
        
        //lt.setVisible(false);
        //lt.dispose();
    }

    public void layoutUpdateTest() {
        lt.liveLayoutUpdateTest();
        lt.setVisible(true);

        try {
            Thread.sleep(300);
            lt.getDocument().processResize();
        } catch (InterruptedException ex) {}

        int[] data = new int[4];

        Robot robot;
        try {
            robot = new Robot();
            Point p = lt.getLocation();
            Rectangle rect = document.getBounds();
            int border = document.borderSize;

            Insets insets = lt.getInsets();
            if (insets != null) {
                p.x += insets.left;
                p.y += insets.top;
            }

            try {
                Thread.sleep(1800);
            } catch (InterruptedException ex) {}

            BufferedImage img = robot.createScreenCapture(new Rectangle(p.x + rect.x + border, p.y + rect.y + border, document.root.width, document.root.height));

            img.getData().getPixel(28, 150, data);
            assertEquals(255, data[0]);
            assertEquals(255,  data[1]);
            assertEquals(255, data[2]);

            try {
                Thread.sleep(1800);
            } catch (InterruptedException ex) {}

            img = robot.createScreenCapture(new Rectangle(p.x + rect.x + border, p.y + rect.y + border, document.root.width, document.root.height));

            img.getData().getPixel(28, 150, data);
            assertEquals(130, data[0]);
            assertEquals(178,  data[1]);
            assertEquals(224, data[2]);

            //System.err.println(data[0] + "," + data[1] + "," + data[2] + "," + data[3]);

            //javax.imageio.ImageIO.write(img, "png", new File("snapshot.png"));
        } catch (Exception ex) {}
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {}
    }

    @Test
    public void linksTest() {
        lt.setVisible(true);
        lt.testLinks();
        
        try {
            Thread.sleep(500);
            lt.getDocument().processResize();
        } catch (InterruptedException ex) {}

        int[] data = new int[4];

        Robot robot;
        try {
            robot = new Robot();
            Point p = lt.getLocation();
            Rectangle rect = document.getBounds();
            int border = document.borderSize;

            Insets insets = lt.getInsets();
            if (insets != null) {
                p.x += insets.left;
                p.y += insets.top;
            }

            //robot.mouseMove(p.x + rect.x + border + 83, p.y + rect.y + border + 50);
            robot.waitForIdle();
            document.repaint();

            Thread.sleep(1000);

            BufferedImage img = robot.createScreenCapture(new Rectangle(p.x + rect.x + border, p.y + rect.y + border, document.root.width, document.root.height));

            img.getData().getPixel(83, 52, data);
            assertTrue(Math.abs(data[0] - 6) < 3);
            assertTrue(Math.abs(data[1] - 66) < 3);
            assertTrue(Math.abs(data[2] - 162) < 3);

            //img.getData().getPixel(83, 54, data);
            //assertTrue(Math.abs(data[0] - 6) < 3);
            //assertTrue(Math.abs(data[1] - 66) < 3);
            //assertTrue(Math.abs(data[2] - 162) < 3);
            //System.err.println(data[0] + "," + data[1] + "," + data[2] + "," + data[3]);

            //javax.imageio.ImageIO.write(img, "png", new File("snapshot.png"));
        } catch (Exception ex) {}

        //lt.dispose();
        //lt.setVisible(false);
    }

    @Test
    public void bordersTest() {
        lt.setVisible(true);
        lt.testBorders();
        
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {}

        int[] data = new int[4];

        Robot robot;
        try {
            robot = new Robot();
            Point p = lt.getLocation();
            Rectangle rect = document.getBounds();
            int border = document.borderSize;

            Insets insets = lt.getInsets();
            if (insets != null) {
                p.x += insets.left;
                p.y += insets.top;
            }

            document.repaint();
            Thread.sleep(800);

            BufferedImage img = robot.createScreenCapture(new Rectangle(p.x + rect.x + border, p.y + rect.y + border, document.root.width, document.root.height));

            img.getData().getPixel(6, 6, data);
            assertTrue(Math.abs(data[0] - 253) < 5);
            assertTrue(Math.abs(data[1] - 0) < 5);
            assertTrue(Math.abs(data[2] - 253) < 5);

            
            if (!document.smooth_borders) {
                img.getData().getPixel(12, 119, data);
                assertTrue(Math.abs(data[0] - 253) < 3);
                assertTrue(Math.abs(data[1] - 0) < 3);
                assertTrue(Math.abs(data[2] - 253) < 3);
            } else {
                img.getData().getPixel(15, 116, data);
                assertTrue(Math.abs(data[0] - 253) < 15);
                assertTrue(Math.abs(data[1] - 0) < 48);
                assertTrue(Math.abs(data[2] - 253) < 15);
            }
            
            if (!document.smooth_borders) {
                img.getData().getPixel(458, 19, data);
                assertTrue(Math.abs(data[0] - 253) < 3);
                assertTrue(Math.abs(data[1] - 0) < 3);
                assertTrue(Math.abs(data[2] - 253) < 3);
            } else {
                img.getData().getPixel(458, 17, data);
                assertTrue(Math.abs(data[0] - 253) < 15);
                assertTrue(Math.abs(data[1] - 0) < 48);
                assertTrue(Math.abs(data[2] - 253) < 15);
            }

            img.getData().getPixel(465, 125, data);
            assertTrue(Math.abs(data[0] - 253) < 3);
            assertTrue(Math.abs(data[1] - 0) < 3);
            assertTrue(Math.abs(data[2] - 253) < 3);

            img.getData().getPixel(12, 120, data);
            assertEquals(255, data[0]);
            assertEquals(255, data[1]);
            assertEquals(255, data[2]);

            img.getData().getPixel(60, 145, data);
            assertEquals(255, data[0]);
            assertEquals(0, data[1]);
            assertEquals(0, data[2]);

            img.getData().getPixel(102, 180, data);
            assertEquals(0, data[0]);
            assertEquals(255, data[1]);
            assertEquals(0, data[2]);

            img.getData().getPixel(60, 230, data);
            assertEquals(255, data[0]);
            assertEquals(255, data[1]);
            assertEquals(0, data[2]);

            img.getData().getPixel(40, 170, data);
            assertEquals(255, data[0]);
            assertEquals(255, data[1]);
            assertEquals(255, data[2]);

            img.getData().getPixel(78, 170, data);
            assertEquals(255, data[0]);
            assertEquals(255, data[1]);
            assertEquals(255, data[2]);

            img.getData().getPixel(40, 212, data);
            assertEquals(255, data[0]);
            assertEquals(255, data[1]);
            assertEquals(255, data[2]);

            img.getData().getPixel(78, 212, data);
            assertEquals(255, data[0]);
            assertEquals(255, data[1]);
            assertEquals(255, data[2]);

            img.getData().getPixel(148, 148, data);
            assertEquals(228, data[0]);
            assertEquals(228, data[1]);
            assertEquals(228, data[2]);

            img.getData().getPixel(190, 218, data);
            assertEquals(255, data[0]);
            assertEquals(255, data[1]);
            assertEquals(255, data[2]);


            img.getData().getPixel(285, 147, data);
            assertEquals(255, data[0]);
            assertEquals(255, data[1]);
            assertEquals(255, data[2]);

            img.getData().getPixel(353, 147, data);
            assertEquals(255, data[0]);
            assertEquals(255, data[1]);
            assertEquals(255, data[2]);

            img.getData().getPixel(285, 233, data);
            assertEquals(255, data[0]);
            assertEquals(255, data[1]);
            assertEquals(255, data[2]);

            img.getData().getPixel(353, 233, data);
            assertEquals(255, data[0]);
            assertEquals(255, data[1]);
            assertEquals(255, data[2]);

            //System.err.println(data[0] + "," + data[1] + "," + data[2] + "," + data[3]);

            //javax.imageio.ImageIO.write(img, "png", new File("snapshot.png"));
        } catch (Exception ex) {}
        
        //lt.dispose();
        //lt.setVisible(false);
    }

    @Test
    public void imagesTest() {
        lt.setVisible(true);
        lt.testImages(230, 170);
        try {
            Thread.sleep(300);
            lt.getDocument().processResize();
        } catch (InterruptedException ex) {}

        assertEquals(Math.round(230 * document.root.ratio), document.root.getChildren().get(0).width);
        assertEquals(Math.round(170 * document.root.ratio), document.root.getChildren().get(0).height);

        int[] data = new int[4];

        Robot robot;
        try {
            robot = new Robot();
            Point p = lt.getLocation();
            Rectangle rect = document.getBounds();
            int border = document.borderSize;

            Insets insets = lt.getInsets();
            if (insets != null) {
                p.x += insets.left;
                p.y += insets.top;
            }

            robot.waitForIdle();
            Thread.sleep(800);

            BufferedImage img = robot.createScreenCapture(new Rectangle(p.x + rect.x + border, p.y + rect.y + border, document.root.width, document.root.height));

            img.getData().getPixel(70, 32, data);
            assertTrue(Math.abs(data[0] - 0) < 9);
            assertTrue(Math.abs(data[1] - 0) < 9);
            assertTrue(Math.abs(data[2] - 0) < 9);

            img.getData().getPixel(402, 272, data);
            assertTrue(Math.abs(data[0] - 0) < 9);
            assertTrue(Math.abs(data[1] - 0) < 9);
            assertTrue(Math.abs(data[2] - 0) < 9);

            img.getData().getPixel(74, 272, data);
            assertTrue(Math.abs(data[0] - 0) < 9);
            assertTrue(Math.abs(data[1] - 0) < 9);
            assertTrue(Math.abs(data[2] - 0) < 9);

            //System.err.println(data[0] + "," + data[1] + "," + data[2] + "," + data[3]);

            javax.imageio.ImageIO.write(img, "png", new File("snapshot.png"));
        } catch (Exception ex) {}
    }

    private WebDocument document;
    private LayoutTests lt;
}
