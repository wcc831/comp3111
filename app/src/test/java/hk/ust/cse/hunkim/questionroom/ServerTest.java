package hk.ust.cse.hunkim.questionroom;

import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.TestCase;

import hk.ust.cse.hunkim.questionroom.server.ServerConfig;
import hk.ust.cse.hunkim.questionroom.server.ServerConnection;

/**
 * Created by cc on 11/21/2015.
 */
public class ServerTest extends TestCase {

    @SmallTest
    public void testMail() throws Exception {
        ServerConfig config = new ServerConfig();
        config.sendEmail("", "", "", 0);
        ServerConnection con = new ServerConnection(config);
        con.execute();
        Thread.sleep(2000);
    }

    @SmallTest
    public void testAuth() throws Exception {
        ServerConfig config = new ServerConfig();
        config.auth("");
        ServerConnection con = new ServerConnection(config);
        con.execute();
    }

}
