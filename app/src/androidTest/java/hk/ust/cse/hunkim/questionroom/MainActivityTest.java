package hk.ust.cse.hunkim.questionroom;

import android.test.ActivityInstrumentationTestCase2;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * Created by cc on 11/19/2015.
 */
public class MainActivityTest extends ActivityUnitTestCase<MainActivity> {


    MainActivity activity;

    public MainActivityTest() {
        super(MainActivity.class);
        setName("MainActivity");
    }

    @Override
    public void setUp() throws Exception {
        activity = getActivity();
    }

    @SmallTest
    public void testLaunch() throws Exception{
    }
}
