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
import android.app.AlertDialog
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.SimpleExpandableListAdapter
import android.widget.TextView
import java.util.ArrayList
import java.util.HashMap
import kotlin.experimental.and

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with `BluetoothLeService`, which in turn interacts with the
 * Bluetooth LE API.
 */
class DeviceControlActivity : Activity() {

    private var mConnectionState: TextView? = null
    private var mDataField: TextView? = null
    private var mDeviceName: String? = null
    private var mDeviceAddress: String? = null
    private var mGattServicesList: ExpandableListView? = null
    private var mBluetoothLeService: BluetoothLeService? = null
    private var mGattCharacteristics: ArrayList<ArrayList<BluetoothGattCharacteristic>>? = ArrayList()
    private var mConnected = false
    private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null

    private val LIST_NAME = "NAME"
    private val LIST_UUID = "UUID"
    //byte[] WriteBytes = null;
    internal var WriteBytes = ByteArray(20)
    // Code to manage Service lifecycle.
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mBluetoothLeService = (service as BluetoothLeService.LocalBinder).service
            if (!mBluetoothLeService!!.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth")
                finish()
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService!!.connect(mDeviceAddress)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBluetoothLeService = null
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothLeService.ACTION_GATT_CONNECTED == action) {
                mConnected = true
                //updateConnectionState(R.string.connected)
                invalidateOptionsMenu()
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED == action) {
                mConnected = false
               // updateConnectionState(R.string.disconnected)
                invalidateOptionsMenu()
                clearUI()
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED == action) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService!!.supportedGattServices)
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE == action) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA))
            }
        }
    }
    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private val servicesListClickListner = ExpandableListView.OnChildClickListener { parent, v, groupPosition, childPosition, id ->
        val et: EditText  //申明变量

        et = EditText(parent.context) //创建对象
        et.setSingleLine(true)  //设置属性

        val etHex: EditText  //申明变量
        etHex = EditText(parent.context) //创建对象
        etHex.setSingleLine(true)  //设置属性

        if (mGattCharacteristics != null) {
            val characteristic = mGattCharacteristics!![groupPosition][childPosition]
            val charaProp = characteristic.properties

            //如果该char可写
            if (charaProp or BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {


                val factory = LayoutInflater.from(parent.context)
                val textEntryView = factory.inflate(R.layout.dialog, null)
                val editTextName = textEntryView.findViewById<View>(R.id.et_UUIDrec) as EditText
                val editTextNumEditText = textEntryView.findViewById<View>(R.id.et_UUIDrec) as EditText
                val ad1 = AlertDialog.Builder(parent.context)
                ad1.setTitle("WriteCharacteristic")
                ad1.setView(textEntryView)
                ad1.setPositiveButton("确定") { dialog, i ->
                    val value = ByteArray(20)
                    value[0] = 0x00.toByte()
                    if (editTextName.text.length > 0) {
                        //write string
                        WriteBytes = editTextName.text.toString().toByteArray()
                    } else if (editTextNumEditText.text.length > 0) {
                        WriteBytes = hex2byte(editTextNumEditText.text.toString().toByteArray())
                    }
                    characteristic.setValue(value[0].toInt(),
                            BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                    characteristic.value = WriteBytes

                    mBluetoothLeService!!.writeCharacteristic(characteristic)
                }
                ad1.setNegativeButton("取消") { dialog, i -> }
                ad1.show()

            }
            if (charaProp or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                mNotifyCharacteristic = characteristic
                mBluetoothLeService!!.setCharacteristicNotification(
                        characteristic, true)
            }
            return@OnChildClickListener true
        }
        false
    }

    private fun clearUI() {
        mGattServicesList!!.setAdapter(null as SimpleExpandableListAdapter?)
        mDataField!!.setText(R.string.no_data)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE)//获取自定义标题栏权限
        setContentView(R.layout.gatt_services_characteristics)
        window.setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.tile)//加载自定义标题栏

        val intent = intent
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME)
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS)

        // Sets up UI references.
        (findViewById<View>(R.id.device_address) as TextView).text = mDeviceAddress
        mGattServicesList = findViewById<View>(R.id.gatt_services_list) as ExpandableListView
        mGattServicesList!!.setOnChildClickListener(servicesListClickListner)
        mConnectionState = findViewById<View>(R.id.connection_state) as TextView
        mDataField = findViewById<View>(R.id.data_value) as TextView

//        actionBar!!.title = mDeviceName
//        actionBar!!.setDisplayHomeAsUpEnabled(true)
        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
        if (mBluetoothLeService != null) {
            val result = mBluetoothLeService!!.connect(mDeviceAddress)
            Log.d(TAG, "Connect request result=$result")
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mGattUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
        mBluetoothLeService = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.gatt_services, menu)
        if (mConnected) {
            menu.findItem(R.id.menu_connect).isVisible = false
            menu.findItem(R.id.menu_disconnect).isVisible = true
        } else {
            menu.findItem(R.id.menu_connect).isVisible = true
            menu.findItem(R.id.menu_disconnect).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_connect -> {
                mBluetoothLeService!!.connect(mDeviceAddress)
                return true
            }
            R.id.menu_disconnect -> {
                mBluetoothLeService!!.disconnect()
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    private fun updateConnectionState(resourceId: Int) {
//        runOnUiThread { mConnectionState!!.setText(resourceId) }
//    }

    private fun displayData(data: String?) {
        if (data != null) {
            mDataField!!.text = data
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return
        var uuid: String? = null
        val unknownServiceString = resources.getString(R.string.unknown_service)
        val unknownCharaString = resources.getString(R.string.unknown_characteristic)
        val gattServiceData = ArrayList<HashMap<String, String>>()
        val gattCharacteristicData = ArrayList<ArrayList<HashMap<String, String>>>()
        mGattCharacteristics = ArrayList()

        // Loops through available GATT Services.
        for (gattService in gattServices) {
            val currentServiceData = HashMap<String, String>()
            uuid = gattService.uuid.toString()
            currentServiceData[LIST_NAME] = SampleGattAttributes.lookup(uuid, unknownServiceString)
            currentServiceData[LIST_UUID] = uuid
            gattServiceData.add(currentServiceData)

            val gattCharacteristicGroupData = ArrayList<HashMap<String, String>>()
            val gattCharacteristics = gattService.characteristics
            val charas = ArrayList<BluetoothGattCharacteristic>()

            // Loops through available Characteristics.
            for (gattCharacteristic in gattCharacteristics) {
                charas.add(gattCharacteristic)
                val currentCharaData = HashMap<String, String>()
                uuid = gattCharacteristic.uuid.toString()
                currentCharaData[LIST_NAME] = SampleGattAttributes.lookup(uuid, unknownCharaString)
                currentCharaData[LIST_UUID] = uuid
                gattCharacteristicGroupData.add(currentCharaData)
            }
            mGattCharacteristics!!.add(charas)
            gattCharacteristicData.add(gattCharacteristicGroupData)
        }

        val gattServiceAdapter = SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                arrayOf(LIST_NAME, LIST_UUID),
                intArrayOf(android.R.id.text1, android.R.id.text2),
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                arrayOf(LIST_NAME, LIST_UUID),
                intArrayOf(android.R.id.text1, android.R.id.text2)
        )
        mGattServicesList!!.setAdapter(gattServiceAdapter)
    }

    companion object {
        private val TAG = DeviceControlActivity::class.java.simpleName

        val EXTRAS_DEVICE_NAME = "DEVICE_NAME"
        val EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS"

        fun bin2hex(bin: String): String {
            val digital = "0123456789ABCDEF".toCharArray()
            val sb = StringBuffer("")
            val bs = bin.toByteArray()
            var bit: Int
            for (i in bs.indices) {
                bit = (bs[i] and 0x0f0.toByte()).toInt() shr 4
                sb.append(digital[bit])
                bit = (bs[i] and 0x0f).toInt()
                sb.append(digital[bit])
            }
            return sb.toString()
        }

        fun hex2byte(b: ByteArray): ByteArray {
            var b = b
            if (b.size % 2 != 0) {
                throw IllegalArgumentException("长度不是偶数")
            }
            val b2 = ByteArray(b.size / 2)
            var n = 0
            while (n < b.size) {
                val item = String(b, n, 2)
                // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个进制字节
                b2[n / 2] = Integer.parseInt(item, 16).toByte()
                n += 2
            }
            return b2
        }

        private fun makeGattUpdateIntentFilter(): IntentFilter {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
            intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
            return intentFilter
        }
    }
}
