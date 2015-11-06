package hk.ust.cse.hunkim.questionroom.server;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import hk.ust.cse.hunkim.questionroom.Generic;

/**
 * Created by cc on 10/9/2015.
 */
public class ServerConnection extends AsyncTask <String, Void, String > {

    private static final String TAG = "ServerConnection";

    private ServerConfig config;

    public ServerConnection(ServerConfig config) {
        this.config = config;
    }

    @Override
    protected String doInBackground(String... params) {

        config.connect();
        return null;
    }

    @Override
    protected void onPostExecute(String str){
        config.onResultReady();
    }


}
