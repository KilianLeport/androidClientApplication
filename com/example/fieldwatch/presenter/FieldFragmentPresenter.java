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
package com.example.fieldwatch.presenter;

import com.example.fieldwatch.model.Device;
import com.example.fieldwatch.model.ModelManager;
import com.example.fieldwatch.model.ModelManagerInt;
import com.example.fieldwatch.view.FieldFragmentInt;
import com.example.fieldwatch.view.ViewInt;

import java.util.List;
/**
 *  This class is the presenter of the FieldFragment.
 *
 * @author Kilian Leport
 *
 */
public class FieldFragmentPresenter implements FieldFragmentPresenterInt {
    private FieldFragmentInt mFieldFragmentInt;
    private ModelManagerInt mModelManagerInt;

    public FieldFragmentPresenter(ModelManager model) {
        mModelManagerInt = model;
    }

    //*******************From model********************
    @Override
    public void serverInformationChanged(String serverIP, int port) {

    }

    @Override
    public void connectionStateChanged(int statusServerDiscovered, int statusTCPConnection) {

    }

    @Override
    public void onDeviceListChange(List<Device> deviceList) {
        if (mFieldFragmentInt != null)
            mFieldFragmentInt.onDeviceListChange(deviceList);
    }

    @Override
    public void onViewAttached(ViewInt view) {
        mFieldFragmentInt = (FieldFragmentInt) view;
        mModelManagerInt.registerPresenter(this);
    }

    @Override
    public void onViewDetached() {
        if (mModelManagerInt != null)
            mModelManagerInt.unRegisterPresenter(this);
        mFieldFragmentInt = null;
    }

    @Override
    public void ackFromServer(int ackCode) {
        //do nothing
    }

    //*******************From view********************
    @Override
    public void onButtonActualizeClicked() {
        if (mModelManagerInt != null) {
            mModelManagerInt.readDevice(Device.BROADCAST_ADDRESS);
        }
    }

    @Override
    public void onButtonClearListClicked() {
        if (mModelManagerInt != null) {
            mModelManagerInt.clear();
        }
    }

    @Override
    public void onButtonEditViewClicked(boolean checked) {
        if (mFieldFragmentInt != null)
            mFieldFragmentInt.enableFieldEdition(checked);
    }

    @Override
    public void onDeviceMoved(byte address, int newCoordinateX, int newCoordinateY) {
        if (mModelManagerInt != null) {
            mModelManagerInt.modifyCoordinateDevice(address, newCoordinateX, newCoordinateY);
            //actualize can be doublon with the handler of ack in ModelManager.java
            mModelManagerInt.readDevice(address);
        }
    }
}
