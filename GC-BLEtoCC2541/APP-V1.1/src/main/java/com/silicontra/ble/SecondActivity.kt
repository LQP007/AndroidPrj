package com.silicontra.ble

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.Window
import kotlinx.android.synthetic.main.activity_measure_view.*
import kotlinx.android.synthetic.main.layout_first.*
import kotlinx.android.synthetic.main.tile.*
import java.util.*

class SecondActivity: Activity() {

    private val nameArray = arrayOf("水星", "金星", "地球", "火星", "木星", "土星")
    private val descArray = arrayOf("水星是太阳系八大行星最内侧也是最小的一颗行星，也是离太阳最近的行星", "金星是太阳系八大行星之一，排行第二，距离太阳0.725天文单位", "地球是太阳系八大行星之一，排行第三，也是太阳系中直径、质量和密度最大的类地行星，距离太阳1.5亿公里", "火星是太阳系八大行星之一，排行第四，属于类地行星，直径约为地球的53%", "木星是太阳系八大行星中体积最大、自转最快的行星，排行第五。它的质量为太阳的千分之一，但为太阳系中其它七大行星质量总和的2.5倍", "土星为太阳系八大行星之一，排行第六，体积仅次于木星")
    var Cnt = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE)
        setContentView(R.layout.activity_measure_view)
        window.setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.tile)
        tv_title.text = "第二屏"

//        val planet= PlanetAdapter(this,Planet.defaultList)
//        lv_planet.adapter = planet

        val planetList = mutableListOf<Planet>()
//        planetList.add(Planet(nameArray[0],descArray[2]) )
        val planet2= PlanetAdapter(this, planetList)
        lv_planet2.adapter = planet2
//        PlanetAdapter(this, planetList).Addlist(Planet(nameArray[2],descArray[2]))
//        PlanetAdapter(this, planetList).Addlist(Planet(nameArray[3],descArray[0]))
//        PlanetAdapter(this, planetList).Addlist(Planet(nameArray[1],descArray[2]))
//        PlanetAdapter(this, planetList).Addlist(Planet(nameArray[0],descArray[1]))
//        val adapter2 = PlanetAdapter(this, Planet.defaultList)
//        nslv_planet.adapter = adapter2
        bt_titleBk.setOnClickListener {

            Cnt++
            if(Cnt ==4)Cnt=0

            when(Cnt){
                1 ->{
                    lv_planet2.setSelection(1)
                }
                2 ->{
                    lv_planet2.setSelection(2)
                }
                3 ->{
                    lv_planet2.setSelection(3)
                }
                else->{

                }
            }

        }
    }

}

