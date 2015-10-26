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

    public Fragment[] tabs;

    public ChatroomPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        return tabs[position];
    }

    @Override
    public int getCount() {
        return tabs.length;
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
            case 3:
                return "Search Result";
        }
        return null;
    }

}
