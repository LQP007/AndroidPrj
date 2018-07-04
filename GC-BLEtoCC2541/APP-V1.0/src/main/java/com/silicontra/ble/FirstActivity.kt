package com.silicontra.ble

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.drm.DrmStore
import android.os.Bundle
import kotlinx.android.synthetic.main.layout_first.*
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.R.attr.button
import android.app.usage.UsageEvents
import android.content.Context
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_measure_view.*
import kotlinx.android.synthetic.main.layout_second.*
import kotlinx.android.synthetic.main.tile.*
import java.util.*
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS




class FirstActivity() :Activity(){
    var Cnt:Int?=0
    private val nameArray = arrayOf("水星", "金星", "地球", "火星", "木星", "土星")
    private val descArray = arrayOf("水星是太阳系八大行星最内侧也是最小的一颗行星，也是离太阳最近的行星", "金星是太阳系八大行星之一，排行第二，距离太阳0.725天文单位", "地球是太阳系八大行星之一，排行第三，也是太阳系中直径、质量和密度最大的类地行星，距离太阳1.5亿公里", "火星是太阳系八大行星之一，排行第四，属于类地行星，直径约为地球的53%", "木星是太阳系八大行星中体积最大、自转最快的行星，排行第五。它的质量为太阳的千分之一，但为太阳系中其它七大行星质量总和的2.5倍", "土星为太阳系八大行星之一，排行第六，体积仅次于木星")
    private var planetList = mutableListOf<Planet>()
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE)
        setContentView(R.layout.layout_first)
        window.setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.tile)
//        setTitle(R.layout.tile)
//        val planet= PlanetAdapter(this,Planet.defaultList)
//        lv_View.adapter = planet

        val planet2= PlanetAdapter(this, planetList)
        lv_View.adapter = planet2

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        bt_connect.setOnClickListener() {
            planet2.Addlist(Planet(ev_name.text.toString(),ev_sex.text.toString(),ev_age.text.toString()))
            planet2.notifyDataSetChanged()
            ev_name.setText("")
            ev_sex.setText("")
            ev_age.setText("")
            ev_name.requestFocus()
//            ev_name.isCursorVisible = true
//            ev_sex.isCursorVisible = false
//            ev_age.isCursorVisible = false
//            if (flg_change == true) {
//                Cnt= Cnt!! + 1
//                tv_ButtonNum.text = "我来加加${Cnt.toString()}"
//                vibrator.vibrate(1000.toLong())
//            Log.d("BLE","切换ACTIVITY")
////                flg_change =false
//            }
//            else{
//                tv_ButtonNum.text = "567"
//                flg_change = true
//            }
        }
//        bt_titleBk.setOnClickListener {
//            //            if (flg_change == true) {
//            Cnt= Cnt!! + 1
////            tv_ButtonNum.text = "我来加加${Cnt.toString()}"
//            vibrator.vibrate(1000.toLong())
//            Log.d("BLE","BLE不支持")
////                flg_change =false
//
////            }
////            else{
////                tv_ButtonNum.text = "567"
////                flg_change = true
////            }
//        }
//        bt_connect.setOnTouchListener(object : View.OnTouchListener {
//            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
//                //按下操作
//                if (motionEvent.action == MotionEvent.ACTION_UP) {
//                    bt_connect.setBackgroundResource(R.drawable.blue2blue)
//                    Tofuntion(this@FirstActivity).disp(tv_listtile12)
//                    Cnt= Cnt!! + 1
//
//                }
//                //抬起操作
//                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
//                    bt_connect.setBackgroundResource(R.drawable.bluett)
//                }
//                //移动操作
//                if (motionEvent.action == MotionEvent.ACTION_MOVE) {
//
//                }
//                return false
//            }
//        })

        bt_ConfigNet.setOnTouchListener(object : View.OnTouchListener {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                //按下操作
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    bt_connect.setBackgroundResource(R.drawable.blue2blue)
                    val intent = Intent(this@FirstActivity, SecondActivity::class.java)
                    startActivity(intent)
                }
                //抬起操作
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    bt_connect.setBackgroundResource(R.drawable.bluett)
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
                    val intent = Intent(this@FirstActivity, DeviceScanActivity::class.java)
                    startActivity(intent)
                }
                //抬起操作
                if (motionEvent.action == MotionEvent.ACTION_UP) {

                }
                //移动操作
                if (motionEvent.action == MotionEvent.ACTION_MOVE) {

                }
                return false
            }
        })
    }

    private fun hideSoftKeyBoard(windowToken: IBinder?) {
        if (windowToken != null) {
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im!!.hideSoftInputFromWindow(windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

}




