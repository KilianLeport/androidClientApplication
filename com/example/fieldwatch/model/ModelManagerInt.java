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

import com.example.fieldwatch.presenter.PresentersInt;

public interface ModelManagerInt {
    /**
     * Connect to server.
     */
    void connectToServer();

    /**
     * Disconnect to server.
     */
    void disconnectToServer();

    /**
     * Toggle connection to server.
     */
    void toggleConnectionToServer();

    /**
     * Register a new listener.
     *
     * @param listener The presenter that want to listen the model
     */
    void registerPresenter(PresentersInt listener);

    /**
     * Unregister listener.
     *
     * @param listener The listener to unregister
     */
    void unRegisterPresenter(PresentersInt listener);

    /**
     * Send a request to the server to add a new device. If the device exists the server will replace it.
     *
     * @param address Address of the device
     * @param type Type of the device
     * @param name Name of the device
     */
    void addModifyDevice(byte address, byte type, String name);

    /**
     * Send a request to the server to action to the device specified by address.
     *
     * @param address Address of the device
     * @param action Action to execute on device
     */
    void actionDevice(byte address, byte action);

    /**
     * Send a request to the server to read to the device specified by address.
     *
     * @param address Address of the device
     */
    void readDevice(byte address);
    /**
     * Send a request to the server to delete to the device specified by address.
     *
     * @param address Address of the device
     */
    void deleteDevice(byte address);

    /**
     * Send a request to the server to modify the coordinates to the device specified by address.
     *
     * @param address Address of the device
     * @param coordinateX New coordinate X
     * @param coordinateY New coordinate Y
     */
    void modifyCoordinateDevice(byte address, int coordinateX, int coordinateY);

    /**
     * Clear the list of device.
     */
    void clear();

    /**
     * Enable automatic connection to server.
     *
     * @param checked Boolean to enable automatic connection
     */
    void enableAutomaticConnection(boolean checked);

    /**
     * Enable automatic update to server. Send read to all device each 10 seconds.
     *
     * @param checked Boolean to enable automatic update
     */
    void enableAutomaticUpdate(boolean checked);
}
