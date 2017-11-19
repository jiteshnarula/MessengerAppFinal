package com.example.jiteshnarula.bakbak;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiteshnarula on 11-11-2017.
 */

class SectionPagerAdapter extends FragmentPagerAdapter {

    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

    switch(position){
        case 0:
            RequestFragment requestFragment = new RequestFragment();
            return requestFragment;

        case 1:
            ChatFragment chatFragment = new ChatFragment();
            return chatFragment;

        case 2:
            FriendsFragment friendsFragment = new FriendsFragment();
            return friendsFragment;


        default:
            return null;

        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){
        switch(position){
            case 0 :
                return "Requests";

            case 1:
                return "Chat";

            case 2:
                return "Friends";

            default:
                return null;
        }
    }
}
