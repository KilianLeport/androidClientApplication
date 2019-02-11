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

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.fieldwatch.presenter.PresentersInt;
import com.example.fieldwatch.view.MainActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
/**
 *  This class is the manager of the model.
 *
 * @author Kilian Leport
 *
 */
public class ModelManager implements ServerScanner.OnServerDiscoveredListener, TCPConnectionHandler.OnTCPEvent, ModelManagerInt, DeviceArray.OnListChangeListener {
    private static final String TAG = "ModelManager";
    private static final int SERVER_DISCOVER_STATE_CHANGED = 0;
    private static final int TCP_CONNECTION_HANDLER_STATE_CHANGED = 1;
    private static final int TCP_CONNECTION_HANDLER_MESSAGE_RECEIVED = 2;
    private final ModelManager.CustomHandler mHandler = new ModelManager.CustomHandler(this);

    private ArrayList<PresentersInt> presenterListeners;

    private TCPConnectionHandler mTCPConnectionHandler;
    private ServerScanner mServerScanner;
    private DeviceArray mDeviceArray;

    private boolean automaticUpdate = false;
    private boolean automaticConnection = false;

    private TimerTask timerTask;
    private final Handler handler = new Handler();
    private static final int interval_time = 10000;

    public ModelManager(MainActivity mainActivity) {
        mServerScanner = new ServerScanner(mainActivity.getApplicationContext(), this);
        mTCPConnectionHandler = new TCPConnectionHandler(true, this);
        mDeviceArray = new DeviceArray(this);
        presenterListeners = new ArrayList<>();

        Timer timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 0, interval_time);
        Log.d(TAG, "ModelManager(MainActivity mainActivity)");
    }

    private void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (automaticUpdate)
                            readDevice(Device.BROADCAST_ADDRESS);
                        if (automaticConnection) {
                            connectToServer();
                        }
                    }
                });
            }
        };
    }

    //communication to Presenter*****************************************************************
    public void connectionStateChanged(int statusServerDiscovered, int statusTCPConnection) {
        for (PresentersInt l : presenterListeners)
            l.connectionStateChanged(statusServerDiscovered, statusTCPConnection);
    }

    public void serverDiscovered(String serverIP, int port) {
        for (PresentersInt l : presenterListeners)
            l.serverInformationChanged(serverIP, port);
    }

    //Communication to TCPConnection***************************************************************
    public void connectionToServer(String serverIP, int port) {
        if (mTCPConnectionHandler != null)
            mTCPConnectionHandler.startConnection(serverIP, port);
    }

    private void sendMessage(byte orderCode, byte address) {
        sendMessage(orderCode, address, Device.ActionCode.UnknownAction);
    }

    private void sendMessage(byte orderCode, byte address, byte action) {
        sendMessage(orderCode, address, action, Device.TypeCode.UnknownType, "");
    }

    private void sendMessage(byte orderCode, byte address, byte action, byte type, String name) {
        sendMessage(orderCode, address, action, type, name, Device.MINIMUM_COORDINATE_X, Device.MINIMUM_COORDINATE_Y);
    }

    private void sendMessage(byte orderCode, byte address, byte action, byte type, String name, int coordinateX, int coordinateY) {
        if (mTCPConnectionHandler != null)
            mTCPConnectionHandler.sendMessage(orderCode, address, action, type, name, coordinateX, coordinateY);
    }

    private void handleAckMessage(int ackCode, List<Device> deviceList) {
        if (deviceList != null && deviceList.size() > 0) {
            int address = (int) deviceList.get(0).getAddress() & 0xFF;
            byte orderCode = deviceList.get(0).getOrderCode();
            if (ackCode == Device.AckCode.OKRead) {
                for (int i = 0; i < deviceList.size(); i++) {
                    mDeviceArray.addDevice(deviceList.get(i));
                }
            } else {
                Device dev;
                switch (orderCode) {
                    case Device.OrderCode.Action:
                        switch (ackCode) {
                            case Device.AckCode.OK:
                                readDevice((byte) address);
                                break;
                            case Device.AckCode.InvalidAddress:
                            case Device.AckCode.UnknownAddress:
                            case Device.AckCode.UnreachedDevice:
                                if (mDeviceArray.hasDevice(address)) {
                                    dev = mDeviceArray.getDevice(address);
                                    dev.setState(Device.StateCode.Unreachable);
                                    mDeviceArray.addDevice(dev);
                                }
                                break;
                            case Device.AckCode.InvalidName:
                                //do nothing
                                break;
                            case Device.AckCode.InvalidFrameFromClient:
                                //do nothing
                                break;
                            case Device.AckCode.InvalidFrameFromServer:
                                //do nothing
                                break;
                        }
                        for (PresentersInt l : presenterListeners)
                            l.ackFromServer(ackCode);
                        break;
                    case Device.OrderCode.AddModify:
                        switch (ackCode) {
                            case Device.AckCode.OK:
                                readDevice((byte) address);
                                break;
                            case Device.AckCode.InvalidAddress:
                            case Device.AckCode.UnknownAddress:
                                //do nothing
                                break;
                            case Device.AckCode.InvalidName:
                                //do nothing
                                break;
                            case Device.AckCode.UnreachedDevice:
                                //do nothing
                                break;
                            case Device.AckCode.InvalidFrameFromClient:
                                //do nothing
                                break;
                            case Device.AckCode.InvalidFrameFromServer:
                                //do nothing
                                break;
                        }
                        for (PresentersInt l : presenterListeners)
                            l.ackFromServer(ackCode);
                        break;
                    case Device.OrderCode.Delete:
                        switch (ackCode) {
                            case Device.AckCode.OK:
                            case Device.AckCode.InvalidAddress:
                            case Device.AckCode.UnknownAddress:
                                mDeviceArray.deleteDevice(address);
                                break;
                            case Device.AckCode.InvalidName:
                                //do nothing
                                break;
                            case Device.AckCode.UnreachedDevice:
                                //do nothing
                                break;
                            case Device.AckCode.InvalidFrameFromClient:
                                //do nothing
                                break;
                            case Device.AckCode.InvalidFrameFromServer:
                                //do nothing
                                break;
                        }
                        for (PresentersInt l : presenterListeners)
                            l.ackFromServer(ackCode);
                        break;
                }
            }
        }
    }

    //*************************From ServerScanner*************************
    @Override
    public void serverDiscovered(int information, String ipServer, int port) {
        Message m = Message.obtain(mHandler, SERVER_DISCOVER_STATE_CHANGED);
        m.arg1 = information;
        m.arg2 = port;
        m.obj = ipServer;
        mHandler.sendMessage(m);
    }

    //*************************From TCPConnectionHandler*************************
    @Override
    public void connectionStateChanged(int information) {
        Message m = Message.obtain(mHandler, TCP_CONNECTION_HANDLER_STATE_CHANGED);
        m.arg1 = information;
        mHandler.sendMessage(m);
    }

    @Override
    public void messageReceived(int ackCode, List<Device> deviceList) {
        Message m = Message.obtain(mHandler, TCP_CONNECTION_HANDLER_MESSAGE_RECEIVED);
        m.arg1 = ackCode;
        m.obj = deviceList;
        mHandler.sendMessage(m);
    }

    //*************************From Presenter*************************
    public void registerPresenter(PresentersInt listener) {
        if (!presenterListeners.contains(listener))
            presenterListeners.add(listener);
    }

    public void unRegisterPresenter(PresentersInt listener) {
        if (presenterListeners.contains(listener))
            presenterListeners.remove(listener);
    }

    @Override
    public void addModifyDevice(byte address, byte type, String name) {
        sendMessage(Device.OrderCode.AddModify, address, Device.ActionCode.UnknownAction, type, name);
    }

    @Override
    public void actionDevice(byte address, byte action) {
        sendMessage(Device.OrderCode.Action, address, action);
    }

    @Override
    public void readDevice(byte address) {
        sendMessage(Device.OrderCode.Read, address);
    }

    @Override
    public void deleteDevice(byte address) {
        sendMessage(Device.OrderCode.Delete, address);
    }

    @Override
    public void modifyCoordinateDevice(byte address, int coordinateX, int coordinateY) {
        int addressInt = (int) address & 0xFF;
        if (Device.isCoordinateXValid(coordinateX)
                && Device.isCoordinateYValid(coordinateY)
                && mDeviceArray.hasDevice(addressInt))
            sendMessage(Device.OrderCode.AddModify,
                    address,
                    Device.ActionCode.UnknownAction,
                    mDeviceArray.getDevice(addressInt).getType(),
                    mDeviceArray.getDevice(addressInt).getName(),
                    coordinateX,
                    coordinateY);
    }

    @Override
    public void connectToServer() {
        if (!mTCPConnectionHandler.isConnected())
            mServerScanner.searchServer();
    }

    @Override
    public void disconnectToServer() {
        if (mTCPConnectionHandler.isConnected())
            mTCPConnectionHandler.stopConnection();
    }

    @Override
    public void toggleConnectionToServer() {
        if (mTCPConnectionHandler.isConnected()) {
            disconnectToServer();
        } else {
            connectToServer();
        }
    }

    @Override
    public void clear() {
        if (mDeviceArray != null)
            mDeviceArray.clear();
    }

    @Override
    public void enableAutomaticConnection(boolean checked) {
        automaticConnection = checked;
    }

    @Override
    public void enableAutomaticUpdate(boolean checked) {
        automaticUpdate = checked;
    }

    //*************************From DeviceArray*************************
    @Override
    public void onListChange() {
        for (PresentersInt l : presenterListeners)
            l.onDeviceListChange(this.mDeviceArray.getListDevice());
    }

    //Handler****************************************************************************
    private static class CustomHandler extends Handler {
        private final WeakReference<ModelManager> mManager;

        private CustomHandler(ModelManager modelManager) {
            mManager = new WeakReference<>(modelManager);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            ModelManager manager = mManager.get();
            if (manager != null) {
                switch (msg.what) {
                    case SERVER_DISCOVER_STATE_CHANGED:
                        switch (msg.arg1) {
                            case ServerScanner.SERVER_FOUND:
                                manager.connectionStateChanged(ServerScanner.SERVER_FOUND, TCPConnectionHandler.STATUS_UNCHANGED);
                                manager.connectionToServer((String) msg.obj, msg.arg2);
                                manager.serverDiscovered((String) msg.obj, msg.arg2);
                                break;
                            case ServerScanner.SERVER_NOT_FOUND:
                                manager.connectionStateChanged(ServerScanner.SERVER_NOT_FOUND, TCPConnectionHandler.STATUS_UNCHANGED);
                                break;
                        }
                        break;
                    case TCP_CONNECTION_HANDLER_STATE_CHANGED:
                        switch (msg.arg1) {
                            case TCPConnectionHandler.CONNECTED:
                                manager.connectionStateChanged(ServerScanner.STATUS_UNCHANGED, TCPConnectionHandler.CONNECTED);
                                break;
                            case TCPConnectionHandler.NOT_CONNECTED:
                                manager.connectionStateChanged(ServerScanner.STATUS_UNCHANGED, TCPConnectionHandler.NOT_CONNECTED);
                                break;
                        }
                        break;
                    case TCP_CONNECTION_HANDLER_MESSAGE_RECEIVED:
                        if (msg.obj != null) {
                            List<Device> deviceList = ((ArrayList<Device>) msg.obj);
                            manager.handleAckMessage(msg.arg1, deviceList);
                        }
                        break;
                }
            }
        }
    }
}
