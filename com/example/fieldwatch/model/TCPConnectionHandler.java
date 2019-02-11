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
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
/**
 *  This class handles the TCP connection with the server.
 *
 * @author Kilian Leport
 *
 */
public class TCPConnectionHandler {
    private static final String TAG = "TCPConnectionHandler";
    private OnTCPEvent mListener;

    private String mServerIP;
    private int mPort;

    private Receiver receiver;
    private Socket socket;

    public static final int STATUS_UNCHANGED = 0;
    public static final int CONNECTED = 1;
    public static final int NOT_CONNECTED = 2;

    private DataOutputStream out;
    private static final String LIVING_STRING = "LIVING";

    private static final int interval_time_send_message = 500;
    private Timer timer;
    private TimerTask timerTask;
    private boolean useHeartBeat;

    private final BlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(100);

    @SuppressWarnings("WeakerAccess")
    public TCPConnectionHandler(boolean useHeartBeat, OnTCPEvent mListener) {
        this.mListener = mListener;
        this.useHeartBeat = useHeartBeat;
        receiver = new Receiver();
        Log.d(TAG, "TCPConnectionHandler(boolean useHeartBeat, OnTCPEvent mListener)");
    }

    public void startConnection(String ipServer, int port) {
        mServerIP = ipServer;
        mPort = port;
        socket = new Socket();
        new Thread(receiver).start();
    }

    public void stopConnection() {
        receiver.cancel();
    }

    public boolean isConnected() {
        return socket != null && (!socket.isClosed() && socket.isConnected());
    }

    private void sendMessageTask() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (isConnected()) {
                    if (out != null) {
                        try {
                            byte[] data = new byte[0];
                            try {
                                data = queue.take();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (isConnected()) {
                                out.write(data);
                                out.flush();
                            }
                        } catch (IOException e) {
                            stopConnection();
                            Log.w(TAG, "Socket Not Connected anymore");
                            // e.printStackTrace();
                        }
                    }

                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void sendMessage(byte orderCode, byte address, byte action, byte type, String name, int coordinateX, int coordinateY) {
        if (isConnected()) {
            ByteArrayOutputStream outputStreamXml = new ByteArrayOutputStream();
            try {
                XmlSerializer xmlSerializer = Xml.newSerializer();
                xmlSerializer.setOutput(outputStreamXml, "utf-8");
                //xmlSerializer.startDocument("UTF-8", true);
                xmlSerializer.startTag(null, Device.Tag.OrderTag);
                xmlSerializer.startTag(null, Device.Tag.OrderCodeTag);
                xmlSerializer.text(String.valueOf((orderCode & 0xFF)));
                xmlSerializer.endTag(null, Device.Tag.OrderCodeTag);
                xmlSerializer.startTag(null, Device.Tag.AddressTag);
                xmlSerializer.text(String.valueOf((address & 0xFF)));
                xmlSerializer.endTag(null, Device.Tag.AddressTag);
                if (orderCode == Device.OrderCode.AddModify) {
                    xmlSerializer.startTag(null, Device.Tag.NameTag);
                    xmlSerializer.text(name);
                    xmlSerializer.endTag(null, Device.Tag.NameTag);
                    xmlSerializer.startTag(null, Device.Tag.TypeTag);
                    xmlSerializer.text(String.valueOf((type & 0xFF)));
                    xmlSerializer.endTag(null, Device.Tag.TypeTag);
                    xmlSerializer.startTag(null, Device.Tag.CoordinateXTag);
                    xmlSerializer.text(String.valueOf(coordinateX));
                    xmlSerializer.endTag(null, Device.Tag.CoordinateXTag);
                    xmlSerializer.startTag(null, Device.Tag.CoordinateYTag);
                    xmlSerializer.text(String.valueOf(coordinateY));
                    xmlSerializer.endTag(null, Device.Tag.CoordinateYTag);
                } else if (orderCode == Device.OrderCode.Action) {
                    xmlSerializer.startTag(null, Device.Tag.ActionTag);
                    xmlSerializer.text(String.valueOf((action & 0xFF)));
                    xmlSerializer.endTag(null, Device.Tag.ActionTag);
                }
                xmlSerializer.endTag(null, Device.Tag.OrderTag);
                xmlSerializer.endDocument();
                xmlSerializer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] size = ByteBuffer.allocate(4).putInt(outputStreamXml.size()).array();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                outputStream.write(size);
                outputStream.write(outputStreamXml.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                queue.put(outputStream.toByteArray());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendAliveMessage() {
        if (isConnected()) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] size = ByteBuffer.allocate(4).putInt(LIVING_STRING.length()).array();
            try {
                outputStream.write(size);
                outputStream.write(LIVING_STRING.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                queue.put(outputStream.toByteArray());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void handleFrame(XmlPullParser parser) {
        if (mListener != null) {
            byte ackCode = Device.AckCode.InvalidFrameFromServer;
            List<Device> deviceList = new ArrayList<>();
            boolean frameValid = true;

            try {
                int event = parser.getEventType();
                while (event != XmlPullParser.END_DOCUMENT && frameValid) {

                    switch (event) {
                        case XmlPullParser.TEXT:
                            break;
                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(Device.Tag.AckCodeTag)) {
                                event = parser.next();
                                if (event == XmlPullParser.TEXT) {
                                    ackCode = (byte) Integer.parseInt(parser.getText());
                                    frameValid &= Device.AckCode.isAckCodeValid(ackCode);
                                } else
                                    frameValid = false;
                                break;
                            }
                            if (Device.Tag.DeviceTag.equals(parser.getName())) {
                                if (ackCode == Device.AckCode.OKRead) {// || ackCode == Device.AckCode.OK) {
                                    boolean endDevice = false;
                                    Device dev = new Device();
                                    while (event != XmlPullParser.END_DOCUMENT && !endDevice && frameValid) {
                                        switch (event) {
                                            case XmlPullParser.START_TAG:
                                                if (parser.getName().equals(Device.Tag.AddressTag)) {
                                                    event = parser.next();
                                                    if (event == XmlPullParser.TEXT) {
                                                        byte add = (byte) Integer.parseInt(parser.getText());
                                                        dev.setAddress(add);
                                                    } else
                                                        frameValid = false;
                                                    break;
                                                }
                                                if (parser.getName().equals(Device.Tag.ValueTag)) {
                                                    event = parser.next();
                                                    if (event == XmlPullParser.TEXT) {
                                                        int value = Integer.parseInt(parser.getText());
                                                        if (Device.isValueValid(value))
                                                            dev.setValue(value);
                                                        else
                                                            frameValid = false;
                                                    } else
                                                        frameValid = false;
                                                    break;
                                                }
                                                if (parser.getName().equals(Device.Tag.TypeTag)) {
                                                    event = parser.next();
                                                    if (event == XmlPullParser.TEXT) {
                                                        byte type = (byte) Integer.parseInt(parser.getText());
                                                        if (Device.isTypeValid(type))
                                                            dev.setType(type);
                                                        else
                                                            frameValid = false;
                                                    } else
                                                        frameValid = false;
                                                    break;
                                                }
                                                if (parser.getName().equals(Device.Tag.NameTag)) {
                                                    event = parser.next();
                                                    if (event == XmlPullParser.TEXT) {
                                                        String name = parser.getText();
                                                        if (Device.isNameValid(name))
                                                            dev.setName(name);
                                                        else
                                                            frameValid = false;
                                                    } else
                                                        frameValid = false;
                                                    break;
                                                }
                                                if (parser.getName().equals(Device.Tag.StateTag)) {
                                                    event = parser.next();
                                                    if (event == XmlPullParser.TEXT) {
                                                        byte state = (byte) Integer.parseInt(parser.getText());
                                                        if (Device.isStateValid(state))
                                                            dev.setState(state);
                                                        else
                                                            frameValid = false;
                                                    } else
                                                        frameValid = false;
                                                    break;
                                                }
                                                if (parser.getName().equals(Device.Tag.CoordinateXTag)) {
                                                    event = parser.next();
                                                    if (event == XmlPullParser.TEXT) {
                                                        int coordinateX = Integer.parseInt(parser.getText());
                                                        if (Device.isCoordinateXValid(coordinateX))
                                                            dev.setCoordinateX(coordinateX);
                                                        else
                                                            frameValid = false;
                                                    } else
                                                        frameValid = false;
                                                    break;
                                                }
                                                if (parser.getName().equals(Device.Tag.CoordinateYTag)) {
                                                    event = parser.next();
                                                    if (event == XmlPullParser.TEXT) {
                                                        int coordinateY = Integer.parseInt(parser.getText());
                                                        if (Device.isCoordinateYValid(coordinateY))
                                                            dev.setCoordinateY(coordinateY);
                                                        else
                                                            frameValid = false;
                                                    } else
                                                        frameValid = false;
                                                    break;
                                                }
                                                break;
                                            case XmlPullParser.END_TAG:
                                                if (parser.getName().equals(Device.Tag.DeviceTag)) {
                                                    endDevice = true;
                                                }
                                                break;
                                        }
                                        event = parser.next();
                                    }
                                    if (!endDevice)
                                        frameValid = false;
                                    else
                                        deviceList.add(dev);
                                } else {
                                    boolean endDevice = false;
                                    Device dev = new Device();
                                    while (event != XmlPullParser.END_DOCUMENT && !endDevice && frameValid) {
                                        switch (event) {
                                            case XmlPullParser.TEXT:
                                                break;
                                            case XmlPullParser.START_TAG:
                                                if (parser.getName().equals(Device.Tag.AddressTag)) {
                                                    event = parser.next();
                                                    if (event == XmlPullParser.TEXT) {
                                                        int add = Integer.parseInt(parser.getText());
                                                        if (add >= 0 && add <= 255)
                                                            dev.setAddress((byte) add);
                                                            //address = (byte) Integer.parseInt(parser.getText());
                                                        else
                                                            frameValid = false;
                                                    } else
                                                        frameValid = false;
                                                    break;
                                                }
                                                if (parser.getName().equals(Device.Tag.OrderCodeTag)) {
                                                    event = parser.next();
                                                    if (event == XmlPullParser.TEXT) {
                                                        byte order = (byte) Integer.parseInt(parser.getText());
                                                        if (Device.OrderCode.isOrderCodeValid(order))
                                                            dev.setOrderCode(order);
                                                            //address = (byte) Integer.parseInt(parser.getText());
                                                        else
                                                            frameValid = false;
                                                    } else
                                                        frameValid = false;
                                                    break;
                                                }
                                                break;
                                            case XmlPullParser.END_TAG:
                                                if (parser.getName().equals(Device.Tag.DeviceTag)) {
                                                    endDevice = true;
                                                }
                                                break;
                                        }
                                        event = parser.next();
                                    }
                                    if (!endDevice)
                                        frameValid = false;
                                    else
                                        deviceList.add(dev);
                                }
                                break;
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            break;
                        default:
                            break;
                    }
                    event = parser.next();
                }
            } catch (Exception e) {
                e.printStackTrace();
                ackCode = Device.AckCode.InvalidFrameFromServer;
            }
            if (!frameValid)
                ackCode = Device.AckCode.InvalidFrameFromServer;
            Log.d(TAG, "message received! ackCode = " + String.valueOf(ackCode) +
                    " taille list   =  " + deviceList.size());
            mListener.messageReceived(ackCode, deviceList);
        }
    }

    //Thread receiver*******************************************************************************
    private class Receiver implements Runnable {

        private volatile boolean mRun;

        Receiver() {

        }

        public void run() {
            try {
                mRun = true;
                SocketAddress socketAddress = new InetSocketAddress(mServerIP, mPort);
                socket.connect(socketAddress, 2000);
                mListener.connectionStateChanged(CONNECTED);
                sendMessageTask();
                if (useHeartBeat) {
                    startTimer();
                }

                InputStream in;

                try {
                    out = new DataOutputStream(socket.getOutputStream());
                    in = socket.getInputStream();
                    byte[] bufferSize = new byte[4];
                    int read;
                    int size = 0;
                    while (mRun) {
                        if (!socket.isConnected()) {
                            mListener.connectionStateChanged(NOT_CONNECTED);
                            mRun = false;
                        }
                        if (size == 0 && in.available() > 4) {
                            read = in.read(bufferSize, 0, 4);
                            if (read != -1)
                                size = ByteBuffer.wrap(bufferSize).getInt();
                        }
                        if (size != 0 && in.available() >= size) {
                            byte[] bufferOrder = new byte[size];
                            read = in.read(bufferOrder, 0, size);

                            if (read != -1) {
                                ByteArrayInputStream frame = new ByteArrayInputStream(bufferOrder);
                                XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
                                XmlPullParser myParser = xmlFactoryObject.newPullParser();
                                myParser.setInput(frame, null);//, "UTF_8");

                                handleFrame(myParser);
                            }

                            size = 0;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Log.d(TAG, "socket.close() ");
                    mListener.connectionStateChanged(NOT_CONNECTED);
                    if (useHeartBeat) {
                        stopTimerTask();
                    }
                    out.flush();
                    out.close();
                    socket.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
                mListener.connectionStateChanged(NOT_CONNECTED);
            }
        }

        private void cancel() {
            mRun = false;
            mListener.connectionStateChanged(NOT_CONNECTED);
        }
    }


    private void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 0, interval_time_send_message); //
    }

    private void stopTimerTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                sendAliveMessage();
            }
        };
    }

    //*****Listener********************
    public interface OnTCPEvent {
        void connectionStateChanged(int information);

        void messageReceived(int ackCode, List<Device> deviceList);
    }
}
