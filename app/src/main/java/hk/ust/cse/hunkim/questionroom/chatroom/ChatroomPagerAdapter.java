package hk.ust.cse.hunkim.questionroom.chatroom;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;
import java.util.Locale;

/**
 * Created by cc on 10/25/2015.
 */
public class ChatroomPagerAdapter extends FragmentPagerAdapter {

    public Fragment[] tabs = new Fragment[3];

    public ChatroomPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return tabs[0];
            case 1:
                return tabs[1];
            case 2:
                return tabs[2];
        }

        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return "Recent Active";
            case 1:

                return "Favorite";
            case 2:

                return "Recent Visited";
        }
        return null;
    }

}
