package hk.ust.cse.hunkim.questionroom;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.Button;
import android.widget.ImageView;

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

    @SmallTest
    public void testLaunch() throws Exception {
        assertTrue(true);
    }

    @MediumTest
    public void testGogoleLogin() throws Exception {
        activity.googleLogin("");
    }

    @UiThreadTest
    public void testLogin() {
        activity.login(activity.findViewById(R.id.login_googleLogin));


        activity.login(activity.findViewById(R.id.login_firebase));
    }

/*    @SmallTest
    public void testSetRole() throws Exception {
        activity.setRole("test");
    }*/

    @UiThreadTest
    public void testActivityForResult() {
        Intent returnIntent = new Intent();

        returnIntent.putExtra("result", "");
        Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, returnIntent);
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(LoginActivity.class.getName(), activityResult , true);
        final ImageView button = (ImageView) activity.findViewById(R.id.login_googleLogin);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.performClick();
            }
        });

        Activity loginActivity =  getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5);


        activity.onActivityResult(0, 0, null);

        activity.onActivityResult(1000, Activity.RESULT_CANCELED, null);

        activity.onActivityResult(1000, -1593, null);

        Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, "wcc831@gmail.com");
        activity.onActivityResult(1000, Activity.RESULT_OK, intent);
    }

    @SmallTest
    public void testProceed() {
        activity.proceed(null);

        UserInfo.getInstance().authenticate();
        activity.proceed(null);

        //activity.proceed(activity.findViewById(R.id.login_proceed_as_visitor));
    }

}
