package hk.ust.cse.hunkim.questionroom;

import android.app.ActionBar;
import android.app.Instrumentation;
import android.content.Intent;
import android.media.Image;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
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
    public void testSearch() throws Exception{

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
        signal.await(10, TimeUnit.SECONDS);


        /*
        Toast.makeText(activity, "here", Toast.LENGTH_SHORT).show();
       // activity.onQueryTextChange("NOENTERPLEASE");
       // Espresso.onView(withId(R.id.join_chatroom)).perform(click());
        Espresso.onView(withId(R.id.messageInput)).perform(typeText("4"),
                closeSoftKeyboard());

        Espresso.onView(withId(R.id.sendButton)).perform(click());*/

    }
}
