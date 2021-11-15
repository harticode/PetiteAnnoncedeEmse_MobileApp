package com.example.petitesannoncesdelemse

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Color.BLUE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.petitesannoncesdelemse.Adapters.NotesAdapter
import com.example.petitesannoncesdelemse.Models.Note
import com.example.petitesannoncesdelemse.Models.User
import com.example.petitesannoncesdelemse.api.NoteApiService
import com.example.petitesannoncesdelemse.tools.ApiServices
import com.example.petitesannoncesdelemse.tools.RestApiService
import com.firebase.ui.auth.AuthUI
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import android.widget.Toast
import com.example.petitesannoncesdelemse.dto.NoteDto
import com.google.android.material.tabs.TabItem
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    //val ANONYMOUS = "anonymous"
    lateinit var sharedPref: SharedPreferences
    lateinit var mNotesAdapter: NotesAdapter
    lateinit var mcardListView: ListView
    lateinit var mEmptyStateTextView: TextView
    var type: Int = 0
    var handler = Handler()
    lateinit var runnable: Runnable
    var apiDelayed = 20 * 1000 //1 second=1000 milisecond, 5*1000=5seconds
    var notes: ArrayList<NoteDto> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Initialize references to views
        mcardListView = findViewById<ListView>(R.id.messageListView)
        val madd = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
        mEmptyStateTextView = findViewById<TextView>(R.id.empty_view)
        val loadingIndicator = findViewById<ProgressBar>(R.id.loading_indicator)
        val mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swiperefresh)

        sharedPref = getSharedPreferences("userinfo", MODE_PRIVATE)
        //setupTabLayout(this)
        type = intent.getIntExtra("type", 0)


        val tabCentreVille = findViewById<Button>(R.id.centreVille)
        val tabRequest = findViewById<Button>(R.id.request)
        val tabConfession = findViewById<Button>(R.id.confessions)

        if(type == 0){
            tabCentreVille.setBackgroundColor(Color.BLUE);
        }else{
            tabRequest.setBackgroundColor(Color.BLUE);
        }

        tabCentreVille.setOnClickListener{
            val i = Intent(this, MainActivity::class.java)
            i.putExtra("type", 0);
            startActivity(i)
            finish()
        }

        tabRequest.setOnClickListener{
            val i = Intent(this, MainActivity::class.java)
            i.putExtra("type", 1);
            startActivity(i)
            finish()
        }

        tabConfession.setOnClickListener{
            val i = Intent(this, ConfessionsActivity::class.java)
            startActivity(i)
            finish()
        }

        //App Logic
        if (!isOnline(this)) {
            loadingIndicator.visibility = View.GONE
            madd.visibility = View.GONE
            mEmptyStateTextView.text = "No Internet Connection"
            RefreshPage(this, mSwipeRefreshLayout)
        } else {
            val isUserSignedIn = FirebaseAuth.getInstance().currentUser != null
            if (!isUserSignedIn){
                //Do Nothing
            }

            val urlphoto = sharedPref.getString("infoPhoto", "")!!
            val email = sharedPref.getString("infoemail", "")!!
            val username = sharedPref.getString("infoUsername", "")!!

            //List Adapters
            loadData()
            loadingIndicator.visibility = View.GONE
            //swipe To Refresh utility
            RefreshPage(this, mSwipeRefreshLayout)

            //TODO: ADD Note
            madd.setOnClickListener {
                val mBuilder: android.app.AlertDialog.Builder =
                    android.app.AlertDialog.Builder(this@MainActivity)
                val mView = layoutInflater.inflate(R.layout.addcard, null)
                val Vplace = mView.findViewById<View>(R.id.place) as EditText
                val mTitleText = mView.findViewById<TextView>(R.id.text)
                val msave = mView.findViewById<View>(R.id.save) as Button

                if(type == 1){
                    mTitleText.text = "Add Request"
                }
                mBuilder.setView(mView)
                val mdialog: android.app.AlertDialog? = mBuilder.create()
                mdialog?.show()

                //todo : save to database
                msave.setOnClickListener {
                    //val gdate = format.format(Date())
                    val unixTime: Long = Date().getTime() / 1000
                    val place = Vplace.text.toString()
                    if (!place.isEmpty()) {
                        //
                        val user = User(email,urlphoto, username)
                        val note = Note(place, unixTime,1,type, user)
                        addNotetoAPI(note)
                        mdialog?.dismiss()
                        loadData()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Fill in blanks",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            //Todo: Edit and delete Note
            mcardListView.onItemClickListener =
                OnItemClickListener { parent, view, position, id ->
                    val noteDto = notes.get(position);
                    if(noteDto.user.email == email) {
                        val mBuilder: android.app.AlertDialog.Builder =
                            android.app.AlertDialog.Builder(this@MainActivity)
                        val mView = layoutInflater.inflate(R.layout.editcard, null)
                        val mEplace = mView.findViewById<View>(R.id.textinput_placeholder) as EditText
                        val medit = mView.findViewById<View>(R.id.edit) as Button
                        val mdelete = mView.findViewById<View>(R.id.delete) as Button

                        mEplace.setText(noteDto.contentOfTheNote)
                        mBuilder.setView(mView)
                        val mdialog: android.app.AlertDialog? = mBuilder.create()
                        mdialog?.show()

                        medit.setOnClickListener{
                            val user: User = User(noteDto.user.email, noteDto.user.photoUrl, noteDto.user.username)
                            val newUnixTime: Long = Date().getTime() / 1000
                            var newNote: Note = Note(mEplace.text.toString()
                                                    ,newUnixTime
                                                    ,noteDto.id
                                                    ,noteDto.type
                                                    ,user);
                            putNotetoAPI(newNote)
                            mdialog?.dismiss()
                            loadData()
                        }

                        mdelete.setOnClickListener{
                            deleteNotefromAPI(noteDto.id)
                            mdialog?.dismiss()
                            loadData()
                        }
                    }
                    Log.d(TAG, "clicked ${noteDto.id}")
                }
        }
    }

    fun loadData(){
        mNotesAdapter = NotesAdapter(this);
        mcardListView.adapter = mNotesAdapter

        lifecycleScope.launch(context = Dispatchers.IO){
            runCatching {
                ApiServices().buildService(NoteApiService::class.java).findByType(type).execute()
            }.onSuccess {
                withContext(context = Dispatchers.Main) {
                    if(it.body()?.size != 0){
                        mEmptyStateTextView.visibility = View.INVISIBLE
                    }else{
                        mEmptyStateTextView.visibility = View.VISIBLE
                        if(type == 0){mEmptyStateTextView.text = "Feel free to add a post"}
                        else{mEmptyStateTextView.text = "Feel free to add a request"}
                    }
                    notes.clear()
                    notes.addAll(it.body() ?: emptyList())
                    mNotesAdapter.update(it.body() ?: emptyList())
                }
            }.onFailure {
                withContext(context = Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "Error on notes loading $it",
                        Toast.LENGTH_LONG
                    ).show()
                    //Log.d(TAG, it.message.toString())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isOnline(this)) {
        } else {
            handler.postDelayed(Runnable { //do your function;
                loadData()
                handler.postDelayed(runnable, apiDelayed.toLong())
            }.also { runnable = it },
                apiDelayed.toLong()
            )
        }// so basically after your getHeroes(), from next time it will be 5 sec repeated
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable!!) //stop handler when activity not visible
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out_menu -> {
                AuthUI.getInstance().signOut(this).addOnSuccessListener {
                    onSignedOutCleanup()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
        }
        return false
    }


    private fun RefreshPage(context: Context, mSwipeRefreshLayout: SwipeRefreshLayout){
        mSwipeRefreshLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                val mHandler = Handler()
                mHandler.postDelayed({
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false)
                    }
                    loadData()
                }, 1300L)
            }
        })
    }

    private fun onSignedOutCleanup() {
        //var mUsername = ANONYMOUS
        //if signed out
        val SignedEditor: SharedPreferences.Editor = sharedPref.edit()
        SignedEditor.putString("signedIn", "false")
        SignedEditor.apply()
        //mNotesAdapter.clear()
        val i = Intent(this, SignInActivity::class.java)
        startActivity(i);
        finish()
    }

    fun addNotetoAPI(note: Note){
        val apiService = RestApiService()

        apiService.addNote(note) {
            if (it?.type!= null) {
                Toast.makeText(this, "Note Added", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error Adding Note $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun putNotetoAPI(note: Note){
        val apiService = RestApiService()

        apiService.updateNoteContent(note) {
            if (it?.id!= null) {
                Toast.makeText(this, "Note Updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error updating Note $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteNotefromAPI(noteId: Long){
        val apiService = RestApiService()

        apiService.deleteNote(noteId) { it ->
            if (it == 204) {
                Toast.makeText(this, "Note Deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error deleting Note $it", Toast.LENGTH_SHORT).show()
            }
        }
    }





}


