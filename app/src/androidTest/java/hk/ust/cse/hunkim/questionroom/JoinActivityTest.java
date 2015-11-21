package hk.ust.cse.hunkim.questionroom;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * Created by hunkim on 7/15/15.
 * based on http://evgenii.com/blog/testing-activity-in-android-studio-tutorial-part-3/
 * and
 * http://developer.android.com/training/testing.html
 */
public class JoinActivityTest extends ActivityInstrumentationTestCase2<JoinActivity> {
    JoinActivity activity;
    TextView roomNameEditText;
    LinearLayout join;

    private static final int TIMEOUT_IN_MS = 5000;

    public JoinActivityTest() {
        super(JoinActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        activity = getActivity();

        roomNameEditText =
                (TextView) activity.findViewById(R.id.join_chatroom);

        join =
                (LinearLayout) activity.findViewById(R.id.search_layout);

    }

    public void testActivityLaunchProperly() {
        assertNotNull(activity);
    }


    @UiThreadTest
    public void testSearch() throws Exception {
        activity.onQueryTextChange("");

        activity.removeAdapter();
        activity.onQueryTextChange("sine");

    }

/*    @UiThreadTest
    public void testEnterByList() throws Exception {
        final View v = activity.findViewById(R.id.index_chatRoom);

        final CountDownLatch signal = new CountDownLatch(1);
        signal.await(10, TimeUnit.SECONDS);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.performClick();
            }
        });
    }*/


    @UiThreadTest
    public void testZEnterBySearch() throws Exception{

        activity.onQueryTextChange("NOENTERPLEASE");
        final View v = activity.findViewById(R.id.join_chatroom);
        assertNotNull(v);
        final CountDownLatch signal = new CountDownLatch(1);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.performClick();
            }
        });
        signal.await(2, TimeUnit.SECONDS);

        /*
        Toast.makeText(activity, "here", Toast.LENGTH_SHORT).show();
       // activity.onQueryTextChange("NOENTERPLEASE");
       // Espresso.onView(withId(R.id.join_chatroom)).perform(click());
        Espresso.onView(withId(R.id.messageInput)).perform(typeText("4"),
                closeSoftKeyboard());

        Espresso.onView(withId(R.id.sendButton)).perform(click());*/

    }

    @UiThreadTest
    public void testIllegalJoin() throws Exception {
        activity.onQueryTextChange(" ");
        final View v = activity.findViewById(R.id.join_chatroom);
        assertNotNull(v);
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

    @UiThreadTest
    public void testSetRole() {

        activity.setUser();
        activity.loadProfile((ImageView) activity.findViewById(R.id.drawer_profileImage),
                (TextView) activity.findViewById(R.id.drawer_profileEmail),
                (TextView) activity.findViewById(R.id.drawer_userRole));
    }
}
