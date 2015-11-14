package hk.ust.cse.hunkim.questionroom;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.File;
import java.util.Map;

import hk.ust.cse.hunkim.questionroom.login.GoogleLogin;
import hk.ust.cse.hunkim.questionroom.login.UserInfo;

public class LoginActivity extends Activity {

    public static Firebase firebaseRef;

    private static final String TAG = "LoginActivity";
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;

    UserInfo user = UserInfo.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //set status bar color
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.action_bar_red));

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Firebase.setAndroidContext(this);
        firebaseRef = new Firebase(MainActivity.FIREBASE_URL);

        ((EditText)findViewById(R.id.login_userName)).setText("test@gamil.com");
        ((EditText)findViewById(R.id.login_password)).setText("password");
    }

    public void login(View view) {

        Log.d(TAG, "logging user....");
        if (user.isAuthenticated()) //logged in
            return;

        findViewById(R.id.login_choose_loginProvider).setVisibility(View.INVISIBLE);
        findViewById(R.id.loging_loading).setVisibility(View.VISIBLE);

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
    }

    public void firebaseLogin(final String userName, final String password) {
        Log.d(TAG, "firebase login selected");
        firebaseRef.authWithPassword(userName, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                user.id = authData.getUid();
                user.email = userName;
                File root = new File(getFilesDir(), "instaquest");
                if (!root.exists())
                    if (!root.mkdir())
                        Log.d(TAG, "failed create directory");

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

                findViewById(R.id.loging_loading).setVisibility(View.INVISIBLE);
                findViewById(R.id.login_choose_loginProvider).setVisibility(View.VISIBLE);
                Toast.makeText(LoginActivity.this, "Failed to login. \n" + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void googleLogin(String email){
        UserInfo userInfo = UserInfo.getInstance();
        GoogleLogin login = new GoogleLogin(LoginActivity.this, firebaseRef, email, userInfo);
        login.exceptionCallback = new GoogleLogin.ExceptionHandler() {
            @Override
            public void handleException(final UserRecoverableAuthException e) {
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
            }
        };
        login.loginCallback = new GoogleLogin.GoogleLoginCallback() {
            @Override
            public void onLoginSucceed(AuthData userData, String token) {

            }

            @Override
            public void onLoginFailed(FirebaseError error) {

            }

            @Override
            public void onPictureReady() {
                UserInfo.getInstance().profileImage = new File(getFilesDir(), "google/googleProfile.jpg");
                loginSucceed();
            }
        };
        login.execute();
    }

    public void loginSucceed() {
        UserInfo user = UserInfo.getInstance();
        user.authenticate();

        proceed(null);
    }

    public void proceed(View view) {

        startActivity(new Intent(this, JoinActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        if (UserInfo.getInstance().isAuthenticated())
            finish();
    }

}
