/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.silicontra.ble

import android.app.Activity
import android.app.ListActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.util.Log
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.activity_devicescan.*
import kotlinx.android.synthetic.main.dialog.*
import kotlinx.android.synthetic.main.layout_first.*
import kotlinx.android.synthetic.main.tile.*
import java.util.*

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
class DeviceScanActivity : Activity() {
    private var mLeDeviceListAdapter: LeDeviceListAdapter? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mScanning: Boolean = false
    private var mHandler: Handler? = null

    // Device scan callback.
    private val mLeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        runOnUiThread {
            mLeDeviceListAdapter!!.addDevice(device)
            mLeDeviceListAdapter!!.addDeviceRSSI(rssi)
//            mLeDeviceListAdapter!!.notifyDataSetChanged()
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE)//获取自定义标题栏权限
        setContentView(R.layout.activity_devicescan)
        window.setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.tile)//加载自定义标题栏

        mHandler = Handler()
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            finish()
        }
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        DealButton()//按键处理
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.main, menu)
//        if (!mScanning) {
//            menu.findItem(R.id.menu_stop).isVisible = false
//            menu.findItem(R.id.menu_scan).isVisible = true
//            menu.findItem(R.id.menu_refresh).actionView = null
//        } else {
//            menu.findItem(R.id.menu_stop).isVisible = true
//            menu.findItem(R.id.menu_scan).isVisible = false
//            menu.findItem(R.id.menu_refresh).setActionView(
//                    R.layout.actionbar_indeterminate_progress)
//        }
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.menu_scan -> {
//                mLeDeviceListAdapter!!.clear()
//                scanLeDevice(true)
//            }
//            R.id.menu_stop -> scanLeDevice(false)
//        }
//        return true
//    }

    override fun onResume() {
        super.onResume()

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter!!.isEnabled) {
            if (!mBluetoothAdapter!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = LeDeviceListAdapter()
        lv_BLElist.adapter = mLeDeviceListAdapter
        scanLeDevice(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish()
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        super.onPause()
        scanLeDevice(false)
        mLeDeviceListAdapter!!.clear()
    }

//    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
//        val device = mLeDeviceListAdapter!!.getDevice(position) ?: return
//        val intent = Intent(this, DeviceControlActivity::class.java)
//        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.name)
//        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.address)
//        if (mScanning) {
//            mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
//            mScanning = false
//        }
//        startActivity(intent)
//    }

    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler!!.postDelayed({
                mScanning = false
                mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
//                invalidateOptionsMenu()
            }, SCAN_PERIOD)

            mScanning = true
            mBluetoothAdapter!!.startLeScan(mLeScanCallback)
        } else {
            mScanning = false
            mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
        }
//        invalidateOptionsMenu()
    }
    private fun DealButton() {
        bt_titleBk.setOnClickListener {
            scanLeDevice(false)
            mLeDeviceListAdapter!!.clear()
            scanLeDevice(true)
        }

        lv_BLElist.setOnItemClickListener(
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    val device = mLeDeviceListAdapter!!.getDevice(position)
                    //val intent = Intent(this, DeviceControlActivity::class.java)
                    val intent = Intent(this, UserControlUI::class.java)
                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device!!.name)
                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device!!.address)
                    if (mScanning) {
                        mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
                        mScanning = false
                    }
                    startActivity(intent)
                })
    }
    // Adapter for holding devices found through scanning.
    private inner class LeDeviceListAdapter : BaseAdapter() {
        private val mLeDevices: ArrayList<BluetoothDevice>
        private val mLeDevicesRSSI: ArrayList<Int>
        private val mInflator: LayoutInflater

        init {
            mLeDevices = ArrayList()
            mLeDevicesRSSI = ArrayList()
            mInflator = this@DeviceScanActivity.layoutInflater
        }

        fun addDevice(device: BluetoothDevice) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device)
                this.notifyDataSetChanged()
            }
        }
        fun addDeviceRSSI(rssi: Int) {
            if (!mLeDevicesRSSI.contains(rssi)) {
                mLeDevicesRSSI.add(rssi)
                this.notifyDataSetChanged()
            }
        }
        fun getDevice(position: Int): BluetoothDevice? {
            return mLeDevices[position]
        }

        fun clear() {
            mLeDevices.clear()
            mLeDevicesRSSI.clear()
            this.notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return mLeDevices.size
        }

        override fun getItem(i: Int): Any {
            return mLeDevices[i]
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
            var view = view
            val viewHolder: ViewHolder
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null)
                viewHolder = ViewHolder()
                viewHolder.deviceAddress = view!!.findViewById<View>(R.id.device_address) as TextView
                viewHolder.deviceName = view.findViewById<View>(R.id.device_name) as TextView
                viewHolder.deviceRSSI = view.findViewById<View>(R.id.device_rssi) as TextView
                view.tag = viewHolder
            } else {
                viewHolder = view.tag as ViewHolder
            }

            val device = mLeDevices[i]
            val rssi = mLeDevicesRSSI[i]
            val deviceName = device.name
            if (deviceName != null && deviceName.length > 0)
                viewHolder.deviceName!!.text = deviceName
            else
                viewHolder.deviceName!!.setText(R.string.unknown_device)
            viewHolder.deviceAddress!!.text = device.address
            viewHolder.deviceRSSI!!.text = rssi.toString()

            return view
        }
    }

    internal class ViewHolder {
        var deviceName: TextView? = null
        var deviceAddress: TextView? = null
        var deviceRSSI: TextView? = null
    }

    companion object {

        private val REQUEST_ENABLE_BT = 1
        // Stops scanning after 10 seconds.
        private val SCAN_PERIOD: Long = 10000
    }
}