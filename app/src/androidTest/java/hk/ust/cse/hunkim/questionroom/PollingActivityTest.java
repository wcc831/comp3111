package hk.ust.cse.hunkim.questionroom;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import hk.ust.cse.hunkim.questionroom.polling.Polling;

/**
 * Created by cc on 11/21/2015.
 */
public class PollingActivityTest extends ActivityInstrumentationTestCase2<PollingActivity> {

    PollingActivity activity;

    public PollingActivityTest() {
        super(PollingActivity.class);
    }

    @Override
    public void setUp() {
        activity = getActivity();
    }

    @SmallTest
    public void testStart() throws Exception {

    }
}
