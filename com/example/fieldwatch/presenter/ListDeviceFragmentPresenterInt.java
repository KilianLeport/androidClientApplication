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

public interface ListDeviceFragmentPresenterInt extends PresentersInt {
    /**
     * Button read is clicked for the device specified by address.
     *
     * @param address Address of the device
     */
    void onButtonReadClicked(byte address);

    /**
     * Button action is clicked for the device specified by address.
     *
     * @param address Address of the device
     * @param action  Action
     */
    void onButtonActionClicked(byte address, byte action);

    /**
     * Button delete is clicked for the device specified by address.
     *
     * @param address Address of the device
     */
    void onButtonDeleteClicked(byte address);

    /**
     * Button clear list is clicked.
     */
    void onButtonClearListClicked();

    /**
     * Button add device is clicked.
     */
    void onButtonAddDeviceClicked();

    /**
     * Button to add device.
     *
     * @param name    New name of device
     * @param address Address of the device
     * @param type    New type of the device
     */
    void onButtonDialogAddDeviceClicked(String name, byte address, byte type);
}
