package com.example.petitesannoncesdelemse

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.example.petitesannoncesdelemse.BuildConfig.DEBUG
import com.example.petitesannoncesdelemse.Models.User
import com.example.petitesannoncesdelemse.databinding.ActivitySetupProfileBinding
import com.example.petitesannoncesdelemse.tools.RestApiService
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class SetupProfileActivity : AppCompatActivity() {

    private val TAG = "SetupProfile"
    val RC_SIGN_IN = 1
    private val RC_PHOTO_PICKER = 2
    var PhotoisSet = booleanArrayOf(false)
    lateinit var pb: ProgressBar
    lateinit var binding : ActivitySetupProfileBinding
    lateinit var sharedPref: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("type", 0);

        sharedPref = getSharedPreferences("userinfo", MODE_PRIVATE)
        val isUserSignedIn = FirebaseAuth.getInstance().currentUser != null
        if (!isUserSignedIn){
            signIn()
        }else{
            if(sharedPref.getString("signedIn", "") == "true"){
                startActivity(intent)
                finish()
            }else{
                val firebaseAuth = FirebaseAuth.getInstance()
                //Log.d("mainUser", user)
                signInDialog(this, firebaseAuth, sharedPref)
            }
        }
    }

    private fun signIn(){
        //Auth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
        val phoneConfigWithDefaultNumber = AuthUI.IdpConfig.EmailBuilder()
            .build()
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.drawable.logomines)
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(
                    Arrays.asList(phoneConfigWithDefaultNumber))
                .build(),
            RC_SIGN_IN)
    }

    private fun signInDialog(
        context: Context,
        firebaseAuth: FirebaseAuth,
        sharedPref: SharedPreferences
    ) {

        //todo : setusername
        Log.d("mainUser", "dialogin")
        val username = firebaseAuth.currentUser?.displayName.toString()
        val email = firebaseAuth.currentUser?.email.toString()

        val usernameTextView = binding.usernametxt
        val emailTextView = binding.emailtxt
        val usernameset = binding.setuser
        val mPhotoPickerButton = binding.photoPickerButton
        pb = binding.pb

        usernameTextView.text = username
        emailTextView.text = email


        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/jpeg"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(
                Intent.createChooser(intent, "Complete action using"),
                RC_PHOTO_PICKER
            )
        })

        usernameset.setOnClickListener {
            if (PhotoisSet[0]) {
                //todo : nevercomeback here;
                val SignedEditor: SharedPreferences.Editor =
                    sharedPref.edit()
                SignedEditor.putString("signedIn", "true")
                SignedEditor.apply()

                //todo : save data in the phone
                val editor: SharedPreferences.Editor = sharedPref.edit()
                editor.putString("infoUsername", username)
                editor.putString("infoemail", email)
                editor.apply()

                //todo: POST user to Spring API
                val photourl = sharedPref.getString("infoPhoto", "")!!
                val user: User = User(email,photourl,username)
                addUsertoAPI(user);

                //todo: redirect to MainActivity
                startActivity(
                    Intent(
                        context,
                        MainActivity::class.java
                    )
                )
                finish()
            } else {
                Toast.makeText(
                    context,
                    "Set your Profile Pictures please ",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in", Toast.LENGTH_SHORT).show()
                val i = Intent(this, SetupProfileActivity::class.java)
                startActivity(i)
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {

            val selectedImageUri: Uri = data?.data!!
            val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.FRANCE)
            val now  =  Date()
            val fileName = formatter.format(now)
            val storageReference = FirebaseStorage.getInstance().getReference("images/$fileName")

            storageReference.putFile(selectedImageUri).addOnSuccessListener {
                Toast.makeText(this@SetupProfileActivity, "Done Uploading", Toast.LENGTH_SHORT).show()
                storageReference.downloadUrl.addOnSuccessListener {
                    var url = it.toString()
                    //Log.d(TAG, url)
                    val editor: SharedPreferences.Editor = sharedPref.edit()
                    editor.putString("infoPhoto", url)
                    editor.apply()
                    Picasso.with(this).load(url).fit().placeholder(R.drawable.userprofile).into(binding.photoPickerButton)
                    PhotoisSet[0] = true
                }
            }.addOnProgressListener {
                val progress =
                    (100 * it.bytesTransferred / it.totalByteCount)
                binding.pb.progress = progress.toInt()
                Toast.makeText(this, "progress = $progress", Toast.LENGTH_SHORT).show()
            }



        }
    }

    fun addUsertoAPI(user: User) {
        val apiService = RestApiService()

        apiService.addUser(user) {
            if (it?.email != null) {
                Toast.makeText(this, "User created", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error registering new user", Toast.LENGTH_SHORT).show()
            }
        }
    }
}