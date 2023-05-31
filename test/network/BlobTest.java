package network;

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
public class BlobTest {

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
     * Test of getSlice method, of class Blob.
     */
    @Test
    public void testNextChunk() {
        byte[] b1 = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        byte[] b2 = new byte[] { 11, 12, 13 };
        byte[] b3 = new byte[] { 14, 15, 16, 17, 18 };

        DataPart.CHUNK_SIZE = 5;

        Blob blob1 = new Blob(new Blob[] { new Blob(b1), new Blob(b2), new Blob(b3) });
        blob1.seek(10);

        byte[] bytes = blob1.nextChunk();

        System.out.println();

        String res = "";

        for (int i = 0; i < bytes.length; i++) {
            res += bytes[i] + " ";
            System.out.print(bytes[i] + " ");
        }
        System.out.println();

        assertEquals("11 12 13 14 15", res.trim()); 
    }

    /**
     * Test of getSlice method, of class Blob.
     */
    @Test
    public void testGetSlice() {
        byte[] b1 = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        byte[] b2 = new byte[] { 11, 12, 13 };
        byte[] b3 = new byte[] { 14, 15, 16, 17, 18 };


        Blob blob1 = new Blob(new Blob[] { new Blob(b1), new Blob(b2), new Blob(b3) });
        Blob slice1 = blob1.getSlice(8, 13);

        System.out.println();

        String res = "";

        System.out.println("Parts number: " + slice1.parts.size());
        assertEquals(2, slice1.parts.size());

        for (int i = 0; i < slice1.parts.size(); i++) {
            for (int j = 0; j < slice1.parts.get(i).content.length; j++) {
                res += slice1.parts.get(i).content[j] + " ";
                System.out.print(slice1.parts.get(i).content[j] + " ");
            }
            System.out.println();
        }
        System.out.println();

        assertEquals("9 10 11 12 13", res.trim());

        Blob slice2 = blob1.getSlice(8, 16);

        System.out.println();

        res = "";

        System.out.println("Parts number: " + slice2.parts.size());
        assertEquals(3, slice2.parts.size());

        for (int i = 0; i < slice2.parts.size(); i++) {
            for (int j = 0; j < slice2.parts.get(i).content.length; j++) {
                res += slice2.parts.get(i).content[j] + " ";
                System.out.print(slice2.parts.get(i).content[j] + " ");
            }
            System.out.println();
        }
        System.out.println();

        assertEquals("9 10 11 12 13 14 15 16", res.trim());
    }

    /**
     * Test of getSlice method, of class Blob.
     */
    @Test
    public void testGetSliceDeep() {
        byte[] b1 = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        byte[] b2 = new byte[] { 11, 12, 13 };
        byte[] b3 = new byte[] { 14, 15 };
        byte[] b4 = new byte[] { 16, 17, 18 };

        Blob blob1 = new Blob(new Blob[] { new Blob(b1), new Blob(new Blob[] { new Blob(b2), new Blob(b3) }), new Blob(b4) });

        Blob.UNWRAP_SINGLE_ITEM_LISTS = false;
        Blob slice1 = blob1.getSlice(10, 15);
        Blob slice2 = blob1.getSlice(10, 17);

        System.out.println("Parts number of slice 1: " + slice1.parts.size());
        assertEquals(1, slice1.parts.size());

        System.out.println("Parts number of slice 2: " + slice2.parts.size());
        assertEquals(2, slice2.parts.size());

        System.out.println();


        Blob.UNWRAP_SINGLE_ITEM_LISTS = true;
        slice1 = blob1.getSlice(10, 15);
        slice2 = blob1.getSlice(10, 17);

        System.out.println("Parts number of slice 1: " + slice1.parts.size());
        assertEquals(2, slice1.parts.size());

        System.out.println("Parts number of slice 2: " + slice2.parts.size());
        assertEquals(2, slice2.parts.size());

        
        String res = "";

        for (int i = 0; i < slice1.parts.size(); i++) {
            Blob part = slice1.parts.get(i);
            byte[] b = part.getSliceBytes(0, part.getSize());
            for (int j = 0; j < b.length; j++) {
                res += b[j] + " ";
                System.out.print(b[j] + " ");
            }
            System.out.println();
        }
        System.out.println();
        
        assertEquals("11 12 13 14 15", res.trim());

        res = "";

        for (int i = 0; i < slice2.parts.size(); i++) {
            Blob part = slice2.parts.get(i);
            byte[] b = part.getSliceBytes(0, part.getSize());
            for (int j = 0; j < b.length; j++) {
                res += b[j] + " ";
                System.out.print(b[j] + " ");
            }
            System.out.println();
        }
        System.out.println();

        assertEquals("11 12 13 14 15 16 17", res.trim());
        
    }
}
