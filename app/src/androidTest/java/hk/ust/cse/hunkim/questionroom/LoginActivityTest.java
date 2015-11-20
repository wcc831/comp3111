package hk.ust.cse.hunkim.questionroom;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.widget.TextView;


import org.w3c.dom.Text;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import hk.ust.cse.hunkim.questionroom.login.UserInfo;

/**
 * Created by cc on 11/18/2015.
 */
public class LoginActivityTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    LoginActivity activity;

    public LoginActivityTest() {
        super(LoginActivity.class);
    }

    @Override
    public void setUp() {
        activity = getActivity();
    }

    @UiThreadTest
    public void testGogoleLogin() throws Exception {

        final View v = activity.findViewById(R.id.login_googleLogin);

        final CountDownLatch signal = new CountDownLatch(1);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.performClick();
            }
        });

        signal.await(10, TimeUnit.SECONDS);

    }

    @UiThreadTest
    public void testProceedWithoutLogin() throws Exception {
        final View v = activity.findViewById(R.id.login_proceed_as_visitor);
        final CountDownLatch signal = new CountDownLatch(1);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.performClick();
            }
        });
        signal.await(10, TimeUnit.SECONDS);

    }

    @UiThreadTest
    public void testFirebaseLogin() throws Exception {

        final View v = activity.findViewById(R.id.login_firebase);


        ((TextView)activity.findViewById(R.id.login_password)).setText("wrongPassword");
        final CountDownLatch signal2 = new CountDownLatch(1);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.performClick();
            }
        });

        signal2.await(15, TimeUnit.SECONDS);

        ((TextView)activity.findViewById(R.id.login_password)).setText("password");
        final CountDownLatch signal = new CountDownLatch(1);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.performClick();
            }
        });

        signal.await(15, TimeUnit.SECONDS);

    }

    @UiThreadTest
    public void testProceedAsVisitor() throws Exception {
        final View v = activity.findViewById(R.id.login_proceed_as_visitor);
        final CountDownLatch signal = new CountDownLatch(1);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.performClick();
            }
        });


        signal.await(15, TimeUnit.SECONDS);
    }

    @UiThreadTest
    public void testOnResultFail() throws Exception{
        activity.onActivityResult(LoginActivity.REQUEST_CODE_PICK_ACCOUNT, 123456, null);

        Thread.sleep(10000);
        activity.onActivityResult(LoginActivity.REQUEST_CODE_PICK_ACCOUNT, Activity.RESULT_CANCELED, null);

        Thread.sleep(10000);

        Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, "wcc831@gmail.com");
        activity.onActivityResult(LoginActivity.REQUEST_CODE_PICK_ACCOUNT, Activity.RESULT_OK, intent);

        Thread.sleep(15000);
    }


    @UiThreadTest
    public void testLoginWithInvalidView() throws Exception {
        activity.login(activity.findViewById(R.id.login_password));
    }

    @SmallTest
    public void testSetRoleNotSupervior() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        activity.setRole("someUnknownKey");


        signal.await(15, TimeUnit.SECONDS);
    }

    @UiThreadTest
    public void testProceedNull() throws Exception {
        activity.proceed(null);

        activity.proceed(activity.findViewById(R.id.login_password));
    }
}
