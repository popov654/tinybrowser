package network;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
public class Base64EncoderTest {

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
     * Test of nextChunk method, of class Blob.
     */
    @Test
    public void testNextChunk() {
        DataPart.CHUNK_SIZE = 10;

        File file = null;
        try {
            file = File.createTempFile("tmp_", null);
            FileWriter fw = new FileWriter(file);
            fw.write("This is a text");
            fw.close();
        } catch (IOException ex) {}

        Blob blob = new Blob(file);

        System.out.println(blob.asTextString());

        Base64Encoder encoder = new Base64Encoder(blob);
        String result = "";
        while (encoder.hasNextChunk()) {
            result += encoder.nextChunkAsString();
        }
        System.out.println(result);

        assertEquals("VGhpcyBpcyBhIHRleHQ=", result);


        DataPart.CHUNK_SIZE = 5;

        blob.seek(0);
        result = "";
        while (encoder.hasNextChunk()) {
            result += encoder.nextChunkAsString();
        }
        System.out.println(result);
        
        assertEquals("VGhpcyBpcyBhIHRleHQ=", result);


        DataPart.CHUNK_SIZE = 6;

        blob.seek(0);
        result = "";
        while (encoder.hasNextChunk()) {
            result += encoder.nextChunkAsString();
        }
        System.out.println(result);

        assertEquals("VGhpcyBpcyBhIHRleHQ=", result);

        System.out.println();

        file.delete();

    }

    @Test
    public void testBlobOutput() {
        DataPart.CHUNK_SIZE = 10;

        File file = null;
        try {
            file = File.createTempFile("tmp_", null);
            FileWriter fw = new FileWriter(file);
            fw.write("This is a text");
            fw.close();
        } catch (IOException ex) {}

        Blob blob = new Blob(file);

        System.out.println(blob.asTextString());

        String result = "";
        blob.base64 = true;

        while (blob.hasNextChunk()) {
            result += new String(blob.nextChunk());
        }

        System.out.println(result);
        System.out.println();
    }
}
