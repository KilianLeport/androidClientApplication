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

import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fieldwatch.model.Device;
import com.example.fieldwatch.model.ModelManager;
import com.example.fieldwatch.R;
import com.example.fieldwatch.presenter.MainPresenter;
import com.example.fieldwatch.presenter.MainPresenterInt;

import java.util.List;

/**
 *  This class shows the main activity.
 *
 * @author Kilian Leport
 *
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, MainActivityInt, Switch.OnCheckedChangeListener {
    private static final String TAG = "MainActivity";
    private Button mButtonConnection;
    private Toolbar mToolbar;
    private ModelManager mModelManager;

    private MainPresenterInt mPresenterInt;

    private int COLOR_NOT_CONNECTED;
    private int COLOR_CONNECTED;

    private TextView mViewTextPortServer;
    private TextView mViewTextAddressServer;

    Switch mSwitchAutomaticConnection;
    Switch mSwitchAutomaticUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonConnection = findViewById(R.id.button_connection);
        mButtonConnection.setOnClickListener(this);

        mToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);

        TabLayout mTabLayout = findViewById(R.id.tab_layout);
        COLOR_CONNECTED = ContextCompat.getColor(getApplicationContext(), R.color.connected_state);
        COLOR_NOT_CONNECTED = ContextCompat.getColor(getApplicationContext(), R.color.not_connected_state);

        DeviceFragmentPagerAdapter fragmentViewPager = new DeviceFragmentPagerAdapter(getSupportFragmentManager());

        CustomViewPager mCustomViewPager = findViewById(R.id.customViewpager);
        mCustomViewPager.setAdapter(fragmentViewPager);
        mCustomViewPager.enableSwipe(false);
        mCustomViewPager.setCurrentItem(0);
        mTabLayout.setupWithViewPager(mCustomViewPager);

        mModelManager = new ModelManager(this);
        mPresenterInt = new MainPresenter(mModelManager);

        //Navigation view
        NavigationView nv = findViewById(R.id.nav_view);
        View headerView = nv.getHeaderView(0);
        mSwitchAutomaticConnection = headerView.findViewById(R.id.switch_automatic_connection);
        mSwitchAutomaticUpdate = headerView.findViewById(R.id.switch_automatic_update);
        mSwitchAutomaticConnection.setOnCheckedChangeListener(this);
        mSwitchAutomaticUpdate.setOnCheckedChangeListener(this);
        mSwitchAutomaticConnection.setChecked(true);

        mViewTextPortServer = headerView.findViewById(R.id.text_address_server);
        mViewTextAddressServer = headerView.findViewById(R.id.text_port_server);

        Log.d(TAG, "onCreate(Bundle savedInstanceState)");
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenterInt.onViewAttached(this);

        mPresenterInt.onSwitchAutomaticConnectionClicked(mSwitchAutomaticConnection.isChecked());
        mPresenterInt.onSwitchAutomaticUpdateClicked(mSwitchAutomaticUpdate.isChecked());
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenterInt.onViewDetached();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Get the ModelManager
     *
     * @return ModelManager
     */
    public ModelManager getModelManager() {
        return mModelManager;
    }

    /**
     * Handle click on switch.
     *
     * @param buttonView Switch that changed
     * @param isChecked  State of the switch that called
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switch_automatic_connection:
                mPresenterInt.onSwitchAutomaticConnectionClicked(isChecked);
                break;
            case R.id.switch_automatic_update:
                mPresenterInt.onSwitchAutomaticUpdateClicked(isChecked);
                break;
            default:
                break;
        }
    }

    /**
     * Handle click on buttons.
     *
     * @param v View that was clicked
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_connection:
                mPresenterInt.onToggleButtonConnectionClicked();
                break;
            default:
                break;
        }
    }
    //*************************From Presenter*************************

    @Override
    public void onDeviceListChange(List<Device> deviceList) {

    }

    @Override
    public void setPortServerInformation(final String port) {
        mViewTextPortServer.setText(port);
    }

    @Override
    public void setAddressServerInformation(final String address) {
        mViewTextAddressServer.setText(address);
    }

    @Override
    public void setConnectionButton(final String title, boolean enabled) {
        mButtonConnection.setText(title);
        mButtonConnection.setEnabled(enabled);
    }

    @Override
    public void setWriteInformation(final String information, boolean longTime) {
        int watchTime = Toast.LENGTH_SHORT;
        if (longTime)
            watchTime = Toast.LENGTH_LONG;
        Toast.makeText(this.getApplicationContext(), information, watchTime).show();
    }

    @Override
    public void setColorToolBar(int color) {
        mToolbar.setBackgroundColor(color);
    }

    @Override
    public int getColorNotConnected() {
        return COLOR_NOT_CONNECTED;
    }

    @Override
    public int getColorConnected() {
        return COLOR_CONNECTED;
    }
}
