package hk.ust.cse.hunkim.questionroom.chatroom;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
                return "Search Result";
        }
        return null;
    }

}
