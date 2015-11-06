package hk.ust.cse.hunkim.questionroom;

import android.app.Instrumentation;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.ActivityInstrumentationTestCase2;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import hk.ust.cse.hunkim.questionroom.server.ServerConfig;
import hk.ust.cse.hunkim.questionroom.server.ServerConnection;

/**
 * Created by cc on 11/5/2015.
 */
public class ServerConnectionTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public ServerConnectionTest() {super(MainActivity.class); }

    @Override
    protected void setUp() throws Exception {

    }

    //@SmallTest

    @SmallTest
    public void testUpload() throws Exception{

        ServerConfig config = new ServerConfig();
        File file = new File(this.getInstrumentation().getContext().getFilesDir(), "google/googleProfile.jpg");

        config.uploadFile(file, "testDir");
        ServerConnection con = new ServerConnection(config);
        con.execute();

    }

    @SmallTest
    public void testmkDir() throws Exception{
        ServerConfig config = new ServerConfig();
        config.mkDir("testDir");
        new ServerConnection(config).execute();
    }
}
