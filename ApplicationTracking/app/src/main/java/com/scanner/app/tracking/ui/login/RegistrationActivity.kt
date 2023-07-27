package com.scanner.app.tracking.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.scanner.app.tracking.MenuActivity
import com.scanner.app.tracking.R
import com.scanner.app.tracking.data.model.UserDetails

class RegistrationActivity : AppCompatActivity() {

    var userName : EditText? = null
    var emailAddress : EditText? = null
    var password : EditText? = null
    var confirmPassword : EditText? = null
    var progressBar : ProgressBar? = null
    private lateinit var database: FirebaseFirestore
    var manufacturerRadioButton : RadioButton ? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        userName = findViewById(R.id.user_name)
        emailAddress = findViewById(R.id.email_address)
        password = findViewById(R.id.password_et)
        confirmPassword = findViewById(R.id.confirm_password_et)
        progressBar = findViewById(R.id.loading)
        manufacturerRadioButton = findViewById(R.id.manufacturer_rb)

        database = Firebase.firestore
    }

    fun registerUser(view: android.view.View) {
        progressBar?.visibility = View.VISIBLE
        if(isValid()) {
            saveData()
        }else
            progressBar?.visibility = View.GONE
    }

    fun isValid(): Boolean{
        if(userName?.text.isNullOrEmpty()) {
            Toast.makeText(this, "Please Enter username", Toast.LENGTH_LONG).show()
            return false
        }
        if(emailAddress?.text.isNullOrEmpty()) {
            Toast.makeText(this, "Please Enter Email", Toast.LENGTH_LONG).show()
            return false
        }
        if(password?.text.isNullOrEmpty()) {
            Toast.makeText(this, "Please Enter password", Toast.LENGTH_LONG).show()
            return false
        }
        if(password?.text.toString().length < 8) {
            Toast.makeText(this, "Password should be more than 8 characters ", Toast.LENGTH_LONG).show()
            return false
        }
        if(confirmPassword?.text.isNullOrEmpty()) {
            Toast.makeText(this, "Please Enter confirm password", Toast.LENGTH_LONG).show()
            return false
        }
        if(password?.text.toString() != confirmPassword?.text.toString()) {
            Toast.makeText(this, "Password and Confirm password should be same", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    fun saveData(){
        var isManufacturer = "N"
        if(manufacturerRadioButton?.isChecked == true)
            isManufacturer = "Y"
        val userDetails = UserDetails(userName?.text.toString(), emailAddress?.text.toString(), password?.text.toString(), isManufacturer)
        database.collection("users").add(userDetails)
            .addOnSuccessListener {documentReference ->
                progressBar?.visibility = View.GONE
                Log.d("firestore", "User added with ID: ${documentReference.id}")
                AlertDialog.Builder(this)
                    .setTitle("Message")
                    .setCancelable(true)
                    .setMessage("Registration Completed")
                    .setPositiveButton("Ok") { dialog, which ->
                        dialog.dismiss()
                        val intent = Intent(this, LoginActivity::class.java)
                        //intent.putExtra("username", username.text.toString())
                        startActivity(intent)
                        finish()
                    }
                    //.setNegativeButton(R.string.no, null)
                    .create()
                    .show()

            }
            .addOnFailureListener {e ->
                progressBar?.visibility = View.GONE
                Log.w("firestore", "Error adding User", e)
            }
    }
}