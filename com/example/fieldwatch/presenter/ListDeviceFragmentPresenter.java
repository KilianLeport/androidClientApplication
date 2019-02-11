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
import com.example.fieldwatch.view.ListDeviceFragmentInt;
import com.example.fieldwatch.view.ViewInt;

import java.util.List;
/**
 *  This class is the presenter of the ListDeviceFragment.
 *
 * @author Kilian Leport
 *
 */
public class ListDeviceFragmentPresenter implements ListDeviceFragmentPresenterInt {
    private ListDeviceFragmentInt mListDeviceFragmentInt;
    private ModelManagerInt mModelManagerInt;

    public ListDeviceFragmentPresenter(ModelManager model) {
        mModelManagerInt = model;
    }

    //*******************From view********************
    @Override
    public void onButtonReadClicked(byte address) {
        if (mModelManagerInt != null) {
            mModelManagerInt.readDevice(address);
        }
    }

    @Override
    public void onButtonActionClicked(byte address, byte action) {
        if (mModelManagerInt != null) {
            mModelManagerInt.actionDevice(address, action);
        }
    }

    @Override
    public void onButtonDeleteClicked(byte address) {
        if (mModelManagerInt != null) {
            mModelManagerInt.deleteDevice(address);
        }
    }

    @Override
    public void onButtonClearListClicked() {
        if (mModelManagerInt != null) {
            mModelManagerInt.clear();
        }
    }

    @Override
    public void onButtonAddDeviceClicked() {
        if (mListDeviceFragmentInt != null)
            mListDeviceFragmentInt.openDialogAddDevice();
    }

    @Override
    public void onButtonDialogAddDeviceClicked(String name, byte address, byte type) {
        if (mModelManagerInt != null) {
            mModelManagerInt.addModifyDevice(address, type, name);
        }
    }

    @Override
    public void onViewAttached(ViewInt view) {
        mListDeviceFragmentInt = (ListDeviceFragmentInt) view;
        mModelManagerInt.registerPresenter(this);
    }

    @Override
    public void onViewDetached() {
        if (mModelManagerInt != null)
            mModelManagerInt.unRegisterPresenter(this);
        mListDeviceFragmentInt = null;
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
        if (mListDeviceFragmentInt != null) {
            mListDeviceFragmentInt.onDeviceListChange(deviceList);
        }
    }

    @Override
    public void ackFromServer(int ackCode) {

    }
}
