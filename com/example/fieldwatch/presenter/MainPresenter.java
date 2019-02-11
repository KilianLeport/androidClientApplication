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
import com.example.fieldwatch.model.ServerScanner;
import com.example.fieldwatch.model.TCPConnectionHandler;
import com.example.fieldwatch.view.MainActivityInt;
import com.example.fieldwatch.view.ViewInt;

import java.util.List;
/**
 *  This class is the presenter of the MainActivity.
 *
 * @author Kilian Leport
 *
 */
public class MainPresenter implements MainPresenterInt {
    private MainActivityInt mMainActivityInterface;
    private ModelManagerInt mModelManagerInt;

    public MainPresenter(ModelManager model) {
        mModelManagerInt = model;
    }

    //*******************From view********************
    @Override
    public void onToggleButtonConnectionClicked() {
        if (mMainActivityInterface != null && mModelManagerInt != null) {
            mMainActivityInterface.setConnectionButton("pending", false);
            mModelManagerInt.toggleConnectionToServer();
        }
    }

    @Override
    public void onSwitchAutomaticConnectionClicked(boolean checked) {
        if (mModelManagerInt != null) {
            mModelManagerInt.enableAutomaticConnection(checked);
        }
    }

    @Override
    public void onSwitchAutomaticUpdateClicked(boolean checked) {
        if (mModelManagerInt != null) {
            mModelManagerInt.enableAutomaticUpdate(checked);
        }
    }

    @Override
    public void onViewAttached(ViewInt view) {
        mMainActivityInterface = (MainActivityInt) view;
        mMainActivityInterface.setColorToolBar(mMainActivityInterface.getColorNotConnected());

        mModelManagerInt.registerPresenter(this);
        mMainActivityInterface.setConnectionButton("pending", false);
        mModelManagerInt.connectToServer();
    }

    @Override
    public void onViewDetached() {
        if (mModelManagerInt != null) {
            mModelManagerInt.disconnectToServer();
            mModelManagerInt.unRegisterPresenter(this);
        }
        mMainActivityInterface = null;
    }

    //*************************From Model*************************
    @Override
    public void serverInformationChanged(String serverIP, int port) {
        if (mMainActivityInterface != null) {
            mMainActivityInterface.setAddressServerInformation(serverIP);
            mMainActivityInterface.setPortServerInformation(String.valueOf(port));
        }
    }

    @Override
    public void connectionStateChanged(int statusServerDiscovered, int statusTCPConnection) {
        if (mMainActivityInterface != null) {
            if (statusServerDiscovered == ServerScanner.SERVER_NOT_FOUND) {
                mMainActivityInterface.setWriteInformation("Server not found", false);
                mMainActivityInterface.setConnectionButton("Connection", true);
            }
            if (statusTCPConnection == TCPConnectionHandler.CONNECTED) {
                mMainActivityInterface.setConnectionButton("Disconnection", true);
                mMainActivityInterface.setColorToolBar(mMainActivityInterface.getColorConnected());

                if (mModelManagerInt != null)
                    mModelManagerInt.readDevice(Device.BROADCAST_ADDRESS);
            }
            if (statusTCPConnection == TCPConnectionHandler.NOT_CONNECTED) {
                mMainActivityInterface.setWriteInformation("Not Connected", true);
                mMainActivityInterface.setConnectionButton("Connection", true);
                mMainActivityInterface.setColorToolBar(mMainActivityInterface.getColorNotConnected());
                mMainActivityInterface.setAddressServerInformation("");
                mMainActivityInterface.setPortServerInformation("");
            }
        }
    }

    @Override
    public void onDeviceListChange(List<Device> deviceList) {

    }

    @Override
    public void ackFromServer(int ackCode) {

        if (mMainActivityInterface != null) {
            switch (ackCode) {
                case Device.AckCode.OKRead:
                case Device.AckCode.OK:
                    break;
                case Device.AckCode.InvalidAddress:
                    mMainActivityInterface.setWriteInformation("Address invalid", false);
                case Device.AckCode.UnknownAddress:
                    mMainActivityInterface.setWriteInformation("Address unknown", false);
                    break;
                case Device.AckCode.InvalidName:
                    mMainActivityInterface.setWriteInformation("Invalid Name", false);
                    break;
                case Device.AckCode.UnreachedDevice:
                    mMainActivityInterface.setWriteInformation("Device Unreachable", false);
                    break;
                case Device.AckCode.InvalidFrameFromClient:
                case Device.AckCode.InvalidFrameFromServer:
                    //do nothing developer information implement Log
                    break;
            }
        }
    }
}
