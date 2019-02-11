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

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 *  This class searches for the server.
 *
 * @author Kilian Leport
 *
 */
public class ServerScanner {
    private static final String TAG = "ServerScanner";
    public static final int STATUS_UNCHANGED = 0;
    public static final int SERVER_FOUND = 1;
    public static final int SERVER_NOT_FOUND = 2;

    private static final int PORT_UDP_CLIENT = 10101;
    private static final int PORT_UDP_SERVER = 10102;
    private static final String CODE_CLIENT = "CODECLIENT";
    private static final String CODE_SERVER = "CODESERVER";

    private OnServerDiscoveredListener mListener;
    private Context mContext;

    private DatagramSocket mSocket;
    private boolean lookingForServer;

    private Receiver receiver;

    @SuppressWarnings("WeakerAccess")
    public ServerScanner(Context mContext, OnServerDiscoveredListener scannerListener) {
        mListener = scannerListener;
        this.mContext = mContext;
        lookingForServer = false;
        receiver = new Receiver();
        Log.d(TAG, "ServerScanner(Context mContext, OnServerDiscoveredListener scannerListener)");
    }

    public void searchServer() {
        if (!lookingForServer) {
            lookingForServer = true;
            new Thread(receiver).start();
        }
    }

    private InetAddress getBroadcastAddress() {
        WifiManager wifi = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp == null) {
            Log.d(TAG, "Could not get dhcp info");
            return null;
        }
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        try {
            return InetAddress.getByAddress(quads);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendBroadcast(String data) {
        try {
            mSocket = new DatagramSocket(PORT_UDP_SERVER);
            mSocket.setBroadcast(true);
            InetAddress broadcastAddress = getBroadcastAddress();
            if (broadcastAddress != null) {
                DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), broadcastAddress, PORT_UDP_CLIENT);
                try {
                    Log.d(TAG, "socket send");
                    mSocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    //Thread receiver
    private class Receiver implements Runnable {

        private boolean mRun = true;

        Receiver() {

        }

        public void run() {
            try {
                Log.d(TAG, "run looking for server ");
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                sendBroadcast(CODE_CLIENT);
                mSocket.setSoTimeout(2000);
                while (mRun) {
                    mSocket.receive(packet);
                    if (packet.getLength() >= (CODE_SERVER.length() + 10)) {
                        ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
                        int strLength = buffer.getInt(0);
                        String str = new String(packet.getData(), 4, strLength);
                        if (str.equals(CODE_SERVER)) {
                            buffer.position(4 + strLength);
                            byte[] ipAddress = new byte[4];
                            buffer.get(ipAddress, 0, 4);
                            String ip = InetAddress.getByAddress(ipAddress).getHostAddress();
                            int port = (int) buffer.getChar(8 + strLength);
                            if (mListener != null)
                                mListener.serverDiscovered(SERVER_FOUND, ip, port);
                            Log.d(TAG, "server found ");
                            mRun = false;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mRun) {
                    if (mListener != null)
                        mListener.serverDiscovered(SERVER_NOT_FOUND, "", 0);
                    Log.d(TAG, "server not found ");
                }
                if (mSocket != null)
                    mSocket.close();
                lookingForServer = false;
                mRun = true;
                Log.d(TAG, "end of the run ");

            }
        }
    }

    //*****Listener********************
    public interface OnServerDiscoveredListener {
        void serverDiscovered(int information, String ipServer, int port);
    }
}