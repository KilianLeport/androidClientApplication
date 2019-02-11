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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
/**
 *  This class handles events on the view pager.
 *
 * @author Kilian Leport
 *
 */
public class CustomViewPager extends ViewPager {
    private static final String TAG = "CustomViewPager";
    private boolean enabled = true;

    public CustomViewPager(@NonNull Context context) {
        super(context);
        Log.d(TAG, "CustomViewPager(@NonNull Context context)");
    }

    public CustomViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "CustomViewPager(@NonNull Context context, @Nullable AttributeSet attrs)");
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return enabled && (super.onInterceptTouchEvent(ev));
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        performClick();
        return enabled && (super.onTouchEvent(ev));
    }

    /**
     * Enable left and right swipe on the Pager
     *
     * @param enabled boolean to enable swipe
     */
    public void enableSwipe(boolean enabled) {
        this.enabled = enabled;

    }
}
