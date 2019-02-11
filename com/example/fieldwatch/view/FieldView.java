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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.fieldwatch.model.Device;
import com.example.fieldwatch.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
/**
 *  This class handles devices in a schematic view that is included in the
 *  FieldFragment.
 *
 * @author Kilian Leport
 *
 */
public class FieldView extends View {
    private static final String TAG = "FieldView";
    private final int COLOR_FIELD_VIEW_DEVICE_ON;
    private final int COLOR_FIELD_VIEW_DEVICE_OFF;
    private final int COLOR_FIELD_VIEW_DEVICE_UNKNOWN;
    private final int COLOR_FIELD_VIEW_TEXT;
    private final float RADIUS_PERCENTAGE = 1.2f;
    List<Device> mDeviceList;

    private Device deviceSelected;
    private boolean editable = true;
    private OnFieldViewEvent mListener;
    private int newCoordinateX = 0;
    private int newCoordinateY = 0;

    private TimerTask timerTask;
    private final Handler handler = new Handler();
    private static final int interval_time = 1000;

    public FieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        COLOR_FIELD_VIEW_DEVICE_ON = ContextCompat.getColor(context.getApplicationContext(), R.color.color_field_view_device_ON);
        COLOR_FIELD_VIEW_DEVICE_OFF = ContextCompat.getColor(context.getApplicationContext(), R.color.color_field_view_device_OFF);
        COLOR_FIELD_VIEW_DEVICE_UNKNOWN = ContextCompat.getColor(context.getApplicationContext(), R.color.color_field_view_device_Unknown);
        COLOR_FIELD_VIEW_TEXT = ContextCompat.getColor(context.getApplicationContext(), R.color.color_field_view_text);
        mDeviceList = new ArrayList<>();

        Timer timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 0, interval_time);
        Log.d(TAG, "FieldView(Context context, AttributeSet attrs)");
    }

    public void setEditable(boolean enable) {
        editable = enable;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        if (editable) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    deviceSelected = deviceAtCoordinate(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (deviceSelected != null) {
                        if ((newCoordinateX != getDeviceCoordinateX((int) event.getX()) || newCoordinateY != getDeviceCoordinateY((int) event.getY()))) {
                            if (Device.isCoordinateXValid(getDeviceCoordinateX((int) event.getX())))
                                newCoordinateX = getDeviceCoordinateX((int) event.getX());
                            if (Device.isCoordinateYValid(getDeviceCoordinateY((int) event.getY())))
                                newCoordinateY = getDeviceCoordinateY((int) event.getY());
                            if (deviceSelected != null
                                    && (Device.isCoordinateXValid(getDeviceCoordinateX((int) event.getX()))
                                    || Device.isCoordinateYValid(getDeviceCoordinateY((int) event.getY())))) {
                                deviceSelected.setCoordinateX(newCoordinateX);
                                deviceSelected.setCoordinateY(newCoordinateY);
                                invalidate();
                            }
                        }
                    }
                    break;
                default:
                    if (deviceSelected != null && mListener != null) {
                        mListener.onDeviceMoved(deviceSelected.getAddress(), newCoordinateX, newCoordinateY);
                        deviceSelected = null;
                        invalidate();
                    }
                    break;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < mDeviceList.size(); i++)
            drawDevice(canvas, mDeviceList.get(i));
        super.onDraw(canvas);
    }

    private void drawDevice(Canvas canvas, Device dev) {
        if (Device.isCoordinateXValid(dev.getCoordinateX()) && Device.isCoordinateYValid(dev.getCoordinateY())) {
            CoordinateDevice coordinate = new CoordinateDevice();
            getCoordinateDevice(coordinate, dev);

            Paint paintCircle = new Paint();
            if (dev.getState() == Device.StateCode.On)
                paintCircle.setColor(COLOR_FIELD_VIEW_DEVICE_ON);
            else
                paintCircle.setColor(COLOR_FIELD_VIEW_DEVICE_OFF);

            if ((System.currentTimeMillis() - dev.getAge()) > 30000)
                paintCircle.setColor(COLOR_FIELD_VIEW_DEVICE_UNKNOWN);

            Paint paintEmptyCircle = new Paint();
            paintEmptyCircle.setStyle(Paint.Style.STROKE);
            float width = getRadiusDevice() / 6;
            paintEmptyCircle.setStrokeWidth(width);

            Paint paintText = new Paint();
            paintText.setColor(COLOR_FIELD_VIEW_TEXT);
            paintText.setTextSize(getRadiusDevice() * 2);
            if (deviceSelected != null)
                if (deviceSelected.getAddress() == dev.getAddress())
                    paintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));


            float textLength = paintText.measureText(dev.getName());
            float textPositionX = coordinate.x + getRadiusDevice() + getMargin();
            float textPositionY = coordinate.y + getRadiusDevice();
            if (coordinate.x > (getMeasuredWidth() - getRadiusDevice() - textLength)) {
                textPositionX = coordinate.x - getRadiusDevice() - getMargin() - textLength;
            }
            canvas.drawCircle(coordinate.x, coordinate.y, getRadiusDevice(), paintCircle);
            canvas.drawCircle(coordinate.x, coordinate.y, getRadiusDevice(), paintEmptyCircle);
            canvas.drawText(dev.getName(), textPositionX, textPositionY, paintText);
        }
    }

    /**
     * CallBack, list of device changed.
     *
     * @param deviceList the new list of devices
     */
    public void onDeviceListChange(List<Device> deviceList) {
        this.mDeviceList = deviceList;
        invalidate();
    }

    private void getCoordinateDevice(CoordinateDevice coor, Device dev) {
        int positionX = dev.getCoordinateX();
        int positionY = dev.getCoordinateY();
        float finalPositionX = getLocalCoordinateX(positionX);
        float finalPositionY = getLocalCoordinateY(positionY);

        if (finalPositionX < getRadiusDevice())
            finalPositionX = getRadiusDevice() + getMargin();
        if (finalPositionY < getRadiusDevice())
            finalPositionY = getRadiusDevice() + getMargin();
        if (finalPositionX > (getMeasuredWidth() - getRadiusDevice())) {
            finalPositionX = (getMeasuredWidth() - getRadiusDevice() - getMargin());
        }
        if (finalPositionY > (getMeasuredHeight() - getRadiusDevice())) {
            finalPositionY = (getMeasuredHeight() - getRadiusDevice() - getMargin());
        }
        coor.x = finalPositionX;
        coor.y = finalPositionY;
    }

    private Device deviceAtCoordinate(float x, float y) {
        for (int i = 0; i < mDeviceList.size(); i++) {
            CoordinateDevice coordinate = new CoordinateDevice();
            getCoordinateDevice(coordinate, mDeviceList.get(i));
            float d = (float) Math.sqrt(Math.pow(Math.abs(coordinate.x - x), 2) + Math.pow(Math.abs(coordinate.y - y), 2));
            if (d <= getRadiusDevice() * 3)
                return mDeviceList.get(i);
        }
        return null;
    }


    private int getLocalCoordinateX(int x) {
        return (getMeasuredWidth() * x) / Device.MAXIMUM_COORDINATE_X;
    }

    private int getLocalCoordinateY(int y) {
        return (getMeasuredHeight() * y) / Device.MAXIMUM_COORDINATE_Y;
    }

    private int getDeviceCoordinateX(int x) {
        return (Device.MAXIMUM_COORDINATE_X * x) / getMeasuredWidth();
    }

    private int getDeviceCoordinateY(int y) {
        return (Device.MAXIMUM_COORDINATE_Y * y) / getMeasuredHeight();
    }

    public float getRadiusDevice() {
        double r;
        r = Math.sqrt(Math.pow(getMeasuredWidth(), 2) + Math.pow(getMeasuredHeight(), 2));
        r = RADIUS_PERCENTAGE * (r / 100);
        return (float) r;
    }

    public float getMargin() {
        return getRadiusDevice() / 2;
    }

    private class CoordinateDevice {
        private float x;
        private float y;
    }

    public interface OnFieldViewEvent {
        /**
         * Device with address specified by address is moved at new coordinates.
         *
         * @param address        Address of the device concerned
         * @param newCoordinateX New coordinate X
         * @param newCoordinateY New coordinate Y
         */
        void onDeviceMoved(byte address, int newCoordinateX, int newCoordinateY);
    }

    /**
     * Add the listener.
     *
     * @param l Listener to add
     */
    public void addOnMessageReceivedListener(OnFieldViewEvent l) {
        mListener = l;
    }

    /**
     * Remove the listener
     */
    public void removeOnMessageReceivedListener() {
        mListener = null;
    }

    /**
     * Lunch timer task to redraw periodically the view.
     */
    private void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        invalidate();
                    }
                });
            }
        };
    }
}
