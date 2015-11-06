package hk.ust.cse.hunkim.questionroom.server;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import hk.ust.cse.hunkim.questionroom.Generic;

/**
 * Created by cc on 11/5/2015.
 */
public class ServerConfig {
    public interface ServerResultCallBack{
        void onResult(InputStream is);
    }

    private static final String TAG = "ServerConfig";
    public static final int UPLOAD = 0;
    public static final int DOWNLOAD = 1;
    public static final int EMAIL = 2;
    public static final int MKDIR = 3;
    public static final String SERVER_URL = "http://ec2-52-27-32-65.us-west-2.compute.amazonaws.com/fileservice/";
    public static final String SERVER_FILE_URL = "http://ec2-52-27-32-65.us-west-2.compute.amazonaws.com/files/public/";

    private ServerResultCallBack serverResultCallBack;

    private int responseCode;
    private String responseMSG;
    private InputStream is;
    private String result;
    private File file;
    private URL url;

    private int action = -1;

    public void uploadFile(File file, String dir) throws IOException {
        action = UPLOAD;
        this.file = file;

        this.url = new URL(SERVER_URL + "upload/" + dir + "/");
    }

    public void downloadFile(String dir, String fileName, File file) throws IOException{
        action = DOWNLOAD;

        this.file = file;
        this.url = new URL(SERVER_FILE_URL + dir + "/" + fileName);
    }

    public void sendEmail(int type) throws IOException{
        action = EMAIL;

        this.url = new URL(SERVER_URL + "email/");
    }

    public void mkDir(String dir) throws IOException{
        action = MKDIR;

        this.url = new URL(SERVER_URL + "mkdir/" + dir);
    }

    public void connect() {
        switch (action){
            case UPLOAD:
                try {
                    upload(this.url);
                }
                catch (IOException ioe){
                    ioe.printStackTrace();
                }
                break;
            case DOWNLOAD:
                try {
                    download(this.url);
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                break;
            case MKDIR:
                try {
                    mkDir(this.url);
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                break;
        }
    }

    public void setServerResultCallBack(ServerResultCallBack callBack){
        this.serverResultCallBack = callBack;
    }

    public void onResultReady() {


        if (serverResultCallBack != null) {
            serverResultCallBack.onResult(is);
            return;
        }

    }

    private void download(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setReadTimeout(10000);
        con.setConnectTimeout(15000);
        con.setRequestMethod("GET");
        con.setDoInput(true);
        con.connect();

        responseCode = con.getResponseCode();
        responseMSG = con.getResponseMessage();

        if (responseCode == 200) {
            is = con.getInputStream();
            Generic.saveInputStreamToFile(is, file);
        }
    }

    private void upload(URL url) throws IOException {
        String endline = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";

        String fileName = Uri.fromFile(file).getLastPathSegment();

        String contentDisposition ="Content-Disposition: form-data; name=\"fileToUpload\"; filename=\"" + fileName + "\"" + endline;
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setDoInput(true); // Allow Inputs
        con.setDoOutput(true); // Allow Outputs
        con.setUseCaches(false); // Don't use a Cached Copy
        con.setRequestMethod("POST");
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("ENCTYPE", "multipart/form-data");
        con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        con.setRequestProperty("uploaded_file", fileName);
        con.setRequestProperty("Cache-Control", "no-cache");

        DataOutputStream dataOS = new DataOutputStream(con.getOutputStream());
        //content wraper
        dataOS.writeBytes(twoHyphens + boundary + endline);
        dataOS.writeBytes(contentDisposition);
        dataOS.writeBytes(endline);
        //content
        dataOS.write(Generic.fileToByteArray(file));
        //end content wraper
        dataOS.writeBytes(endline);
        dataOS.writeBytes(twoHyphens + boundary + endline);

        dataOS.flush();
        dataOS.close();

        //get response
        responseCode = con.getResponseCode();
        responseMSG = con.getResponseMessage();

        if (responseCode == 200) {
            is = con.getInputStream();
        }
        con.disconnect();

        logInputStream();
    }

    private void mkDir(URL url) throws IOException {
        download(url);

        logInputStream();
    }

    private void saveFile(File dir, String fileName) {
        /*
        if (!dir.exists()){
            if (dir.mkdir())
                Log.d(TAG, dir.toString() + " created");

        }
        File file =  new File(dir, fileName);
        */
        Generic.saveInputStreamToFile(is, file);
    }

    public File getFile() { return file; }

    public void logUrlResponse() {
        Log.d(TAG, "repsonse code: " + Integer.toString(responseCode) + ", msg: " + responseMSG);
    }

    public void logInputStream() throws IOException {
        Log.d(TAG, "result: " + Generic.inputStreamToString(is));
    }
}
