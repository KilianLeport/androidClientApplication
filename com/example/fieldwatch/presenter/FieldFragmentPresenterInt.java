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

public interface FieldFragmentPresenterInt extends PresentersInt {
    /**
     * Button actualized is clicked.
     */
    void onButtonActualizeClicked();

    /**
     * Button clear the list is clicked.
     */
    void onButtonClearListClicked();

    /**
     * Button edit view is clicked.
     */
    void onButtonEditViewClicked(boolean checked);

    /**
     * Device at address moved with new coordinates.
     *
     * @param address        Address of the device
     * @param newCoordinateX New coordinate X
     * @param newCoordinateY New coordinate Y
     */
    void onDeviceMoved(byte address, int newCoordinateX, int newCoordinateY);
}
