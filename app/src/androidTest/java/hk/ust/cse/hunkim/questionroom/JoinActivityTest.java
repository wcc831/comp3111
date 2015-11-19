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

    protected Fragment waitForFragment(String tag, int timeout) {
        long endTime = SystemClock.uptimeMillis() + timeout;
        while (SystemClock.uptimeMillis() <= endTime) {

            Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(tag);
            if (fragment != null) {
                return fragment;
            }
        }
        return null;
    }

    public void testActivityLaunchProperly() {
        assertNotNull(activity);
    }

    public void testActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        assertNotNull(actionBar);

        String title = actionBar.getTitle().toString();
        assertEquals("title", title, "InstaQuest");

        SearchView searchView = (SearchView)activity.findViewById(R.id.action_search);
        assertNotNull(searchView);

        EditText editText = (EditText)searchView.findViewById(activity.getResources().getIdentifier("android:id/search_src_text", null, null));
        assertNotNull(editText);

    }

    public void testLayout() {

        ViewPager viewPager = (ViewPager) activity.findViewById(R.id.chatroom_list_pager);
        assertNotNull(viewPager);
        assertEquals("width", WindowManager.LayoutParams.FILL_PARENT, viewPager.getLayoutParams().width);
        assertEquals("height", WindowManager.LayoutParams.FILL_PARENT, viewPager.getLayoutParams().height);

        PagerTabStrip pagerTabStrip = (PagerTabStrip)activity.findViewById(R.id.chatroom_list_tab_strip);
        assertNotNull(pagerTabStrip);
        assertEquals("width", WindowManager.LayoutParams.WRAP_CONTENT, pagerTabStrip.getLayoutParams().width);
        assertEquals("height", (int) (30 * activity.getResources().getDisplayMetrics().density), pagerTabStrip.getLayoutParams().height);

    }

    @SmallTest
    public void testSearch() {
        activity.onQueryTextChange("");
    }


    /*
        @UiThreadTest
    public void testSearch() {

        activity.onQueryTextChange("");
        View view = activity.findViewById(R.id.join_chatroom);
        activity.onQueryTextChange("string");
    }
    * */

    @SmallTest
    public void testFragment() {
    }

    @SmallTest
    public void testJoin() {
        //activity.attemptJoin(activity.findViewById(R.id.index_chatRoom));
    }

    /*
    public void testIntentSetting() {

        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                roomNameEditText.requestFocus();
            }
        });

        getInstrumentation().sendStringSync("all");
        getInstrumentation().waitForIdleSync();

        String actualText = roomNameEditText.getText().toString();
        assertEquals("all", actualText);

        // Tap "Join" button
        // ----------------------

        TouchUtils.clickView(this, joinButton);
        //Wait until all events from the MainHandler's queue are processed
        getInstrumentation().waitForIdleSync();

        Intent intent = activity.getIntent();
        assertNotNull("Intent should be set", intent);

        assertEquals("all", intent.getStringExtra(LoginActivity.ROOM_NAME));
    }

*/
    public void ttestCreatingActivity() {

        //Create and add an ActivityMonitor to monitor interaction between the system and the
        //ReceiverActivity
        Instrumentation.ActivityMonitor receiverActivityMonitor = getInstrumentation()
                .addMonitor(MainActivity.class.getName(), null, false);

        //Request focus on the EditText field. This must be done on the UiThread because?
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                roomNameEditText.requestFocus();
            }
        });
        //Wait until all events from the MainHandler's queue are processed
        getInstrumentation().waitForIdleSync();

        //Send the room name
        getInstrumentation().sendStringSync("all");
        getInstrumentation().waitForIdleSync();

        //Click on the sendToReceiverButton to send the message to ReceiverActivity
        TouchUtils.clickView(this, join);

        //Wait until all events from the MainHandler's queue are processed
        getInstrumentation().waitForIdleSync();

        //Wait until MainActivity was launched and get a reference to it.
        MainActivity mainActivity = (MainActivity) receiverActivityMonitor
                .waitForActivityWithTimeout(TIMEOUT_IN_MS);

        //Verify that MainActivity was started
        assertNotNull("ReceiverActivity is null", mainActivity);
        assertEquals("Monitor for MainActivity has not been called", 1,
                receiverActivityMonitor.getHits());
        assertEquals("Activity is of wrong type", MainActivity.class,
                mainActivity.getClass());

        /*
        //Read the message received by ReceiverActivity
        final TextView receivedMessage = (TextView) mainActivity
                .findViewById(R.id.received_message_text_view);
        //Verify that received message is correct
        assertNotNull(receivedMessage);
        assertEquals("Wrong received message", TEST_MESSAGE, receivedMessage.getText().toString());
        */

        Intent intent = mainActivity.getIntent();
        assertNotNull("Intent should be set", intent);

        assertEquals("all", intent.getStringExtra(JoinActivity.ROOM_NAME));

        assertEquals("This is set correctly", "Room name: all", mainActivity.getTitle());

        //Unregister monitor for ReceiverActivity
        getInstrumentation().removeMonitor(receiverActivityMonitor);

    }
}
