package com.alstarsoft.tinybrowser.network;

import com.alstarsoft.tinybrowser.network.DataPart;
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
public class DataPartTest {

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
     * Test of getSlice method, of class DataPart.
     */
    @Test
    public void testGetSlice() {
        byte[] b = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

        DataPart p = new DataPart(b);
        DataPart.CHUNK_SIZE = 3;

        DataPart slice = p.getSlice(2, 7);
        String res = "";
        for (int i = 0; i < slice.content.length; i++) {
            res += slice.content[i] + " ";
            System.out.print(slice.content[i] + " ");
        }
        System.out.println();

        assertEquals("3 4 5 6 7", res.trim());


        res = "";
        slice = p.getSlice(2, 13);

        for (int i = 0; i < slice.content.length; i++) {
            res += slice.content[i] + " ";
            System.out.print(slice.content[i] + " ");
        }

        assertEquals("3 4 5 6 7 8 9 10", res.trim());
    }
}
