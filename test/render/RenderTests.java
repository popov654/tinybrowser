package render;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    @Test
    public void linksTest() {
        lt.testLinks();
        lt.setVisible(true);
        
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

            robot.mouseMove(p.x + rect.x + border + 83, p.y + rect.y + border + 53);
            robot.waitForIdle();
            document.repaint();

            Thread.sleep(800);

            BufferedImage img = robot.createScreenCapture(new Rectangle(p.x + rect.x + border, p.y + rect.y + border, document.root.width, document.root.height));

            img.getData().getPixel(83, 53, data);
            assertTrue(Math.abs(data[0] - 182) < 3);
            assertTrue(Math.abs(data[1] - 206) < 3);
            assertTrue(Math.abs(data[2] - 228) < 3);

            img.getData().getPixel(83, 56, data);
            assertTrue(Math.abs(data[0] - 182) < 3);
            assertTrue(Math.abs(data[1] - 206) < 3);
            assertTrue(Math.abs(data[2] - 228) < 3);
            //System.err.println(data[0] + "," + data[1] + "," + data[2] + "," + data[3]);

            //javax.imageio.ImageIO.write(img, "png", new File("snapshot.png"));
        } catch (Exception ex) {}

        //lt.dispose();
        //lt.setVisible(false);
    }

    @Test
    public void bordersTest() {
        lt.testBorders();
        lt.setVisible(true);
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

            img.getData().getPixel(19, 119, data);
            assertTrue(Math.abs(data[0] - 253) < 3);
            assertTrue(Math.abs(data[1] - 0) < 3);
            assertTrue(Math.abs(data[2] - 253) < 3);

            img.getData().getPixel(458, 19, data);
            assertTrue(Math.abs(data[0] - 253) < 3);
            assertTrue(Math.abs(data[1] - 0) < 3);
            assertTrue(Math.abs(data[2] - 253) < 3);

            img.getData().getPixel(465, 125, data);
            assertTrue(Math.abs(data[0] - 253) < 3);
            assertTrue(Math.abs(data[1] - 0) < 3);
            assertTrue(Math.abs(data[2] - 253) < 3);

            img.getData().getPixel(12, 120, data);
            assertEquals(255, data[0]);
            assertEquals(255, data[1]);
            assertEquals(255, data[2]);

            img.getData().getPixel(460, 15, data);
            assertEquals(255, data[0]);
            assertEquals(255, data[1]);
            assertEquals(255, data[2]);

            img.getData().getPixel(16, 180, data);
            assertEquals(0, data[0]);
            assertEquals(255, data[1]);
            assertEquals(255, data[2]);

            img.getData().getPixel(60, 140, data);
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

            img.getData().getPixel(24, 151, data);
            assertEquals(255, data[0]);
            assertEquals(255, data[1]);
            assertEquals(255, data[2]);

            img.getData().getPixel(92, 151, data);
            assertEquals(255, data[0]);
            assertEquals(255, data[1]);
            assertEquals(255, data[2]);

            img.getData().getPixel(92, 218, data);
            assertEquals(255, data[0]);
            assertEquals(255, data[1]);
            assertEquals(255, data[2]);

            img.getData().getPixel(24, 218, data);
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
            Thread.sleep(200);
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
            System.err.println(data[0] + "," + data[1] + "," + data[2] + "," + data[3]);

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

    private BufferedImage getImage(Block block) {
        BufferedImage img = new BufferedImage(block.width, block.height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D gc = (Graphics2D) img.getGraphics();
        gc.setBackground(block.bgcolor);
        gc.clearRect(0, 0, document.root.width, document.root.height);
        block.drawSubtree(gc, block._x_, block._y_);
        return img;
    }

    private WebDocument document;
    private LayoutTests lt;
}
