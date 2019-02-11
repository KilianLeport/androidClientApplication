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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;

import com.example.fieldwatch.model.Device;
import com.example.fieldwatch.R;
import com.example.fieldwatch.presenter.ListDeviceFragmentPresenter;
import com.example.fieldwatch.presenter.ListDeviceFragmentPresenterInt;

import java.util.List;
/**
 *  This class shows the list of devices.
 *
 * @author Kilian Leport
 *
 */
public class ListDeviceFragment extends Fragment implements ListDeviceFragmentInt, View.OnClickListener {
    private static final String TAG = "ListDeviceFragment";
    private ExpandableListView mExpandableListView;
    private DeviceExpandableListAdapter mDeviceExpandableListAdapter;
    private ListDeviceFragmentPresenterInt mPresenterInt;

    public ListDeviceFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        final View rootView = inflater.inflate(R.layout.fragment_list_device, container, false);
        mExpandableListView = rootView.findViewById(R.id.expandableListView_Devices);
        mDeviceExpandableListAdapter = new DeviceExpandableListAdapter(getActivity(), R.layout.device_item_parent, R.layout.device_item_child, this);

        Button mButtonRead = rootView.findViewById(R.id.button_read_all_device);
        mButtonRead.setOnClickListener(this);

        Button mButtonClearAll = rootView.findViewById(R.id.button_clear_all_device);
        mButtonClearAll.setOnClickListener(this);

        Button mButtonAdd = rootView.findViewById(R.id.button_add_device);
        mButtonAdd.setOnClickListener(this);

        mExpandableListView.setAdapter(mDeviceExpandableListAdapter);
        mExpandableListView.setGroupIndicator(null);
        mExpandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                if (previousGroup != groupPosition)
                    mExpandableListView.collapseGroup(previousGroup);
                previousGroup = groupPosition;
            }
        });
        mPresenterInt = new ListDeviceFragmentPresenter(((MainActivity) getActivity()).getModelManager());
        Log.d(TAG, "onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)");
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenterInt.onViewAttached(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenterInt.onViewDetached();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_read_all_device:
                if (mPresenterInt != null)
                    mPresenterInt.onButtonReadClicked(Device.BROADCAST_ADDRESS);
                break;
            case R.id.button_clear_all_device:
                if (mPresenterInt != null)
                    mPresenterInt.onButtonClearListClicked();
                break;
            case R.id.button_add_device:
                if (mPresenterInt != null)
                    mPresenterInt.onButtonAddDeviceClicked();
                break;
            default:
                break;
        }
    }

    //*************************From Presenter*************************
    @Override
    public void onDeviceListChange(List<Device> deviceList) {
        mDeviceExpandableListAdapter.onDeviceListChange(deviceList);
        mDeviceExpandableListAdapter.notifyDataSetChanged();
    }

    @Override
    public void openDialogAddDevice() {
        Context mContext = getActivity();
        if (mContext != null) {
            View actionDialog = LayoutInflater.from(mContext).inflate(R.layout.custom_dialog_add_device, null);
            final EditText addressInput = actionDialog.findViewById(R.id.editTextAddress);
            addressInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!s.toString().isEmpty()) {
                        if (s.toString().matches("\\d+(?:\\.\\d+)?")) {
                            if (Integer.parseInt(s.toString()) > Device.ADDRESS_MAXIMUM) {
                                addressInput.setText(String.valueOf(Device.ADDRESS_MAXIMUM));
                                addressInput.setSelection(addressInput.getText().length());
                            }
                        } else
                            addressInput.setText("");
                    }
                }
            });
            final Spinner typeSpinner = actionDialog.findViewById(R.id.type_spinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                    android.R.layout.simple_spinner_item, Device.TypeCode.getTypeListString());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(adapter);
            final EditText nameInput = actionDialog.findViewById(R.id.editTextDeviceName);
            nameInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!Device.isNameValid(s.toString())) {
                        String value = nameInput.getText().toString();
                        if (value.length() > 0)
                            nameInput.setText(new String(value.toCharArray(), 0, (value.length() - 1)));
                        nameInput.setSelection(nameInput.getText().length());
                    }
                }
            });

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
            alertDialogBuilder.setView(actionDialog);
            alertDialogBuilder.setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (addressInput.getText().length() > 0) {
                                        int type = typeSpinner.getSelectedItemPosition();
                                        int address = Integer.parseInt(addressInput.getText().toString());
                                        String name = nameInput.getText().toString();
                                        if (address != Device.BROADCAST_ADDRESS && mPresenterInt != null) {
                                            mPresenterInt.onButtonDialogAddDeviceClicked(name, (byte) address, (byte) type);
                                        }
                                    }
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            }).setTitle(R.string.custom_dialog_add_device_title);
            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }
    }

    //*************************From ExpandableListAdapter*************************

    /**
     * Button read in the expand view is clicked.
     *
     * @param address Address of the device concerned by the button
     */
    void onButtonReadClicked(byte address) {
        if (mPresenterInt != null)
            mPresenterInt.onButtonReadClicked(address);
    }

    /**
     * Button read in the expand view is clicked.
     *
     * @param address Address of the device concerned by the button
     * @param action  Action chose
     */
    void onButtonActionClicked(byte address, byte action) {
        if (mPresenterInt != null)
            mPresenterInt.onButtonActionClicked(address, action);
    }

    /**
     * Button delete in the expand view is clicked.
     *
     * @param address Address of the device concerned by the button
     */
    void onButtonDeleteClicked(byte address) {
        if (mPresenterInt != null)
            mPresenterInt.onButtonDeleteClicked(address);
    }
}
