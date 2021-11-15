package com.example.petitesannoncesdelemse.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.petitesannoncesdelemse.R
import com.example.petitesannoncesdelemse.dto.ConfessionDto
import com.example.petitesannoncesdelemse.dto.NoteDto
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ConfessionsAdapter( private val context: Context) : BaseAdapter(){

    private val items = mutableListOf<ConfessionDto>()

    fun update(confessions: List<ConfessionDto>) {
        items.clear()
        items.addAll(confessions)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var rowView: View? = convertView
        if (rowView == null) {
            rowView = LayoutInflater.from(context).inflate(
                R.layout.activity_confession,
                parent,
                false
            )
        }

        val datetextView = rowView?.findViewById<TextView>(R.id.theconfession_date)
        val contentTextView = rowView?.findViewById<TextView>(R.id.theconfession_content)
        val idTextView = rowView?.findViewById<TextView>(R.id.the_id)

        //position
        val confession: ConfessionDto = items[position]
        //SetData
        if (confession != null) {
            val format: DateFormat = SimpleDateFormat("YYYY-MM-DD'T'HH:mm:ss.SSSz", Locale.FRANCE)
            val date = Date(confession.dateofPost*1000)
            datetextView?.setText(TimeAgo(date))

            contentTextView?.setText(confession.contentOfTheConfession)
            idTextView?.setText("Confession #"+confession.id.toString())
        }
        return rowView!!
    }

    open fun TimeAgo(date: Date): String{
        val format = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss")
        val past: Date = date;
        val now = Date()
        val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(now.time - past.time)
        val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
        val hours: Long = TimeUnit.MILLISECONDS.toHours(now.time - past.time)
        val days: Long = TimeUnit.MILLISECONDS.toDays(now.time - past.time)

        if (seconds < 60) {
            return "$seconds sec ago"
        } else if (minutes < 60) {
            return "$minutes min ago"
        } else if (hours < 24) {
            return "$hours hrs ago"
        } else {
            return "$days Days ago"
        }
    }
}