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
package com.example.fieldwatch.model;

import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;
/**
 *  This class handle store the list of device.
 *
 * @author Kilian Leport
 *
 */
public class DeviceArray {
    private static final String TAG = "DeviceArray";
    private SparseArray<Device> deviceSparseArray;
    private OnListChangeListener mListener;

    @SuppressWarnings("WeakerAccess")
    public DeviceArray(OnListChangeListener listener) {
        deviceSparseArray = new SparseArray<>();
        mListener = listener;
        Log.d(TAG, "DeviceArray()");
    }

    public void addDevice(Device dev) {
        dev.setAge(System.currentTimeMillis());
        deviceSparseArray.append((int) dev.getAddress() & 0xff, dev);
        mListener.onListChange();
    }

    public void deleteDevice(int address) {
        if (hasDevice(address)) {
            deviceSparseArray.remove(address);
            mListener.onListChange();
        }
    }

    public Device getDevice(int address) {
        if (hasDevice(address))
            return deviceSparseArray.get(address);
        return null;
    }

    public List<Device> getListDevice() {
        if (deviceSparseArray == null)
            return null;
        List<Device> deviceList = new ArrayList<>(deviceSparseArray.size());
        for (int i = 0; i < deviceSparseArray.size(); i++)
            deviceList.add(deviceSparseArray.valueAt(i));
        return deviceList;
    }

    public boolean hasDevice(int address) {
        return (deviceSparseArray.get(address) != null);
    }

    public void clear() {
        deviceSparseArray.clear();
        mListener.onListChange();
    }

    //Listener
    public interface OnListChangeListener {
        void onListChange();
    }
}
