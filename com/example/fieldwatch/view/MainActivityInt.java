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
package com.example.fieldwatch.view;

public interface MainActivityInt extends ViewInt {
    /**
     * Set port in Navigation View.
     *
     * @param port String that represents the port
     */
    void setPortServerInformation(final String port);

    /**
     * Set address in Navigation View.
     *
     * @param address String that represents the address
     */
    void setAddressServerInformation(final String address);

    /**
     * Change the name and enable the button connection.
     *
     * @param title   Name of the button
     * @param enabled Enable the button connection
     */
    void setConnectionButton(final String title, boolean enabled);

    /**
     * Show Toast message a long time or not.
     *
     * @param information String to show
     * @param longTime    Boolean to show message a long time or not
     */
    void setWriteInformation(final String information, boolean longTime);

    /**
     * Change the background color with the param color.
     *
     * @param color An int that represents the color
     */
    void setColorToolBar(int color);

    /**
     * Get the color not connected.
     *
     * @return Color not connected
     */
    int getColorNotConnected();

    /**
     * Get the color connected.
     *
     * @return Color connected
     */
    int getColorConnected();
}
