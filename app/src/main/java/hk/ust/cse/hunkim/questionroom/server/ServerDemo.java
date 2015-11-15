package hk.ust.cse.hunkim.questionroom.server;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import hk.ust.cse.hunkim.questionroom.Generic;

/**
 * Created by cc on 11/5/2015.
 */
public class ServerDemo {


    /*
    * to user the ServerConnection and ServerConfig class is simple, the config will save the
    * request of the connection, then put it in a ServerConnection and call execute.
    * */

    public static void mkDir() {
        /*
        * consider the mkdir function, simply instantiate a ServerConfig class,
        * set to send mkdir command to server by invoking the config.mkdir function with the name of the
        * directory you want to create, then the config is ready.
        *
        * put it into the ServerConnection and call execute() tho execute the url request
        * */
        final ServerConfig config = new ServerConfig();
        try {
            config.mkDir("testDir2");
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        new ServerConnection(config).execute();
    }

    public static void upload (File file) {

        /*
        * uploading file is as simple as making directory
        * just cast the file as a File type,
        * then upt that file along with the directory the you want it be saved as the paramater
        * of the config.uploadfile() function
        * */

        final ServerConfig config = new ServerConfig();

        try {
            config.uploadFile(file, "testDir");
            ServerConnection con = new ServerConnection(config);
            con.execute();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void download() {

        /*
        * download a file need to give three parameter, the first is the directory of the file in server
        * second is the filename and last to directory you want the file saved
        * */

        try {
            final File dir = new File(Environment.getExternalStorageDirectory().toString() + "/test_folder");
            final String fileName = "test.jpg";

            final ServerConfig config = new ServerConfig();
            config.downloadFile("testDir", "googleProfile.jpg", new File(dir, fileName));

            /*
            * when can implement the serverResultCallback to make the serverConnection perform
            * some action after the connection is finished
            * */

            config.setServerResultCallBack(new ServerConfig.ServerResultCallBack() {
                @Override
                public void onResult(InputStream is, String str) {

                    config.logUrlResponse();

                }
            });
            new ServerConnection(config).execute();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }

    }


    public static void email() {
        try{
            ServerConfig config = new ServerConfig();
            config.sendEmail("DA Yang", "ccwongam@connect.ust.hk", "ccw", 1);
            new ServerConnection(config).execute();

        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void test (Context context) {
        ServerDemo.upload(new File(context.getFilesDir(), "google/googleProfile.jpg"));
        ServerDemo.download();
    }

}
