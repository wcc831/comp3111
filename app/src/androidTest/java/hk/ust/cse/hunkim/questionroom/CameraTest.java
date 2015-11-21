package hk.ust.cse.hunkim.questionroom;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import hk.ust.cse.hunkim.questionroom.chatroom.ChatRoom;

/**
 * Created by cc on 11/21/2015.
 */
public class CameraTest extends ActivityInstrumentationTestCase2<CameraViewActivity> {

    CameraViewActivity activity;

    public CameraTest() { super(CameraViewActivity.class); }

    @Override
    public void setUp() {
        activity = getActivity();
    }

    @SmallTest
    public void testStart() throws Exception {}


}
