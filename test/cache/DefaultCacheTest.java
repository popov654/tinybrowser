package cache;

import java.io.File;
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
public class DefaultCacheTest {

    public DefaultCacheTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
    }

    /**
     * Test of get method, of class DefaultCache.
     */
    @Test
    public void testPaths() {
        DefaultCache cache = new DefaultCache();

        String[] paths = new String[] { cache.cacheDir + File.separatorChar + "29" + File.separatorChar + "f368adb5", cache.cacheDir + File.separatorChar + "81" + File.separatorChar + "60dff955" };
        boolean[] existed = new boolean[paths.length];

        for (int i = 0; i < paths.length; i++) {
            File f = new File(paths[i]);
            existed[i] = f.exists();
        }

        assertEquals(cache.cacheDir + File.separatorChar + "29" + File.separatorChar + "f368adb5" + File.separatorChar + "style.css", cache.get("http://popov654.pp.ru/isn/style.css"));
        assertEquals(cache.cacheDir + File.separatorChar + "81" + File.separatorChar + "60dff955" + File.separatorChar + "photo.jpg", cache.get("http://popov654.pp.ru/copybox/photo.jpg"));

        for (int i = 0; i < paths.length; i++) {
            File f = new File(paths[i]);
            if (!existed[i] && f.exists()) {
                File[] files = f.listFiles();
                for (File file: files) {
                    file.delete();
                }
                f.delete();
                
                f = f.getParentFile();
                if (f != null && f.isDirectory() && f.listFiles().length == 0) {
                    f.delete();
                }
            }
        }
    }

     /**
     * Test of contains method, of class DefaultCache.
     */
    @Test
    public void testContains() {
        DefaultCache cache = new DefaultCache();

        String[] paths = new String[] { cache.cacheDir + File.separatorChar + "81" + File.separatorChar + "60dff955" };
        boolean[] existed = new boolean[paths.length];

        for (int i = 0; i < paths.length; i++) {
            File f = new File(paths[i]);
            existed[i] = f.exists();
        }
        
        cache.get("http://popov654.pp.ru/copybox/photo.jpg");
        assertTrue(cache.contains("http://popov654.pp.ru/copybox/photo.jpg"));

        for (int i = 0; i < paths.length; i++) {
            File f = new File(paths[i]);
            if (!existed[i] && f.exists()) {
                File[] files = f.listFiles();
                for (File file: files) {
                    file.delete();
                }
                f.delete();

                f = f.getParentFile();
                if (f != null && f.isDirectory() && f.listFiles().length == 0) {
                    f.delete();
                }
            }
        }
    }

}