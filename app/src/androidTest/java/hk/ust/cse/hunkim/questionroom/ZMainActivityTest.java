package hk.ust.cse.hunkim.questionroom;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ActivityUnitTestCase;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import hk.ust.cse.hunkim.questionroom.login.UserInfo;
import hk.ust.cse.hunkim.questionroom.polling.Polling;

/**
 * Created by cc on 11/19/2015.
 */
public class ZMainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {


    MainActivity activity;

    public ZMainActivityTest() {
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
        activity.onQueryTextChange("@unit");
        Thread.sleep(500);
        activity.onQueryTextChange("");
        Thread.sleep(500);
    }

    @UiThreadTest
    public void testLikeDislike() throws Exception{
        //activity.updateDislike("");
        activity.updateDislike("-K3dYQTp3hOzadl_n6rO");
        //activity.updateEcho("");
        activity.updateEcho("-K3dYQTp3hOzadl_n6rO");
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
    public void testXCapture() throws Exception {
        activity.startCamera(null);
    }

    @SmallTest
    public void testUserjson() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("picture", "test");
        obj.put("name", "test");
        obj.put("id", "test");
        obj.put("role", "0");
        obj.put("email", "test");
        obj.put("hideMessage", "test");
        UserInfo.getInstance().fromJson(obj.toString());


        JSONObject obj2 = new JSONObject();
        obj2.put("test", "test");
        UserInfo.getInstance().fromJson(obj2.toString());

    }

    @UiThreadTest
    public void testZQuestionActivity() throws Exception {
        Intent intent = new Intent(activity, QuestionActivity.class);
        intent.putExtra("room", "-K3djkApKzx7PvQPw-ps");
        intent.putExtra("key", "all");
        activity.startActivity(intent);
        Thread.sleep(500);
    }

    @UiThreadTest
    public void testAnimation() throws Exception {
        final ImageView textView = new ImageView(activity);

        textView.setOnTouchListener(Generic.getAnimateColorListener(0, 100));


        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.performClick();
            }
        });

    }

    @SmallTest
    public void testPolling() throws Exception {
        String[] str = new String[10];
        str[0] = "";

        for (int i = 1; i < 10; i++){
            str[i] = "test";
        }
        new Polling("test", str, 10);
    }

}
