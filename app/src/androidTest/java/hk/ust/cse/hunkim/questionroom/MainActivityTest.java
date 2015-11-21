package hk.ust.cse.hunkim.questionroom;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ActivityUnitTestCase;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by cc on 11/19/2015.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {


    MainActivity activity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        activity = getActivity();
    }

    @UiThreadTest
    public void testSearch() throws Exception {
        activity.onQueryTextChange("2");
        Thread.sleep(500);

        activity.onQueryTextChange("#test");
        Thread.sleep(500);
        activity.onQueryTextChange("@test");
        Thread.sleep(500);
    }

    @UiThreadTest
    public void testSnedMessageFail() throws Exception {
        ((TextView)activity.findViewById(R.id.messageInput)).setText("");
        final View v = activity.findViewById(R.id.sendButton);
        final CountDownLatch signal = new CountDownLatch(1);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.performClick();
            }
        });


        signal.await(2, TimeUnit.SECONDS);
    }

    @UiThreadTest
    public void testSnedMessageSucceed() throws Exception {
        ((TextView)activity.findViewById(R.id.messageInput)).setText("unit test message.");
        final View v = activity.findViewById(R.id.sendButton);
        final CountDownLatch signal = new CountDownLatch(1);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.performClick();
            }
        });


        signal.await(2, TimeUnit.SECONDS);
    }

    @UiThreadTest
    public void testHideMessage() throws Exception{

        final ImageView v = (ImageView)activity.findViewById(R.id.hide_message);
        assertNotNull(v);
        final CountDownLatch signal = new CountDownLatch(1);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.performClick();
            }
        });
        signal.await(2, TimeUnit.SECONDS);


    }

    @UiThreadTest
    public void testHideMessage2() throws Exception {

        final ImageView v = (ImageView)activity.findViewById(R.id.hide_message);
        activity.setUser(true);
        final CountDownLatch signal2 = new CountDownLatch(1);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.performClick();
            }
        });
        signal2.await(2, TimeUnit.SECONDS);
    }

    @SmallTest
    public void testOnResutl() throws Exception {
        activity.onActivityResult(MainActivity.REQUEST_IMAGE_CAPTURE, Activity.RESULT_CANCELED, null);

        activity.onActivityResult(123456, Activity.RESULT_CANCELED, null);
    }

    @UiThreadTest
    public void testZCapture() throws Exception {
        activity.startCamera(null);
    }
}
