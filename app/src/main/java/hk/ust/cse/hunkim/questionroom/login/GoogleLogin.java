package hk.ust.cse.hunkim.questionroom.login;

import android.app.ActionBar;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import hk.ust.cse.hunkim.questionroom.Generic;
import hk.ust.cse.hunkim.questionroom.JoinActivity;
import hk.ust.cse.hunkim.questionroom.R;

/**
 * Created by cc on 9/26/2015.
 */
public class GoogleLogin extends AsyncTask<Void , Void , Void> {

    public interface GoogleLoginCallback{
        void onLoginSucceed(AuthData userData, String token);
        void onLoginFailed(FirebaseError error);
        void onPictureReady();
    }

    public interface ExceptionHandler{
       void handleException(UserRecoverableAuthException usrae);
    }

    private static final String TAG = "GoogleLogin";
    private static final String getUserInfoUrl = "https://www.googleapis.com/oauth2/v1/userinfo";
    private static final String getUserTokenUrl = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

    public GoogleLoginCallback LoginCallback = null;
    public ExceptionHandler ExceptionCallback = null;

    private Activity mActivity;
    private String userEmail = null;
    private Firebase mFirebaseRef = null;
    private UserInfo mUserInfo = null;

    public GoogleLogin(Activity activity, Firebase firebase, String email, UserInfo userInfo){
        this.mActivity = activity;
        this.mFirebaseRef = firebase;
        this.userEmail = email;
        this.mUserInfo = userInfo;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            final String token = fetchToken();
            Log.d(TAG, token);
            mFirebaseRef.authWithOAuthToken("google", token, new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    Log.d(TAG, "auth succeed");

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            /*
                            Button loginButton = (Button) mActivity.findViewById(R.id.login_button);
                            loginButton.setId(R.id.logout_button);
                            loginButton.setText("LOG OUT");

                            ((ViewGroup)loginButton.getParent()).removeView(loginButton);

                            Button logoutButton = new Button(mActivity);
                            logoutButton.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));
                            */
                        }
                    });


                    try {
                        run(token);

                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    if (LoginCallback != null)
                        LoginCallback.onLoginFailed(firebaseError);
                }
            });

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void param){
        Log.d(TAG, mUserInfo.toString());
    }

    protected String run(final String token) throws IOException{

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            String json;
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    json = getUserInfo(new URL(getUserInfoUrl + "?access_token=" + token));
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(final Void param){
                AsyncTask<String, String, String> task = new AsyncTask<String, String, String>() {

                    @Override
                    protected String doInBackground(String... params) {
                        try {
                            saveUserProfileImage(json);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        return null;
                    }
                };

                try {
                    UserInfo userInfo = new UserInfo();
                    userInfo.fromJson(json);
                    task.execute(userInfo.pictureUrl);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        task.execute();

        return null;
    }

    protected String getUserInfo (URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setReadTimeout(10000);
        con.setConnectTimeout(15000);
        con.setRequestMethod("GET");
        con.setDoInput(true);
        con.connect();

        if (con.getResponseCode() == 200) {
            String json = Generic.inputStreamToString(con.getInputStream());
            Log.d(TAG, json);
            return json;
        }
        else
            Log.d(TAG, "connection failed");

        return null;
    }

    protected void saveUserProfileImage(String json) throws IOException{
        AsyncTask<String, String, String> task = new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setReadTimeout(10000);
                    con.setConnectTimeout(15000);
                    con.setRequestMethod("GET");
                    con.setDoInput(true);
                    con.connect();

                    if (con.getResponseCode() == 200) {
                        File googleProfileDir = new File(mActivity.getFilesDir(), "google");
                        if (!googleProfileDir.exists() && !googleProfileDir.mkdir())
                            Log.d(TAG, "directory not exist.");
                        else
                            Log.d(TAG, "folder created");

                        Generic.saveInputStreamToFile(con.getInputStream(), new File(googleProfileDir, "googleProfile.jpg"));
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LoginCallback.onPictureReady();
                            }
                        });
                    }
                    else
                        Log.d(TAG, "retrieve failed");
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }
        };

        try {
            UserInfo userInfo = new UserInfo();
            userInfo.fromJson(json);
            task.execute(userInfo.pictureUrl);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    protected String fetchToken() throws IOException {
        try {
            Log.d(TAG, "retrieving token....");
            return GoogleAuthUtil.getToken(mActivity, userEmail, getUserTokenUrl);
        }
        catch (UserRecoverableAuthException usrae) {
            if (ExceptionCallback != null) {
                ExceptionCallback.handleException(usrae);
            }
            else {
                usrae.printStackTrace();
            }
        }
        catch (GoogleAuthException fatalException) {
            fatalException.printStackTrace();
        }
        return null;
    }
}
