package com.example.petitesannoncesdelemse.Adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.petitesannoncesdelemse.Models.Note
import com.example.petitesannoncesdelemse.R
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

import android.widget.BaseAdapter
import com.example.petitesannoncesdelemse.dto.NoteDto

import com.squareup.okhttp.OkHttpClient
import com.squareup.picasso.OkHttpDownloader
import com.squareup.picasso.Picasso
import java.text.DateFormat


class NotesAdapter( private val context: Context) : BaseAdapter(){



    private val items = mutableListOf<NoteDto>()

    fun update(notes: List<NoteDto>) {
        items.clear()
        items.addAll(notes)
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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var rowView: View? = convertView
        if (rowView == null) {
            rowView = LayoutInflater.from(context).inflate(
                R.layout.activity_note,
                parent,
                false
            )
        }

        val datetextView = rowView?.findViewById<TextView>(R.id.the_date)
        val contentTextView = rowView?.findViewById<TextView>(R.id.the_content)
        val UsernameTextView = rowView?.findViewById<TextView>(R.id.the_user)
        val photoImageView = rowView?.findViewById<CircleImageView>(R.id.photo)
        val ColorImageView = rowView?.findViewById<ImageView>(R.id.the_note)



        //position
        val note: NoteDto = items[position]
        //setdata
        if (note != null) {
            val format: DateFormat = SimpleDateFormat("YYYY-MM-DD'T'HH:mm:ss.SSSz", Locale.FRANCE)
            val date = Date(note.dateofPost*1000)
            datetextView?.setText(TimeAgo(date))

            contentTextView?.setText(note.contentOfTheNote)
            UsernameTextView?.setText(note.user.username)
            ColorImageView?.setBackgroundResource(colorType(note.type))
            //profil pic
            if (photoImageView != null && note.user.photoUrl != "") {
                //photoImageView.setImageBitmap(getBitmapFromURL(note.user.profilePic))
                //photoImageView.load(note.user.profilePic)
                val c = photoImageView.context
                val pic = initPicasso(c);
                pic.load(note.user.photoUrl).placeholder(R.drawable.userprofile).into(photoImageView)
                //Picasso.get().load("http://i.imgur.com/DvpvklR.png").resize(50, 50).centerCrop().into(photoImageView)
            }
        }
        return rowView!!
    }

    open fun colorType(type: Int): Int {
        return when (type) {
            1 -> R.color.purple_500
            2 -> R.color.green
            else -> R.color.green
        }
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

    /*open fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            // Log exception
            null
        }
    }*/

    open fun initPicasso(context: Context?): Picasso {
        var picasso: Picasso = Picasso.with(context);
        if (picasso == null) {
            var picassoBuilder = Picasso.Builder(context)
            try {
                Picasso.setSingletonInstance(picasso)
            } catch (ignored: IllegalStateException) {
                // Picasso instance was already set
                // cannot set it after Picasso.with(Context) was already in use
            }
            picassoBuilder.downloader(OkHttpDownloader(OkHttpClient())).memoryCache(com.squareup.picasso.Cache.NONE)
                .loggingEnabled(true).indicatorsEnabled(true)
            picasso = picassoBuilder.build()
        }
        return picasso
    }
}