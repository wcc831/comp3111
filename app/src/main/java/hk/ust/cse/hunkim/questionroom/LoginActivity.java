package hk.ust.cse.hunkim.questionroom;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

import hk.ust.cse.hunkim.questionroom.login.GoogleLogin;
import hk.ust.cse.hunkim.questionroom.login.UserInfo;

public class LoginActivity extends Activity {

    public static Firebase firebaseRef;
    String userEmail = null;
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Firebase.setAndroidContext(this);
        firebaseRef = new Firebase(MainActivity.FIREBASE_URL);
    }

    public void login(View view) {

        if (userEmail != null) //logged in
            return;

        if (view.getId() == R.id.login_googleLogin) {
            view.setVisibility(View.INVISIBLE);
            findViewById(R.id.loging_google_loading).setVisibility(View.VISIBLE);
            String[] accountTypes = new String[]{"com.google"};
            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                    accountTypes, false, null, null, null, null);
            startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //request google account
        if(requestCode == REQUEST_CODE_PICK_ACCOUNT){
            if (resultCode == RESULT_OK){
                userEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                googleLogin(userEmail);
                Log.d("google auth", userEmail);
            }
            else if (resultCode == RESULT_CANCELED){
                Toast.makeText(LoginActivity.this, "canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void googleLogin(String email){
        UserInfo userInfo = new UserInfo();
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
                proceed(null);
            }
        };
        login.execute();
    }

    public void proceed(View view) {
        Intent intent = new Intent(this, JoinActivity.class);
        intent.putExtra("email", userEmail);



        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        if (userEmail != null)
            finish();
    }

}
