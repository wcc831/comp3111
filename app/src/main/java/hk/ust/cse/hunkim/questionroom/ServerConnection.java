package hk.ust.cse.hunkim.questionroom;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by cc on 10/9/2015.
 */
public class ServerConnection extends AsyncTask <String, Void, String > {

    public static final int UPLOAD = 0;
    public static final int DOWNLOAD = 1;

    public File uploadFile;

    private static final String TAG = "ServerConnection";
    private int responseCode;
    private String responseMSG;
    private InputStream is;

    private int action;

    public ServerConnection(int action){
        this.action = action;
    }

    @Override
    protected String doInBackground(String... params) {
        switch (action){
            case UPLOAD:
                try {
                    upload(new URL(params[0]));
                }
                catch (IOException e){
                    e.printStackTrace();
                }
                break;
            case DOWNLOAD:
                try {
                    download(new URL(params[0]));
                }
                catch (IOException e){
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String str){

    }

    public void download(URL url) throws IOException{
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setReadTimeout(10000);
        con.setConnectTimeout(15000);
        con.setRequestMethod("GET");
        con.setDoInput(true);
        con.connect();

        responseCode = con.getResponseCode();

        if (responseCode == 200) {
            is = con.getInputStream();
        }
    }

    public void upload(URL url) throws IOException {
        String endline = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";
        
        String fileName = Uri.fromFile(uploadFile).getLastPathSegment();

        String contentDisposition ="Content-Disposition: form-data; name=\"uploadedFile\"; filename=\"" + fileName + "\"" + endline;
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
        dataOS.write(Generic.fileToByteArray(uploadFile));
        //end content wraper
        dataOS.writeBytes(endline);
        dataOS.writeBytes(twoHyphens + boundary + endline);

        dataOS.flush();
        dataOS.close();

        //get response
        InputStream is = con.getInputStream();
        responseCode = con.getResponseCode();
        responseMSG = con.getResponseMessage();
        Log.d(TAG, "upload repsonse code: " + Integer.toString(responseCode) + "msg: " + responseMSG);

        String response = Generic.inputStreamToString(con.getInputStream());
        con.disconnect();
    }
}
