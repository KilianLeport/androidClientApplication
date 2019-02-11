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
import com.example.fieldwatch.view.ViewInt;

import java.util.List;

public interface PresentersInt {
    /**
     * Server information changed.
     *
     * @param serverIP new IP of server
     * @param port new port of the server
     */
    void serverInformationChanged(String serverIP, int port);

    /**
     *  Send information to the MainActivity to show short message.
     *
     * @param statusServerDiscovered new status of server discovering
     * @param statusTCPConnection new status of the TCP connection
     */
    void connectionStateChanged(int statusServerDiscovered, int statusTCPConnection);

    /**
     * The list of device stored in the DeviceArray class changed.
     *
     * @param deviceList the new list of device
     */
    void onDeviceListChange(List<Device> deviceList);

    /**
     * A new view want to subscribe to the corresponding presenter.
     *
     * @param view the view that wants to resume
     */
    void onViewAttached(ViewInt view);

    /**
     * The view is detached.
     */
    void onViewDetached();

    /**
     * A acknowledgment is received from the server.
     *
     * @param ackCode The code of the acknowledgment
     */
    void ackFromServer(int ackCode);
}
