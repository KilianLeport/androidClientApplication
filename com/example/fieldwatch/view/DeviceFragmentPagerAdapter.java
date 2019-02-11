/**
 * Monitoring Program
 * Copyright (C) 2018 Kilian Leport
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.example.fieldwatch.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
/**
 *  This class handles the different pages of the  CustomViewPager.
 *
 * @author Kilian Leport
 *
 */
public class DeviceFragmentPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = "DeviceFragPagerAdapter";
    private static final int NUMBER_OF_PAGES = 2;

    @SuppressWarnings("WeakerAccess")
    public DeviceFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        Log.d(TAG, "DeviceFragmentPagerAdapter(FragmentManager fm)");
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new ListDeviceFragment();
            case 1:
                return new FieldFragment();
            default:
                return new ListDeviceFragment();
        }
    }

    @Override
    public int getCount() {
        return NUMBER_OF_PAGES;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return super.getItemPosition(object);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "List";
            case 1:
                return "Field";
            default:
                return "list";

        }
    }
}
