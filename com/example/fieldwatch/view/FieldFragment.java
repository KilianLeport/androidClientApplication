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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;

import com.example.fieldwatch.model.Device;
import com.example.fieldwatch.R;
import com.example.fieldwatch.presenter.FieldFragmentPresenter;
import com.example.fieldwatch.presenter.FieldFragmentPresenterInt;

import java.util.List;
/**
 *  This class show the view of the field.
 *
 * @author Kilian Leport
 *
 */
public class FieldFragment extends Fragment implements FieldFragmentInt, View.OnClickListener, FieldView.OnFieldViewEvent {
    private static final String TAG = "FieldFragment";
    private FieldView mFieldVIew;
    private ToggleButton mButtonEditView;
    private FieldFragmentPresenterInt mPresenterInt;

    public FieldFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_field_view, container, false);
        mPresenterInt = new FieldFragmentPresenter(((MainActivity) getActivity()).getModelManager());
        mFieldVIew = rootView.findViewById(R.id.layout_field_view);
        Button mButtonActualize = rootView.findViewById(R.id.button_actualize);
        mButtonActualize.setOnClickListener(this);
        Button mButtonClearAll = rootView.findViewById(R.id.button_clear_all_device);
        mButtonClearAll.setOnClickListener(this);
        mButtonEditView = rootView.findViewById(R.id.button_edit_view);
        mButtonEditView.setOnClickListener(this);
        Log.d(TAG, "onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)");
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFieldVIew.addOnMessageReceivedListener(this);
        mPresenterInt.onViewAttached(this);

        //init the view
        mPresenterInt.onButtonEditViewClicked(mButtonEditView.isChecked());
    }

    @Override
    public void onPause() {
        super.onPause();
        mFieldVIew.removeOnMessageReceivedListener();
        mPresenterInt.onViewDetached();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_actualize:
                if (mPresenterInt != null)
                    mPresenterInt.onButtonActualizeClicked();
                break;
            case R.id.button_clear_all_device:
                if (mPresenterInt != null)
                    mPresenterInt.onButtonClearListClicked();
                break;
            case R.id.button_edit_view:
                if (mPresenterInt != null)
                    mPresenterInt.onButtonEditViewClicked(mButtonEditView.isChecked());
                break;
            default:
                break;
        }
    }

    @Override
    public void onDeviceMoved(byte address, int newCoordinateX, int newCoordinateY) {
        if (mPresenterInt != null)
            mPresenterInt.onDeviceMoved(address, newCoordinateX, newCoordinateY);
    }

    //*************************From Presenter*************************
    @Override
    public void onDeviceListChange(List<Device> deviceList) {
        mFieldVIew.onDeviceListChange(deviceList);
    }

    @Override
    public void enableFieldEdition(boolean enable) {
        mFieldVIew.setEditable(enable);
    }
}
