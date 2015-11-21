package hk.ust.cse.hunkim.questionroom;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import hk.ust.cse.hunkim.questionroom.animation.AnimationFactory;
import hk.ust.cse.hunkim.questionroom.login.GoogleLogin;
import hk.ust.cse.hunkim.questionroom.login.UserInfo;

public class LoginActivity extends Activity {

    public static Firebase firebaseRef;

    private static final String TAG = "LoginActivity";
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;

    //facebook login manager
    CallbackManager callbackManager;

    UserInfo user = UserInfo.getInstance();

    File root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);

        //set status bar color
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.action_bar_red));

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        facebookLogin();

        root = new File(getFilesDir(), "instaquest");
        if (!root.exists())
            root.mkdir();


        Firebase.setAndroidContext(this);
        firebaseRef = new Firebase(MainActivity.FIREBASE_URL);

        AnimationFactory.fadeIn(findViewById(R.id.login_container), 1300);


        ((EditText)findViewById(R.id.login_userName)).setText("test@gamil.com");
        ((EditText)findViewById(R.id.login_password)).setText("password");
    }

    public void login(View view) {

        Log.d(TAG, "logging user....");

        AnimationFactory.crossFade(findViewById(R.id.login_choose_loginProvider), findViewById(R.id.loging_loading), 250);

        switch (view.getId()){
            case R.id.login_googleLogin:
                String[] accountTypes = new String[]{"com.google"};
                Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                        accountTypes, false, null, null, null, null);
                startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
                break;
            case R.id.login_firebase:
                firebaseLogin(((TextView) findViewById(R.id.login_userName)).getText().toString(),
                        ((TextView) findViewById(R.id.login_password)).getText().toString());
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //request google account
        if(requestCode == REQUEST_CODE_PICK_ACCOUNT){
            if (resultCode == RESULT_OK){
                user.email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                googleLogin(user.email);
                Log.d("google auth", user.email);
            }
            else if (resultCode == RESULT_CANCELED){
                Toast.makeText(LoginActivity.this, "canceled", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Log.d(TAG, "test");
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void firebaseLogin(final String userName, final String password) {
        Log.d(TAG, "firebase login selected");
        firebaseRef.authWithPassword(userName, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                user.id = authData.getUid();
                user.email = userName;


                try {
                    File firebseProfile = new File(root, "firebse_login.jpg");
                    Generic.bitmapToFile(BitmapFactory.decodeResource(getResources(), R.drawable.firebase_login),
                            firebseProfile, Bitmap.CompressFormat.JPEG);

                    UserInfo.getInstance().profileImage = firebseProfile;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "authenticated: " + user.id);
                loginSucceed();

            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {

                Log.d(TAG, "login failed" + firebaseError.getDetails() + ", " + firebaseError.getMessage());

                if (firebaseError.getCode() == FirebaseError.USER_DOES_NOT_EXIST) {
                    Log.d(TAG, "register new user");
                    firebaseRef.createUser(userName, password, new Firebase.ValueResultHandler<Map<String, Object>>() {

                        @Override
                        public void onSuccess(Map<String, Object> result) {
                            Log.d(TAG, "created new user: " + result.get("uid"));
                            firebaseLogin(userName, password);
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            Log.d(TAG, "register failed: " + firebaseError.getDetails() + ", " + firebaseError.getMessage());
                        }
                    });

                }

                loginFail("Failed to login. \n" + firebaseError.getMessage());
            }
        });

    }

    public void googleLogin(String email){
        UserInfo userInfo = UserInfo.getInstance();
        final GoogleLogin login = new GoogleLogin(LoginActivity.this, firebaseRef, email, userInfo);
        login.exceptionCallback = new GoogleLogin.ExceptionHandler() {
            @Override
            public void handleException(final UserRecoverableAuthException e) {

                /*
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (e instanceof GooglePlayServicesAvailabilityException) {
                            int statusCode = ((GooglePlayServicesAvailabilityException) e)
                                    .getConnectionStatusCode();
                            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                                    LoginActivity.this,
                                    REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                            dialog.show();
                        } else if (e instanceof UserRecoverableAuthException) {
                            Intent intent = ((UserRecoverableAuthException) e).getIntent();
                            startActivityForResult(intent,
                                    REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                        }
                    }
                });
                */

            }
        };
        login.loginCallback = new GoogleLogin.GoogleLoginCallback() {
            @Override
            public void onLoginSucceed(AuthData userData, String token) {

            }

            @Override
            public void onLoginFailed(FirebaseError error) {
                loginFail("fail to login" + error.getMessage());
            }

            @Override
            public void onPictureReady() {
                UserInfo.getInstance().profileImage = new File(getFilesDir(), "google/googleProfile.jpg");
                loginSucceed();
            }
        };
        login.execute();
    }

    public void facebookLogin() {
        //initialize facebook login
        LoginButton facebookLogin = (LoginButton) findViewById(R.id.login_facebookLogin);
        facebookLogin.setReadPermissions(Arrays.asList("basic_info", "email"));


        facebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                final String userId = loginResult.getAccessToken().getUserId();

                Log.d(TAG, "user id: " + userId
                        + ", token: " + loginResult.getAccessToken().getToken());


                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(final JSONObject object, GraphResponse response) {
                        Log.i("LoginActivity", response.toString());
                        // Get facebook data from login

                        new AsyncTask<String, String, String>() {

                            @Override
                            protected String doInBackground(String[] params) {

                                try {
                                    HttpURLConnection con = (HttpURLConnection) new URL("http://graph.facebook.com/" + userId + "/picture?type=large").openConnection();
                                    Log.d(TAG, "con message: " + con.getResponseMessage() + ", repsonse code:" + Integer.toString(con.getResponseCode()));

                                    con.setInstanceFollowRedirects(false);
                                    URL redirectedURL = new URL(con.getHeaderField("Location"));

                                    Bitmap userPic = BitmapFactory.decodeStream(redirectedURL.openConnection().getInputStream());
                                    File file = new File(root, "facebook_profile.jpg");
                                    Generic.bitmapToFile(userPic, file, Bitmap.CompressFormat.JPEG);
                                    UserInfo.getInstance().id = userId;
                                    UserInfo.getInstance().profileImage = file;
                                    UserInfo.getInstance().email = object.getString("last_name") + "@facebook.com";
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(String params) {
                                loginSucceed();
                            }
                        }.execute();

                        Log.d(TAG, object.toString());
                    }
                });
                Bundle params = new Bundle();
                params.putString("fields", "id, first_name, last_name, email, gender, birthday, location");
                request.setParameters(params);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "succeed");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, error.toString());
            }
        });

    }

    public void loginSucceed() {
        UserInfo user = UserInfo.getInstance();
        user.authenticate();
        setRole(user.id);

    }

    public void loginFail(String errorMessage) {
        AnimationFactory.crossFade(findViewById(R.id.loging_loading), findViewById(R.id.login_choose_loginProvider), 250);
        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();

    }

    public void proceed(View view) {

        if (view != null && view.getId() == R.id.login_proceed_as_visitor){
            try {
                user.profileImage = new File(root, "visitor.jpg");
                Generic.bitmapToFile(BitmapFactory.decodeResource(getResources(), R.drawable.no_profile), user.profileImage, Bitmap.CompressFormat.JPEG);
                user.email = "Anonymous";

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        startActivity(new Intent(this, JoinActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        if (UserInfo.getInstance().isAuthenticated())
            finish();
    }

    public void setRole(String key) {

        Log.d(TAG, user.toString());

        firebaseRef.child("users").child("teachingStaff").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {
                    UserInfo.getInstance().role = UserInfo.SUPERVISOR;
                    Log.d(TAG, "is supervisor");
                }
                else {
                    UserInfo.getInstance().role = UserInfo.NORMAL_USER;
                    Log.d(TAG, "is student");
                }
                proceed(null);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}
