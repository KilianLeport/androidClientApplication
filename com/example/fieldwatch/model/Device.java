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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 *  This class represents a device.
 *
 * @author Kilian Leport
 *
 */
public class Device {
    private String name;
    private byte address;
    private int value;
    private byte type;
    private byte state;
    private int coordinateX;
    private int coordinateY;
    private byte orderCode;

    private long age;

    private static final int MAXIMUM_VALUE = 65535;
    private static final int MAXIMUM_SIZE_NAME_DEVICE = 32;
    public static final byte BROADCAST_ADDRESS = (byte) 255;
    public static final int ADDRESS_MAXIMUM = 255;

    public static final int MINIMUM_COORDINATE_X = 0;
    public static final int MAXIMUM_COORDINATE_X = 100;
    public static final int MINIMUM_COORDINATE_Y = 0;
    public static final int MAXIMUM_COORDINATE_Y = 100;

    public Device(String name, byte address, int value, byte type, byte state, int coordinateX, int coordinateY, byte orderCode) {
        this.name = name;
        this.address = address;
        this.value = value;
        this.type = type;
        this.state = state;
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
        this.orderCode = orderCode;
    }

    public Device(String name, byte address, int value, byte type, byte state, int coordinateX, int coordinateY) {
        this.name = name;
        this.address = address;
        this.value = value;
        this.type = type;
        this.state = state;
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
        this.orderCode = OrderCode.UnknownCode;
    }

    public Device() {
        this.name = "";
        this.address = (byte) 255;
        this.value = 0;
        this.type = 0;
        this.state = 0;
        this.coordinateX = 0;
        this.coordinateY = 0;
        this.orderCode = OrderCode.UnknownCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(byte address) {
        this.address = address;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public void setCoordinateX(int coordinateX) {
        this.coordinateX = coordinateX;
    }

    public void setCoordinateY(int coordinateY) {
        this.coordinateY = coordinateY;
    }

    public void setOrderCode(byte orderCode) {
        this.orderCode = orderCode;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public static boolean isStateValid(byte state) {
        if (state >= StateCode.UnknownState && state <= StateCode.Unreachable)
            return true;
        else
            return false;
    }

    public static boolean isTypeValid(byte type) {
        if (type >= TypeCode.UnknownType && type <= TypeCode.Measure)
            return true;
        else
            return false;
    }

    public static boolean isActionValid(byte action) {
        if (action >= ActionCode.UnknownAction && action <= ActionCode.TurnOff)
            return true;
        else
            return false;
    }

    public static boolean isValueValid(int value) {
        if (value <= MAXIMUM_VALUE)
            return true;
        else
            return false;
    }

    public static boolean isNameValid(String name) {
        String regexValidator = "[A-Za-z0-9 ]{0," + MAXIMUM_SIZE_NAME_DEVICE + "}";
        if (name.matches(regexValidator))
            return true;
        else
            return false;
    }

    public static boolean isCoordinateXValid(int coordinateX) {
        if (coordinateX >= MINIMUM_COORDINATE_X && coordinateX <= MAXIMUM_COORDINATE_X)
            return true;
        else
            return false;
    }

    public static boolean isCoordinateYValid(int coordinateY) {
        if (coordinateY >= MINIMUM_COORDINATE_Y && coordinateY <= MAXIMUM_COORDINATE_Y)
            return true;
        else
            return false;
    }

    public String getName() {
        return name;
    }

    public byte getAddress() {
        return address;
    }

    public int getValue() {
        return value;
    }

    public byte getType() {
        return type;
    }

    public byte getState() {
        return state;
    }

    public String getTypeString() {
        return TypeCode.getTypeString(type);
    }

    public String getStateString() {
        return StateCode.getStateString(state);
    }

    public int getCoordinateX() {
        return coordinateX;
    }

    public int getCoordinateY() {
        return coordinateY;
    }

    public byte getOrderCode() {
        return orderCode;
    }

    public long getAge() {
        return age;
    }

    public static class ActionCode {
        public static final byte UnknownAction = 0;
        public static final byte TurnOn = 1;
        public static final byte TurnOff = 2;
        private static final List<String> actionString = new ArrayList<>(Arrays.asList("Unknown Action",
                "Turn On",
                "Turn Off"));

        public static List<String> getActionListString() {
            return actionString;
        }

    }

    public static class StateCode {
        public static final byte UnknownState = 0;
        public static final byte On = 1;
        public static final byte Off = 2;
        public static final byte Unreachable = 3;

        private static final List<String> stateString = new ArrayList<>(Arrays.asList("Unknown",
                "On",
                "Off",
                "Unreachable"));

        static String getStateString(int state) {
            if (stateString.get(state) == null)
                return stateString.get(UnknownState);
            return stateString.get(state);
        }
    }

    public static class TypeCode {
        public static final byte UnknownType = 0;
        public static final byte Interrupter = 1;
        public static final byte Measure = 2;

        private static final List<String> typeString = new ArrayList<>(Arrays.asList("Unknown",
                "Interrupter",
                "Measure"));

        static String getTypeString(int type) {
            if (typeString.get(type) == null)
                return typeString.get(UnknownType);
            return typeString.get(type);
        }

        public static List<String> getTypeListString() {
            return typeString;
        }
    }

    public static class AckCode {
        public static final byte OKRead = 1;
        public static final byte OK = 2;
        public static final byte InvalidAddress = 3;
        public static final byte UnknownAddress = 4;
        public static final byte InvalidName = 5;
        public static final byte UnreachedDevice = 6;
        public static final byte InvalidFrameFromClient = 7;
        public static final byte InvalidFrameFromServer = 8;
        public static final byte ServerOverloaded = 9;

        public static boolean isAckCodeValid(byte ackCode) {
            if (ackCode >= OKRead && ackCode <= ServerOverloaded)
                return true;
            else
                return false;
        }
    }

    public static class OrderCode {
        public static final byte UnknownCode = 0;
        public static final byte AddModify = 1;
        public static final byte Delete = 2;
        public static final byte Read = 3;
        public static final byte Action = 4;

        public static boolean isOrderCodeValid(byte orderCode) {
            if (orderCode >= UnknownCode && orderCode <= Action)
                return true;
            else
                return false;
        }
    }

    public class Tag {
        public static final String AckTag = "ack";
        public static final String AckCodeTag = "ackCode";
        public static final String DeviceTag = "device";
        public static final String AddressTag = "address";
        public static final String ValueTag = "value";
        public static final String TypeTag = "type";
        public static final String NameTag = "name";
        public static final String StateTag = "state";
        public static final String OrderTag = "order";
        public static final String OrderCodeTag = "orderCode";
        public static final String ActionTag = "action";
        public static final String CoordinateXTag = "coordinateX";
        public static final String CoordinateYTag = "coordinateY";
    }
}