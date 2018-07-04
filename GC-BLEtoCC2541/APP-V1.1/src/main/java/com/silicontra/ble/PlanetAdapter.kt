package com.silicontra.ble

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

//方法实现
class PlanetAdapter(private val context: Context,private val planeList: MutableList<Planet>) : BaseAdapter(){
    var textsizemg : Float? = null
    override fun getCount(): Int = planeList.size
    override fun getItem(position: Int): Any = planeList[position]
    override fun getItemId(position: Int): Long =position.toLong()

    fun Addlist(addlist :Planet){
//        if(!planeList.contains(addlist))
//        {
                planeList.add(addlist)
//        }
        this.notifyDataSetChanged()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup) :View{
        var view =convertView
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_second,null)
            holder = ViewHolder()
            holder.tv_name = view.findViewById(R.id.tv_name)
            holder.tv_message = view.findViewById(R.id.tv_message)
            holder.tv_name2 = view.findViewById(R.id.tv_name2)
            view.tag =holder
            textsizemg= holder.tv_message.getTextSize()
        }else{
            holder = view!!.tag as ViewHolder
        }
        val planet = planeList[position]
        holder.tv_name.text = planet.name
        holder.tv_message.text = planet.message
        holder.tv_name2.text = planet.name2
        if (holder.tv_message.text.length > 9){
            holder.tv_message.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsizemg!!-15)
        }
        else{
            holder.tv_message.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsizemg!!)
        }

        if(planet.name2 == "APP")
        holder.tv_message.setBackgroundResource(R.drawable.qq_chatto_bg_normal11)
        else{
            holder.tv_message.setBackgroundResource(R.drawable.qq_chatfrom_bg_normal123)
        }
        return view!!
    }

    inner  class ViewHolder{
        lateinit var ll_item : LinearLayout
        lateinit var iv_icon : ImageView
        lateinit var tv_name : TextView
        lateinit var tv_message : TextView
        lateinit var tv_name2 : TextView
    }
}
