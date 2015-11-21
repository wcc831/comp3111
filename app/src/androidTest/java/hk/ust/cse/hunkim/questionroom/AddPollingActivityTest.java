package hk.ust.cse.hunkim.questionroom;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * Created by cc on 11/21/2015.
 */
public class AddPollingActivityTest extends ActivityInstrumentationTestCase2<AddPollingActivity> {

    AddPollingActivity activity;

    public AddPollingActivityTest() {
        super(AddPollingActivity.class);
    }

    @Override
    public void setUp() {
        activity = getActivity();
    }

    @SmallTest
    public void testStart() throws Exception {
    }
}
