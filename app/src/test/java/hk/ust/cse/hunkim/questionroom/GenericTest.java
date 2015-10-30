package hk.ust.cse.hunkim.questionroom;

import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by cc on 10/30/2015.
 */
public class GenericTest extends TestCase {

    protected void setup() throws Exception{
        super.setUp();
    }

    @SmallTest
    public void testInputStreamToString () {
        String source = "test string";
        InputStream in = new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));

        try {
            String str = Generic.inputStreamToString(in);
            assertEquals("string", source, str);
        }
        catch (IOException ioe){
            ioe.printStackTrace();
        }


    }
}
