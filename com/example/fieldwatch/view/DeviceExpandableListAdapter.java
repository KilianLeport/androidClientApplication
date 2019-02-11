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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.fieldwatch.model.Device;
import com.example.fieldwatch.R;

import java.util.ArrayList;
import java.util.List;
/**
 *  This class handles a custom expandable list for the ListDeviceFragment.
 *
 * @author Kilian Leport
 *
 */
public class DeviceExpandableListAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "DeviceExpListAdapter";

    private Context mContext;
    private int mParentResourceId;
    private int mChildResourceId;
    private List<Device> deviceList = new ArrayList<>();
    private ListDeviceFragment mListener;

    @SuppressWarnings("WeakerAccess")
    public DeviceExpandableListAdapter(Context context, int parentResource, int ChildResource, ListDeviceFragment listener) {
        super();
        this.mContext = context;
        this.mParentResourceId = parentResource;
        this.mChildResourceId = ChildResource;
        this.mListener = listener;
        Log.d(TAG, "DeviceExpandableListAdapter(Context context, int parentResource, int ChildResource, ListDeviceFragment listener)");
    }

    @Override
    public int getGroupCount() {
        if (deviceList != null)
            return deviceList.size();
        return 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        if (deviceList != null)
            return deviceList.get(groupPosition);
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mParentResourceId, parent, false);
        }
        // Lookup view for data population
        TextView textName = convertView.findViewById(R.id.device_name);
        TextView textAddress = convertView.findViewById(R.id.device_address);
        TextView textValue = convertView.findViewById(R.id.device_value);
        TextView textType = convertView.findViewById(R.id.device_type);
        TextView textState = convertView.findViewById(R.id.device_state);
        // Populate the data into the template view using the data object
        Device dev = deviceList.get(groupPosition);
        textAddress.setText(String.valueOf(dev.getAddress() & 0xFF));
        textName.setText(dev.getName());
        textValue.setText(String.valueOf(dev.getValue()));
        textType.setText(dev.getTypeString());
        textState.setText(dev.getStateString());
        if (groupPosition % 2 == 0) {
            convertView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.White));
        } else {
            convertView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.BlanchedAlmond));
        }
        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, final ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mChildResourceId, parent, false);
        }

        Button buttonDeleteDevice = convertView.findViewById(R.id.button_delete_device);
        buttonDeleteDevice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Device dev = (Device) getGroup(groupPosition);
                if (mListener != null && dev != null) {
                    final byte address = dev.getAddress();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext)
                            .setTitle("Confirmation")
                            .setMessage("Do you really want to delete the device " + String.valueOf((int) address & 0xFF) + "?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int id) {
                                    mListener.onButtonDeleteClicked(address);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });
        Button buttonReadDevice = convertView.findViewById(R.id.button_read_device);
        buttonReadDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Device dev = (Device) getGroup(groupPosition);
                if (mListener != null && dev != null)
                    mListener.onButtonReadClicked(dev.getAddress());
            }
        });
        Button buttonActionDevice = convertView.findViewById(R.id.button_action_device);
        buttonActionDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                        android.R.layout.simple_spinner_item, Device.ActionCode.getActionListString());
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                View actionDialog = LayoutInflater.from(mContext).inflate(R.layout.custom_dialog_action_device, parent, false);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                alertDialogBuilder.setView(actionDialog);
                final Spinner spinner = actionDialog.findViewById(R.id.action_spinner);
                spinner.setAdapter(adapter);
                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        int action = spinner.getSelectedItemPosition();
                                        if (action != Device.ActionCode.UnknownAction && mListener != null) {
                                            Device dev = (Device) getGroup(groupPosition);
                                            mListener.onButtonActionClicked(dev.getAddress(), (byte) action);
                                        }
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                }).setTitle(R.string.custom_dialog_action_device_title);
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        if (groupPosition % 2 == 0) {
            convertView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.White));
        } else {
            convertView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.BlanchedAlmond));
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    /**
     * CallBack, list of device changed.
     *
     * @param deviceList the new list of devices
     */
    public void onDeviceListChange(List<Device> deviceList) {
        this.deviceList = deviceList;
    }
}
