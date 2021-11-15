package com.example.petitesannoncesdelemse

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.petitesannoncesdelemse.Adapters.ConfessionsAdapter
import com.example.petitesannoncesdelemse.Adapters.NotesAdapter
import com.example.petitesannoncesdelemse.Models.Confession
import com.example.petitesannoncesdelemse.Models.Note
import com.example.petitesannoncesdelemse.Models.User
import com.example.petitesannoncesdelemse.api.ConfessionApiService
import com.example.petitesannoncesdelemse.api.NoteApiService
import com.example.petitesannoncesdelemse.dto.ConfessionDto
import com.example.petitesannoncesdelemse.dto.NoteDto
import com.example.petitesannoncesdelemse.tools.ApiServices
import com.example.petitesannoncesdelemse.tools.RestApiService
import com.firebase.ui.auth.AuthUI
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class ConfessionsActivity : AppCompatActivity() {
    private val TAG = "ConfessionsActivity"
    lateinit var sharedPref: SharedPreferences
    lateinit var mConfessionAdapter: ConfessionsAdapter
    lateinit var mconfListView: ListView
    lateinit var mEmptyStateTextView: TextView
    var handler = Handler()
    lateinit var runnable: Runnable
    var apiDelayed = 30 * 1000 //1 second=1000 milisecond, 5*1000=5seconds
    var confessions: ArrayList<ConfessionDto> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confessions)

        //init sharedPref
        sharedPref = getSharedPreferences("userinfo", MODE_PRIVATE)
        // Initialize references to views
        mconfListView = findViewById<ListView>(R.id.confessionsListView)
        val maddconfession = findViewById<FloatingActionButton>(R.id.floatingActionButton3)
        mEmptyStateTextView = findViewById<TextView>(R.id.empty_viewcf)
        val loadingIndicator = findViewById<ProgressBar>(R.id.loading_indicatorcf)
        val mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swiperefreshcf)

        val tabCentreVille = findViewById<Button>(R.id.centreVille)
        val tabRequest = findViewById<Button>(R.id.request)
        val tabConfession = findViewById<Button>(R.id.confessions)

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
            maddconfession.visibility = View.GONE
            mEmptyStateTextView.text = "No Internet Connection"
            RefreshPage(this, mSwipeRefreshLayout)
        } else {

            val isUserSignedIn = FirebaseAuth.getInstance().currentUser != null
            if (!isUserSignedIn){
                //Do Nothing
            }

            //List Adapters
            loadConfessionsData()
            loadingIndicator.visibility = View.GONE
            //swipe To Refresh utility
            RefreshPage(this, mSwipeRefreshLayout)

            //TODO : ADD Confession
            maddconfession.setOnClickListener{
                val mBuildercf: android.app.AlertDialog.Builder =
                    android.app.AlertDialog.Builder(this@ConfessionsActivity)
                val mViewcf = layoutInflater.inflate(R.layout.addconfession, null)
                val Vplacecf = mViewcf.findViewById<View>(R.id.confesiontext) as EditText
                val msavecf = mViewcf.findViewById<View>(R.id.savecf) as Button

                mBuildercf.setView(mViewcf)
                val mdialog: android.app.AlertDialog? = mBuildercf.create()
                mdialog?.show()

                //todo : save to database
                msavecf.setOnClickListener {
                    val unixTime: Long = Date().getTime() / 1000
                    val place = Vplacecf.text.toString()
                    if (!place.isEmpty()) {
                        val confession = Confession(place, unixTime,1)
                        addConfessiontoAPI(confession)
                        mdialog?.dismiss()
                        loadConfessionsData()
                    } else {
                        Toast.makeText(
                            this@ConfessionsActivity,
                            "Fill in blanks",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }


            }

        }
    }

    override fun onResume() {
        super.onResume()
        if (!isOnline(this)) {
        } else {
            handler.postDelayed(Runnable { //do your function;
                loadConfessionsData()
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

    fun loadConfessionsData(){
        mConfessionAdapter = ConfessionsAdapter(this);
        mconfListView.adapter = mConfessionAdapter

        lifecycleScope.launch(context = Dispatchers.IO){
            runCatching {
                ApiServices().buildService(ConfessionApiService::class.java).findAllConfessions().execute()
            }.onSuccess {
                withContext(context = Dispatchers.Main) {
                    if(it.body()?.size != 0){
                        mEmptyStateTextView.visibility = View.INVISIBLE
                    }else{
                        mEmptyStateTextView.visibility = View.VISIBLE
                        mEmptyStateTextView.text = "Feel free to add a confession"
                    }
                    confessions.clear()
                    confessions.addAll(it.body() ?: emptyList())
                    mConfessionAdapter.update(it.body() ?: emptyList())
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

    private fun RefreshPage(context: Context, mSwipeRefreshLayout: SwipeRefreshLayout){
        mSwipeRefreshLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                val mHandler = Handler()
                mHandler.postDelayed({
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false)
                    }
                    loadConfessionsData()
                }, 1300L)
            }
        })
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

    fun addConfessiontoAPI(confession: Confession){
        val apiService = RestApiService()

        apiService.addConfession(confession) {
            if (it?.id!= null) {
                Toast.makeText(this, "Confession Added", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error Adding Confession $it", Toast.LENGTH_SHORT).show()
            }
        }
    }
}