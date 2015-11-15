package hk.ust.cse.hunkim.questionroom.server;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import hk.ust.cse.hunkim.questionroom.Generic;

/**
 * Created by cc on 11/5/2015.
 */
public class ServerConfig {
    public interface ServerResultCallBack{
        void onResult(InputStream is, String result);
    }

    private static final String TAG = "ServerConfig";
    public static final int UPLOAD = 0;
    public static final int DOWNLOAD = 1;
    public static final int EMAIL = 2;
    public static final int MKDIR = 3;
    public static final int QUERY = 4;
    public static final int AUTH = 5;
    public static final String SERVER_URL = "http://ec2-52-27-32-65.us-west-2.compute.amazonaws.com/fileservice/";
    public static final String SERVER_FILE_URL = "http://ec2-52-27-32-65.us-west-2.compute.amazonaws.com/files/public/";

    private ServerResultCallBack serverResultCallBack;

    private int responseCode;
    private String responseMSG;
    private InputStream is;
    private String result;
    private File file;
    private URL url;

    String sender;
    String receiver;
    String receiverName;
    int type;

    private int action = -1;

    /**
     * Upload a file to server
     *
     * @param file The target file
     * @param dir The directory on server that the target file save
     */

    public void uploadFile(File file, String dir) throws IOException {
        action = UPLOAD;
        this.file = file;

        this.url = new URL(SERVER_URL + "upload/" + dir + "/");
    }
    /**
     * Download file from server
     *
     * @param dir The directory of the target file in server
     * @param fileName The name of the target file
     * @param file The file that the target file will be saved in
     */

    public void downloadFile(String dir, String fileName, File file) throws IOException{
        action = DOWNLOAD;

        this.file = file;
        this.url = new URL(SERVER_FILE_URL + dir + "/" + fileName);
    }

    /**
     * request server send an email
     *
     * @param sender The sender of the email.
     * @param receiver The email address of the receiver
     * @param receiverName The name of the receiver
     * @param type type of email, only type == 1 is available at the moment
     */

    public void sendEmail(String sender, String receiver, String receiverName, int type) throws IOException{
        action = EMAIL;

        this.sender = sender;
        this.receiver = receiver;
        this.receiverName = receiverName;
        this.type = type;
        this.url = new URL(SERVER_URL + "mail/");
    }

    public void mkDir(String dir) throws IOException{
        action = MKDIR;

        this.url = new URL(SERVER_URL + "mkdir/" + dir);
    }

    public void query(String dir) throws IOException{
        this.action = QUERY;

        this.url = new URL(SERVER_URL + "query/" + dir);
    }

    public void auth(String token) throws IOException {
        this.action = AUTH;

        this.url = new URL(SERVER_URL + "auth/" + token);
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
            case QUERY:
                try{
                    query(this.url);
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                break;
            case EMAIL:
                try{
                    email(this.url);
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                break;
            case AUTH:
                try {
                    auth(this.url);
                }
                catch (IOException ioe){
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
            serverResultCallBack.onResult(is, result);
            return;
        }

        if (action == DOWNLOAD)
            Generic.saveInputStreamToFile(is, file);
    }

    private void email(URL url) throws IOException {

        String data = URLEncoder.encode("sender", "UTF-8") + "=" + URLEncoder.encode(this.sender, "UTF-8") + "&" +
                URLEncoder.encode("receiver", "UTF-8") + "=" + URLEncoder.encode(this.receiver, "UTF-8") + "&" +
                URLEncoder.encode("receiverName", "UTF-8") + "=" + URLEncoder.encode(this.receiverName, "UTF-8") + "&" +
                URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode(Integer.toString(this.type), "UTF-8");


        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setReadTimeout(10000);
        con.setConnectTimeout(15000);
        con.setRequestMethod("POST");
        con.setDoInput(true);
        con.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
        con.setRequestProperty("Content-Language", "en-US");

        OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
        osw.write(data);
        osw.flush();
        osw.close();

        responseCode = con.getResponseCode();
        responseMSG = con.getResponseMessage();
        if (responseCode == 200){
            is = con.getInputStream();
            result = Generic.inputStreamToString(is);
            logUrlResponse();
            logResult();
        }

        con.disconnect();

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
            result = Generic.inputStreamToString(is);
            logResult();
        }
        con.disconnect();

    }

    private void mkDir(URL url) throws IOException {
        download(url);

        result = Generic.inputStreamToString(is);
        logResult();
    }



    private void query(URL url) throws IOException {
        download(url);

        result = Generic.inputStreamToString(is);
        logResult();
    }

    private void auth(URL url) throws IOException {
        download(url);

        result = Generic.inputStreamToString(is);
        logResult();
    }

    public File getFile() { return file; }

    public String getResult() { return result; }

    public void logUrlResponse() {
        Log.d(TAG, "repsonse code: " + Integer.toString(responseCode) + ", msg: " + responseMSG);
    }

    public void logInputStream() throws IOException {
        Log.d(TAG, "result: " + Generic.inputStreamToString(is));
    }

    public void logResult() {
        Log.d(TAG, "result: " + result);
    }
}
