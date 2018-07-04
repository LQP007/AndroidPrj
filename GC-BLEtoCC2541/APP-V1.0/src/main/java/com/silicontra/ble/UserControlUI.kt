package com.silicontra.ble

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.layout_usercontrol.*
import kotlinx.android.synthetic.main.tile.*
import java.util.ArrayList
import java.util.HashMap
import kotlin.experimental.and


class UserControlUI:Activity() {
    private var mDeviceName: String? = null
    private var mDeviceAddress: String? = null
    var senddatacnt : Int =0
    var userDataAdapter : PlanetAdapter? = null
    val planetList = mutableListOf<Planet>()
    var FlagGetGattSevice : Boolean = false
    private var mGattCharacteristics: ArrayList<ArrayList<BluetoothGattCharacteristic>>? = ArrayList()
    private var mBluetoothLeService: BluetoothLeService? = null
    private var bleGattCharacteristicRead: BluetoothGattCharacteristic? = null
    private var bleGattCharacteristicWrite: BluetoothGattCharacteristic? = null
    private var bleGattCharacteristicGetVersion: BluetoothGattCharacteristic? = null
    private var bleGattCharacteristicGetModuleMaker: BluetoothGattCharacteristic? = null
    private var mUUIDnoty = "0000ff12"
    private var mUUIDwrite = "0000ff11"

    internal var WriteBytes = ByteArray(20)
    // Code to manage Service lifecycle.
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mBluetoothLeService = (service as BluetoothLeService.LocalBinder).service
            if (!mBluetoothLeService!!.initialize()) {
                Log.e("BLE", "Unable to initialize Bluetooth")
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
//                mConnected = true
                userDataAdapter!!.Addlist(
                        Planet("   ", "BLE设备连接成功", "APP"))
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED == action) {
//                mConnected = false
                userDataAdapter!!.Addlist(
                        Planet("   ", "BLE设备断开连接……", "APP"))
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED == action) {
                // Show all the supported services and characteristics on the user interface.
                getGattServices(mBluetoothLeService!!.supportedGattServices)
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE == action) {
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA))

                when(BluetoothLeService.UUID_BRread!!){
                    ConstantUtils.UUID_NOTIFY->{
                        //显示的数据会在后台BluetoothLeService.broadcastUpdate方法中选择str或者hex
                        userDataAdapter!!.Addlist(Planet("BLE",
                                intent.getStringExtra(BluetoothLeService.EXTRA_DATA), "   "))
                        lv_userdata.setSelection(userDataAdapter!!.count)
                    }
                    ConstantUtils.UUID_GetModuleMaker->{
                        if(FlagGetGattSevice){
                            tv_ModuleMaker.text = intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
                            FlagGetGattSevice = false
                            userDataAdapter!!.Addlist(
                                    Planet("   ", "UUID加载完成", "APP"))
                        }
                    }
                    ConstantUtils.UUID_GetModuleSWversion->{
                        if(FlagGetGattSevice){
                            tv_ModuleSWversion.text = intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
                        }
                        mBluetoothLeService!!.readCharacteristic(bleGattCharacteristicGetModuleMaker!!)

                    }
                }

            }
        }
    }
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE)//获取自定义标题栏权限
        setContentView(R.layout.layout_usercontrol)
        window.setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.tile)//加载自定义标题栏

        bt_titleBk.text = getResources().getString(R.string.tiltebt_declare)
        tv_title.text = getResources().getString(R.string.tiltename_chat)

        val intent = intent
        mDeviceName = intent.getStringExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME)
        mDeviceAddress = intent.getStringExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS)

        tv_ModuleTYPE.text =mDeviceName
        tv_ModuleMAC.text =mDeviceAddress

        Flag_tb_SendStr = true
        Flag_tb_RecStr = true
        FlagGetGattSevice = true

        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)

        userDataAdapter= PlanetAdapter(this, planetList)
        lv_userdata.adapter = userDataAdapter
        userDataAdapter!!.Addlist(
                Planet("   ", "正在加载UUID……", "APP"))
        DealButton()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
        if (mBluetoothLeService != null) {
            val result = mBluetoothLeService!!.connect(mDeviceAddress)
            Log.d(TAG, "Connect request result=$result")
        }

    }
    //暂停状态，切换到其他应用时
    override fun onPause() {
        super.onPause()
        unregisterReceiver(mGattUpdateReceiver)

    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
        mBluetoothLeService = null
    }
    private fun DealButton(){
        bt_senddata.setOnTouchListener(object : View.OnTouchListener {
            @SuppressLint("ResourceAsColor")
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                //按下操作
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    if(bleGattCharacteristicWrite == null){
                        Toast.makeText(this@UserControlUI, R.string.BLEGetGATTtoast, Toast.LENGTH_SHORT).show()
                        return false
                    }
                    senddatacnt++
                    if(et_senddata.text.toString() != "") {
                        //将发送数据用listview显示出来
                        userDataAdapter!!.Addlist(
                                Planet("   ", et_senddata.text!!.toString(), "APP"))
                        //将发送数据通过蓝牙发送出去
                        if (et_senddata.text.length > 0) {
                            //write string
                            if(Flag_tb_SendStr!!){//字符串显示
                                WriteBytes = et_senddata.text.toString().toByteArray()
                            }
                            else{//HEX显示，即十六进制显示
                                WriteBytes = hex2byte(et_senddata.text.toString().toByteArray())
                            }
                        }
//                        bleGattCharacteristicWrite!!.setValue(value[0].toInt(),
//                                BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                        bleGattCharacteristicWrite!!.value = WriteBytes
                        mBluetoothLeService!!.writeCharacteristic(bleGattCharacteristicWrite!!)

                    }
                    //清空输入框的内容
                    et_senddata.setText("")
                    //设置listview的显示光标在最新一行
                    lv_userdata.setSelection(userDataAdapter!!.count)
                }
                //抬起操作
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {

                }
                //移动操作
                if (motionEvent.action == MotionEvent.ACTION_MOVE) {

                }
                return false
            }
        })
        im_logo.setOnTouchListener(object : View.OnTouchListener {
            @SuppressLint("ResourceAsColor", "ClickableViewAccessibility")
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                //按下操作
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    var tFlag_tb_Send:Boolean?=null
                    var tFlag_tb_Rec:Boolean?=null
                    val factory = LayoutInflater.from(this@UserControlUI)
                    val textEntryView = factory.inflate(R.layout.dialog, null)
                    val et_UUIDrec = textEntryView.findViewById<View>(R.id.et_UUIDrec) as EditText
                    val et_UUIDsend = textEntryView.findViewById<View>(R.id.et_UUIDsend) as EditText
                    val tb_rec = textEntryView.findViewById<View>(R.id.tb_rec) as ToggleButton
                    val tb_send = textEntryView.findViewById<View>(R.id.tb_send) as ToggleButton
                    val ad1 = AlertDialog.Builder(this@UserControlUI)
                    et_UUIDrec.setText(mUUIDnoty)
                    et_UUIDsend.setText(mUUIDwrite)
                    tb_rec.isChecked = Flag_tb_RecStr!!
                    tb_send.isChecked = Flag_tb_SendStr!!
                    tFlag_tb_Rec = Flag_tb_RecStr!!
                    tFlag_tb_Send = Flag_tb_SendStr!!
                    tb_rec.setOnCheckedChangeListener { buttonView, isChecked ->
                        tFlag_tb_Rec = isChecked
                    }
                    tb_send.setOnCheckedChangeListener { buttonView, isChecked ->
                        tFlag_tb_Send = isChecked
                    }
                    ad1.setTitle("Setting")
                    ad1.setView(textEntryView)
                    ad1.setPositiveButton("确定") { dialog, i ->
                        //初始化控件
                        mUUIDwrite=et_UUIDsend.text.toString()
                        mUUIDnoty=et_UUIDrec.text.toString()
                        Flag_tb_RecStr = tFlag_tb_Rec!!
                        Flag_tb_SendStr = tFlag_tb_Send!!
                        //重新加载GATT的UUID
                        getGattServices(mBluetoothLeService!!.supportedGattServices)
                        //
                        if(!Flag_tb_SendStr!!){
                            et_senddata.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        }
                        else{
                            et_senddata.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                        }
                    }
                    ad1.setNegativeButton("取消") { dialog, i -> }
                    ad1.show()
                }
                //抬起操作
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {

                }
                //移动操作
                if (motionEvent.action == MotionEvent.ACTION_MOVE) {

                }
                return false
            }
        })
        bt_titleBk.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                //按下操作
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    val factory = LayoutInflater.from(this@UserControlUI)
                    val textEntryView = factory.inflate(R.layout.dialog_declare, null)
                    val ad1 = AlertDialog.Builder(this@UserControlUI)
                    ad1.setTitle("操作说明")
                    ad1.setView(textEntryView)
//                    ad1.setPositiveButton("确定") { dialog, i ->
//
//                    }
                    ad1.setNegativeButton("关闭") { dialog, i -> }
                    ad1.show()
                }
                //抬起操作
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {

                }
                //移动操作
                if (motionEvent.action == MotionEvent.ACTION_MOVE) {

                }
                return false
            }
        })
        et_senddata.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.isNullOrEmpty()){
                    return
                }
                if(Flag_tb_SendStr!!){
                    return
                }
                when(s!![s.length-1]){
                    in '1'..'9'->{}
                    in 'a'..'f'->{}
                    in 'A'..'F'->{}
                    else ->{
                        Toast.makeText(this@UserControlUI, "请输入正确的字符：0~9，a~f或者A~F", Toast.LENGTH_SHORT).show()
                        et_senddata.text!!.delete(s.length-1,s.length)
                    }
                }
            }
        })
    }
    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private fun getGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return
        bleGattCharacteristicGetVersion = null
        bleGattCharacteristicWrite =null
        bleGattCharacteristicGetModuleMaker = null
        bleGattCharacteristicRead = null
        mGattCharacteristics = ArrayList()
        // Loops through available GATT Services.
        for (gattService in gattServices) {
            val currentServiceData = HashMap<String, String>()
            val gattCharacteristics = gattService.characteristics
            val charas = ArrayList<BluetoothGattCharacteristic>()

            ConstantUtils.UUID_Write = mUUIDwrite+ConstantUtils.UUID_Base
            ConstantUtils.UUID_NOTIFY = mUUIDnoty+ConstantUtils.UUID_Base
            // Loops through available Characteristics.
            for (gattCharacteristic in gattCharacteristics) {
                charas.add(gattCharacteristic)
                if (gattCharacteristic.uuid.toString()
                                .equals(ConstantUtils.UUID_NOTIFY, ignoreCase = true)){
                    bleGattCharacteristicRead = gattCharacteristic as BluetoothGattCharacteristic
                    mBluetoothLeService!!.setCharacteristicNotification(bleGattCharacteristicRead!!, true)
                }
                if (gattCharacteristic.uuid.toString()
                                .equals(ConstantUtils.UUID_Write, ignoreCase = true)){
                    bleGattCharacteristicWrite = gattCharacteristic as BluetoothGattCharacteristic
                }
                //读取BLE模组的生产商的UUID
                if (gattCharacteristic.uuid.toString()
                                .equals(ConstantUtils.UUID_GetModuleMaker, ignoreCase = true)){
                    bleGattCharacteristicGetModuleMaker = gattCharacteristic as BluetoothGattCharacteristic
                }
                //读取BLE模组的软件版本的UUID
                if (gattCharacteristic.uuid.toString()
                                .equals(ConstantUtils.UUID_GetModuleSWversion, ignoreCase = true)){
                    bleGattCharacteristicGetVersion = gattCharacteristic as BluetoothGattCharacteristic
                    mBluetoothLeService!!.readCharacteristic(bleGattCharacteristicGetVersion!!)
                }
            }
            //mGattCharacteristics!!.add(charas)
        }
        if(bleGattCharacteristicWrite == null){
            userDataAdapter!!.Addlist(
                    Planet("   ", "未找到可用的串口发送UUID", "APP"))
//            Toast.makeText(this, R.string.BLEGetGATTtoast, Toast.LENGTH_SHORT).show()
        }
        if(bleGattCharacteristicRead == null){
            userDataAdapter!!.Addlist(
                    Planet("   ", "未找到可用的串口接收UUID", "APP"))
//            Toast.makeText(this, R.string.BLEGetGATTtoast, Toast.LENGTH_SHORT).show()
        }

    }
    companion object {
        private val TAG = DeviceControlActivity::class.java.simpleName

        var Flag_tb_SendStr:Boolean?=true
        var Flag_tb_RecStr:Boolean?=true
        fun bin2hex(bin: String): String {
            val digital = "0123456789ABCDEF ".toCharArray()
            val sb = StringBuffer("")
            val bs = bin.toByteArray()
            var bit: Int
            for (i in bs.indices) {
                bit = (bs[i] and 0x0f0.toByte()).toInt() shr 4
                sb.append(digital[bit])
                bit = (bs[i] and 0x0f).toInt()
                sb.append(digital[bit])
                sb.append(digital[16])
            }
            return sb.toString()
        }

        fun hex2byte(b: ByteArray): ByteArray {
            var b = b
            var FlagisEven : Boolean = true
            var item : String = String()
            var b2 : ByteArray
            b2 = ByteArray(b.size / 2)
            if (b.size % 2 != 0) {
                //throw IllegalArgumentException("长度不是偶数")
                FlagisEven = false
                b2 =b2+1
            }
            var n = 0
            while (n < b.size) {
                if(n<(b.size-1)){
                    item = String(b, n, 2)
                }
                else{
                    if (FlagisEven == true){
                        item = String(b, n, 2)
                    }
                    else{
                        item = String(b, n, 1)
                    }
                }
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

